package com.ngdesk.role.layout.dao;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.repositories.RoleLayoutRepository;
import com.ngdesk.repositories.RoleRepository;
import com.ngdesk.role.dao.Role;
import com.ngdesk.role.module.dao.Module;
import com.ngdesk.role.module.dao.ModuleField;

@Component
public class BeforeSaveListener extends AbstractMongoEventListener<RoleLayout> {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private AuthManager authManager;

	@Autowired
	private ModuleRepository moduleRepository;

	@Autowired
	private RoleLayoutRepository roleLayoutRepository;

	@Override
	public void onBeforeConvert(BeforeConvertEvent<RoleLayout> event) {

		RoleLayout layout = event.getSource();
		uniqueName(layout, event.getCollectionName());
		validRole(layout);
		validModule(layout);
		validateOneDefaultPerRole(layout);

	}

	private void validRole(RoleLayout roleLayout) {
		Optional<Role> optionalRole = roleRepository.findById(roleLayout.getRole(),
				"roles_" + authManager.getUserDetails().getCompanyId());
		if (optionalRole.isEmpty()) {
			String[] vars = {};
			throw new BadRequestException("INVALID_ROLE", vars);
		}
	}

	private void validModule(RoleLayout layout) {
		List<Tab> modules = layout.getTabs();
		if (modules.size() == 0) {
			String[] vars = {};
			throw new BadRequestException("ATLEAST_ONE_LAYOUT_REQUIRED", vars);
		}

		for (Tab layoutModule : modules) {
			Optional<Module> optionalModule = moduleRepository.findById(layoutModule.getModule(),
					"modules_" + authManager.getUserDetails().getCompanyId());

			if (optionalModule.isEmpty()) {
				String[] vars = {};
				throw new BadRequestException("MODULE_INVALID", vars);
			}
			validColumns(layoutModule, optionalModule.get());
			validOrderBy(layoutModule, optionalModule.get());
			validateColumnsInCondition(layoutModule, optionalModule.get());
		}
	}

	private void validColumns(Tab layoutModule, Module module) {
		List<ModuleField> fields = module.getFields();
		List<String> showColumns = layoutModule.getColumnsShow();
		validateForApprovalField(layoutModule, module);
		for (String showColumn : showColumns) {
			Optional<ModuleField> optionalField = fields.stream()
					.filter(field -> field.getFieldId().equalsIgnoreCase(showColumn)).findFirst();

			if (optionalField.isEmpty()) {
				String[] vars = { module.getName() };
				throw new BadRequestException("INVALID_FIELD", vars);
			}
		}
	}

	private void validOrderBy(Tab layoutModule, Module module) {
		List<ModuleField> fields = module.getFields();
		String orderByColumn = layoutModule.getOrderBy().getColumn();
		Optional<ModuleField> optionalField = fields.stream()
				.filter(field -> field.getFieldId().equalsIgnoreCase(layoutModule.getOrderBy().getColumn())).findAny();
		if (optionalField.isEmpty()) {
			String vars[] = { module.getName() };
			throw new BadRequestException("INVALID_FIELD_ORDER_BY", vars);
		}
		if (!layoutModule.getColumnsShow().contains(orderByColumn)) {
			String vars[] = { module.getName() };
			throw new BadRequestException("ORDER_BY_NOT_IN_SHOW_COLUMNS", vars);
		}
	}

	private void uniqueName(RoleLayout layout, String collectionName) {

		if (layout.getLayoutId() == null) {
			Optional<RoleLayout> optional = roleLayoutRepository.findDuplicateRoleLayoutName(layout.getName(),
					layout.getRole(), layout.getCompanyId(), collectionName);
			if (optional.isPresent()) {
				String[] vars = { "ROLE_LAYOUT", "name" };
				throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", vars);
			}
		} else {
			Optional<RoleLayout> optional = roleLayoutRepository.findOtherRoleLayoutWithDuplicateName(layout.getName(),
					layout.getRole(), layout.getCompanyId(), layout.getLayoutId(), collectionName);
			if (optional.isPresent()) {
				String[] vars = { "ROLE_LAYOUT", "name" };
				throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", vars);
			}
		}

	}

	private void validateColumnsInCondition(Tab layoutModule, Module module) {
		List<Condition> conditions = layoutModule.getConditions();
		for (Condition condition : conditions) {
			if (!condition.getCondition().contains("InputMessage")) {
				Optional<ModuleField> conditionField = module.getFields().stream()
						.filter(field -> field.getFieldId().equalsIgnoreCase(condition.getCondition())).findAny();
				if (conditionField.isEmpty()) {
					String[] vars = { module.getName() };
					throw new BadRequestException("INVALID_CONDITION_FIELD", vars);
				}
			}
		}

	}

	private void validateOneDefaultPerRole(RoleLayout roleLayout) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("role").is(roleLayout.getRole()), Criteria.where("defaultLayout").is(true));
		query.addCriteria(criteria);
		List<RoleLayout> roleLayouts = roleLayoutRepository.findAll(query, "role_layouts");
		if (roleLayouts.size() == 0 && !roleLayout.isDefaultLayout()) {
			String[] vars = {};
			throw new BadRequestException("NO_DEFAULT_FOR_ROLE", vars);
		}
	}

	private void validateForApprovalField(Tab layoutModule, Module module) {
		List<ModuleField> fields = module.getFields();
		List<Condition> conditions = layoutModule.getConditions();
		for (Condition condition : conditions) {
			ModuleField conditionField = fields.stream()
					.filter(field -> field.getFieldId().equals(condition.getCondition())).findFirst().get();

			String displayDatatype = conditionField.getDataType().getDisplay();
			System.out.println(displayDatatype);
			if (displayDatatype.equalsIgnoreCase("Approval")) {

				Pattern approvalPattern = Pattern.compile("APPROVED|REQUIRED|REJECTED");

				Matcher approvalMatcher = approvalPattern.matcher(condition.getConditionValue());
				if (!approvalMatcher.find()) {

					throw new BadRequestException("APPROVAL_VALUE_REQUIRED", null);
				}

			}
		}
	}
}
