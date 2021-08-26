package com.ngdesk.workflow.executor.dao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.workflow.dao.Condition;
import com.ngdesk.workflow.module.dao.Module;
import com.ngdesk.workflow.module.dao.ModuleField;

@Component
public class ConditionService {

	@Autowired
	ModuleEntryRepository entryRepository;

	public boolean executeWorkflow(List<Condition> conditions, Map<String, Object> entry, Map<String, Object> oldCopy,
			Module module, String companyId) {

		if (conditions == null || conditions.size() == 0) {
			return true;
		} else if (entry == null) {
			return false;
		}

		Map<String, String> fieldIdToNameMap = new HashMap<String, String>();
		List<String> dateFieldIds = new ArrayList<String>();

		module.getFields().forEach(field -> {
			String displayDataType = field.getDataType().getDisplay();
			if (displayDataType.equalsIgnoreCase("Date/Time") || displayDataType.equalsIgnoreCase("Time")
					|| displayDataType.equalsIgnoreCase("Date")) {
				dateFieldIds.add(field.getFieldId());
			}
			fieldIdToNameMap.put(field.getFieldId(), field.getName());
		});

		ModuleField discussionField = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Discussion")).findFirst()
				.orElse(null);

		List<Boolean> all = new ArrayList<Boolean>();
		List<Boolean> any = new ArrayList<Boolean>();

		for (Condition condition : conditions) {
			String operator = condition.getOperator();
			String requirementType = condition.getRequirementType();

			String fieldName = fieldIdToNameMap.get(condition.getCondition());

			if (!condition.getCondition().contains("InputMessage") && !entry.containsKey(fieldName)) {
				return false;
			}

			switch (operator) {

			case "CHANGED":

				if (requirementType.equalsIgnoreCase("all")) {
					if (discussionField == null) {
						all.add(evaluateChanged(entry, oldCopy, fieldName, discussionField));

					} else if (discussionField != null && !fieldName.equals(discussionField.getName())) {

						all.add(evaluateChanged(entry, oldCopy, fieldName, discussionField));
					} else {
						all.add(evaluateChangedMessage(entry, oldCopy, discussionField, fieldName));
					}
				} else {
					if (discussionField == null) {
						any.add(evaluateChanged(entry, oldCopy, fieldName, discussionField));

					} else if (discussionField != null && !fieldName.equals(discussionField.getName())) {
						any.add(evaluateChanged(entry, oldCopy, fieldName, discussionField));
					} else {
						any.add(evaluateChangedMessage(entry, oldCopy, discussionField, fieldName));
					}
				}
				break;
			case "EQUALS_TO":
			case "IS":

				if (requirementType.equalsIgnoreCase("all")) {

					all.add(evaluateEqualsTo(fieldName, condition, entry, discussionField, companyId));
				} else {
					any.add(evaluateEqualsTo(fieldName, condition, entry, discussionField, companyId));
				}
				break;
			case "NOT_EQUALS_TO":

				if (requirementType.equalsIgnoreCase("all")) {

					all.add(!evaluateEqualsTo(fieldName, condition, entry, discussionField, companyId));
				} else {
					any.add(!evaluateEqualsTo(fieldName, condition, entry, discussionField, companyId));
				}
				break;
			case "CONTAINS":

				if (requirementType.equalsIgnoreCase("all")) {

					all.add(evaluateContains(fieldName, condition.getValue(), entry, discussionField));
				} else {

					any.add(evaluateContains(fieldName, condition.getValue(), entry, discussionField));
				}
				break;
			case "DOES_NOT_CONTAIN":

				if (requirementType.equalsIgnoreCase("all")) {

					all.add(!evaluateContains(fieldName, condition.getValue(), entry, discussionField));
				} else {
					any.add(!evaluateContains(fieldName, condition.getValue(), entry, discussionField));
				}
				break;
			case "REGEX":

				if (requirementType.equalsIgnoreCase("all")) {

					all.add(evaluateRegex(fieldName, condition.getValue(), entry));
				} else {
					any.add(evaluateRegex(fieldName, condition.getValue(), entry));
				}
				break;

			case "LESS_THAN":

				if (requirementType.equalsIgnoreCase("all")) {

					all.add(evaluateLessThan(condition, fieldName, dateFieldIds, entry));
				} else {
					any.add(evaluateLessThan(condition, fieldName, dateFieldIds, entry));
				}
				break;
			case "GREATER_THAN":

				if (requirementType.equalsIgnoreCase("all")) {

					all.add(!evaluateLessThan(condition, fieldName, dateFieldIds, entry));
				} else {
					any.add(!evaluateLessThan(condition, fieldName, dateFieldIds, entry));
				}
				break;
			case "LENGTH_GREATER_THAN":

				if (requirementType.equalsIgnoreCase("all")) {

					all.add(!evaluateLengthLessThan(entry, fieldName, condition.getValue()));
				} else {
					any.add(!evaluateLengthLessThan(entry, fieldName, condition.getValue()));
				}
				break;
			case "LENGTH_LESS_THAN":

				if (requirementType.equalsIgnoreCase("all")) {

					all.add(evaluateLengthLessThan(entry, fieldName, condition.getValue()));
				} else {
					any.add(evaluateLengthLessThan(entry, fieldName, condition.getValue()));
				}
				break;
			case "IS_UNIQUE":

				if (requirementType.equalsIgnoreCase("all")) {

					all.add(isUnique(fieldName, condition.getValue(), module, companyId));
				} else {
					any.add(isUnique(fieldName, condition.getValue(), module, companyId));
				}
				break;

			default:

				if (requirementType.equalsIgnoreCase("all")) {

					all.add(false);
				} else {

					any.add(false);
				}
				break;
			}
		}

		return evaluateValues(all, any);
	}

