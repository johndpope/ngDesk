package com.ngdesk.module.task.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.data.manager.DataProxy;
import com.ngdesk.module.field.dao.DiscussionMessage;
import com.ngdesk.module.field.dao.ModuleField;
import com.ngdesk.module.field.dao.Sender;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.task.TaskRepository;

import io.swagger.v3.oas.annotations.media.Schema;

@Component
public class CreateEntry extends Action {

	@Autowired
	TaskRepository repository;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	DataProxy dataProxy;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "MODULE" })
	private String moduleId;

	@Schema(required = true, description = "fields that need to be added to the module")
	@Valid
	private List<Fields> fields;

	public CreateEntry() {
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public List<Fields> getFields() {
		return fields;
	}

	public void setFields(List<Fields> fields) {
		this.fields = fields;
	}

	public CreateEntry(String moduleId, List<Fields> fields) {
		this.moduleId = moduleId;
		this.fields = fields;
	}

	public void executeCreateEntry(Action action, String companyId, String userId) {
		CreateEntry createEntry = (CreateEntry) action;

		Optional<Module> modules = repository.findByModuleId(createEntry.getModuleId(), "modules_" + companyId);

		if (modules == null) {
			return;
		}

		List<ModuleField> allFields = modules.get().getFields();

		if (allFields == null) {
			return;
		}

		Map<String, Object> payload = new HashMap<String, Object>();

		for (ModuleField field : allFields) {
			String fieldPresent = field.getFieldId().toString();

			Fields field_present = createEntry.getFields().stream()
					.filter(field_s -> field_s.getFieldId().equals(fieldPresent)).findFirst().orElse(null);

			if (field_present != null) {
				String value = field_present.getValue();

				if (field.getDataType().getDisplay().equalsIgnoreCase("Discussion")) {

					DiscussionMessage message = buildDiscussionPayload(value.toString(), createEntry.getModuleId(),
							companyId, userId);

					payload.put(field.getName(), Arrays.asList(message));
				} else if (field.getDataType().getDisplay().equalsIgnoreCase("List Text")) {

					List<String> list = listTextValues(value, field);
					payload.put(field.getName(), list);

				} else if (field.getDataType().getDisplay().equalsIgnoreCase("Relationship")) {

					payload = formatRelationship(modules.get(), payload, field_present, value);

				} else {

					payload.put(field.getName(), value);
				}

			}

		}

		if (getUserUuid(userId, companyId) == null) {

			return;
		}
		String userUuid = getUserUuid(userId, companyId);

		dataProxy.postModuleEntry(payload, createEntry.getModuleId(), false, companyId, userUuid);

	}

	public DiscussionMessage buildDiscussionPayload(String message, String moduleId, String companyId, String userId) {

		Optional<Map<String, Object>> optionalUser = entryRepository.findEntryById(userId, "Users_" + companyId);

		if (optionalUser == null) {

			return null;
		}

		Map<String, Object> user = optionalUser.get();

		String contactId = user.get("CONTACT").toString();

		Optional<Map<String, Object>> optionalContact = entryRepository.findById(contactId, "Contacts_" + companyId);
		if (optionalContact.get() == null) {

			return null;
		}

		Map<String, Object> contact = optionalContact.get();

		Sender sender = new Sender(contact.get("FIRST_NAME").toString(), contact.get("LAST_NAME").toString(),
				user.get("USER_UUID").toString(), user.get("ROLE").toString());

		return new DiscussionMessage(null, message, sender, moduleId, new Date());

	}

	public String getUserUuid(String userId, String companyId) {
		Optional<Map<String, Object>> optionalUser = entryRepository.findEntryById(userId, "Users_" + companyId);
		
		Map<String, Object> user = optionalUser.get();

		String userUuid = user.get("USER_UUID").toString();
		return userUuid;
	}

	public List<String> listTextValues(String value, ModuleField field) {

		List<String> list = new ArrayList<String>();
		if (value != null) {
			try {

				try {
					list = new ObjectMapper().readValue(value, List.class);
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (ClassCastException e) {
				String[] items = value.toString().split(",");
				for (String item : items) {
					if (field.getListTextUnique() != null) {
						if (field.getListTextUnique() && !list.contains(item)) {
							list.add(item);
						} else if (!field.getListTextUnique()) {
							list.add(item);
						}
					}

				}
			}
		}

		return list;

	}

	public Map<String, Object> formatRelationship(Module module, Map<String, Object> payload, Fields field_present,
			String value) {

		String[] ignored = { "LAST_UPDATED_BY", "CREATED_BY" };
		List<String> ignoredFields = Arrays.asList(ignored);
		List<ModuleField> relationshipFields = module.getFields().stream()
				.filter(fields -> fields.getDataType().getDisplay().equals("Relationship"))
				.collect(Collectors.toList());

		relationshipFields = relationshipFields.stream()
				.filter(fields -> !fields.getRelationshipType().equals("One To Many")).collect(Collectors.toList());
		relationshipFields = relationshipFields.stream().filter(fields -> !ignoredFields.contains(fields.getName()))
				.collect(Collectors.toList());

		Optional<ModuleField> OptionalrelationshipField = relationshipFields.stream()
				.filter(fields -> fields.getFieldId().equals(field_present.getFieldId())).findAny();
		if (OptionalrelationshipField.isEmpty()) {
			return null;

		}
		ObjectMapper mapper = new ObjectMapper();
		ModuleField relationshipField = OptionalrelationshipField.get();

		try {

			if (relationshipField.getRelationshipType().equalsIgnoreCase("One To One")
					|| relationshipField.getRelationshipType().equalsIgnoreCase("Many To One")) {

				Relationship relationshipValue = mapper.readValue(value, Relationship.class);
				if (relationshipValue.getDataId() != null) {
					payload.put(relationshipField.getName(), relationshipValue.getDataId());
				}

			} else if (relationshipField.getRelationshipType().equalsIgnoreCase("Many To Many")) {

				List<Relationship> values = mapper.readValue(value,
						mapper.getTypeFactory().constructCollectionType(List.class, Relationship.class));
				List<String> relationshipValues = new ArrayList<String>();

				values.forEach(relationShipValue -> {
					relationshipValues.add(relationShipValue.getDataId());
				});
				payload.put(relationshipField.getName(), relationshipValues);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return payload;
	}

}
