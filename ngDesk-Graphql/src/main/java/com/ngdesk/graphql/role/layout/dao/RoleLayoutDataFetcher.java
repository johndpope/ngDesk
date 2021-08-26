package com.ngdesk.graphql.role.layout.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.role.dao.Role;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.role.layout.RoleLayoutRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class RoleLayoutDataFetcher implements DataFetcher<RoleLayout> {

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	RoleLayoutRepository roleLayoutRepository;

	@Override
	public RoleLayout get(DataFetchingEnvironment environment) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String id = environment.getArgument("id");

		Optional<RoleLayout> optional = roleLayoutRepository.findByCompanyIdAndId(companyId, id, "role_layouts");

		List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);
		sessionManager.getSessionInfo().put("modulesMap", modules);
		String roleId = authManager.getUserDetails().getRole();
		Optional<Role> optionalRole = roleLayoutRepository.findRoleName(roleId, "roles_" + companyId);

		if (optionalRole.get().getName().equals("SystemAdmin")) {
			return optional.get();
		} else {
			Optional<RoleLayout> optionalRoleId = roleLayoutRepository.findLayoutByRoleIdAndCompanyId(companyId, roleId,
					id, "role_layouts");
			if (optionalRoleId.isEmpty()) {
				return null;
			}
			return optionalRoleId.get();
		}
	}
}
