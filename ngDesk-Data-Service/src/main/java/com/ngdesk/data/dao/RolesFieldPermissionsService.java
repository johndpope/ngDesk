package com.ngdesk.data.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.data.roles.dao.FieldPermission;
import com.ngdesk.data.roles.dao.Permission;
import com.ngdesk.data.roles.dao.Role;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;
import com.ngdesk.repositories.roles.RolesRepository;

@Component
public class RolesFieldPermissionsService {

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ModuleService moduleService;

	@Autowired
	DataService dataService;

	public void isAuthorized(String roleId, String moduleId, Map<String, Object> entry, String requestType) {

		Optional<Role> optionalRole = rolesRepository.findById(roleId,
				"roles_" + authManager.getUserDetails().getCompanyId());
		if (optionalRole.isEmpty()) {
			throw new BadRequestException("INVALID_ROLE", null);
		}

		Role role = optionalRole.get();
		if (role.getName().equals("SystemAdmin")) {
			return;
		}

		Optional<Module> optionalModule = modulesRepository.findById(moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE", null);
		}
		Module module = optionalModule.get();

		Permission modulePermission = role.getPermissions().stream()
				.filter(permission -> permission.getModule().equals(moduleId)).findFirst().orElse(null);

		if (modulePermission == null) {
			throw new BadRequestException("INVALID_MODULE_PERMISSIONS", null);
		}

		Map<String, String> fieldsMap = new HashMap<String, String>();
		module.getFields().forEach(field -> {
			fieldsMap.put(field.getFieldId(), field.getName());
		});

		String currentUserId = authManager.getUserDetails().getUserId();

		List<FieldPermission> allFieldPermissions = modulePermission.getFieldPermissions();

		for (FieldPermission allFieldPermission : allFieldPermissions) {

			List<ModuleField> allFields = module.getFields();
			Optional<ModuleField> optionalFields = allFields.stream()
					.filter(field -> field.getFieldId().equals(allFieldPermission.getFieldId())).findAny();
			if (optionalFields.isEmpty()) {
				return;
			}
			ModuleField field = optionalFields.get();
			String fieldPermission = allFieldPermission.getPermission().toUpperCase();
			String fieldName = fieldsMap.get(allFieldPermission.getFieldId());
			String fieldId = allFieldPermission.getFieldId();
			switch (fieldPermission) {
			case "READ":
				checkReadFieldPermission(requestType, entry, fieldName, fieldId, field, module);
				break;
			case "WRITE ONLY CREATOR":
				checkWriteOnlyCreatorFieldPermission(requestType, entry, fieldName, fieldId, currentUserId, field,
						module);
				break;
			case "NOT EDITABLE":
				checkNotEditibleFieldPermission(requestType, entry, fieldName, fieldId, field, module);
				break;
			case "READ/WRITE":
				break;
			case "NOT SET":
				break;
			case "WRITE BY TEAM":
				checkWriteByTeamFieldPermission(requestType, entry, fieldName, fieldId, currentUserId, field, module);
				break;
			default:
				break;
			}
		}
	}

	private void checkReadFieldPermission(String requestType, Map<String, Object> entry, String fieldName,
			String fieldId, ModuleField field, Module module) {

		String displayLabel = field.getDisplayLabel();
		boolean isRestricted = false;
		if (requestType.equals("POST")) {
			isRestricted = isValuePresent(entry, fieldName, fieldId, field, module);
		} else if (requestType.equals("PUT")) {
			isRestricted = isValueChanged(entry, fieldName, fieldId, field, module);
		}
		if (isRestricted) {
			String[] vars = { displayLabel };
			throw new BadRequestException("RESTRICTED_FIELD", vars);
		}

	}

	private void checkWriteOnlyCreatorFieldPermission(String requestType, Map<String, Object> entry, String fieldName,
			String fieldId, String currentUserId, ModuleField field, Module module) {

		String collectionName = moduleService.getCollectionName(module.getName(),
				authManager.getUserDetails().getCompanyId());
		String displayLabel = field.getDisplayLabel();
		boolean isRestricted = false;
		if (requestType.equals("PUT")) {
			String dataId = entry.get("DATA_ID").toString();
			Optional<Map<String, Object>> optionalPreivousCopy = moduleEntryRepository.findById(dataId, collectionName);
			if (optionalPreivousCopy.isEmpty()) {
				return;
			}
			Map<String, Object> previousCopy = optionalPreivousCopy.get();
			String createdByUserId = previousCopy.get("CREATED_BY").toString();

			if (createdByUserId != null) {
				if (!createdByUserId.equals(currentUserId)) {
					isRestricted = isValueChanged(entry, fieldName, fieldId, field, module);
				} else {
					isRestricted = false;
				}
			}
		}
		if (isRestricted) {
			String[] vars = { displayLabel };
			throw new BadRequestException("RESTRICTED_FIELD_WRITE_ONLY", vars);
		}

	}