	private String getCollectionName(Module module, String companyId) {
		String collectionName = module.getName().replace("\\s+", "_");
		return collectionName + "_" + companyId;
	}

	private boolean evaluateChanged(Map<String, Object> newCopy, Map<String, Object> oldCopy, String fieldName,
			ModuleField discussionField) {

		if (newCopy.get(fieldName) != null && oldCopy.get(fieldName) == null) {

			return true;
		} else if (oldCopy.get(fieldName) != null && newCopy.get(fieldName) == null) {

			return true;
		} else if (newCopy.get(fieldName) != null && oldCopy.get(fieldName) != null
				&& !newCopy.get(fieldName).equals(oldCopy.get(fieldName))) {

			return true;
		}

		return false;
	}

	private boolean evaluateEqualsTo(String fieldName, Condition condition, Map<String, Object> newCopy,
			ModuleField discussionField, String companyId) {
		if (discussionField != null && condition.getCondition()
				.equals("{{InputMessage." + discussionField.getName() + ".LATEST.SENDER}}")) {

			if (newCopy.containsKey(discussionField.getName()) && newCopy.get(discussionField.getName()) != null) {
				return evaluateMessageSender(newCopy, discussionField, condition, companyId);
			}
		} else if (discussionField != null && fieldName.equalsIgnoreCase(discussionField.getName())) {

			if (newCopy.containsKey(discussionField.getName()) && newCopy.get(discussionField.getName()) != null) {
				return evaluateDiscussionMessage(newCopy, discussionField, condition.getValue(), true);
			}
		} else if (newCopy.get(fieldName) != null && condition.getValue() != null
				&& condition.getValue().equals(newCopy.get(fieldName).toString())) {
			return true;
		}
		return false;
	}

	private boolean evaluateContains(String fieldName, String value, Map<String, Object> entry,
			ModuleField discussionField) {
		if (entry.get(fieldName) != null && discussionField != null
				&& fieldName.equalsIgnoreCase(discussionField.getName())) {
			return evaluateDiscussionMessage(entry, discussionField, value, false);
		} else if (entry.get(fieldName) != null && entry.get(fieldName).toString().contains(value)) {
			return true;
		}

		return false;
	}

	private boolean evaluateRegex(String fieldName, String value, Map<String, Object> entry) {
		Pattern pattern = Pattern.compile(value);
		Matcher matcher = pattern.matcher(entry.get(fieldName).toString());
		return matcher.find();
	}

	private boolean evaluateLessThan(Condition condition, String fieldName, List<String> dateFieldIds,
			Map<String, Object> entry) {
		if (dateFieldIds.contains(condition.getCondition())) {
			Date entryDate = new Date((long) entry.get(fieldName));
			Instant instant = null;
			instant = Instant.parse(condition.getValue());
			Date dateValue = (Date) Date.from(instant);

			if (entryDate.before(dateValue)) {
				return true;
			}
		} else {
			if (Long.parseLong(entry.get(fieldName).toString()) < Long.parseLong(condition.getValue())) {
				return true;
			}
		}

		return false;
	}

