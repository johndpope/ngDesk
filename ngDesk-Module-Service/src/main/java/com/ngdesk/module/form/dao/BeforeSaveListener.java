package com.ngdesk.module.form.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.field.dao.ModuleField;
import com.ngdesk.repositories.FieldRepository;
import com.ngdesk.repositories.FormRepository;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.workflow.dao.Workflow;

@Component
public class BeforeSaveListener extends AbstractMongoEventListener<Form> {

	@Autowired
	FormRepository formRepository;

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	FieldRepository fieldRepository;

	public void onBeforeConvert(BeforeConvertEvent<Form> event) {

		Form form = event.getSource();
		checkValidModuleId(form.getModuleId(), "modules_" + form.getCompanyId());
		checkForDuplicates(form, event);
		checkValidFieldId(form.getPanels(), form.getModuleId(), "modules_" + form.getCompanyId());
		requiredFieldsValidation(form.getPanels(), form.getModuleId(), "modules_" + form.getCompanyId());
		checkValidWorkflow(form);

	}

	private void checkForDuplicates(Form form, BeforeConvertEvent<Form> event) {
		if (form.getFormId() == null) {
			Optional<Form> optional = formRepository.findFormByName(form.getCompanyId(), form.getModuleId(),
					form.getName(), form.getCompanyId(), event.getCollectionName());
			if (optional.isPresent()) {
				String[] variables = { "FORM_NAME" };
				throw new BadRequestException("FORM_DAO_VARIABLE_ALREADY_EXISTS", variables);
			}
		} else {
			Optional<Form> optional = formRepository.findFormWithDuplicateName(form.getModuleId(), form.getName(),
					form.getCompanyId(), form.getFormId(), event.getCollectionName());
			if (optional.isPresent()) {
				String[] variables = { "FORM_NAME" };
				throw new BadRequestException("FORM_DAO_VARIABLE_ALREADY_EXISTS", variables);
			}
		}
	}

	private void checkValidModuleId(String moduleId, String companyId) {

		Optional<Module> module_Id = moduleRepository.findById(moduleId, companyId);
		if (module_Id.isEmpty()) {
			throw new BadRequestException("NOT_VALID_MODULE_ID", null);
		}
	}

	private void checkValidFieldId(List<FormPanel> formPanels, String module, String companyId) {

		for (FormPanel formPanel : formPanels) {
			List<List<Grid>> grids = formPanel.getGrids();
			Optional<Module> modules = moduleRepository.findById(module, companyId);
			List<ModuleField> fields = modules.get().getFields();
			List<String> lists = new ArrayList<String>();
			for (List<Grid> grid : grids) {
				for (Grid gridPresent : grid) {
					if (gridPresent.isEmpty() == false) {
						Optional<ModuleField> fieldId = fields.stream()
								.filter(moduleField -> moduleField.getFieldId().equals(gridPresent.getFieldId()))
								.findAny();
						if (!fieldId.isPresent()) {
							throw new BadRequestException("FIELD_ID_INVALID", null);
						}
						lists.add(gridPresent.getFieldId());
					}
				}
			}
			if (lists.isEmpty()) {
				throw new BadRequestException("FIELD_ID_REQUIRED", null);
			}
		}

	}

	public void requiredFieldsValidation(List<FormPanel> formPanels, String module, String companyId) {

		Optional<Module> modules = moduleRepository.findById(module, companyId);
		List<ModuleField> fields = modules.get().getFields();
		List<String> list = new ArrayList<String>();
		for (FormPanel formPanel : formPanels) {
			List<List<Grid>> grids = formPanel.getGrids();
			for (List<Grid> grid : grids) {
				for (Grid gridPresent : grid) {
					if (gridPresent.isEmpty() == false) {
						list.add(gridPresent.getFieldId());
					}
				}
			}
		}
		for (ModuleField field : fields) {
			if (field.getRequired() && !field.getDataType().getDisplay().equals("Auto Number")
					&& field.getDefaultValue() == null) {
				if (!list.contains(field.getFieldId())) {
					String[] vars = { field.getDisplayLabel() };
					throw new BadRequestException("REQUIRED_FIELD_MISSING", vars);
				}
			}
		}
	}

	private void checkValidWorkflow(Form form) {
		if (form.getWorkflow() != null) {
			Optional<Workflow> optionalWorkflow = formRepository.findWorkflowByModuleIdAndWorkflowId(form.getWorkflow(),
					form.getModuleId(), form.getCompanyId());
			if (optionalWorkflow.isEmpty()) {
				throw new BadRequestException("INVALID_FORM_WORKFLOW", null);
			}
		}

	}

}
