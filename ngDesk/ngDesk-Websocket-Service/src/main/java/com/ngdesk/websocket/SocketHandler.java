package com.ngdesk.websocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.bson.types.ObjectId;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthProxy;
import com.ngdesk.commons.models.User;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.SingleWorkflowPayload;
import com.ngdesk.repositories.RolesRepository;
import com.ngdesk.websocket.approval.dao.Approval;
import com.ngdesk.websocket.approval.dao.ApprovalService;
import com.ngdesk.websocket.channels.chat.ChatChannelService;
import com.ngdesk.websocket.channels.chat.PageLoad;
import com.ngdesk.websocket.dao.WebSocketService;
import com.ngdesk.websocket.graphql.dao.GraphqlProxy;
import com.ngdesk.websocket.modules.dao.ButtonTypeService;
import com.ngdesk.websocket.report.dao.ReportInput;
import com.ngdesk.websocket.roles.dao.Role;
import com.ngdesk.websocket.sam.dao.ControllerInstruction;
import com.ngdesk.websocket.sam.dao.FileRuleNotification;
import com.ngdesk.websocket.sam.dao.FileRuleService;
import com.ngdesk.websocket.sam.dao.InstructionController;
import com.ngdesk.websocket.sam.dao.Log;
import com.ngdesk.websocket.sam.dao.LogController;
import com.ngdesk.websocket.sam.dao.Ping;
import com.ngdesk.websocket.sam.dao.ProbeController;

import io.micrometer.core.instrument.MeterRegistry;

@Component
public class SocketHandler extends TextWebSocketHandler {

	@Autowired
	SessionService sessionService;

	@Autowired
	WebSocketService webSocketService;

	MeterRegistry meterRegistry;

	@Autowired
	LogController logController;

	@Autowired
	ProbeController probeController;

	@Autowired
	InstructionController instructionController;

	@Autowired
	ButtonTypeService buttonTypeService;

	@Autowired
	FileRuleService fileRuleService;

	@Autowired
	ApprovalService approvalService;

	@Autowired
	ChatChannelService chatChannelService;

	@Autowired
	private AuthProxy authProxy;

	@Autowired
	GraphqlProxy graphqlProxy;

	@Autowired
	RedissonClient redisson;

	@Autowired
	RolesRepository rolesRepository;

	private static ReentrantLock lock = new ReentrantLock();

	AtomicInteger websocketConnections = new AtomicInteger(0);

	private final Logger log = LoggerFactory.getLogger(SocketHandler.class);