	private boolean evaluateLengthLessThan(Map<String, Object> entry, String fieldName, String value) {
		if ((entry.get(fieldName).toString()).length() < Integer.parseInt(value)) {
			return true;
		}
		return false;
	}

	private boolean isUnique(String fieldName, String value, Module module, String companyId) {
		List<Map<String, Object>> entries = entryRepository.findEntriesByVariable(fieldName, value,
				getCollectionName(module, companyId));
		if (entries.size() >= 1) {
			return true;
		}
		return false;
	}

	private boolean evaluateMessageSender(Map<String, Object> entry, ModuleField discussionField, Condition condition,
			String companyId) {
		ObjectMapper mapper = new ObjectMapper();
		List<DiscussionMessage> messages;
		try {

			messages = mapper.readValue(mapper.writeValueAsString(entry.get(discussionField.getName())),
					mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class));

			for (int i = messages.size() - 1; i >= 0; i--) {

				DiscussionMessage message = messages.get(i);
				if (message.getMessageType().equalsIgnoreCase("META_DATA")) {
					continue;
				}
				String userUuid = message.getSender().getUserUuid();
				String userId = getUserIdFromUuid(userUuid, companyId);
				if (condition.getValue().equalsIgnoreCase("{{REQUESTOR}}")) {
					if (entry.get("REQUESTOR") != null && entry.get("REQUESTOR").equals(userId)) {
						return true;
					}
				} else if (userId.equals(condition.getValue())) {
					return true;
				}
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	private String getUserIdFromUuid(String userUuid, String companyId) {
		Map<String, Object> user = entryRepository.findUserIdByUuid("USER_UUID", userUuid, "Users_" + companyId);
		return user.get("_id").toString();
	}

	private boolean evaluateDiscussionMessage(Map<String, Object> entry, ModuleField discussionField, String value,
			boolean isEqualToCondition) {
		ObjectMapper mapper = new ObjectMapper();
		List<DiscussionMessage> messages;
		try {

			messages = mapper.readValue(mapper.writeValueAsString(entry.get(discussionField.getName())),
					mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class));
			for (DiscussionMessage message : messages) {
				if (message.getMessageType().equals("META_DATA")
						|| message.getMessageType().equalsIgnoreCase("INTERNAL_COMMENT")) {
					continue;
				}
				String parsedMessage = Jsoup.parse(message.getMessage()).text();
				if (isEqualToCondition) {
					if (parsedMessage.equals(value)) {
						return true;
					}
				} else {
					if (parsedMessage.contains(value)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	private boolean evaluateChangedMessage(Map<String, Object> entry, Map<String, Object> oldCopy,
			ModuleField discussionField, String fieldName) {
		ObjectMapper mapper = new ObjectMapper();
		List<DiscussionMessage> messages;
		List<DiscussionMessage> oldMessages;
		try {
			if (entry.get(fieldName) != null && oldCopy.get(fieldName) == null) {

				return true;
			} else if (oldCopy.get(fieldName) != null && entry.get(fieldName) == null) {

				return true;
			} else {

				messages = mapper.readValue(mapper.writeValueAsString(entry.get(discussionField.getName())),
						mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class));
				oldMessages = mapper.readValue(mapper.writeValueAsString(oldCopy.get(discussionField.getName())),
						mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class));
				messages = messages.stream().filter(m -> m.getMessageType().equalsIgnoreCase("MESSAGE"))
						.collect(Collectors.toList());
				oldMessages = oldMessages.stream().filter(m -> m.getMessageType().equalsIgnoreCase("MESSAGE"))
						.collect(Collectors.toList());
				if (messages.size() != oldMessages.size()) {
					return true;

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean evaluateValues(List<Boolean> all, List<Boolean> any) {
		boolean allValue = true;
		for (boolean booleanValue : all) {
			if (!booleanValue) {
				allValue = false;
				break;
			}
		}
		boolean anyValue = true;
		for (boolean booleanValue : any) {
			if (!booleanValue) {
				anyValue = false;
			} else {
				anyValue = true;
				break;
			}
		}

		return allValue && anyValue;
	}
}
