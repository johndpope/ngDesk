package com.ngdesk.graphql.role.layout.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.modules.dao.Condition;
import com.ngdesk.graphql.role.dao.ModuleLevelPermission;
import com.ngdesk.graphql.role.dao.Permission;
import com.ngdesk.graphql.role.dao.Role;
import com.ngdesk.repositories.role.RolesRepository;

@Component
public class RoleService {

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	AuthManager authManager;

	public List<Condition> convertCondition(List<RoleLayoutCondition> Roleconditions) {

		List<Condition> conditions = new ArrayList<Condition>();
		Roleconditions.forEach(Rolecondition -> {
			Condition condition = new Condition(Rolecondition.getRequirementType(), Rolecondition.getOperator(),
					Rolecondition.getCondition(), Rolecondition.getConditionValue());
			conditions.add(condition);
		});

		return conditions;

	}

	public boolean isCustomer(String roleId) {
		Assert.notNull(roleId, "Role Id is required");
		Optional<Role> optionalRole = rolesRepository.findById(roleId,
				"roles_" + authManager.getUserDetails().getCompanyId());
		if (optionalRole.isEmpty()) {
			return false;
		}

		Role role = optionalRole.get();
		if (role.getName().equals("Customers")) {
			return true;
		}
		return false;

	}

	public boolean isSystemAdmin(String roleId) {
		Assert.notNull(roleId, "Role Id is required");
		Optional<Role> optionalRole = rolesRepository.findById(roleId,
				"roles_" + authManager.getUserDetails().getCompanyId());
		if (optionalRole.isEmpty()) {
			return false;
		}

		Role role = optionalRole.get();
		if (role.getName().equals("SystemAdmin")) {
			return true;
		}
		return false;
	}

	public boolean isAuthorizedForRecord(String roleId, String requestType, String moduleId) {

		Assert.notNull(roleId, "Role ID is required");
		Assert.notNull(requestType, "Request type is required");
		Assert.notNull(moduleId, "Module ID is required");

		try {
			Optional<Role> optionalRole = rolesRepository.findById(roleId,
					"roles_" + authManager.getUserDetails().getCompanyId());

			if (optionalRole.isEmpty()) {
				if (authManager.getUserDetails().getCompanySubdomain().equalsIgnoreCase("subscribeit")) {
					System.out.println("1");
				}
				return false;
			}

			Role role = optionalRole.get();
			if (role.getName().equals("SystemAdmin")) {
				return true;
			}

			Permission modulePermission = role.getPermissions().stream()
					.filter(permission -> permission.getModule().equals(moduleId)).findFirst().orElse(null);

			if (modulePermission == null) {
				if (authManager.getUserDetails().getCompanySubdomain().equalsIgnoreCase("subscribeit")) {
					System.out.println("2");
				}
				return false;
			}

			ModuleLevelPermission moduleLevelPermission = modulePermission.getModulePermission();
			if (moduleLevelPermission.getAccess().equalsIgnoreCase("disabled")) {
				if (authManager.getUserDetails().getCompanySubdomain().equalsIgnoreCase("subscribeit")) {
					System.out.println("3");
				}
				return false;
			} else {
				if (requestType.equals("GET")) {
					if (authManager.getUserDetails().getCompanySubdomain().equalsIgnoreCase("subscribeit")) {
						System.out.println("4");
					}
					if (moduleLevelPermission.getView().equals("None")) {
						if (authManager.getUserDetails().getCompanySubdomain().equalsIgnoreCase("subscribeit")) {
							System.out.println("5");
						}
						return false;
					}
				} else if (requestType.equals("POST") || requestType.equals("PUT")) {
					if (moduleLevelPermission.getEdit().equals("None")) {
						return false;
					}
				} else if (requestType.equals("DELETE")) {
					if (moduleLevelPermission.getDelete().equals("None")) {
						return false;
					}
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