	private void checkNotEditibleFieldPermission(String requestType, Map<String, Object> entry, String fieldName,
			String fieldId, ModuleField field, Module module) {

		String displayLabel = field.getDisplayLabel();
		boolean isRestricted = false;
		if (requestType.equals("PUT")) {
			isRestricted = isValueChanged(entry, fieldName, fieldId, field, module);
		}
		if (isRestricted) {
			String[] vars = { displayLabel };
			throw new BadRequestException("RESTRICTED_FIELD_NOT_EDITIBLE", vars);
		}

	}

	private void checkWriteByTeamFieldPermission(String requestType, Map<String, Object> entry, String fieldName,
			String fieldId, String currentUserId, ModuleField field, Module module) {

		String collectionName = moduleService.getCollectionName(module.getName(),
				authManager.getUserDetails().getCompanyId());
		String displayLabel = field.getDisplayLabel();
		boolean isRestricted = false;
		boolean isDifferentTeam = false;
		if (requestType.equals("PUT")) {
			String dataId = entry.get("DATA_ID").toString();
			Optional<Map<String, Object>> optionalPreivousCopy = moduleEntryRepository.findById(dataId, collectionName);
			if (optionalPreivousCopy.isEmpty()) {
				return;
			}
			Map<String, Object> previousCopy = optionalPreivousCopy.get();
			String createdBy = previousCopy.get("CREATED_BY").toString();
			isDifferentTeam = isUserDifferentTeam(createdBy, currentUserId);
			if (isDifferentTeam) {
				isRestricted = isValueChanged(entry, fieldName, fieldId, field, module);
			}
		}
		if (isRestricted) {
			String[] vars = { displayLabel };
			throw new BadRequestException("RESTRICTED_FIELD_WRITE_BY_TEAM", vars);
		}

	}

