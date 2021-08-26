package com.ngdesk.sam.controllers.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.LogsRepository;
import com.ngdesk.repositories.RolesRepository;
import com.ngdesk.sam.roles.dao.Role;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RefreshScope
public class LogsAPI {

	@Autowired
	private AuthManager authManager;

	@Autowired
	private LogsRepository logsRepository;
 
	@Autowired
	private RolesRepository rolesRepository;

	@GetMapping("/controller/{controller_id}/logs")
	@Operation(summary = "Get all", description = "Gets all the logs for a controller")
	public Page<Log> getLogs(
			@Parameter(description = "Controller ID", required = true) @PathVariable("controller_id") String controllerId,
			@Parameter(description = "Application Name", required = true) @RequestParam("application_name") String applicationName,
			@Parameter(description = "limit", required = true) @RequestParam("limit") Integer limit) {

		String companyId = authManager.getUserDetails().getCompanyId();

		Optional<Role> optionalRole = rolesRepository.findById(authManager.getUserDetails().getRole(),
				"roles_" + companyId);
		if (optionalRole.isEmpty()) {
			throw new ForbiddenException("FORBIDDEN"); 
		}
		
		Role role = optionalRole.get();
		if (!role.getName().equals("SystemAdmin")) {
			throw new ForbiddenException("FORBIDDEN");
		}
		
		Pageable pageable = PageRequest.of(0, limit, Sort.by("DATE_CREATED").descending());
		return logsRepository.findAllApplicationLogs(controllerId, applicationName, pageable, "probe_logs");
	}
}
