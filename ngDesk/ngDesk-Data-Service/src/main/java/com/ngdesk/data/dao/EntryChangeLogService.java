package com.ngdesk.data.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.modules.dao.DataType;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;

@Component
public class EntryChangeLogService {

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	Global global;

	@Autowired
	ModuleEntryRepository entryRepository;

	public Map<String, Object> addDiscussionMetadataAfterFieldUpdate(Map<String, Object> existingEntry, Module module,
			Map<String, Object> entry, String companyId) {
		ModuleField discussionField = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Discussion")).findAny()
				.orElse(null);
		if (discussionField == null) {
			return entry;
		}
		Map<String, Object> fieldDisplayNames = getMetaDataFields(module);
		checkModifiedEntry(fieldDisplayNames, module, existingEntry, entry);
		return entry;
	}

	private Map<String, Object> getMetaDataFields(Module module) {
		Map<String, Object> fieldDisplayNames = new HashMap<String, Object>();
		for (ModuleField fieldName : module.getFields()) {
			DataType dataType = fieldName.getDataType();
			if (dataType.getDisplay().equalsIgnoreCase("Picklist")
					|| dataType.getDisplay().equalsIgnoreCase("Auto Number")
					|| dataType.getDisplay().equalsIgnoreCase("Text Area")
					|| dataType.getDisplay().equalsIgnoreCase("Text") || dataType.getDisplay().equalsIgnoreCase("Email")
					|| dataType.getDisplay().equalsIgnoreCase("Checkbox")
					|| dataType.getDisplay().equalsIgnoreCase("Date/Time")
					|| dataType.getDisplay().equalsIgnoreCase("Date") || dataType.getDisplay().equalsIgnoreCase("Time")
					|| dataType.getDisplay().equalsIgnoreCase("Street 1")
					|| dataType.getDisplay().equalsIgnoreCase("Street 2")
					|| dataType.getDisplay().equalsIgnoreCase("City")
					|| dataType.getDisplay().equalsIgnoreCase("Country")
					|| dataType.getDisplay().equalsIgnoreCase("Zipcode")) {
				if (!(fieldName.getName().equals("DATE_CREATED") || fieldName.getName().equals("DATE_UPDATED"))) {
					fieldDisplayNames.put(fieldName.getName(), fieldName.getDisplayLabel());
				}

			}

		}
		return fieldDisplayNames;
	}

	private Map<String, Object> checkModifiedEntry(Map<String, Object> fieldDisplayNames, Module module,
			Map<String, Object> existingEntry, Map<String, Object> entry) {

		String userUUID = authManager.getUserDetails().getUserUuid();
		Optional<Map<String, Object>> optionalUser = moduleEntryRepository.findEntryByFieldName("USER_UUID", userUUID,
				"Users_" + authManager.getUserDetails().getCompanyId());
		String userEmail = optionalUser.get().get("EMAIL_ADDRESS").toString();
		String metadataHtml = global.getFile("metadata_field_value_updated.html");
		String metadataList = "";
		for (String field : fieldDisplayNames.keySet()) {

			Map<String, Object> value = getUpdatedValue(field, existingEntry, entry);
			String oldFieldValue = value.get("oldFieldValue").toString();

			String latestFieldValue = value.get("latestFieldValue").toString();

			if (!oldFieldValue.equalsIgnoreCase(latestFieldValue)) {
				if (!oldFieldValue.isEmpty() && !latestFieldValue.isEmpty()) {

					Pattern pattern = Pattern.compile(
							"<div class='oldvalue'>(.*)?<\\/div>(.*?)<div class='emptyOldValue'", Pattern.DOTALL);

					Matcher matcher = pattern.matcher(metadataHtml);
					if (matcher.find()) {
						String metadataDiv = matcher.group(1);
						metadataDiv = metadataDiv.replace("FIELD_NAME_REPLACE",
								fieldDisplayNames.get(field).toString());
						metadataDiv = metadataDiv.replace("OLD_FIELD_VALUE_REPLACE", oldFieldValue);
						metadataDiv = metadataDiv.replace("NEW_FIELD_VALUE_REPLACE", latestFieldValue);
						metadataDiv = metadataDiv.replace("EMAIL_IDS_REPLACE", userEmail);
						metadataList = metadataList + metadataDiv;

					}
				} else if (latestFieldValue.isEmpty() && !oldFieldValue.isEmpty()) {
					Pattern pattern = Pattern.compile("<div class='emptyIncomingValue'>(.*)?<\\/div>", Pattern.DOTALL);

					Matcher matcher = pattern.matcher(metadataHtml);
					if (matcher.find()) {
						String metadataDiv = matcher.group(1);
						metadataDiv = metadataDiv.replace("FIELD_NAME_REPLACE",
								fieldDisplayNames.get(field).toString());
						metadataDiv = metadataDiv.replace("OLD_FIELD_VALUE_REPLACE", oldFieldValue);
						metadataDiv = metadataDiv.replace("EMAIL_IDS_REPLACE", userEmail);
						metadataList = metadataList + metadataDiv;

					}
				} else if (!latestFieldValue.isEmpty() && oldFieldValue.isEmpty()) {
					Pattern pattern = Pattern.compile("<div class='emptyOldValue'>(.*)?<\\/div>(.*?)<div class=",
							Pattern.DOTALL);

					Matcher matcher = pattern.matcher(metadataHtml);
					if (matcher.find()) {
						String metadataDiv = matcher.group(1);
						metadataDiv = metadataDiv.replace("FIELD_NAME_REPLACE",
								fieldDisplayNames.get(field).toString());
						metadataDiv = metadataDiv.replace("NEW_FIELD_VALUE_REPLACE", latestFieldValue);
						metadataDiv = metadataDiv.replace("EMAIL_IDS_REPLACE", userEmail);
						metadataList = metadataList + metadataDiv;

					}
				}

			}
		}

		if (!metadataList.isEmpty()) {
			setDisscussionMessageInEntry(metadataList, userEmail, module, entry, metadataHtml, existingEntry);
		}

		return entry;

	}

