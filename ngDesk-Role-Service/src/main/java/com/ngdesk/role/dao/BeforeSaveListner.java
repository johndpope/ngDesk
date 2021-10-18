package com.ngdesk.role.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.RoleRepository;

@Component
public class BeforeSaveListner extends AbstractMongoEventListener<Role> {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private AuthManager authManager;

	@Override
	public void onBeforeConvert(BeforeConvertEvent<Role> event) {
		Role role = event.getSource();
		findDuplicateName(role);
	}

	public void findDuplicateName(Role role) {
		String companyId = authManager.getUserDetails().getCompanyId();
		String collectionName = "roles_" + companyId;
		Optional<Role> optionalRole = roleRepository.findRoleByName(role.getName(), collectionName);
		if (optionalRole.isPresent()) {
			String[] variables = { "ROLE", "NAME" };
			throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", variables);
		}
	}
}
