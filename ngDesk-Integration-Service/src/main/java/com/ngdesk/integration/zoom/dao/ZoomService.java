package com.ngdesk.integration.zoom.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bson.internal.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.MessageAttachment;
import com.ngdesk.data.dao.PublishDiscussionMessage;
import com.ngdesk.data.dao.Sender;
import com.ngdesk.repositories.ModuleEntryRepository;

@Service
public class ZoomService {

	@Autowired
	AuthManager authManager;

	@Value("${zoom.client.id}")
	String zoomClientId;

	@Value("${zoom.client.secret}")
	String zoomClientSecret;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	Global global;

	private final Logger log = LoggerFactory.getLogger(ZoomService.class);

	public void postDiscussionToEntry(String entryId, String moduleId, ZoomMeeting zoomMeeting) {

		String message = global.getFile("zoom_meeting_invite.html");
		message = message.replace("JOIN_URL_REPLACE", zoomMeeting.getJoinUrl());
		message = message.replace("MEETING_ID_REPLACE", zoomMeeting.getMeetingId().toString());
		DiscussionMessage discussionMessage = buildDiscussionPayload(message, moduleId, entryId);
		addToDiscussionQueue(new PublishDiscussionMessage(discussionMessage,
				authManager.getUserDetails().getCompanySubdomain(), authManager.getUserDetails().getUserId(), false));
	}

	public void addToDiscussionQueue(PublishDiscussionMessage message) {
		try {
			log.debug("Publishing to websocket");
			log.debug(new ObjectMapper().writeValueAsString(message));
			rabbitTemplate.convertAndSend("publish-discussion", message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private DiscussionMessage buildDiscussionPayload(String message, String moduleId, String entryId) {
		Optional<Map<String, Object>> optionalUser = entryRepository.findEntryByVariable("_id",
				authManager.getUserDetails().getUserId(), "Users_" + authManager.getUserDetails().getCompanyId());

		Map<String, Object> user = optionalUser.get();
		String contactId = user.get("CONTACT").toString();

		Optional<Map<String, Object>> optionalContact = entryRepository.findById(contactId,
				"Contacts_" + authManager.getUserDetails().getCompanyId());
		Map<String, Object> contact = optionalContact.get();

		Sender sender = new Sender(contact.get("FIRST_NAME").toString(), contact.get("LAST_NAME").toString(),
				user.get("USER_UUID").toString(), user.get("ROLE").toString());

		return new DiscussionMessage(message, new Date(), UUID.randomUUID().toString(), "MESSAGE",
				new ArrayList<MessageAttachment>(), sender, moduleId, entryId, null);

	}

	public ZoomMeeting createInstantZoomMeeting(String userId, String userAuthentication, String topic) {

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders httpHeadersMe = new HttpHeaders();

		httpHeadersMe.add("Authorization", "Bearer " + userAuthentication);

		String urlCreateMeeting = "https://api.zoom.us/v2/users/" + userId + "/meetings";

		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put("topic", topic);
		requestBody.put("type", 1);

		HttpEntity<Map<String, Object>> entity1 = new HttpEntity<>(requestBody, httpHeadersMe);
		try {
			ResponseEntity<Map> response3 = restTemplate.exchange(urlCreateMeeting, HttpMethod.POST, entity1,
					Map.class);

			return getMeetingDetails(response3.getBody());
		} catch (HttpClientErrorException e) {
			String vars[] = { e.getStatusCode().toString() };
			throw new BadRequestException("UNABLE_TO_CREATE_MEETING", vars);
		}
	}

	public ZoomMeeting getMeetingDetails(Map<String, Object> meetingObject) {
		ZoomMeeting zoomMeeting = new ZoomMeeting();
		zoomMeeting.setJoinUrl(meetingObject.get("join_url").toString());
		zoomMeeting.setMeetingId((Long) meetingObject.get("id"));
		zoomMeeting.setMeetingStartUrl(meetingObject.get("start_url").toString());
		zoomMeeting.setTopic(meetingObject.get("topic").toString());
		zoomMeeting.setDateCreated(new Date());
		zoomMeeting.setCreatedBy(authManager.getUserDetails().getUserId());
		return zoomMeeting;

	}

	public ZoomUserInformation getZoomUserInformation(String accessToken) {

		RestTemplate restTemplate = new RestTemplate();

		String zoomGetUserUrl = "https://api.zoom.us/v2/users/me";
		HttpHeaders httpHeaders = new HttpHeaders();

		httpHeaders.add("Authorization", "Bearer " + accessToken);

		HttpEntity<Map<String, String>> entity = new HttpEntity<Map<String, String>>(httpHeaders);

		ResponseEntity<Map> userResponse = restTemplate.exchange(zoomGetUserUrl, HttpMethod.GET, entity, Map.class);

		if (userResponse.getStatusCodeValue() < 200 && userResponse.getStatusCodeValue() > 300) {
			String[] vars = { "ZOOM_USER" };
			throw new BadRequestException("DAO_NOT_FOUND", vars);
		}

		return transformZoomUserInfo(userResponse.getBody());
	}

	private ZoomUserInformation transformZoomUserInfo(Map<String, String> zoomUser) {
		ZoomUserInformation userInfo = new ZoomUserInformation();

		userInfo.setAccountId(zoomUser.get("account_id"));
		userInfo.setUserId(zoomUser.get("id"));
		userInfo.setFirstName(zoomUser.get("first_name"));
		userInfo.setLastName(zoomUser.get("last_name"));
		userInfo.setPersonalMeetingUrl(zoomUser.get("personal_meeting_url"));
		userInfo.setRoleName(zoomUser.get("role_name"));
		userInfo.setEmailAddress(zoomUser.get("email"));
		userInfo.setCompanyName(zoomUser.get("company"));

		return userInfo;
	}

	public ZoomAuthentication refreshAccessToken(String refreshToken) {
		String refreshTokenUrl = "https://zoom.us/oauth/token?grant_type=refresh_token&refresh_token=" + refreshToken;

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders httpHeaders = new HttpHeaders();

		String base64String = Base64.encode((zoomClientId + ":" + zoomClientSecret).getBytes());
		httpHeaders.add("Authorization", "Basic " + base64String);

		HttpEntity<Map<String, String>> entity = new HttpEntity<>(httpHeaders);

		ResponseEntity<ZoomAuthentication> response = restTemplate.postForEntity(refreshTokenUrl, entity,
				ZoomAuthentication.class);

		ZoomAuthentication authentication = response.getBody();

		return authentication;
	}
}
