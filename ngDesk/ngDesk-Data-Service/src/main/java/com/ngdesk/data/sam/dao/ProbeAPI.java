package com.ngdesk.data.sam.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.websocket.SendResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.dao.RolesFieldPermissionsService;
import com.ngdesk.data.dao.SamPayload;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.data.roles.dao.RolesService;
import com.ngdesk.data.validator.Validator;
import com.ngdesk.repositories.module.entry.ModulesRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RefreshScope
public class ProbeAPI {

	private final Logger log = LoggerFactory.getLogger(ProbeAPI.class);

	@Autowired
	AuthManager authManager;

	@Autowired
	Validator validator;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	RolesService rolesService;

	@Autowired
	ModuleService moduleService;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	RolesFieldPermissionsService rolesFieldPermissionsService;

	@PostMapping("/modules/{module_id}/probes/data")
	@Operation(summary = "Post Module Entry", description = "Post a single entry for a module from probe")
	public void postModuleEntry(@RequestBody HashMap<String, Object> entry,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
			@Parameter(description = "Company ID", required = false) @RequestParam(value = "company_id", required = false) String companyId,
			@Parameter(description = "User UUID", required = false) @RequestParam(value = "user_uuid", required = false) String userUuid) {

		companyId = authManager.getUserDetails().getCompanyId();
		String roleId = authManager.getUserDetails().getRole();
		if (!validator.isValidObjectId(moduleId)) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		if (!rolesService.isAuthorizedForRecord(roleId, "POST", moduleId)) {
			throw new ForbiddenException("FORBIDDEN");
		}

		rolesFieldPermissionsService.isAuthorized(roleId, moduleId, entry, "POST");

		if (entry.get("ENTRIES") == null) {
			throw new BadRequestException("ENTRIES_REQUIRED", null);
		}

		Optional<List<Module>> optionalModules = modulesRepository.findAllModules("modules_" + companyId);
		List<Module> allModules = optionalModules.get();

		Module accountsModule = allModules.stream().filter(module -> module.getName().equals("Accounts")).findFirst()
				.orElse(null);
		Module productsModule = allModules.stream().filter(module -> module.getName().equals("Software Products"))
				.findFirst().orElse(null);
		Module standardizedSoftwareInstallationModule = allModules.stream()
				.filter(module -> module.getName().equals("Standardized Software Installation")).findFirst()
				.orElse(null);

		if (optionalModule.get().getName().equals("Software Installation")) {
			if (accountsModule == null) {
				String[] vars = { "Accounts" };
				throw new BadRequestException("MODULE_MISSING", vars);
			}
			if (productsModule == null) {
				String[] vars = { "Software Products" };
				throw new BadRequestException("MODULE_MISSING", vars);
			}
			if (standardizedSoftwareInstallationModule == null) {
				String[] vars = { "Standardized Software Installation" };
				throw new BadRequestException("MODULE_MISSING", vars);
			}
		}

		List<Map<String, Object>> entries = (List<Map<String, Object>>) entry.get("ENTRIES");
		for (Map<String, Object> entryToPost : entries) {
			SamPayload payload = new SamPayload();
			payload.setCompanyId(companyId);
			payload.setModuleId(moduleId);
			payload.setEntry(entryToPost);
			payload.setUserId(authManager.getUserDetails().getUserId());
			payload.setRoleId(authManager.getUserDetails().getRole());
			payload.setUserUuid(authManager.getUserDetails().getUserUuid());

			rabbitTemplate.convertAndSend("new-sam-entry", payload);
		}

	}

}