	private boolean isValuePresent(Map<String, Object> entry, String fieldName, String fieldId, ModuleField field,
			Module module) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			String displayType = null;
			if (!(field.getDataType().getDisplay().equals("Relationship")
					&& field.getRelationshipType().equals("One to Many"))
					&& !field.getDataType().getDisplay().equalsIgnoreCase("Discussion")
					&& !field.getDataType().getDisplay().equalsIgnoreCase("Aggregate")
					&& !field.getDataType().getDisplay().equalsIgnoreCase("Auto Number")) {
				displayType = field.getDataType().getDisplay();
			}
			if (entry.containsKey(fieldName) && displayType != null) {

				if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()) {
					if (displayType.equalsIgnoreCase("Phone")) {
						BasePhone phone = mapper.readValue(mapper.writeValueAsString(entry.get(fieldName)),
								BasePhone.class);
						BasePhone defaultPhone = mapper.readValue(field.getDefaultValue().toString(), BasePhone.class);
						if (!(phone.getPhoneNumber() == null || phone.getPhoneNumber().isEmpty())) {
							if (!phone.getPhoneNumber().equals(defaultPhone.getPhoneNumber())) {
								return true;
							}
						}
					} else {
						if (entry.get(fieldName) != null) {
							String presentValue = entry.get(fieldName).toString();
							String defaultValue = field.getDefaultValue().toString();
							if (displayType.equals("Relationship")
									&& ((field.getRelationshipType().equalsIgnoreCase("One To One")
											|| field.getRelationshipType().equalsIgnoreCase("Many To One")))) {
								Map<String, Object> fieldValue = (Map<String, Object>) entry.get(fieldName);
								presentValue = fieldValue.get("DATA_ID").toString();
								if (!defaultValue.equalsIgnoreCase(presentValue)) {
									return true;
								}
							} else if (displayType.equals("Relationship")
									&& field.getRelationshipType().equals("Many to Many")) {
								List<Relationship> values = new ArrayList<Relationship>();
								values = mapper.readValue(mapper.writeValueAsString(entry.get(fieldName)), mapper
										.getTypeFactory().constructCollectionType(List.class, Relationship.class));
								for (Relationship value : values) {
									presentValue = value.getDataId();
									if (!defaultValue.equalsIgnoreCase(presentValue)) {

										return true;
									}
								}
							} else {
								if (!defaultValue.equalsIgnoreCase(presentValue)) {
									return true;
								}
							}
						}
					}
				} else {
					if (displayType.equalsIgnoreCase("List Text")
							|| field.getDataType().getDisplay().equalsIgnoreCase("Picklist (Multi-Select)")
							|| ((field.getDataType().getDisplay().equals("Relationship")
									&& field.getRelationshipType().equals("Many to Many")))) {
						List<String> fieldValues = (List<String>) entry.get(fieldName);
						if (fieldValues.size() > 0) {
							return true;
						}
					} else if (displayType.equalsIgnoreCase("Checkbox")) {
						if (!entry.get(fieldName).equals(false)) {
							return true;
						}
					} else {
						if (!(entry.get(fieldName) == null || entry.get(fieldName).toString().isEmpty())) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
		return false;

	}

	private boolean isValueChanged(Map<String, Object> entry, String fieldName, String fieldId, ModuleField field,
			Module module) {

		String collectionName = moduleService.getCollectionName(module.getName(),
				authManager.getUserDetails().getCompanyId());

		String dataId = entry.get("DATA_ID").toString();
		String displayType = null;

		if (!(field.getDataType().getDisplay().equals("Relationship")
				&& field.getRelationshipType().equals("One to Many"))
				&& !field.getDataType().getDisplay().equalsIgnoreCase("Discussion")
				&& !field.getDataType().getDisplay().equalsIgnoreCase("Aggregate")
				&& !field.getDataType().getDisplay().equalsIgnoreCase("Auto Number")) {
			displayType = field.getDataType().getDisplay();
		}
		Object value = "";
		Optional<Map<String, Object>> optionalPreivousCopy = moduleEntryRepository.findById(dataId, collectionName);
		if (optionalPreivousCopy.isEmpty()) {
			return false;
		}
		Map<String, Object> previousCopy = optionalPreivousCopy.get();
		if (entry.containsKey(fieldName) && displayType != null) {

			if (displayType.equalsIgnoreCase("List Text")) {
				return isListTextChanged(entry, previousCopy, fieldName);
			} else if (displayType.equals("Relationship") && field.getRelationshipType().equals("Many to Many")) {
				return isRelationshipManyToManyChanged(entry, previousCopy, fieldName);
			} else if (displayType.equalsIgnoreCase("Phone")) {
				return isPhoneChanged(entry, previousCopy, fieldName);
			} else if (displayType.equalsIgnoreCase("Chronometer")) {
				return isChronometerValueChanged(entry, previousCopy, fieldName);
			} else {

				if ((entry.get(fieldName) == null || entry.get(fieldName).toString().isEmpty())
						&& (previousCopy.get(fieldName) == null || previousCopy.get(fieldName).toString().isEmpty())) {
					return false;
				} else if (previousCopy.get(fieldName) == null && (entry.get(fieldName) != null)) {
					return true;
				} else if (entry.get(fieldName) == null && previousCopy.get(fieldName) != null) {
					return true;
				}

				if (displayType.equalsIgnoreCase("Relationship")
						&& (field.getRelationshipType().equalsIgnoreCase("One To One")
								|| field.getRelationshipType().equalsIgnoreCase("Many To One"))) {
					Map<String, Object> fieldValue = (Map<String, Object>) entry.get(fieldName);
					value = fieldValue.get("DATA_ID").toString();
				} else if (displayType.equalsIgnoreCase("Number")) {
					if (!entry.get(fieldName).toString().isBlank()) {
						value = Integer.parseInt(entry.get(fieldName).toString());
					}
				} else if (displayType.equalsIgnoreCase("Formula")) {
					value = Double.parseDouble(entry.get(fieldName).toString());
				} else if (displayType.equalsIgnoreCase("Date") || displayType.equalsIgnoreCase("Date/Time")
						|| displayType.equalsIgnoreCase("Time")) {
					String dateString = entry.get(fieldName).toString();
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSX");
					Date dateValue = null;
					try {
						dateValue = formatter.parse(dateString);
					} catch (Exception e) {
						e.printStackTrace();
					}
					value = dateValue;
				} else {
					value = entry.get(fieldName).toString();
				}
				if (previousCopy.get(fieldName) != null) {
					Object previousValue = previousCopy.get(fieldName);
					if (!previousValue.toString().equals(value.toString())) {
						return true;
					}
				}
			}
		}
		return false;

	}

	private boolean isChronometerValueChanged(Map<String, Object> entry, Map<String, Object> previousEntry,
			String fieldName) {

		if (!entry.get(fieldName).toString().isBlank() && previousEntry.get(fieldName) == null) {
			return true;
		}
		if (!entry.get(fieldName).toString().isBlank()) {
			Object previousValue = Integer.parseInt(previousEntry.get(fieldName).toString());
			Object value = dataService.getChronometerValueInMinutes(entry.get(fieldName).toString());
			if (!previousValue.toString().equals(value.toString())) {
				return true;
			}
		}
		return false;

	}

	private boolean isPhoneChanged(Map<String, Object> entry, Map<String, Object> previousCopy, String fieldName) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			BasePhone phone = mapper.readValue(mapper.writeValueAsString(entry.get(fieldName)), BasePhone.class);
			BasePhone previousPhone = mapper.readValue(mapper.writeValueAsString(previousCopy.get(fieldName)),
					BasePhone.class);

			if ((phone.getPhoneNumber() == null || phone.getPhoneNumber().isEmpty())
					&& (previousPhone.getPhoneNumber() == null || previousPhone.getPhoneNumber().isEmpty())) {
				return false;
			} else if (previousPhone.getPhoneNumber() == null && phone.getPhoneNumber() != null) {
				return true;
			} else if (phone.getPhoneNumber() == null && previousPhone.getPhoneNumber() != null) {
				return true;
			}

			String value = phone.getDialCode() + phone.getPhoneNumber();
			String previousValue = previousPhone.getDialCode() + previousPhone.getPhoneNumber();
			if (!previousValue.equals(value)) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
		return false;

	}