	public SocketHandler(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
		this.meterRegistry.gauge("websocket_connections", websocketConnections);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		try {
			lock.lock();

			websocketConnections.incrementAndGet();
			URI uri = new URI(session.getUri().toString());
			String queryParams = uri.getQuery();
			queryParams = queryParams.replaceAll("\\s+", "");

			Map<String, String> queryParamMap = new HashMap<String, String>();
			for (String keyValuePair : queryParams.split("&")) {
				queryParamMap.put(keyValuePair.split("=")[0], keyValuePair.split("=")[1]);
			}

			String subdomain = webSocketService.getSubdomain(uri.getHost());

			if (queryParamMap.containsKey("authentication_token")) {
				try {
					String authToken = queryParamMap.get("authentication_token");
					User user = authProxy.getUserDetails(authToken);
					String companyId = authProxy.getUserDetails(authToken).getCompanyId();

					if (user == null) {
						session.close();
						throw new BadRequestException("USER_ID_EMPTY", null);
					}
					String roleId = user.getRole();
					Optional<Role> role = rolesRepository.findById(roleId, "roles_" + companyId);
					if (role.isPresent()) {
						if (!role.get().getName().equalsIgnoreCase("Customer")) {

							ConcurrentHashMap<String, ConcurrentLinkedQueue<WebSocketSession>> userSessions = sessionService.sessions
									.computeIfAbsent(subdomain,
											newSession -> new ConcurrentHashMap<String, ConcurrentLinkedQueue<WebSocketSession>>());
							ConcurrentLinkedQueue<WebSocketSession> sessions = userSessions.computeIfAbsent(
									user.getUserId(), newSession -> new ConcurrentLinkedQueue<WebSocketSession>());
							sessions.add(session);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					session.close();
				}
			} else if (queryParamMap.containsKey("type") && queryParamMap.containsKey("id")) {

				String id = queryParamMap.get("id");
				String type = queryParamMap.get("type");
				if (id == null || id.isBlank()) {
					session.close();
					throw new BadRequestException("ID_EMPTY", null);
				}
				if (!ObjectId.isValid(id)) {
					session.close();
					throw new BadRequestException("ID_INVALID", null);
				}
				if (type == null || type.isBlank()) {
					session.close();
					throw new BadRequestException("TYPE_EMPTY", null);
				}

				type = type.toUpperCase();
				String[] types = { "ASSET", "SOFTWARE", "PATCH", "CONTROLLER" };

				List<String> typeList = Arrays.asList(types);

				if (!typeList.contains(type)) {
					session.close();
					throw new BadRequestException("INVALID_TYPE", null);
				}

				ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSession>> companyProbes = sessionService.probeSessions
						.computeIfAbsent(subdomain,
								newSession -> new ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSession>>());

				ConcurrentHashMap<String, WebSocketSession> probeSessions = companyProbes.computeIfAbsent(id,
						newSession -> new ConcurrentHashMap<String, WebSocketSession>());

				probeSessions.put(type, session);

			} else if (queryParamMap.containsKey("sessionUUID") && queryParamMap.containsKey("subdomain")) {
				String sessionUUID = queryParamMap.get("sessionUUID");
				subdomain = queryParamMap.get("subdomain");
				ConcurrentHashMap<String, ConcurrentLinkedQueue<WebSocketSession>> sessionUUIDSessions = sessionService.sessions
						.computeIfAbsent(sessionUUID,
								newSession -> new ConcurrentHashMap<String, ConcurrentLinkedQueue<WebSocketSession>>());

				ConcurrentLinkedQueue<WebSocketSession> sessions = sessionUUIDSessions.computeIfAbsent(subdomain,
						newSession -> new ConcurrentLinkedQueue<WebSocketSession>());
				sessions.add(session);
			} else {
				session.close();
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BadRequestException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}

	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		try {
			lock.lock();
			URI uri = new URI(session.getUri().toString());
			String queryParams = uri.getQuery();

			Map<String, String> queryParamMap = new HashMap<String, String>();
			for (String keyValuePair : queryParams.split("&")) {
				queryParamMap.put(keyValuePair.split("=")[0], keyValuePair.split("=")[1]);
			}
			String subdomain = webSocketService.getSubdomain(uri.getHost());

			if (queryParamMap.containsKey("authentication_token")) {
				String authToken = queryParamMap.get("authentication_token");
				User user = authProxy.getUserDetails(authToken);

				if (user != null) {
					ConcurrentHashMap<String, ConcurrentLinkedQueue<WebSocketSession>> userSessions = sessionService.sessions
							.get(subdomain);
					userSessions.get(user.getUserId()).remove(session);

					if (sessionService.sessions.get(subdomain).get(user.getUserId()).size() == 0) {
						sessionService.sessions.get(subdomain).remove(user.getUserId());
					}
					if (sessionService.sessions.get(subdomain).size() == 0) {
						sessionService.sessions.remove(subdomain);
					}
				}

			} else if (queryParamMap.containsKey("type") && queryParamMap.containsKey("id")) {

				String type = queryParamMap.get("type");
				type = type.toUpperCase();

				String id = queryParamMap.get("id");

				ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSession>> companyProbes = sessionService.probeSessions
						.get(subdomain);

				companyProbes.get(id).remove(type);

				if (sessionService.probeSessions.get(subdomain).get(id).size() == 0) {
					sessionService.probeSessions.get(subdomain).remove(id);
				}

				if (sessionService.probeSessions.get(subdomain).size() == 0) {
					sessionService.probeSessions.remove(subdomain);
				}
			}

		} catch (URISyntaxException e) {
			e.printStackTrace();
		} finally {
			websocketConnections.decrementAndGet();
			lock.unlock();
		}
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			URI uri = new URI(session.getUri().toString());
			String subdomain = webSocketService.getSubdomain(uri.getHost());
			String queryParams = uri.getQuery();
			Map<String, String> queryParamMap = new HashMap<String, String>();
			for (String keyValuePair : queryParams.split("&")) {
				queryParamMap.put(keyValuePair.split("=")[0], keyValuePair.split("=")[1]);
			}
			if (queryParamMap.containsKey("authentication_token")) {
				String authenticationToken = queryParamMap.get("authentication_token");
				User user = authProxy.getUserDetails(authenticationToken);
				if (user != null) {
					try {
						DiscussionMessage discussionMessage = mapper.readValue(textMessage.getPayload(),
								DiscussionMessage.class);

						Assert.notNull(discussionMessage.getMessageType(), "Message Type should not be null");
						if (!discussionMessage.getMessageType().equalsIgnoreCase("ping")) {
							Assert.notNull(discussionMessage.getMessage(), "Message should not be null");
							Assert.notNull(discussionMessage.getDataId(), "Entry ID should not be null");

							if (!discussionMessage.getMessageType().equals("MESSAGE")
									&& !discussionMessage.getMessageType().equals("INTERNAL_COMMENT")) {
								throw new IllegalArgumentException();
							}
							webSocketService.addDiscussionToEntry(discussionMessage, subdomain, user.getUserId(),
									false);
						}
					} catch (Exception e) {
						try {
							ControllerInstruction controllerInstruction = mapper.readValue(textMessage.getPayload(),
									ControllerInstruction.class);
							instructionController.publishInstructionToProbe(subdomain,
									controllerInstruction.getControllerId(), controllerInstruction.getInstruction());
						} catch (Exception e1) {
							try {
								SingleWorkflowPayload payload = mapper.readValue(textMessage.getPayload(),
										SingleWorkflowPayload.class);
								buttonTypeService.executeWorkflow(payload, subdomain, user.getUserId());
							} catch (Exception e2) {

								try {
									FileRuleNotification fileRule = mapper.readValue(textMessage.getPayload(),
											FileRuleNotification.class);
									fileRuleService.publishRuleToProbe(subdomain, fileRule.getControllerId(), fileRule);
								} catch (Exception e3) {
									try {
										Approval approval = mapper.readValue(textMessage.getPayload(), Approval.class);
										approvalService.getApprovalDetailsAndExecute(approval, user);

									} catch (Exception e4) {
										try {
											ReportInput reportInput = mapper.readValue(textMessage.getPayload(),
													ReportInput.class);
											graphqlProxy.reportGenerate(reportInput, user.getCompanyId(),
													user.getUserUuid());

										} catch (Exception e5) {

										}
									}
								}
							}

						}
					}
				}
			} else if (queryParamMap.containsKey("type") && queryParamMap.containsKey("id")) {
				String id = queryParamMap.get("id");
				try {
					Ping ping = mapper.readValue(textMessage.getPayload(), Ping.class);
					probeController.updateLastSeen(ping.getControllerId(), subdomain, ping.getApplicationName());
				} catch (Exception e) {
					Log newLog = mapper.readValue(textMessage.getPayload(), Log.class);
					logController.addLogToApplication(newLog, subdomain, id);
				}
			} else if (queryParamMap.containsKey("sessionUUID") && queryParamMap.containsKey("subdomain")) {
				try {
					PageLoad pageLoad = mapper.readValue(textMessage.getPayload(), PageLoad.class);
					chatChannelService.publishPageLoad(pageLoad);

				} catch (Exception e) {
				}

			}

		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			log.error("Required parameters are missing");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
