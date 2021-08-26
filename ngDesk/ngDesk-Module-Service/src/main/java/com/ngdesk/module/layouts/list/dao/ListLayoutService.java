package com.ngdesk.module.layouts.list.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.field.dao.ModuleField;
import com.ngdesk.module.layout.dao.Condition;
import com.ngdesk.module.layout.dao.LayoutService;
import com.ngdesk.module.layout.dao.ListLayout;
import com.ngdesk.repositories.FieldRepository;
import com.ngdesk.repositories.ListLayoutRepository;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.repositories.RoleRepository;

@Component
public class ListLayoutService {

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	FieldRepository fieldRepository;

	@Autowired
	MongoOperations mongoOperations;

	@Autowired
	LayoutService layoutService;

	@Autowired
	ListLayoutRepository listLayoutRepository;
	@Autowired
	AuthManager authManager;

	public void moduleIdIsValid(String moduleId, String collectionName) {

		Optional<Module> optionalModule = moduleRepository.findById(moduleId, collectionName);
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE_ID", null);
		}

	}

	public List<ListLayout> validModuleIdGetListLayouts(String moduleId, String collectionName) {

		Optional<Module> optionalModule = moduleRepository.findById(moduleId, collectionName);
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE_ID", null);
		}
		List<ListLayout> listLayouts = optionalModule.get().getListLayout();

		return listLayouts;

	}

	public void layoutIdIsValid(String moduleId, String layoutId, String collectionName) {

		Optional<Module> optionalModule = moduleRepository.findById(moduleId, collectionName);
		List<ListLayout> listLayout = optionalModule.get().getListLayout();
		List<String> layoutIds = new ArrayList<String>();
		for (ListLayout layout : listLayout) {
			layoutIds.add(layout.getLayoutId());
		}
		if (!(layoutIds.contains(layoutId))) {
			throw new BadRequestException("INVALID_LAYOUT_ID", null);
		}
	}

	public ListLayout validateAndGetListLayout(Module module, String layoutId) {

		Optional<ListLayout> optionalListLayout = module.getListLayout().stream()
				.filter(listLayout -> listLayout.getLayoutId().equalsIgnoreCase(layoutId)).findFirst();

		if (optionalListLayout.isEmpty()) {
			String[] vars = { "LIST_LAYOUT" };
			throw new BadRequestException("DAO_NOT_FOUND", vars);
		}

		return optionalListLayout.get();
	}

	public void isRoleNameExists(String name, String role, String moduleId, String collectionName) {

		Optional<Module> optionalModule = moduleRepository.findById(moduleId, collectionName);
		Module module = optionalModule.get();
		List<ListLayout> listLayout = module.getListLayout();
		List<String> roleIds = new ArrayList<String>();
		List<String> names = new ArrayList<String>();
		for (ListLayout layout : listLayout) {
			roleIds.add(layout.getRole());
			names.add(layout.getName());
		}
		if ((names.contains(name)) && (roleIds.contains(role))) {
			throw new BadRequestException("NAME_AND_ROLE_ALREADY_EXISTS", null);
		}

	}

	public void isValidOrderBy(ListLayout listLayout) {
		List<String> fieldIds = listLayout.getColumnShow().getFields();
		if (!(fieldIds.contains(listLayout.getOrderBy().getColumn()))) {
			throw new BadRequestException("INVALID_ORDER_BY", null);

		}
	}

	public void isValidListLayoutFields(ListLayout listLayout, Module module) {

		List<ListLayout> listlayouts = module.getListLayout();
		List<String> columnFieldIds = listLayout.getColumnShow().getFields();
		List<ModuleField> fields = module.getFields();
		List<ModuleField> relationshipFields = fields.stream()
				.filter(field -> field.getDataType().getDisplay().equals("RelationShip")).collect(Collectors.toList());

		if (relationshipFields != null) {

			for (String field : columnFieldIds) {
				for (int i = 0; i < relationshipFields.size(); i++) {

					if (relationshipFields.get(i).getFieldId().equals(field)) {
						if ((relationshipFields.get(i).getDataType().getDisplay().equals("RelationShip"))
								&& ((relationshipFields.get(i).getRelationshipType().equals("One to Many"))
										|| (relationshipFields.get(i).getRelationshipType().equals("Many to Many")))) {

							throw new BadRequestException("INVALID_RELATIONSHIP", null);

						}

					}
				}

			}
		}

	}

	public void isDefault(ListLayout listLayout, String moduleId, Module module) {

		List<ListLayout> layouts = module.getListLayout();

		if (listLayout.getIsDefault()) {
			for (ListLayout lay : layouts) {

				if ((lay.getIsDefault() == true) && (listLayout.getRole().equals(lay.getRole()))) {

					lay.setIsDefault(false);
					listLayoutRepository.removeListLayout(moduleId, lay.getLayoutId(),
							"modules_" + authManager.getUserDetails().getCompanyId());
					listLayoutRepository.saveListLayout("modules_" + authManager.getUserDetails().getCompanyId(), lay,
							moduleId, authManager.getUserDetails().getCompanyId());
				}
			}
		} else {
			boolean atLeastOneDefaultLayout = false;
			for (ListLayout lay : layouts) {

				if ((lay.getIsDefault() == true) && (listLayout.getRole().equals(lay.getRole()))) {
					atLeastOneDefaultLayout = true;
					break;
				}
			}
			if (!atLeastOneDefaultLayout) {
				throw new BadRequestException("DEFAULT_LIST_LAYOUT_REQUIRED", null);
			}
		}

	}

	
	public void isValidConditions(ListLayout listLayout, Module module) {
		List<ModuleField> allFields = module.getFields();
		List<Condition> conditions = listLayout.getConditions();
		if (conditions != null) {
			for (Condition conditionField : conditions) {
				Optional<ModuleField> fields = allFields.stream()
						.filter(field -> field.getFieldId().equals(conditionField.getCondition())).findFirst();
						
				if (fields.isEmpty()) {
					throw new BadRequestException("INVALID_CONDITION", null);

				}

			}
		}

	}

}
