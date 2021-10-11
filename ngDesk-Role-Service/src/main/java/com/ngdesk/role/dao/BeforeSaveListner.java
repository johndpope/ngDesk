package com.ngdesk.role.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.RoleRepository;

@Component
public class BeforeSaveListner extends AbstractMongoEventListener<Role> {

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	AuthManager authManager;

	@Override
	public void onBeforeConvert(BeforeConvertEvent<Role> event) {
		Role role = event.getSource();
		findDuplicateName(role);
	}

	private void findDuplicateName(Role role) {
		Optional<Role> optionalRole = roleRepository.findRoleByName(role.getName(),
				"roles_" + authManager.getUserDetails().getCompanyId());

	}
}