	private boolean isListTextChanged(Map<String, Object> entry, Map<String, Object> previousCopy, String fieldName) {

		List<String> fieldValues = (List<String>) entry.get(fieldName);
		if (fieldValues.size() == 0 && previousCopy.get(fieldName) == null) {
			return false;
		} else if (previousCopy.get(fieldName) == null && fieldValues.size() > 0) {
			return true;
		}
		Object previousValue = previousCopy.get(fieldName);
		Object value = fieldValues;
		if (!previousValue.equals(value)) {
			return true;
		}
		return false;

	}

	private boolean isRelationshipManyToManyChanged(Map<String, Object> entry, Map<String, Object> previousCopy,
			String fieldName) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			List<Relationship> values = mapper.readValue(mapper.writeValueAsString(entry.get(fieldName)),
					mapper.getTypeFactory().constructCollectionType(List.class, Relationship.class));
			List<String> previousValues = (List<String>) previousCopy.get(fieldName);
			if (values.size() > 0 && previousValues.size() == 0) {
				return true;
			} else if (values.size() == 0 && previousValues.size() > 0) {
				return true;
			} else if (values.size() != previousValues.size()) {
				return true;
			}

			for (Relationship value : values) {
				String presentValues = value.getDataId();
				if (!previousValues.contains(presentValues))
					return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
		return false;

	}

	private boolean isUserDifferentTeam(String createdBy, String currentUserId) {

		Optional<Map<String, Object>> optionalCreatedByUser = moduleEntryRepository.findById(createdBy,
				"Users_" + authManager.getUserDetails().getCompanyId());
		if (!optionalCreatedByUser.isEmpty()) {
			Map<String, Object> createdByUser = optionalCreatedByUser.get();
			List<String> teamIds = (List<String>) createdByUser.get("TEAMS");
			Optional<List<Map<String, Object>>> optionalUser = moduleEntryRepository.findAllTeams(teamIds,
					currentUserId, authManager.getUserDetails().getCompanyId());
			if (!optionalUser.isEmpty()) {
				List<Map<String, Object>> user = optionalUser.get();
				if (user.isEmpty()) {
					return true;
				}
			}
		}
		return false;

	}

}
