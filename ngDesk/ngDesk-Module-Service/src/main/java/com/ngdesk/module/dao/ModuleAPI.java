package com.ngdesk.module.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import javax.validation.Valid;

import org.bson.types.ObjectId;
import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.exceptions.UnauthorizedException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.elastic.dao.ElasticService;
import com.ngdesk.module.layout.dao.CreateEditLayout;
import com.ngdesk.module.layout.dao.ListLayout;
import com.ngdesk.module.mobile.layout.dao.CreateEditMobileLayout;
import com.ngdesk.module.mobile.layout.dao.ListMobileLayout;
import com.ngdesk.module.role.dao.RoleService;
import com.ngdesk.module.validations.dao.ModuleValidation;
import com.ngdesk.repositories.ModuleRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RefreshScope
public class ModuleAPI {

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	RoleService roleService;

	@Autowired
	ModuleService moduleService;

	@Autowired
	ElasticService elasticService;
	
	@GetMapping("/modules")
	@Operation(summary = "Get all", description = "Gets all the modules with pagination and search")
	@PageableAsQueryParam
	public Page<Module> getModules(
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable) {

		String collectionName = "modules_" + authManager.getUserDetails().getCompanyId();
		return moduleRepository.findModulesByAggregation(pageable, collectionName);

	}

	@GetMapping("/module/{id}")
	@Operation(summary = "Get by ID", description = "Gets the module based on ID")
	public Module getModuleById(@Parameter(description = "Module ID", required = true) @PathVariable("id") String id) {

		String collectionName = "modules_" + authManager.getUserDetails().getCompanyId();
		Optional<Module> optional = moduleRepository.findById(id, collectionName);
		if (optional.isEmpty()) {
			String vars[] = { "MODULE" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		return moduleRepository.findModuleByAggregation(id, collectionName);
	}

	@PostMapping("/module")
	@Operation(summary = "Post a new Module", description = "Posts a new module")
	public Module postModule(@Valid @RequestBody Module module) {
		String companyId = authManager.getUserDetails().getCompanyId();
		String userId = authManager.getUserDetails().getUserId();

		if (!roleService.isSystemAdmin()) {
			throw new UnauthorizedException("UNAUTHORIZED");
		}
		
		moduleService.isValidModuleName(module.getName());
		moduleService.duplicateModuleCheck(module.getName());

		module = moduleService.setDefaults(module);
		
		if (module.getParentModule() != null) {
			if (!ObjectId.isValid(module.getParentModule())) {
				throw new BadRequestException("INVALID_PARENT_MODULE", null);
			}

			Optional<Module> optionalModule = moduleRepository.findById(module.getParentModule(),
					"modules_" + companyId);
			if (optionalModule.isEmpty()) {
				throw new BadRequestException("INVALID_PARENT_MODULE", null);
			}
		} else {
			module = moduleService.postDefaultFields(module);
		}

		// TODO: POST A DEFAULT EMAIL CHANNEL

		module= moduleRepository.save(module, "modules_" + companyId);
		
		moduleService.updateRolePermissions(module);
		
		elasticService.loadModuleDataIntoFieldLookUp(authManager.getUserDetails().getCompanyId(), module);
		
		String moduleCollectionName = module.getName().replaceAll("\\s+", "_") + "_" + companyId;
		moduleRepository.createCollection(moduleCollectionName);

		moduleService.publishToGraphql(authManager.getUserDetails().getCompanySubdomain());
		return module;
	}

	@PutMapping("/module")
	@Operation(summary = "Edit a Module", description = "Updates a module")
	public Module updateModule(@Valid @RequestBody Module module) {

		if (!roleService.isSystemAdmin()) {
			throw new UnauthorizedException("UNAUTHORIZED");
		}

		moduleService.validateModule(module);

		Optional<Module> optionalModule = moduleRepository.findById(module.getModuleId(),
				"modules_" + authManager.getUserDetails().getCompanyId());

		Module existingModule = optionalModule.get();

		String oldName = existingModule.getName();

		// SET ONLY NAME, PLURAL_NAME, SINGULAR_NAME, DESCRIPTION ON UPDATE
		existingModule.setName(module.getName());
		existingModule.setDateUpdated(new Date());
		existingModule.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		existingModule.setDescription(module.getDescription());
		existingModule.setSingular(module.getSingular());
		existingModule.setPlural(module.getPlural());

		if (!oldName.equals(module.getName())) {
			moduleService.duplicateModuleCheck(module.getName());

			String oldCollectionName = oldName.replaceAll("\\s+", "_") + "_"
					+ authManager.getUserDetails().getCompanyId();
			String newCollectionName = module.getName().replaceAll("\\s+", "_") + "_"
					+ authManager.getUserDetails().getCompanyId();

			// CHANGING THE COLLECTION NAME
			moduleRepository.changeCollectionName(oldCollectionName, newCollectionName);
		}

		return moduleRepository.save(existingModule, "modules_" + authManager.getUserDetails().getCompanyId());

	}
}
