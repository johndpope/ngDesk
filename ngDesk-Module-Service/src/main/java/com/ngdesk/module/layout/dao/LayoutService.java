package com.ngdesk.module.layout.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.field.dao.ModuleField;
import com.ngdesk.module.role.dao.FieldPermission;
import com.ngdesk.module.role.dao.Permission;
import com.ngdesk.module.role.dao.Role;
import com.ngdesk.repositories.RoleRepository;

@Service
public class LayoutService {

	@Autowired
	AuthManager authManager;

	@Autowired
	RoleRepository roleRepository;

	public CreateEditLayout initializeDefaultsForPostCall(CreateEditLayout layout) {
		layout.setDateCreated(new Date());
		layout.setDateUpdated(new Date());
		layout.setCreatedBy(authManager.getUserDetails().getUserId());
		layout.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		layout.setLayoutId(UUID.randomUUID().toString());
		return layout;
	}

	public CreateEditLayout initializeDefaultsForPutCall(CreateEditLayout layout, CreateEditLayout existingLayout) {
		layout.setDateCreated(existingLayout.getDateCreated());
		layout.setDateUpdated(new Date());
		layout.setCreatedBy(existingLayout.getCreatedBy());
		layout.setLastUpdatedBy(existingLayout.getLastUpdatedBy());
		return layout;
	}

	public void isValidFieldId(List<String> fieldid, Module module) {

		List<ModuleField> fields = module.getFields();
		List<String> moduleFields = new ArrayList<String>();

		for (ModuleField field : fields) {
			moduleFields.add(field.getFieldId());
		}

		for (String layoutFeild : fieldid) {
			if (layoutFeild.contains(".")) {
				continue;
			}
			if (!moduleFields.contains(layoutFeild)) {
				throw new BadRequestException("FIELD_ID_INVALID", null);
			}
		}

	}

	public void requiredFieldsValidation(List<String> fieldIds, Module module) {
		if (!module.getName().equalsIgnoreCase("Tickets") && !module.getName().equalsIgnoreCase("Chat")) {
			for (ModuleField field : module.getFields()) {
				if (field.getRequired() && !field.getName().equals("TEAMS") && !field.getNotEditable()
						&& !field.getInternal()) {
					if (!fieldIds.contains(field.getFieldId())) {
						String[] vars = { field.getDisplayLabel() };
						throw new BadRequestException("REQUIRED_FIELD_MISSING", vars);
					}
				}
			}
		}
	}

	public List<String> getFieldIdsFromLayouts(CreateEditLayout createLayout, Module module) {

		List<String> fieldIds = new ArrayList<String>();

		if (createLayout.getPreDefinedTemplate() != null) {
			List<PreDefinedTemplate> predefinedTemplates = createLayout.getPreDefinedTemplate();
			for (PreDefinedTemplate predefinedTemplate : predefinedTemplates) {
				List<PreDefinedTemplateField> predefineTemplateFields = predefinedTemplate.getFields();
				for (PreDefinedTemplateField predefineTemplateField : predefineTemplateFields) {
					fieldIds.add(predefineTemplateField.getFieldId());
				}
			}
		} else if (createLayout.getPanels() != null) {
			List<Panel> panels = createLayout.getPanels();
			List<String> pannelFields = new ArrayList<String>();
			for (Panel panel : panels) {
				List<List<Grid>> gridFeilds = panel.getGrids();
				for (List<Grid> gridFeild : gridFeilds) {
					for (Grid grid : gridFeild) {
						if (grid.getFieldId() != null && !grid.getFieldId().isBlank()) {
							pannelFields.add(grid.getFieldId());
						}
					}
				}
				if (pannelFields.isEmpty()) {
					String[] vars = { panel.getPanelName() };
					throw new BadRequestException("LAYOUT_FIELDS_EMPTY_FOR_PANNEL", vars);
				}
				fieldIds.addAll(pannelFields);
			}
		} else if (createLayout.getCustomLayout() != null) {
			for (ModuleField moduleField : module.getFields()) {
				String regex = "<div class=\"SIDEBAR_FIELD_ID\"(.*?)</div>";
				regex = regex.replace("FIELD_ID", moduleField.getFieldId());
				Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
				Matcher match = pattern.matcher(createLayout.getCustomLayout());
				if (match.find()) {
					fieldIds.add(moduleField.getFieldId());
				}
			}

		}

		return fieldIds;

	}

	public void validateFieldPermission(String roleId, String moduleId, Module module) {
		Optional<Role> optionalrole = roleRepository.findById(roleId,
				"roles_" + authManager.getUserDetails().getCompanyId());

		if (optionalrole.isEmpty()) {
			throw new BadRequestException("ROLE_ID_INVALID", null);
		}

		Role role = optionalrole.get();
		List<FieldPermission> fieldPermissions = new ArrayList<FieldPermission>();
		List<String> moduleFieldIds = new ArrayList<String>();

		if (!role.getName().equals("SystemAdmin")) {

			Permission permissionObject = role.getPermissions().stream()
					.filter(permission -> permission.getModule().equals(moduleId)).findFirst().orElse(null);

			if (permissionObject != null) {

				fieldPermissions = permissionObject.getFieldPermissions();

				for (ModuleField moduleField : module.getFields()) {
					moduleFieldIds.add(moduleField.getFieldId());
				}

				for (FieldPermission fieldPermission : fieldPermissions) {
					if (moduleFieldIds.contains(fieldPermission.getFieldId())) {
						if (fieldPermission.getPermission().equals("Read")) {
							throw new BadRequestException("CONTACT_SYSTEM_ADMIN", null);
						}
					}
				}
			}
		}
	}
}
