package com.ngdesk.data.dao;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.company.dao.Company;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.repositories.companies.CompanyRepository;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;

@Component
public class FormulaService {

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	ModuleService moduleService;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	Global global;

	@Autowired
	CompanyRepository companyRepository;

	private SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	ObjectMapper mapper = new ObjectMapper();

	public String getValue(Module module, Map<String, Object> inputMessage, String path) {
		try {
			String companyId = authManager.getUserDetails().getCompanyId();
			Optional<Company> optionalCompany = companyRepository.findById(companyId, "companies");
			Company company = optionalCompany.get();

			String section = path.split("\\.")[0];

			if (isDataType(module, section, "Aggregate")) {
				ModuleField aggregateField = module.getFields().stream()
						.filter(field -> field.getName().equals(section)).findFirst().orElse(null);
				ModuleField oneToManyField = module.getFields().stream()
						.filter(field -> field.getFieldId().equals(aggregateField.getAggregationField())).findFirst()
						.orElse(null);

				System.out.println("**************************************");
				System.out.println(path);
				System.out.println(section);
				System.out.println(aggregateField.getName());
				System.out.println(oneToManyField.getName());

				Optional<Module> optionalRelatedModule = modulesRepository.findById(oneToManyField.getModule(),
						"modules_" + companyId);

				if (optionalRelatedModule.isPresent()) {

					Module relatedModule = optionalRelatedModule.get();
					System.out.println(relatedModule.getName());

					ModuleField aggregationRelationshipField = relatedModule.getFields().stream()
							.filter(field -> field.getFieldId().equals(aggregateField.getAggregationRelatedField()))
							.findFirst().orElse(null);
					ModuleField relatedField = relatedModule.getFields().stream().filter(
							moduleField -> moduleField.getFieldId().equals(oneToManyField.getRelationshipField()))
							.findFirst().orElse(null);

					System.out.println(relatedField.getName());
					System.out.println(inputMessage.get("_id").toString());

					Criteria criteria = new Criteria();
					criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFECTIVE_TO").is(null),
							Criteria.where(relatedField.getName()).is(inputMessage.get("_id").toString()));
					Query query = new Query(criteria);

					List<Map<String, Object>> relatedEntries = entryRepository.findAll(query,
							moduleService.getCollectionName(relatedModule.getName(), companyId));

					// TODO: HARDCODED TO HANDLE ONLY SUM FOR AGGREGATE FIELDS
					double totalCost = 0;
					for (Map<String, Object> entry : relatedEntries) {
						if (entry.get(aggregationRelationshipField.getName()) != null) {
							totalCost += Double.valueOf(entry.get(aggregationRelationshipField.getName()).toString());
						}
					}
					return String.valueOf(totalCost);
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
					return getEmailAddressesForTeams(inputMessage, companyId);
				} else {
					Optional<Module> optionalRelatedModule = modulesRepository.findById(field.getModule(),
							"modules_" + companyId);
					if (optionalRelatedModule.isEmpty()) {
						return "";
					}
					Module relatedModule = optionalRelatedModule.get();
					Optional<Map<String, Object>> optionalRelatedEntry = entryRepository.findEntryById(
							inputMessage.get(section).toString(), relatedModule.getName() + "_" + companyId);

					if (optionalRelatedEntry.isEmpty()) {
						return "";
					}

					Map<String, Object> relatedEntry = optionalRelatedEntry.get();
					String dataId = relatedEntry.get("_id").toString();
					relatedEntry.put("DATA_ID", dataId);

					if (path.split("\\.").length > 1) {
						return getValue(relatedModule, relatedEntry, path.split(section + "\\.")[1]);
					}
					return mapper.writeValueAsString(relatedEntry);
				}
			} else if (isDataType(module, section, "Discussion")) {

				if (!inputMessage.containsKey(section) || inputMessage.get(section) == null) {
					return "";
				} // Checks If its Latest Message
				boolean isLatestMessage = false;
				if (path.split("\\.").length == 2 && path.split("\\.")[1].equalsIgnoreCase("Latest")) {
					isLatestMessage = true;
				}
				return getDiscussionMessage(company, isLatestMessage, inputMessage, section, module);
			} else {
				if (path.split("\\.").length > 1) {
					if (inputMessage.get(section) == null) {
						return "";
					}
					Map<String, Object> newMap = (Map<String, Object>) inputMessage.get(section);
					return getValue(module, newMap, path.split(section + "\\.")[1]);
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
					} else if (isDataType(module, section, "Currency")) {
						return inputMessage.get(section).toString();
					} else if (isDataType(module, section, "Formula")) {
						return inputMessage.get(section).toString();
					} else {
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

	private boolean isDataType(Module module, String fieldName, String dataType) {

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

	private String getDiscussionMessage(Company company, boolean isLatestMessage, Map<String, Object> inputMessage,
			String section, Module module) {

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

				discussionMessageBody += getFormattedHtmlMessage(message, sender, company.getCompanyName());

				discussionMessageBody = addAttachmentsToDiscussionMessageBody(message.getAttachments(),
						company.getCompanySubdomain(), discussionMessageBody, message.getMessageId(), inputMessage,
						module);
				discussionMessageBody += "<br/><hr/>";

				if (i == 0) {
					String ticketLink = "<a href=\"https://" + company.getCompanySubdomain() + ".ngdesk.com/render/"
							+ module.getModuleId() + "/edit/" + inputMessage.get("_id").toString()
							+ "\"> View it on ngdesk </a>";
					discussionMessageBody += ticketLink;
					discussionMessageBody += "<br/><br/>";
					discussionMessageBody += "*****************************************************************";
					discussionMessageBody += "<br/>" + "Do not modify the following content: <br/>" + "ENTRY_ID: "
							+ inputMessage.get("_id").toString() + "<br/>" + "MODULE_ID: " + module.getModuleId()
							+ "<br/>" + "COMPANY_SUBDOMAIN: " + company.getCompanySubdomain();
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

	private String getEmailAddressesForTeams(Map<String, Object> inputMessage, String companyId) {
		try {
			List<String> teamIds = (List<String>) inputMessage.get("TEAMS");
			Optional<List<Map<String, Object>>> optionalUsers = entryRepository.findAllEntriesByFieldName(teamIds,
					"TEAMS", "Users_" + companyId);
			List<Map<String, Object>> users = optionalUsers.get();
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

	private String getFormattedPhoneNumber(BasePhone phoneNumber) {
		if (phoneNumber.getDialCode() != null && phoneNumber.getPhoneNumber() != null) {
			return phoneNumber.getDialCode() + phoneNumber.getPhoneNumber();
		} else if (phoneNumber.getDialCode() == null && phoneNumber.getPhoneNumber() != null) {
			return phoneNumber.getPhoneNumber();
		}
		return "";
	}

	private String getFormattedDateTimeValue(String value) {
		try {
			Date parsedDate = parseFormat.parse(value);
			return new SimpleDateFormat("MMMM dd, yyyy HH:mm a").format(parsedDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
	}

	private String getFormattedDateValue(String value) {
		try {
			Date parsedDate = parseFormat.parse(value);
			return new SimpleDateFormat("MMMM dd, yyyy").format(parsedDate);
		} catch (ParseException e) {
			try {
				Date parsedDate = new Date(Long.valueOf(value));
				return new SimpleDateFormat("MMMM dd, yyyy").format(parsedDate);
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			}
		}
		return "";
	}

	private String getFormattedTimeValue(String value) {
		try {
			Date parsedDate = parseFormat.parse(value);
			return new SimpleDateFormat("HH:mm a").format(parsedDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
	}

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

	private String addAttachmentsToDiscussionMessageBody(List<MessageAttachment> attachments, String companySubdomain,
			String discussionMessageBody, String messageId, Map<String, Object> inputMessage, Module module) {
		boolean firstAttachment = true;
		for (MessageAttachment attachment : attachments) {
			if (firstAttachment) {
				firstAttachment = false;
				discussionMessageBody += "<br/>";
				discussionMessageBody += "Attachments: <br/>";
			}
			discussionMessageBody += "<a target=\"_blank\" href=\"https://" + companySubdomain
					+ ".ngdesk.com/ngdesk-rest/ngdesk/attachments?attachment_uuid=" + attachment.getAttachmentUuid()
					+ "&message_id=" + messageId + "&entry_id=" + inputMessage.get("_id").toString() + "&module_id="
					+ module.getModuleId() + "\">" + attachment.getFileName() + "</a><br/>";
		}
		return discussionMessageBody;
	}

	private String getFormattedHtmlMessage(DiscussionMessage message, Sender sender, String companyName) {
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

		messageHtml = messageHtml.replaceAll("FIRST_NAME", sender.getFirstName());
		messageHtml = messageHtml.replaceAll("LAST_NAME", senderLastName);
		messageHtml = messageHtml.replaceAll("COMPANY_NAME", Matcher.quoteReplacement(companyName));
		messageHtml = messageHtml.replaceAll("DATE_AND_TIME", formattedDateCreated);
		messageHtml = messageHtml.replaceAll("MESSAGE_REPLACE", Matcher.quoteReplacement(messageBody));

		return messageHtml;
	}

	public boolean listFormulaAdded(Map<String, Object> entry, ModuleField field, String formulaName) {
		if (entry.get(field.getName()) == null) {
			return false;
		}
		List<ListFormulaFieldValue> listFormulaFieldValues = new ArrayList<ListFormulaFieldValue>();
		try {
			listFormulaFieldValues = mapper.readValue(mapper.writeValueAsString(entry.get(field.getName())),
					mapper.getTypeFactory().constructCollectionType(List.class, ListFormulaFieldValue.class));
		} catch (Exception e) {
			e.printStackTrace();

		}
		if (listFormulaFieldValues == null) {
			return false;
		} else if (listFormulaFieldValues.size() == 0) {
			return false;
		}
		Optional<ListFormulaFieldValue> optionalFieldValue = listFormulaFieldValues.stream()
				.filter(listFormulaFieldValue -> listFormulaFieldValue.getFormulaName().equalsIgnoreCase(formulaName))
				.findFirst();
		return optionalFieldValue.isPresent();
	}
}