	private Map<String, Object> getUpdatedValue(String field, Map<String, Object> existingEntry,
			Map<String, Object> entry) {
		String oldFieldValue = "";
		String latestFieldValue = "";
		Map<String, Object> value = new HashMap<String, Object>();
		if (existingEntry.containsKey(field) || entry.containsKey(field)) {

			if (existingEntry.get(field) != null) {
				oldFieldValue = existingEntry.get(field).toString();

			}

			if (entry.get(field) != null && !entry.toString().isEmpty()) {

				latestFieldValue = entry.get(field).toString();

			}
		}
		value.put("oldFieldValue", oldFieldValue);
		value.put("latestFieldValue", latestFieldValue);
		return value;
	}

	private Map<String, Object> setDisscussionMessageInEntry(String metadataList, String userEmail, Module module,
			Map<String, Object> entry, String metadataHtml, Map<String, Object> existingEntry) {

		ModuleField discussionField = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Discussion")).findAny()
				.orElse(null);

		if (discussionField == null) {
			return entry;
		}

		if (!entry.containsKey(discussionField.getName())) {

			entry.put(discussionField.getName(), Arrays.asList());

		}

		Pattern p = Pattern.compile("<div class='header'>(.*)?<\\/div>(.*?)<div class='oldvalue", Pattern.DOTALL);

		Matcher match = p.matcher(metadataHtml);
		if (match.find()) {
			String metadataHeader = match.group(1);
			metadataHeader = metadataHeader.replace("MODULE_NAME_REPLACE", module.getSingularName());
			metadataHeader = metadataHeader.replace("EMAIL_IDS_REPLACE", userEmail);
			metadataList = metadataHeader + metadataList;
		}
		List<DiscussionMessage> existingMessages;
		if (entry.containsKey(discussionField.getName())) {

			ObjectMapper mapper = new ObjectMapper();
			try {

				existingMessages = (mapper.readValue(mapper.writeValueAsString(entry.get(discussionField.getName())),
						mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class)));

				existingMessages.add(addMetadataAfterFieldUpdate(metadataList));

				entry.put(discussionField.getName(), existingMessages);

			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}

		return entry;

	}

	private Sender getSender() {
		Optional<Map<String, Object>> optionalSystemUser = entryRepository.findEntryByFieldName("EMAIL_ADDRESS",
				"system@ngdesk.com", "Users_" + authManager.getUserDetails().getCompanyId());

		Map<String, Object> systemUser = optionalSystemUser.get();

		String contactDetails = systemUser.get("CONTACT").toString();

		Optional<Map<String, Object>> optionalSystemUserContact = entryRepository.findById(contactDetails,
				"Contacts_" + authManager.getUserDetails().getCompanyId());
		Map<String, Object> systemUserContact = optionalSystemUserContact.get();

		Sender sender = new Sender(systemUserContact.get("FIRST_NAME").toString(),
				systemUserContact.get("LAST_NAME").toString(), systemUser.get("USER_UUID").toString(),
				systemUser.get("ROLE").toString());

		return sender;
	}

	private DiscussionMessage addMetadataAfterFieldUpdate(String metaList) {
		DiscussionMessage message = new DiscussionMessage();
		message.setDateCreated(new Date());
		message.setMessage(metaList);
		message.setMessageId(UUID.randomUUID().toString());
		message.setAttachments(new ArrayList<MessageAttachment>());
		message.setMessageType("META_DATA");
		message.setSender(getSender());
		return message;
	}
}
