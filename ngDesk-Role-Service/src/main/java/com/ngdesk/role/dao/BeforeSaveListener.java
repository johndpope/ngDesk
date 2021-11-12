package com.ngdesk.role.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.RoleRepository;

@Component("saveListener")
public class BeforeSaveListener extends AbstractMongoEventListener<Role> {

	@Autowired
	RoleRepository roleRepository;
	@Autowired
	AuthManager authManager;

	@Override
	public void onBeforeConvert(BeforeConvertEvent<Role> event) {
		Role role = event.getSource();
		validateDuplicateName(role);
	}

	public void validateDuplicateName(Role role) {
		if (role.getId() == null || role.getId() == "") {
			Optional<Role> optionalRole = roleRepository.findRoleByName(role.getName(),
					"roles_" + authManager.getUserDetails().getCompanyId());
			if (optionalRole.isPresent()) {
				String[] variables = { "ROLE", "Name" };
				throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", variables);
			}
		} else {
			Optional<Role> optionalRole = roleRepository.findRoleByNameAndRoleId(role.getName(), role.getId(),
					"roles_" + authManager.getUserDetails().getCompanyId());
			if (optionalRole.isPresent()) {
				String[] variables = { "ROLE", "Name" };
				throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", variables);
			}
		}
	}
}