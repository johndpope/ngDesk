package com.ngdesk.workflow.executor.dao;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.bson.Document;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.util.Calendar;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.commons.Global;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.MessageAttachment;
import com.ngdesk.data.dao.PublishDiscussionMessage;
import com.ngdesk.data.dao.Sender;
import com.ngdesk.data.dao.WorkflowExecutionInstance;
import com.ngdesk.repositories.AttachmentRepository;
import com.ngdesk.repositories.BlackListWhiteListRepository;
import com.ngdesk.repositories.EmailChannelRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.repositories.RoleRepository;
import com.ngdesk.workflow.company.dao.Company;
import com.ngdesk.workflow.dao.UpdateEntryNode;
import com.ngdesk.workflow.data.dao.BasePhone;
import com.ngdesk.workflow.module.dao.Module;
import com.ngdesk.workflow.module.dao.ModuleField;
import com.ngdesk.workflow.module.dao.ModulesService;

import io.grpc.netty.shaded.io.netty.handler.timeout.TimeoutException;

@Component
public class NodeOperations {

	private final Logger log = LoggerFactory.getLogger(NodeOperations.class);

	@Autowired
	private ModulesRepository moduleRepository;

	@Autowired
	private ModuleEntryRepository entryRepository;

	@Autowired
	private AttachmentRepository attachmentRepository;

	@Autowired
	private BlackListWhiteListRepository blackListWhiteListRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private EmailChannelRepository emailChannelRepository;

	@Autowired
	private AggregationService aggregationService;

	@Autowired
	Global global;

	@Autowired
	RedissonClient redisson;

	@Autowired
	ModulesService moduleService;

	private ObjectMapper mapper = new ObjectMapper();

	private SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	@Autowired
	RabbitTemplate rabbitTemplate;

	/*
	 * Replaces variables in a path and returns value
	 */
	public String getValue(WorkflowExecutionInstance instance, Module module, Map<String, Object> inputMessage,
			String path, List<String> replaceValues) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Company company = instance.getCompany();

			String section = path.split("\\.")[0];

