package com.ngdesk.channels.workflows;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;
import com.ngdesk.workflow.Condition;
import com.ngdesk.workflow.Field;
import com.ngdesk.workflow.Node;
import com.ngdesk.workflow.Workflow;

@RestController
@Component
public class ChannelWorkflowService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private Global global;

	@Autowired
	private RoleService roleService;
	
	@Value("${manager.host}")
	private String managerHost;

	private final Logger log = LoggerFactory.getLogger(ChannelWorkflowService.class);

	@GetMapping("/channels/{channel_type}/{channel_name}/workflow")
	public Workflow getWorkflow(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("channel_type") String channelType, @PathVariable("channel_name") String channelName) {

		Workflow workflow = new Workflow();

		try {
			log.trace("Enter ChannelWorkflowService.getWorkflow(), ChannelType: " + channelType + ", ChannelName: "
					+ channelName);

			// GET COMPANY ID
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");

			// ACCESS MONGO
			MongoCollection<Document> collection = mongoTemplate
					.getCollection("channels_" + channelType + "_" + companyId);

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			Document channelDocument = collection.find(Filters.eq("NAME", channelName)).first();

			// CHECK DOC
			if (channelDocument != null) {
				// CHECK WORKFLOW
				Document workflowDocument = (Document) channelDocument.get("WORKFLOW");
				if (workflowDocument != null) {
					workflow = new ObjectMapper().readValue(workflowDocument.toJson(), Workflow.class);
				} else {
					// RETURN EMPTY OBJECT
					List<Node> nodes = new ArrayList<Node>();
					workflow.setNodes(nodes);
				}
				log.trace("Exit ChannelWorkflowService.getWorkflow(), ChannelType: " + channelType + ", ChannelName: "
						+ channelName);
				return workflow;

			} else {
				throw new ForbiddenException("CHANNEL_DOES_NOT_EXIST");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/channels/{channel_type}/{channel_name}/workflow")
	public Workflow postWorkflow(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("channel_type") String channelType, @PathVariable("channel_name") String channelName,
			@Valid @RequestBody Workflow workflow) {

		try {
			log.trace("Enter ChannelWorkflowService.postWorkflow(), ChannelType: " + channelType + ", ChannelName: "
					+ channelName);
			// GET COMPANY ID
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			// DO CHECK FOR TYPE GET ENTRIES, CAN'T DO THIS ONE WITHIN NODEVALIDATOR
			List<Node> nodes = workflow.getNodes();
			for (Node node : nodes) {
				if (node.getType().equals("GetEntries")) {
					String moduleId = node.getValues().getModuleId();
					if (!ObjectId.isValid(moduleId)) {
						throw new BadRequestException("INVALID_MODULE_ID");
					}
					if (moduleId == null) {
						throw new BadRequestException("INCORRECT_GET_ENTRIES_NODE_VALUES");
					} else if (moduleExists(moduleId, companyId)) {
						continue;
					} else {
						throw new BadRequestException("MODULE_DOES_NOT_EXIST");
					}
				} else if (node.getType().equals("CreateEntry")) {
					String moduleId = node.getValues().getModuleId();
					List<Field> fields = node.getValues().getFields();
					if (!ObjectId.isValid(moduleId)) {
						throw new BadRequestException("INVALID_MODULE_ID");
					}
					if (moduleId == null) {
						throw new BadRequestException("INCORRECT_CREATE_ENTRY_NODE_VALUE_MODULE_ID");
					} else if (fields == null) {
						throw new BadRequestException("INCORRECT_CREATE_ENTRY_NODE_VALUE_FIELDS");
					} else if (moduleExists(moduleId, companyId)) {

						MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
						Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

						List<Document> moduleFields = (List<Document>) module.get("FIELDS");
						List<String> requiredFields = new ArrayList<String>();
						for (Document field : moduleFields) {
							if (field.getBoolean("REQUIRED")) {
								requiredFields.add(field.getString("FIELD_ID"));
							}
						}

						List<String> passedFields = new ArrayList<String>();
						for (Field field : fields) {
							passedFields.add(field.getFieldId());
						}

						for (String fieldId : requiredFields) {
							if (!passedFields.contains(fieldId)) {
								throw new BadRequestException("REQUIRED_FIELDS_MISSING");
							}
						}

						for (Field field : fields) {
							String fieldId = field.getFieldId();
							if (fieldExists(fieldId, moduleId, companyId)) {
								continue;
							} else {
								throw new BadRequestException("FIELD_DOES_NOT_EXIST");
							}
						}
					} else {
						throw new BadRequestException("MODULE_DOES_NOT_EXIST");
					}
				} else if (node.getType().equals("UpdateEntry")) {
					String moduleId = node.getValues().getModuleId();
					String entryId = node.getValues().getEntryId();
					List<Field> fields = node.getValues().getFields();
					if (moduleId == null) {
						throw new BadRequestException("INCORRECT_UPDATE_ENTRY_NODE_VALUE_MODULE_ID");
					} else if (entryId == null) {
						throw new BadRequestException("INCORRECT_UPDATE_ENTRY_NODE_VALUE_ENTRY_ID");
					} else if (fields == null) {
						throw new BadRequestException("INCORRECT_UPDATE_ENTRY_NODE_VALUE_FIELDS");
					} else if (moduleExists(moduleId, companyId)) {
						for (Field field : fields) {
							String fieldId = field.getFieldId();
							if (fieldExists(fieldId, moduleId, companyId)) {
								continue;
							} else {
								throw new BadRequestException("FIELD_DOES_NOT_EXIST");
							}
						}
					} else {
						throw new BadRequestException("MODULE_DOES_NOT_EXIST");
					}
				} else if (node.getType().equals("CreateEntryAndAssign")) {
					String moduleId = node.getValues().getModuleId();
					List<Field> fields = node.getValues().getFields();
					List<String> teams = node.getValues().getTeams();
					// CHECK ATTRIBUTES
					if (moduleId == null) {
						throw new BadRequestException("INCORRECT_CREATE_ENTRY_AND_ASSIGN_NODE_VALUE_MODULE_ID");
					} else if (fields == null) {
						throw new BadRequestException("INCORRECT_CREATE_ENTRY_AND_ASSIGN_NODE_VALUE_FIELDS");
						// CHECK MODULE
					} else if (teams == null) {
						throw new BadRequestException("INCORRECT_CREATE_ENTRY_AND_ASSIGN_NODE_VALUE_TEAMS");
					} else if (moduleExists(moduleId, companyId)) {

						MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
						Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

						List<Document> moduleFields = (List<Document>) module.get("FIELDS");
						List<String> requiredFields = new ArrayList<String>();
						for (Document field : moduleFields) {
							if (field.getBoolean("REQUIRED")) {
								requiredFields.add(field.getString("FIELD_ID"));
							}
						}

						List<String> passedFields = new ArrayList<String>();
						for (Field field : fields) {
							passedFields.add(field.getFieldId());
						}

						for (String fieldId : requiredFields) {
							if (!passedFields.contains(fieldId)) {
								throw new BadRequestException("REQUIRED_FIELDS_MISSING");
							}
						}

						// CHECK VALID FIELDS
						for (Field field : fields) {
							// CHECK FIELD FOR EACH FIELD
							String fieldId = field.getFieldId();
							if (fieldExists(fieldId, moduleId, companyId)) {
								continue;
							} else {
								throw new BadRequestException("FIELD_DOES_NOT_EXIST");
							}
						}

						// CHECK VALID TEAMS
						for (String teamId : teams) {
							if (teamExists(teamId, companyId)) {
								continue;
							} else {
								throw new BadRequestException("TEAM_DOES_NOT_EXIST");
							}
						}
					} else {
						throw new BadRequestException("MODULE_DOES_NOT_EXIST");
					}
				} else if (node.getType().equals("Route")) {
					// CHECK VALUE VS VALUES
					List<Condition> conditions = node.getValues().getConditions();
					for (Condition condition : conditions) {
						if (condition.getOperator().equals("between")) {
							if (condition.getValues() == null) {
								throw new BadRequestException("CONDITION_VALUES_NULL");
							}
						} else {
							if (condition.getValue() == null) {
								throw new BadRequestException("CONDITION_VALUE_NULL");
							}
						}
					}

					// CHECK VALID VARIABLE
					if (isValidChannelWorkflowVariable(node.getValues().getVariable(), channelType)) {
						continue;
					} else {
						throw new BadRequestException("CHANNEL_WORKFLOW_VARIABLE_INVALID");
					}
				} else if (node.getType().equals("StartEscalation")) {
					String escalationId = node.getValues().getEscalationId();
					if (!validEscalationId(escalationId, companyId)) {
						throw new BadRequestException("ESCALATION_DOES_NOT_EXIST");
					}
				}
			}

			// ACCESS MONGO
			MongoCollection<Document> collection = mongoTemplate
					.getCollection("channels_" + channelType + "_" + companyId);
			Document channelDocument = collection.find(Filters.eq("NAME", channelName)).first();

			// CHECK DOC
			if (channelDocument != null) {
				// CONVERT

				workflow.setDateUpdated(new Timestamp(new Date().getTime()));
				workflow.setLastUpdated(userId);
				String workflowJson = new ObjectMapper().writeValueAsString(workflow);
				Document newWorkflowDocument = Document.parse(workflowJson);

				// POST WORKFLOW
				collection.updateOne(Filters.eq("NAME", channelName), Updates.set("WORKFLOW", newWorkflowDocument));
				checkNode(workflow, companySubdomain);

				log.trace("Exit ChannelWorkflowService.postWorkflow(), ChannelType: " + channelType + ", ChannelName: "
						+ channelName);
				return workflow;

			} else {
				throw new ForbiddenException("CHANNEL_DOES_NOT_EXIST");
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public boolean moduleExists(String id, String companyId) {
		try {
			log.trace("Enter ChannelWorkflowService.moduleExists(), id: " + id + ", companyId: " + companyId);
			// ACCESS MONGO
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			if (new ObjectId().isValid(id)) {
				Document moduleDocument = collection.find(Filters.eq("_id", new ObjectId(id))).first();

				if (moduleDocument != null) {
					log.trace("Exit ChannelWorkflowService.moduleExists(), id: " + id + ", companyId: " + companyId);
					return true;
				} else {
					log.trace("Exit ChannelWorkflowService.moduleExists(), id: " + id + ", companyId: " + companyId);
					return false;
				}
			} else {
				throw new BadRequestException("INVALID_ENTRY_ID");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

	public boolean fieldExists(String fieldId, String moduleId, String companyId) {
		try {
			log.trace("Enter ChannelWorkflowService.fieldExists(), fieldId: " + fieldId + ", moduleId: " + moduleId
					+ ", companyId: " + companyId);
			// ACCESS MONGO
			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			Document fieldDocument = collection.find(
					Filters.and(Filters.eq("_id", new ObjectId(moduleId)), Filters.eq("FIELDS.FIELD_ID", fieldId)))
					.first();

			if (fieldDocument != null) {
				log.trace("Exit ChannelWorkflowService.fieldExists(), fieldId: " + fieldId + ", moduleId: " + moduleId
						+ ", companyId: " + companyId);
				return true;
			} else {
				log.trace("Exit ChannelWorkflowService.fieldExists(), fieldId: " + fieldId + ", moduleId: " + moduleId
						+ ", companyId: " + companyId);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

	public boolean teamExists(String teamId, String companyId) {
		try {
			log.trace("Enter ChannelWorkflowService.teamExists(), teamId: " + teamId + ", companyId: " + companyId);
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			if (new ObjectId().isValid(teamId)) {
				Document teamDocument = teamsCollection.find(Filters.eq("_id", new ObjectId(teamId))).first();

				if (teamDocument != null) {
					log.trace("Exit ChannelWorkflowService.teamExists(), teamId: " + teamId + ", companyId: "
							+ companyId);
					return true;
				} else {
					log.trace("Exit ChannelWorkflowService.teamExists(), teamId: " + teamId + ", companyId: "
							+ companyId);
					return false;
				}
			} else {
				throw new BadRequestException("INVALID_ENTRY_ID");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

	public boolean isValidChannelWorkflowVariable(String value, String channelType) {
		log.trace("Enter ChannelWorkflowService.isValidChannelWorkflowVariable(), value: " + value + ", channelType: "
				+ channelType);
		// IS VALID IN LIST
		boolean flag = true;

		switch (channelType) {
		case "chat":
			if (!Global.chatVariables.contains(value)) {
				flag = false;
			}
			break;
		case "email":
			if (!Global.emailVariables.contains(value)) {
				flag = false;
			}
			break;
		case "interval":
			if (!Global.intervalVariables.contains(value)) {
				flag = false;
			}
		default:
			break;
		}
		log.trace("Exit ChannelWorkflowService.isValidChannelWorkflowVariable(), value: " + value + ", channelType: "
				+ channelType);
		return flag;
	}

	public void checkNode(Workflow workflow, String companySubdomain) {
		try {
			log.trace("Enter ChannelWorkflowService.checkNode(), companySubdomain: " + companySubdomain);
			List<Node> nodes = workflow.getNodes();
			for (Node node : nodes) {
				if (node.getType().equals("CreateEntryAndAssign")) {
					String nodeId = node.getNodeId();
					String nodeJson;
					nodeJson = new ObjectMapper().writeValueAsString(node);
					log.trace("Exit ChannelWorkflowService.checkNode(), companySubdomain: " + companySubdomain);
					global.request("http://" + managerHost + ":9081/ngdesk/" + companySubdomain
							+ "/queues?node_id=" + nodeId, nodeJson, "POST", null);
				}
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}

	}

	public boolean validEscalationId(String escalationId, String companyId) {
		try {
			String collectionName = "escalations_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (new ObjectId().isValid(escalationId)) {
				Document document = collection.find(Filters.eq("_id", new ObjectId(escalationId))).first();
				if (document == null) {
					return false;
				}
			} else {
				throw new BadRequestException("ESCALATION_DOES_NOT_EXIST");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		return true;
	}
}
