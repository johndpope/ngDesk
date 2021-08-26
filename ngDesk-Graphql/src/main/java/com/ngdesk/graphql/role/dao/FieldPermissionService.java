package com.ngdesk.graphql.role.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

@Component
public class FieldPermissionService {

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	public List<EditablePermission> fieldPermissionForCreateLayout(Permission permission) {
		List<EditablePermission> fieldPermissionsList = new ArrayList<EditablePermission>();

		for (FieldPermission fieldPermission : permission.getFieldPermissions()) {
			if (fieldPermission.getPermission().toLowerCase().equals("read")) {
				fieldPermissionsList.add(createEditablePermissionObject(fieldPermission.getFieldId(), true));
			}
		}
		return fieldPermissionsList;
	}

	public List<EditablePermission> fieldPermissionForEditLayout(Permission permission, Module module, String dataId) {
		List<EditablePermission> fieldPermissionsList = new ArrayList<EditablePermission>();

		for (FieldPermission fieldPermission : permission.getFieldPermissions()) {
			String currentUserId = authManager.getUserDetails().getUserId();
			String companyId = authManager.getUserDetails().getCompanyId();
			String moduleName = module.getName().replaceAll("\\s+", "_");
			Optional<Map<String, Object>> optionalEntry = moduleEntryRepository.findById(dataId,
					moduleName + "_" + companyId);
			if (optionalEntry.isEmpty()) {
				return null;
			}
			Map<String, Object> entry = optionalEntry.get();
			String createdUserId = entry.get("CREATED_BY").toString();

			ModuleField moduleField = module.getFields().stream()
					.filter(field -> field.getFieldId().equals(fieldPermission.getFieldId())).findFirst().orElse(null);

			if (moduleField != null) {
				switch (fieldPermission.getPermission().toLowerCase()) {
				case "read":
					fieldPermissionsList.add(createEditablePermissionObject(fieldPermission.getFieldId(), true));
					break;

				case "not editable":
					fieldPermissionsList.add(createEditablePermissionObject(fieldPermission.getFieldId(), true));
					break;

				case "read/write":
					fieldPermissionsList.add(createEditablePermissionObject(fieldPermission.getFieldId(), false));
					break;

				case "not set":
					fieldPermissionsList.add(createEditablePermissionObject(fieldPermission.getFieldId(), false));
					break;

				case "write only creator":

					if (createdUserId.equals(currentUserId)) {
						fieldPermissionsList.add(createEditablePermissionObject(fieldPermission.getFieldId(), false));
					} else {
						fieldPermissionsList.add(createEditablePermissionObject(fieldPermission.getFieldId(), true));
					}
					break;

				case "write by team":

					boolean isDifferentTeam = false;
					isDifferentTeam = isUserDifferentTeam(createdUserId, currentUserId);

					if (isDifferentTeam) {
						fieldPermissionsList.add(createEditablePermissionObject(fieldPermission.getFieldId(), true));
					} else {
						fieldPermissionsList.add(createEditablePermissionObject(fieldPermission.getFieldId(), false));
					}
					break;

				default:
					break;
				}
			} else {
				fieldPermissionsList.add(createEditablePermissionObject(fieldPermission.getFieldId(), true));
			}
		}

		return fieldPermissionsList;

	}

	public EditablePermission createEditablePermissionObject(String fieldId, Boolean value) {
		EditablePermission editablePermission = new EditablePermission();
		editablePermission.setFieldId(fieldId);
		editablePermission.setNotEditable(value);

		return editablePermission;
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
