package com.ngdesk.data.dao;

import java.util.Map;

import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RefreshScope
public class UserAPI {

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleEntryRepository entryRepository;

	@GetMapping("/users/invite/pending")
	@PageableAsQueryParam
	public PageImpl<Map<String, Object>> pendingInvitesAPI(
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable) {
		String contactCollectionName = "Contacts_" + authManager.getUserDetails().getCompanyId();
		String userCollectionName = "Users_" + authManager.getUserDetails().getCompanyId();
		return entryRepository.findAllPendingInvitedUsers(pageable, contactCollectionName, userCollectionName);

	}

}
