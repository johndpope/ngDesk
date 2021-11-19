package com.ngdesk.data.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.data.modules.dao.DataType;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;

@Component
@RabbitListener(queues = "add-events", concurrency = "5")
public class MetaDataService {

	private final Logger log = LoggerFactory.getLogger(MetaDataService.class);

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	Global global;

	@Autowired
	ModuleService moduleService;

	@Autowired
	SlaService slaService;

	@Autowired
	DataService service;

	@RabbitHandler
	public void addMetaDataField(MetadataPayload metaDataPayload) {

		String companyId = metaDataPayload.getCompanyId();
		String currentModuleId = metaDataPayload.getModuleId();
		String userId = metaDataPayload.getUserId();
		Map<String, Object> entry = metaDataPayload.getEntry();
		Map<String, Object> existingEntry = metaDataPayload.getExistingEntry();
		String entryId = metaDataPayload.getEntryId();

		try {

			Optional<Module> optionalModule = modulesRepository.findById(currentModuleId, "modules_" + companyId);

			if (optionalModule.isPresent()) {

				Module module = optionalModule.get();
				String collectionName = moduleService.getCollectionName(module.getName(), companyId);

				if (existingEntry == null) {

					if (entry == null) {
						addMetadataEventsField(companyId, module, entryId, userId, null, collectionName);
					} else {
						addMetadataEventsField(companyId, module, entryId, userId, entry, collectionName);
					}

				} else {

					Map<String, Object> fieldDisplayNames = getMetaDataFields(module);
					checkModifiedEntry(fieldDisplayNames, module, entryId, userId, companyId, existingEntry, entry,
							collectionName);

				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

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

	private void checkModifiedEntry(Map<String, Object> fieldDisplayNames, Module module, String entryId, String userId,
			String companyId, Map<String, Object> existingEntry, Map<String, Object> entry, String collectionName)
			throws Exception {

		Map<String, Object> userDetails = getUserDetails(userId, companyId);
		String userEmail = userDetails.get("EMAIL_ADDRESS").toString();
		String userName = userDetails.get("FULL_NAME").toString();
		Map<String, Object> formattedEntry = formatLongDateTypes(entry, module, companyId);
		Map<String, Object> formattedExistingEntry = formatLongDateTypes(existingEntry, module, companyId);

		String updateMessage = "<html><body><div class=\"mat-caption\" style=\"color: #68737d;\">"
				+ "<div style=\"display: inline-block; vertical-align: top;margin-left:5px\">This</div>"
				+ "<div style=\"display: inline-block; vertical-align: top;margin-left:5px\">"
				+ module.getSingularName()
				+ "</div><div style=\"display: inline-block; vertical-align: top;margin-left:5px\">has been modified by</div>"
				+ "<div style=\"display: inline-block; vertical-align: top;margin-left:5px\">" + userName
				+ "</div><div style=\"display: inline-block; vertical-align: top;margin-left:5px\">" + userEmail
				+ "</div></div></body></html>";

		for (String field : fieldDisplayNames.keySet()) {

			Map<String, Object> value = getUpdatedValue(field, formattedExistingEntry, formattedEntry);
			String oldFieldValue = value.get("oldFieldValue").toString();

			String latestFieldValue = value.get("latestFieldValue").toString();

			if (!oldFieldValue.equalsIgnoreCase(latestFieldValue)) {
				if (!oldFieldValue.isEmpty() && !latestFieldValue.isEmpty()) {

					String valueChangeMessage = "<html><body><div class=\"mat-caption\" style=\"color: #68737d;\">"
							+ "<div style=\"display: inline-block; vertical-align: top;margin-left:5px\">"
							+ fieldDisplayNames.get(field).toString()
							+ "</div><div style=\"display: inline-block; vertical-align: top;margin-left:5px\">has been changed from</div>"
							+ "<div style=\"display: inline-block; vertical-align: top;margin-left:5px;\">"
							+ oldFieldValue
							+ "</div><div style=\"display: inline-block; vertical-align: top;margin-left:5px\">to</div>"
							+ "<div style=\"display: inline-block; vertical-align: top;margin-left:5px;\">"
							+ latestFieldValue + "</div></div></body></html>";
					updateMessage = updateMessage + valueChangeMessage;
				}
			}
		}

		DiscussionMessage discussion = addDiscussionMessage(updateMessage, companyId);
		moduleEntryRepository.updateMetadataEvents(entryId, discussion, collectionName);

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

	private DiscussionMessage addDiscussionMessage(String message, String companyId) {

		Optional<Map<String, Object>> optionalSystemUser = moduleEntryRepository.findEntryByFieldName("EMAIL_ADDRESS",
				"system@ngdesk.com", "Users_" + companyId);

		if (!optionalSystemUser.isEmpty()) {

			Map<String, Object> systemUser = optionalSystemUser.get();
			String contactDetails = systemUser.get("CONTACT").toString();
			Optional<Map<String, Object>> optionalSystemUserContact = moduleEntryRepository.findById(contactDetails,
					"Contacts_" + companyId);

			if (!optionalSystemUserContact.isEmpty()) {

				Map<String, Object> systemUserContact = optionalSystemUserContact.get();
				Sender sender = new Sender(systemUserContact.get("FIRST_NAME").toString(),
						systemUserContact.get("LAST_NAME").toString(), systemUser.get("USER_UUID").toString(),
						systemUser.get("ROLE").toString());

				return new DiscussionMessage(message, new Date(), UUID.randomUUID().toString(), "META_DATA",
						new ArrayList<MessageAttachment>(), sender);

			}

		}
		return null;

	}

	private void addMetadataEventsField(String companyId, Module module, String entryId, String userId,
			Map<String, Object> entry, String collectionName) throws Exception {
		ObjectMapper mapper = new ObjectMapper();

		Map<String, Object> userDetails = getUserDetails(userId, companyId);
		String userEmail = userDetails.get("EMAIL_ADDRESS").toString();
		String userName = userDetails.get("FULL_NAME").toString();

		Map<String, Object> metaData = new HashMap<String, Object>();
		// entry = service.formatDateAndTimeField(entry,module);
		Map<String, Object> formattedEntry = formatLongDateTypes(entry, module, companyId);
		if (formattedEntry != null) {

			String createMetadataMessage = "<html><body><div class=\"mat-caption\" style=\"color: #68737d;\">"
					+ "<div style=\"display: inline-block; vertical-align: top;margin-left:5px\">This</div>"
					+ "<div style=\"display: inline-block; vertical-align: top;margin-left:5px\">"
					+ module.getSingularName()
					+ "</div><div style=\"display: inline-block; vertical-align: top;margin-left:5px\">has been created by</div>"
					+ "<div style=\"display: inline-block; vertical-align: top;margin-left:5px\">" + userName
					+ "</div><div style=\"display: inline-block; vertical-align: top;margin-left:5px\">" + userEmail
					+ "</div></div></body></html>";
			DiscussionMessage discussion = addDiscussionMessage(createMetadataMessage, companyId);

			Map<String, Object> existingMetadata = mapper.readValue(mapper.writeValueAsString(entry.get("META_DATA")),
					Map.class);
			List<DiscussionMessage> events = new ArrayList<DiscussionMessage>();
			events.add(discussion);
			metaData.put("EVENTS", events);

			moduleEntryRepository.addMetadataEntry(entryId, formattedEntry, collectionName);

			if (existingMetadata != null) {
				existingMetadata.putAll(metaData);
				moduleEntryRepository.addMetadataEntry(entryId, existingMetadata, collectionName);
			} else {
				moduleEntryRepository.addMetadataEntry(entryId, metaData, collectionName);
			}

		} else {

			String deleteMetadataMessage = "<html><body><div class=\"mat-caption\" style=\"color: #68737d;\">"
					+ "<div style=\"display: inline-block; vertical-align: top;margin-left:5px\">This</div>"
					+ "<div style=\"display: inline-block; vertical-align: top;margin-left:5px\">"
					+ module.getSingularName()
					+ "</div><div style=\"display: inline-block; vertical-align: top;margin-left:5px\">has been deleted by</div>"
					+ "<div style=\"display: inline-block; vertical-align: top;margin-left:5px\">" + userName
					+ "</div><div style=\"display: inline-block; vertical-align: top;margin-left:5px\">" + userEmail
					+ "</div></div></body></html>";
			DiscussionMessage discussion = addDiscussionMessage(deleteMetadataMessage, companyId);
			Optional<Map<String, Object>> optionalPreivousCopy = moduleEntryRepository.findById(entryId,
					collectionName);
			if (!optionalPreivousCopy.isEmpty()) {

				moduleEntryRepository.updateMetadataEvents(entryId, discussion, collectionName);
			}

		}
	}

	private Map<String, Object> formatLongDateTypes(Map<String, Object> entry, Module module, String companyId) {
		String[] fieldNames = { "DATE_CREATED", "DATE_UPDATED", "EFFECTIVE_FROM", "EFFECTIVE_TO" };

		List<String> fieldsToIgnore = new ArrayList<String>();
		fieldsToIgnore.addAll(Arrays.asList(fieldNames));
		List<ModuleField> payloadFields = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equals("Date/Time")
						|| field.getDataType().getDisplay().equals("Date")
						|| field.getDataType().getDisplay().equals("Time"))
				.collect(Collectors.toList());

		List<String> slaFieldNames = slaService.generateSlaFieldNames(module.getModuleId(), companyId);
		if (slaFieldNames.size() > 0 && slaFieldNames != null) {
			fieldsToIgnore.addAll(slaFieldNames);
		}
		payloadFields = payloadFields.stream().filter(field -> !fieldsToIgnore.contains(field.getName()))
				.collect(Collectors.toList());

		payloadFields.forEach(field -> {
			String fieldName = field.getName();
			if (entry != null) {
				if ((entry.get(fieldName) != null)) {
					String dateString = entry.get(fieldName).toString();
					try {

						Long l = Long.parseLong(dateString);

						Date date = new Date(l);
						entry.put(fieldName, date);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		return entry;
	}

	private Map<String, Object> getUserDetails(String userId, String companyId) {

		Map<String, Object> userDetails = new HashMap<String, Object>();

		Optional<Map<String, Object>> optionalUser = moduleEntryRepository.findEntryByFieldName("_id", userId,
				"Users_" + companyId);

		if (!optionalUser.isEmpty()) {

			String userEmail = optionalUser.get().get("EMAIL_ADDRESS").toString();
			Optional<Map<String, Object>> optionalContact = moduleEntryRepository.findEntryByFieldName("USER", userId,
					"Contacts_" + companyId);

			if (!optionalContact.isEmpty()) {

				String userName = optionalContact.get().get("FULL_NAME").toString();
				userDetails.put("EMAIL_ADDRESS", userEmail);
				userDetails.put("FULL_NAME", userName);

			}
		}

		return userDetails;
	}

}