			if (isDataType(module, section, "Aggregate")) {
				ModuleField aggregateField = module.getFields().stream()
						.filter(field -> field.getName().equals(section)).findFirst().orElse(null);
				ModuleField oneToManyField = module.getFields().stream()
						.filter(field -> field.getFieldId().equals(aggregateField.getAggregationField())).findFirst()
						.orElse(null);

				Optional<Module> optionalRelatedModule = moduleRepository.findById(oneToManyField.getModule(),
						"modules_" + company.getCompanyId());

				if (optionalRelatedModule.isPresent()) {

					Module relatedModule = optionalRelatedModule.get();

					ModuleField aggregationRelationshipField = relatedModule.getFields().stream()
							.filter(field -> field.getFieldId().equals(aggregateField.getAggregationRelatedField()))
							.findFirst().orElse(null);
					ModuleField relatedField = relatedModule.getFields().stream().filter(
							moduleField -> moduleField.getFieldId().equals(oneToManyField.getRelationshipField()))
							.findFirst().orElse(null);

					Float aggValue = aggregationService.getAggregationValue(instance, aggregateField.getConditions(),
							aggregateField, relatedField.getName(), instance.getEntry().get("DATA_ID").toString(),
							relatedModule, aggregationRelationshipField.getName());

					DecimalFormat formatter = new DecimalFormat("#,###.00");
					if (aggValue == null) {
						return "0.00";
					}
					return formatter.format(aggValue);
				}

			} else if (isDataType(module, section, "Relationship")) {
				if (!inputMessage.containsKey(section) || inputMessage.get(section) == null) {
					return "";
				}
				ModuleField field = module.getFields().stream()
						.filter(moduleField -> moduleField.getName().equals(section)).findFirst().orElse(null);
				if (field == null) {
					return "";
				}
				if (field.getName().equals("TEAMS")) {
					// TODO: NEEDS TO BE HANDLED CORRECTLY FOR ALL MANY TO MANY TYPES
					// Note: Currently returns email addresses of all users for teams
					return getEmailAddressesForTeams(inputMessage, company.getCompanyId());
				} else {
					Optional<Module> optionalRelatedModule = moduleRepository.findById(field.getModule(),
							"modules_" + company.getCompanyId());
					if (optionalRelatedModule.isEmpty()) {
						return "";
					}
					Module relatedModule = optionalRelatedModule.get();
					Optional<Map<String, Object>> optionalRelatedEntry = entryRepository.findEntryById(
							inputMessage.get(section).toString(),
							relatedModule.getName() + "_" + company.getCompanyId());

					if (optionalRelatedEntry.isEmpty()) {
						return "";
					}

					Map<String, Object> relatedEntry = optionalRelatedEntry.get();
					String dataId = relatedEntry.get("_id").toString();
					relatedEntry.put("DATA_ID", dataId);

					if (path.split("\\.").length > 1) {
						return getValue(instance, relatedModule, relatedEntry, path.split(section + "\\.")[1], null);
					}
					return mapper.writeValueAsString(relatedEntry);
				}
			} else if (isDataType(module, section, "Discussion")) {

				if (!inputMessage.containsKey(section) || inputMessage.get(section) == null) {
					return "";
				}
				// Checks If its Latest Message
				boolean isLatestMessage = false;
				if (path.split("\\.").length == 2 && path.split("\\.")[1].equalsIgnoreCase("Latest")) {
					isLatestMessage = true;
				}

				return getDiscussionMessage(company, isLatestMessage, inputMessage, instance, section, replaceValues);
			} else {
				if (path.split("\\.").length > 1) {
					if (inputMessage.get(section) == null) {
						return "";
					}
					Map<String, Object> newMap = (Map<String, Object>) inputMessage.get(section);
					return getValue(instance, module, newMap, path.split(section + "\\.")[1], null);
				} else {
					if (inputMessage.get(section) == null) {
						return "";
					}
					if (isDataType(module, section, "Phone")) {
						BasePhone number = mapper.readValue(mapper.writeValueAsString(inputMessage.get(section)),
								BasePhone.class);
						return getFormattedPhoneNumber(number);
					} else if (isDataType(module, section, "Date/Time")) {
						return getFormattedDateTimeValue(inputMessage.get(section).toString());
					} else if (isDataType(module, section, "Date")) {
						return getFormattedDateValue(inputMessage.get(section).toString());
					} else if (isDataType(module, section, "Time")) {
						return getFormattedTimeValue(inputMessage.get(section).toString());
					} else if (isDataType(module, section, "Chronometer")) {
						int value = Integer.parseInt(inputMessage.get(section).toString());
						return getUserReadableChronometerValue(value, "");
					} else if (isDataType(module, section, "Currency") || isDataType(module, section, "Currency Exchange")) {
						Double value = Double.parseDouble(inputMessage.get(section).toString());
						ModuleField currencyField = module.getFields().stream()
								.filter(field -> field.getName().equals(section)).findFirst().orElse(null);
						String format = currencyField.getNumericFormat();
						if (format == null || format.isBlank() || format.equals("None")) {
							DecimalFormat formatter = new DecimalFormat("#,###.0000");
							return formatter.format(value);
						} else {
							DecimalFormat formatter = new DecimalFormat(format + ".00");
							return formatter.format(value);
						}
					} else if (isDataType(module, section, "Formula")) {
						ModuleField formulaField = module.getFields().stream()
								.filter(field -> field.getName().equals(section)).findFirst().orElse(null);

						if (NumberUtils.isParsable(inputMessage.get(section).toString())) {
							Double value = Double.parseDouble(inputMessage.get(section).toString());
							String format = formulaField.getNumericFormat();
							if (format == null || format.isBlank() || format.equals("None")) {
								DecimalFormat formatter = new DecimalFormat("#,###.00");
								return formatter.format(value);
							} else {
								DecimalFormat formatter = new DecimalFormat(format + ".00");
								return formatter.format(value);
							}

						} else {
							return inputMessage.get(section).toString();

						}
					} else {
						System.out.println(section);
						System.out.println(inputMessage.get(section));
						return inputMessage.get(section).toString();
					}

				}
			}
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return "";
	}

	private String getEmailAddressesForTeams(Map<String, Object> inputMessage, String companyId) {
		try {
			List<String> teamIds = (List<String>) inputMessage.get("TEAMS");
			List<Map<String, Object>> users = entryRepository.findEntriesByTeamIds(teamIds, "Users_" + companyId);

			List<String> emailAddresses = new ArrayList<String>();
			for (Map<String, Object> user : users) {
				emailAddresses.add(user.get("EMAIL_ADDRESS").toString());
			}

			return mapper.writeValueAsString(emailAddresses);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "";
	}

	/*
	 * Returns Discussion Messages formatted top down in html Messages do not
	 * include meta data The returning message includes attachments if any
	 */
	private String getDiscussionMessage(Company company, boolean isLatestMessage, Map<String, Object> inputMessage,
			WorkflowExecutionInstance instance, String section, List<String> replaceValues) {
		try {
			String discussionMessageBody = global.getFile("reply-above.html");
			discussionMessageBody = discussionMessageBody.replaceAll("REPLY_ABOVE_REPLACE",
					Global.errorMsg(company.getLanguage(), "REPLY_ABOVE"));

			List<DiscussionMessage> messages = mapper.readValue(mapper.writeValueAsString(inputMessage.get(section)),
					new TypeReference<List<DiscussionMessage>>() {
					});
			messages = messages.stream().filter(message -> !message.getMessageType().equals("META_DATA"))
					.collect(Collectors.toList());

			for (int i = messages.size() - 1; i >= 0; i--) {
				DiscussionMessage message = messages.get(i);
				Sender sender = message.getSender();

				discussionMessageBody += getFormattedHtmlMessage(message, sender, company.getCompanyName(), replaceValues);

				discussionMessageBody = addAttachmentsToDiscussionMessageBody(message.getAttachments(),
						company.getCompanySubdomain(), instance, discussionMessageBody, message.getMessageId());
				discussionMessageBody += "<br/><hr/>";

				if (i == 0) {
					String ticketLink = "<a href=\"https://" + company.getCompanySubdomain() + ".ngdesk.com/render/"
							+ instance.getModule().getModuleId() + "/edit/"
							+ instance.getEntry().get("DATA_ID").toString() + "\"> View it on ngdesk </a>";
					discussionMessageBody += ticketLink;
					discussionMessageBody += "<br/><br/>";
					discussionMessageBody += "*****************************************************************";
					discussionMessageBody += "<br/>" + "Do not modify the following content: <br/>" + "ENTRY_ID: "
							+ instance.getEntry().get("DATA_ID").toString() + "<br/>" + "MODULE_ID: "
							+ instance.getModule().getModuleId() + "<br/>" + "COMPANY_SUBDOMAIN: "
							+ company.getCompanySubdomain();
				}
				if (isLatestMessage) {
					break;
				}

			}
			return discussionMessageBody;
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "";
	}

	/*
	 * This function adds attachments to the discussion message body
	 */
	private String addAttachmentsToDiscussionMessageBody(List<MessageAttachment> attachments, String companySubdomain,
			WorkflowExecutionInstance instance, String discussionMessageBody, String messageId) {
		boolean firstAttachment = true;
		for (MessageAttachment attachment : attachments) {
			if (firstAttachment) {
				firstAttachment = false;
				discussionMessageBody += "<br/>";
				discussionMessageBody += "Attachments: <br/>";
			}
			discussionMessageBody += "<a target=\"_blank\" href=\"https://" + companySubdomain
					+ ".ngdesk.com/ngdesk-rest/ngdesk/attachments?attachment_uuid=" + attachment.getAttachmentUuid()
					+ "&message_id=" + messageId + "&entry_id=" + instance.getEntry().get("DATA_ID").toString()
					+ "&module_id=" + instance.getModule().getModuleId() + "\">" + attachment.getFileName()
					+ "</a><br/>";
		}
		return discussionMessageBody;
	}

	/*
	 * This function is used to generate a html message for each discussion message
	 */
	private String getFormattedHtmlMessage(DiscussionMessage message, Sender sender, String companyName, List<String> replaceValues) {
		String messageBody = message.getMessage();
		Pattern pattern = Pattern.compile("<body>(.*?)<\\/body>");
		Matcher matcher = pattern.matcher(messageBody);
		if (matcher.find()) {
			String match = matcher.group(1);
			match = match.replaceAll("\n", "<br/>");
			messageBody = messageBody.replaceAll("<body>(.*?)<\\/body>",
					Matcher.quoteReplacement("<body>" + match + "</body>"));
		}

		Timestamp dateCreated = new Timestamp(message.getDateCreated().getTime());
		String formattedDateCreated = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(dateCreated);
		String messageHtml = global.getFile("email_message.html");
		if (message.getMessageType().equalsIgnoreCase("INTERNAL_COMMENT")) {
			messageHtml = global.getFile("email_internal_comment.html");
		}

		String senderLastName = "";
		if (sender.getLastName() != null) {
			senderLastName = sender.getLastName();
		}

//		messageHtml = messageHtml.replaceAll("FIRST_NAME", sender.getFirstName());
//		messageHtml = messageHtml.replaceAll("LAST_NAME", senderLastName);
//		messageHtml = messageHtml.replaceAll("COMPANY_NAME", Matcher.quoteReplacement(companyName));
//		messageHtml = messageHtml.replaceAll("DATE_AND_TIME", formattedDateCreated);
		messageHtml = messageHtml.replaceAll("FIRST_REPLACE", replaceValues.get(0));
		messageHtml = messageHtml.replaceAll("SECOND_REPLACE", replaceValues.get(1));
		messageHtml = messageHtml.replaceAll("THIRD_REPLACE", replaceValues.get(2));
		messageHtml = messageHtml.replaceAll("MESSAGE_REPLACE", Matcher.quoteReplacement(messageBody));

		return messageHtml;
	}

	/*
	 * Function returns chronometer value in user readable format
	 */
	public String getUserReadableChronometerValue(int value, String formattedTime) {
		// Conversion rates 1d = 8h, 1w = 5d, 1mo = 20d(4w)
		try {
			if (value >= 9600) {
				// 1 Month = 9600 minutes
				int remainder = value % 9600;
				if (remainder == 0) {
					return value / 9600 + "mo";
				} else {
					formattedTime = value / 9600 + "mo";
					return getUserReadableChronometerValue(remainder, formattedTime);
				}
			} else if (value >= 2400) {
				// 1 Week = 2400 minutes
				int remainder = value % 2400;
				if (remainder == 0) {
					if (formattedTime.length() > 0) {
						return formattedTime + " " + value / 2400 + "w";
					} else {
						return value / 2400 + "w";
					}
				} else {
					if (formattedTime.length() > 0) {
						formattedTime = formattedTime + ' ' + (value / 2400) + 'w';
					} else {
						formattedTime = (value / 2400) + "w";
					}
					return getUserReadableChronometerValue(remainder, formattedTime);
				}
			} else if (value >= 480) {
				// 1 Day = 480 minutes
				int remainder = value % 480;
				if (remainder == 0) {
					if (formattedTime.length() > 0) {
						return formattedTime + " " + value / 480 + "d";
					} else {
						return value / 480 + "d";
					}
				} else {
					if (formattedTime.length() > 0) {
						formattedTime = formattedTime + ' ' + (value / 480) + 'd';
					} else {
						formattedTime = (value / 480) + "d";
					}
					return getUserReadableChronometerValue(remainder, formattedTime);
				}
			} else if (value >= 60) {
				// 1 Hour = 60 minutes
				int remainder = value % 60;
				if (remainder == 0) {
					if (formattedTime.length() > 0) {
						return formattedTime + " " + value / 60 + "h";
					} else {
						return value / 60 + "h";
					}
				} else {
					if (formattedTime.length() > 0) {
						formattedTime = formattedTime + ' ' + (value / 60) + "h";
					} else {
						formattedTime = (value / 60) + "h";
					}
					return getUserReadableChronometerValue(remainder, formattedTime);
				}
			} else {
				if (formattedTime.length() > 0) {
					return formattedTime + " " + value + "m";
				} else {
					return formattedTime + value + "m";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/*
	 * Returns Phone number in Dial code + number Format
	 */
	private String getFormattedPhoneNumber(BasePhone phoneNumber) {
		if (phoneNumber.getDialCode() != null && phoneNumber.getPhoneNumber() != null) {
			return phoneNumber.getDialCode() + phoneNumber.getPhoneNumber();
		} else if (phoneNumber.getDialCode() == null && phoneNumber.getPhoneNumber() != null) {
			return phoneNumber.getPhoneNumber();
		}
		return "";
	}

	/*
	 * This function returns timestamp in formatted string
	 */
	private String getFormattedDateTimeValue(String value) {
		try {
			Date parsedDate = new Date(Long.parseLong(value));
			return new SimpleDateFormat("MMM dd, yyyy HH:mm a").format(parsedDate);
		} catch (NumberFormatException e) {
//			e.printStackTrace();
		}
		return "";
	}

	/*
	 * This function returns date in formatted string
	 */
	private String getFormattedDateValue(String value) {
		try {
			Date parsedDate = new Date(Long.valueOf(value));
			return new SimpleDateFormat("MMM dd, yyyy").format(parsedDate);
		} catch (NumberFormatException e1) {
//			e1.printStackTrace();
		}
		return "";
	}

	/*
	 * This function returns time in formatted string
	 */
	private String getFormattedTimeValue(String value) {
		try {
			Date parsedDate = new Date(Long.valueOf(value));
			return new SimpleDateFormat("HH:mm a").format(parsedDate);
		} catch (NumberFormatException e) {
//			e.printStackTrace();
		}
		return "";
	}

	/*
	 * Checks if a field belongs to a certain data type returns true / false
	 */
	public boolean isDataType(Module module, String fieldName, String dataType) {

		ModuleField field = module.getFields().stream().filter(moduleField -> moduleField.getName().equals(fieldName))
				.findFirst().orElse(null);

		if (field != null) {
			String displayDataType = field.getDataType().getDisplay();

			if (displayDataType.equals("Relationship") && dataType.equals("Relationship")) {
				if (field.getRelationshipType().equals("One to One")
						|| field.getRelationshipType().equals("Many to One")
						|| (field.getRelationshipType().equals("Many to Many") && field.getName().equals("TEAMS"))) {
					return true;
				}

			} else {
				if (displayDataType.equals(dataType)) {
					return true;
				}
			}
		}

		return false;
	}

	/*
	 * This function builds Meta Data payload
	 */

	public DiscussionMessage buildMetaDataPayload(String message, WorkflowExecutionInstance instance) {

		Optional<Map<String, Object>> optionalUser = entryRepository.findEntryByVariable("EMAIL_ADDRESS",
				"system@ngdesk.com", "Users_" + instance.getCompany().getCompanyId());

		Map<String, Object> systemUser = optionalUser.get();

		String contactId = systemUser.get("CONTACT").toString();

		Optional<Map<String, Object>> optionalContact = entryRepository.findById(contactId,
				"Contacts_" + instance.getCompany().getCompanyId());
		Map<String, Object> contact = optionalContact.get();

		Sender sender = new Sender(contact.get("FIRST_NAME").toString(), contact.get("LAST_NAME").toString(),
				systemUser.get("USER_UUID").toString(), systemUser.get("ROLE").toString());
		String companySubDomain = instance.getCompany().getCompanySubdomain();

		return new DiscussionMessage(message, new Date(), UUID.randomUUID().toString(), "META_DATA",
				new ArrayList<MessageAttachment>(), sender, instance.getModule().getModuleId(),
				instance.getEntry().get("DATA_ID").toString(), null, companySubDomain);

	}

	/*
	 * This function builds Discussion Message payload
	 */

	public DiscussionMessage buildDiscussionPayload(String message, WorkflowExecutionInstance instance) {
		Optional<Map<String, Object>> optionalUser = entryRepository.findEntryById(instance.getUserId(),
				"Users_" + instance.getCompany().getCompanyId());

		Map<String, Object> user = optionalUser.get();

		String contactId = user.get("CONTACT").toString();

		Optional<Map<String, Object>> optionalContact = entryRepository.findById(contactId,
				"Contacts_" + instance.getCompany().getCompanyId());
		Map<String, Object> contact = optionalContact.get();

		Sender sender = new Sender(contact.get("FIRST_NAME").toString(), contact.get("LAST_NAME").toString(),
				user.get("USER_UUID").toString(), user.get("ROLE").toString());

		String companySubDomain = instance.getCompany().getCompanySubdomain();
		return new DiscussionMessage(message, new Date(), UUID.randomUUID().toString(), "MESSAGE",
				new ArrayList<MessageAttachment>(), sender, null, null, null, companySubDomain);

	}

	/*
	 * This function Used for getting value for update entry nodes
	 */
	public Object getValueForCreateUpdateEntry(List<String> value, ModuleField field,
			Map<String, Object> inputMessage) {

		try {

			Object valueObject = null;
			String valuePattern = mapper.writeValueAsString(value);
			if (value.size() == 1) {
				valuePattern = value.get(0);
			}

			String reg = "\\{\\{(.*)\\}\\}";
			Pattern r = Pattern.compile(reg);
			Matcher m = r.matcher(valuePattern.toString());

			if (m.find()) {
				String path = m.group(1);

				String sections[] = path.split("\\.");
				if (sections.length > 1 && sections[0].equalsIgnoreCase("INPUTMESSAGE")) {
					Object obj = inputMessage;
					for (int j = 1; j < sections.length; ++j) {
						obj = ((Map<String, Object>) obj).get(sections[j]);
						if (obj == null)
							break;
					}
					if (obj != null) {
						valueObject = obj;
					}
				}
			} else {
				if (field != null && field.getDataType().getDisplay().equals("Relationship")) {
					String relationshipType = field.getRelationshipType();
					if (!relationshipType.equalsIgnoreCase("Many to Many")) {
						valueObject = valuePattern;
					} else {
						valueObject = valuePattern;
					}
				} else {
					valueObject = valuePattern;
				}
			}
			return valueObject;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "";
	}

	/*
	 * This function Used for getting value for Create entry nodes
	 */
	public Object getValueForCreateEntry(List<String> value, ModuleField field, Map<String, Object> inputMessage) {
		try {

			Object valueObject = null;
			String valuePattern = mapper.writeValueAsString(value);
			if (value.size() == 1) {
				valuePattern = value.get(0).toString();
			}

			String regex = "\\{\\{(.*?)\\}\\}";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(valuePattern.trim());

			if (matcher.find()) {
				String path = matcher.group(1);

				String sections[] = path.split("\\.");
				if (sections.length > 1 && sections[0].equalsIgnoreCase("INPUTMESSAGE")) {
					Object obj = inputMessage;
					for (int j = 1; j < sections.length; ++j) {
						obj = ((Map<String, Object>) obj).get(sections[j]);
						if (obj == null)
							break;
					}
					if (obj != null) {
						valueObject = valuePattern.replaceAll("\\{\\{" + path + "\\}\\}", obj.toString());
					}
				}
			} else {
				if (field != null && field.getDataType().getDisplay().equals("Relationship")) {
					String relationshipType = field.getRelationshipType();
					if (!relationshipType.equalsIgnoreCase("Many to Many")) {
						valueObject = valuePattern;
					} else {
						valueObject = valuePattern;
					}
				} else {
					valueObject = valuePattern;
				}
			}
			return valueObject;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "";
	}

	/*
	 * This function is used for parsing attachment for create and update entry
	 */
	public List<MessageAttachment> getAttachmentFromString(Object attachment) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			List<MessageAttachment> attachments = mapper.readValue(attachment.toString(),
					new TypeReference<List<MessageAttachment>>() {
					});
			return attachments;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;

	}

	/*
	 * This function used for getting userUUID for post and put data call
	 */
	public String getUserUuid(String userId, String companyId) {
		Optional<Map<String, Object>> optionalUser = entryRepository.findEntryById(userId, "Users_" + companyId);
		Map<String, Object> user = optionalUser.get();
		return user.get("USER_UUID").toString();
	}

	public Map<String, Object> getUser(String userId, String companyId) {
		Optional<Map<String, Object>> optionalUser = entryRepository.findEntryById(userId, "Users_" + companyId);
		Map<String, Object> user = optionalUser.get();
		return user;
	}

	public Map<String, Object> getEntry(String entryId, String collectionName) {
		if (entryId == null) {
			return null;
		}
		Optional<Map<String, Object>> optionalEntry = entryRepository.findEntryById(entryId, collectionName);
		return optionalEntry.get();
	}

	public String handleCustomReplaces(String message, WorkflowExecutionInstance instance) {

		// CONDITIONAL SECTIONS
		String conditionalRegex = "~~(.*?)~~";
		Pattern conditionalRegexPattern = Pattern.compile(conditionalRegex, Pattern.DOTALL);
		Matcher conditionalRegexMatcher = conditionalRegexPattern.matcher(message);
		while (conditionalRegexMatcher.find()) {
			String matchedMessage = conditionalRegexMatcher.group(1);
			String conditionRegex = "\\{\\{(.*?)\\}\\}";
			Pattern conditionRegexPattern = Pattern.compile(conditionRegex);
			Matcher conditionRegexMatcher = conditionRegexPattern.matcher(matchedMessage);
			if (conditionRegexMatcher.find()) {
				String condition = conditionRegexMatcher.group(1);
				String path = "";
				String value = "";
				String givenValue = "";
				Boolean conditionToEvaluate = null;
				if (condition.indexOf("==") != -1) {
					path = condition.split("==")[0].split("(?i)inputMessage\\.")[1].trim();
					value = getValue(instance, instance.getModule(), instance.getEntry(), path, null);
					givenValue = condition.split("==")[1].trim();
					conditionToEvaluate = value.equals(givenValue);
				} else if (condition.indexOf("!=") != -1) {
					path = condition.split("!=")[0].split("(?i)inputMessage\\.")[1].trim();
					value = getValue(instance, instance.getModule(), instance.getEntry(), path, null);
					givenValue = condition.split("!=")[1].trim();
					if (givenValue.equals("''")) {
						givenValue = "";
					}
					conditionToEvaluate = !value.equals(givenValue);
				}

				if (conditionToEvaluate) {
					String capturedText = conditionalRegexMatcher.group(0);
					capturedText = capturedText.replaceAll("~~", "");
					capturedText = capturedText.replaceAll("\\{\\{(?i)(inputMessage.*?)\\}\\}", "");
					message = message.replace(conditionalRegexMatcher.group(0), capturedText);
				} else {
					message = message.replace(conditionalRegexMatcher.group(0), "");
				}
			}
		}

		// DATE ADDITION
		String dateAdditionRegex = "\\{\\{(CURRENT_DATE)\\s*\\+\\s*([0-9]+)(d)\\}\\}";
		Pattern dateAdditionPattern = Pattern.compile(dateAdditionRegex);
		Matcher dateAdditionMatcher = dateAdditionPattern.matcher(message);
		while (dateAdditionMatcher.find()) {
			String match = dateAdditionMatcher.group(0);
			Date date = new Date();
			Integer valueToAdd = Integer.parseInt(dateAdditionMatcher.group(2));
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.DATE, valueToAdd);
			date = calendar.getTime();
			String userReadableDate = new SimpleDateFormat("MMMM dd, yyyy HH:mm a").format(date);
			message = message.replace(match, userReadableDate);
		}

		// DATE REPLACE
		String dateRegex = "\\{\\{CURRENT_DATE\\}\\}";
		Pattern datePattern = Pattern.compile(dateRegex);
		Matcher dateMatcher = datePattern.matcher(message);

		while (dateMatcher.find()) {
			String match = dateMatcher.group(0);
			Date date = new Date();
			String userReadableDate = new SimpleDateFormat("MMMM dd, yyyy").format(date);
			message = message.replace(match, userReadableDate);
		}

		// USER OPERATIONS
		String userRegex = "\\{\\{CURRENT_USER\\.([A-Z_]+)\\}\\}";
		Pattern userPattern = Pattern.compile(userRegex);
		Matcher userMatcher = userPattern.matcher(message);
		while (userMatcher.find()) {
			String match = userMatcher.group(0);
			String variable = userMatcher.group(1);

			Map<String, Object> userMap = getUser(instance.getUserId(), instance.getCompany().getCompanyId());
			if (userMap != null) {
				String value = "";
				// TODO: INTEGRATE WITH NODE OPERATIONS GET VALUE TEMPORARILY HARDOCDED
				// -Shashank
				if (variable.equals("FIRST_NAME") || variable.equals("LAST_NAME")) {
					Map<String, Object> contactMap = getEntry(userMap.get("CONTACT").toString(),
							"Contacts_" + instance.getCompany().getCompanyId());
					if (contactMap != null) {
						value = contactMap.get(variable).toString();
					}
				} else {
					value = userMap.get(variable).toString();
				}
				message = message.replace(match, value);
			}
		}

		return message;
	}

	/*
	 * This function checks whether a user is an admin/agent
	 */
	public boolean isAdminOrAgent(String emailAddress, String companyId) {
		Optional<Map<String, Object>> optionalUser = entryRepository.findEntryByVariable("EMAIL_ADDRESS",
				emailAddress.strip(), "Users_" + companyId);
		if (optionalUser.isEmpty()) {
			return false;
		}

		Map<String, Object> userEntry = optionalUser.get();

		if (!userEntry.containsKey("ROLE") || userEntry.get("ROLE") == null) {
			return false;
		}
		Optional<Role> optionalRole = roleRepository.findById(userEntry.get("ROLE").toString(), "roles_" + companyId);
		if (optionalRole.isEmpty()) {
			return false;
		}

		Role role = optionalRole.get();
		if (role.getName().equals("SystemAdmin") || role.getName().equals("Agent")) {
			return true;
		}
		return false;
	}

	/*
	 * This function checks if a channel exists for given email address
	 */
	public boolean isEmailAddressAChannel(String emailAddress, String companyId) {
		Optional<EmailChannel> optionalChannel = emailChannelRepository.findChannelByEmailAddress(emailAddress,
				companyId);
		if (optionalChannel.isPresent()) {
			return true;
		}
		return false;
	}

	/*
	 * This function checks the outgoing blacklist and white list records and
	 * returns boolean to send an email
	 */
	public boolean emailClearedToSend(String emailAddress, String companyId) {
		String domain = emailAddress.split("@")[1];
		String type = "OUTGOING";

		Optional<BlackListWhiteList> optionalEmailWhiteListRecord = blackListWhiteListRepository
				.findWhiteListedRecordByEmailAddressAndType(emailAddress, companyId, type);

		if (optionalEmailWhiteListRecord.isPresent()) {
			return true;
		}

		Optional<BlackListWhiteList> optionalDomainWhiteListRecord = blackListWhiteListRepository
				.findWhiteListedRecordByDomainAndType(domain, companyId, type);

		if (optionalDomainWhiteListRecord.isPresent()) {
			return true;
		}

		Optional<BlackListWhiteList> optionalEmailBlackListRecord = blackListWhiteListRepository
				.findBlackListedRecordByEmailAddressAndType(emailAddress, companyId, type);
		if (optionalEmailBlackListRecord.isPresent()) {
			return false;
		}

		Optional<BlackListWhiteList> optionalDomainBlackListRecord = blackListWhiteListRepository
				.findBlackListedRecordByDomainAndType(domain, companyId, type);
		if (optionalDomainBlackListRecord.isPresent()) {
			return false;
		}

		return true;
	}

	/*
	 * This function checks if email or domain is white listed
	 */
	public boolean isOutgoingEmailOrDomainWhitelisted(String emailAddress, String companyId) {
		String domain = emailAddress.split("@")[1];
		String type = "OUTGOING";

		Optional<BlackListWhiteList> optionalEmailWhiteListRecord = blackListWhiteListRepository
				.findWhiteListedRecordByEmailAddressAndType(emailAddress, companyId, type);

		if (optionalEmailWhiteListRecord.isPresent()) {
			return true;
		}

		Optional<BlackListWhiteList> optionalDomainWhiteListRecord = blackListWhiteListRepository
				.findWhiteListedRecordByDomainAndType(domain, companyId, type);

		if (optionalDomainWhiteListRecord.isPresent()) {
			return true;
		}

		return false;
	}

	/*
	 * This function is used for adding discussion message to websocket queue
	 */
	public void addToDiscussionQueue(PublishDiscussionMessage message) {
		try {
			log.debug("Publishing to websocket");
			log.debug(new ObjectMapper().writeValueAsString(message));
			rabbitTemplate.convertAndSend("publish-discussion", message);

		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	/*
	 * This function is used for getting list text for create entry and update entry
	 * node
	 */
	public List<String> listTextValues(Map<String, Object> inputMessage, Object value, ModuleField field,
			Boolean replace) {

		List<String> lists = new ArrayList<String>();

		if (value != null && (replace == null || replace == false)) {

			try {
				lists = (List<String>) inputMessage.get(field.getName());
				List<String> list = mapper.readValue(value.toString(), List.class);
				lists.addAll(list);
			} catch (Exception e) {
				String[] items = value.toString().split(",");
				for (String item : items) {
					if (field.getListTextUnique() != null) {

						if (field.getListTextUnique() && !lists.contains(item)) {
							lists.add(item);
						} else if (!field.getListTextUnique()) {
							lists.add(item);
						}

					} else {
						if (!lists.contains(item)) {
							lists.add(item);
						}

					}

				}
			}

		}

		else if (value != null) {
			lists.add(value.toString());
		}

		return lists;

	}

	/*
	 * This function is used for getting Picklist (Multi-Select) for create entry
	 * and update entry node
	 */
	public List<String> picklistMultiSelectValues(Object value, ModuleField field) {
		List<String> picklistMultiSelect = new ArrayList<String>();
		if (value != null) {
			try {
				picklistMultiSelect = mapper.readValue(value.toString(), List.class);

			} catch (Exception e) {
				String[] items = value.toString().split(",");
				for (String item : items) {
					if (field.getListTextUnique() != null) {
						if (field.getListTextUnique() && !picklistMultiSelect.contains(item)) {
							picklistMultiSelect.add(item);
						} else if (!field.getListTextUnique()) {
							picklistMultiSelect.add(item);
						}
					}
				}
			}
		}
		return picklistMultiSelect;
	}

	public List<String> relationshipArrayValues(Map<String, Object> inputMessage, Object value, ModuleField field,
			Boolean replace) {
		List<String> relationShipFieldIds = new ArrayList<String>();
		if (value != null && (replace == null || replace == false)) {
			try {
				relationShipFieldIds = (List<String>) inputMessage.get(field.getName());
				Map<String, Object> relationshipValues = mapper.readValue(value.toString(), Map.class);
				String dataId = relationshipValues.get("DATA_ID").toString();
				if (!relationShipFieldIds.contains(dataId)) {
					relationShipFieldIds.add(dataId);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (value != null) {

			try {
				Map<String, Object> values = new ObjectMapper().readValue(value.toString(), Map.class);
				String NewValue = values.get("DATA_ID").toString();
				if (!relationShipFieldIds.contains(NewValue)) {
					relationShipFieldIds.add(NewValue);
				}

			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

		}
		return relationShipFieldIds;
	}

	/*
	 * This function used for getting System User's UUID
	 */
	public String getSystemUserUuid(String companyId) {
		Optional<Map<String, Object>> optionalUser = entryRepository.findEntryByVariable("EMAIL_ADDRESS",
				"system@ngdesk.com", "Users_" + companyId);

		Map<String, Object> user = optionalUser.get();

		return user.get("USER_UUID").toString();
	}

	public boolean isInternalCommentAdded(Map<String, Object> entry, Map<String, Object> existingEntry,
			ModuleField discussionField) {
		ObjectMapper mapper = new ObjectMapper();
		if (existingEntry.isEmpty() || discussionField == null) {
			return false;
		}
		if (existingEntry.get(discussionField.getName()) == null || entry.get(discussionField.getName()) == null) {
			return false;
		}
		try {
			List<DiscussionMessage> messages = mapper.readValue(
					mapper.writeValueAsString(entry.get(discussionField.getName())),
					mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class));

			List<DiscussionMessage> existingMessages = mapper.readValue(
					mapper.writeValueAsString(existingEntry.get(discussionField.getName())),
					mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class));

			if (messages.size() == existingMessages.size()) {
				return false;
			} else if (messages.size() > existingMessages.size()) {
				DiscussionMessage message = messages.get(messages.size() - 1);
				if (message.getMessageType().equalsIgnoreCase("INTERNAL_COMMENT")) {
					return true;
				}
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return false;
	}

	public String roleName(String roleId, String companyId) {
		Optional<Role> optionalRole = roleRepository.findById(roleId, "roles_" + companyId);
		return optionalRole.get().getName();
	}

	public String getSystemUser(String companyId) {
		String UUID = "";
		try {

			Optional<Map<String, Object>> optionalUser = entryRepository.findEntryByVariable("EMAIL_ADDRESS",
					"system@ngdesk.com", "Users_" + companyId);
			UUID = optionalUser.get().get("USER_UUID").toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return UUID;
	}

}
