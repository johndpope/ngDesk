package com.ngdesk.module.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.websocket.SendResult;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.commons.exceptions.UnauthorizedException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.field.dao.ModuleField;
import com.ngdesk.module.layout.dao.CreateEditLayout;
import com.ngdesk.module.layout.dao.ListLayout;
import com.ngdesk.module.mobile.layout.dao.CreateEditMobileLayout;
import com.ngdesk.module.mobile.layout.dao.ListMobileLayout;
import com.ngdesk.module.role.dao.FieldPermission;
import com.ngdesk.module.role.dao.ModuleLevelPermission;
import com.ngdesk.module.role.dao.Permission;
import com.ngdesk.module.validations.dao.ModuleValidation;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.repositories.RoleRepository;

@Service
public class ModuleService {

	private final Logger log = LoggerFactory.getLogger(ModuleService.class);

	@Autowired
	private Global globals;

	@Autowired
	private AuthManager authManager;

	@Autowired
	private ModuleRepository moduleRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private ModuleEntryRepository entryRepository;

	@Autowired
	RabbitTemplate rabbitTemplate;

	public static String[] restrictedModuleNames = { "Accounts", "Tickets", "Users", "Teams" };

	public Module postDefaultFields(Module module) {
		List<ModuleField> fields = getDefaultFields();
		fields = initializeFields(fields);
		module.setFields(fields);
		return module;
	}

	public Module setDefaults(Module module) {

		module.setCreatedBy(authManager.getUserDetails().getUserId());
		module.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		module.setDateCreated(new Date());
		module.setDateUpdated(new Date());

		module.setListLayout(new ArrayList<ListLayout>());
		module.setCreateLayout(new ArrayList<CreateEditLayout>());
		module.setEditLayout(new ArrayList<CreateEditLayout>());
		module.setListMobileLayouts(new ArrayList<ListMobileLayout>());
		module.setEditMobileLayout(new ArrayList<CreateEditMobileLayout>());
		module.setCreateMobileLayout(new ArrayList<CreateEditMobileLayout>());
		module.setValidations(new ArrayList<ModuleValidation>());
		return module;
	}

	public void updateRolePermissions(Module module) {
		ModuleLevelPermission moduleLevelPermission = new ModuleLevelPermission("Enabled", "Not Set", "Not Set",
				"Not Set", "Not Set");
		List<FieldPermission> fieldPermissions = new ArrayList<FieldPermission>();
		module.getFields().stream().forEach(field -> {
			FieldPermission fieldPermission = new FieldPermission(field.getFieldId(), "Not Set");
			fieldPermissions.add(fieldPermission);
		});
		Permission permission = new Permission(module.getModuleId(), moduleLevelPermission, fieldPermissions);
		roleRepository.updatePermissions(permission, "roles_" + authManager.getUserDetails().getCompanyId());
	}

	private List<ModuleField> getDefaultFields() {
		try {
			String fieldString = globals.getFile("DefaultFields.json");
			ObjectMapper mapper = new ObjectMapper();
			List<ModuleField> fields = mapper.readValue(fieldString,
					mapper.getTypeFactory().constructCollectionLikeType(List.class, ModuleField.class));

			return fields;
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	private List<ModuleField> initializeFields(List<ModuleField> fields) {

		String[] modulesToFind = { "Users", "Teams" };

		Optional<List<Module>> optionalModules = moduleRepository.findAllModules(Arrays.asList(modulesToFind),
				"modules_" + authManager.getUserDetails().getCompanyId());

		if (optionalModules.isEmpty()) {
			throw new BadRequestException("USERS_TEAMS_MODULE_NOT_FOUND", null);
		}

		List<Module> modules = optionalModules.get();

		fields.forEach(field -> {
			field.setCreatedBy(authManager.getUserDetails().getUserId());
			field.setLastUpdatedBy(authManager.getUserDetails().getUserId());
			field.setDateCreated(new Date());
			field.setDateUpdated(new Date());
			if (field.getName().equalsIgnoreCase("Teams")) {
				Optional<Map<String, Object>> optionalGlobalTeam = entryRepository.findEntryByName("NAME", "Global",
						"Teams_" + authManager.getUserDetails().getCompanyId());

				if (optionalGlobalTeam.isEmpty()) {
					throw new BadRequestException("GLOBAL_TEAM_MISSING", null);
				}

				String globalTeamId = optionalGlobalTeam.get().get("_id").toString();

				Module teamsModule = modules.stream().filter(module -> module.getName().equalsIgnoreCase("Teams"))
						.findFirst().orElse(null);

				field.setModule(teamsModule.getModuleId());
				field.setDefaultValue(globalTeamId);
			} else if (field.getName().equalsIgnoreCase("LAST_UPDATED_BY")
					|| field.getName().equalsIgnoreCase("CREATED_BY")) {
				Module usersModule = modules.stream().filter(module -> module.getName().equalsIgnoreCase("Users"))
						.findFirst().orElse(null);

				field.setModule(usersModule.getModuleId());
			}
		});

		return fields;
	}

	public void validateModule(Module module) {
		if (module.getModuleId() == null) {
			throw new BadRequestException("MODULE_ID_INVALID", null);
		} else if (!ObjectId.isValid(module.getModuleId())) {
			throw new BadRequestException("MODULE_ID_INVALID", null);
		}

		Optional<Module> optionalModule = moduleRepository.findById(module.getModuleId(),
				"modules_" + authManager.getUserDetails().getCompanyId());

		if (optionalModule.isEmpty()) {
			String[] vars = { "MODULE" };
			throw new BadRequestException("DAO_NOT_FOUND", vars);
		}

		if (Arrays.asList(restrictedModuleNames).contains(optionalModule.get().getName())) {
			throw new UnauthorizedException("UNAUTHORIZED");
		}
	}

	public void duplicateModuleCheck(String moduleName) {
		Optional<Module> optionalDuplicateModule = moduleRepository.findModuleByName(moduleName,
				"modules_" + authManager.getUserDetails().getCompanyId());

		if (optionalDuplicateModule.isPresent()) {
			String[] vars = { "MODULE", "NAME" };
			throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", vars);
		}
	}

	public void publishToGraphql(String subdomain) {
		log.debug("Publishing to graphql queue");
		rabbitTemplate.convertAndSend("update-company-schema", subdomain);
	}

	public Module validateAndGetModule(String moduleId) {

		Optional<Module> optionalmodule = moduleRepository.findById(moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());

		if (optionalmodule.isEmpty()) {
			throw new BadRequestException("MODULE_ID_INVALID", null);
		}

		return optionalmodule.get();
	}

	public String getCollectionName(String moduleName, String companyId) {
		return moduleName.replaceAll("\\s+", "_") + "_" + companyId;
	}

	public void isValidModuleName(String moduleName) {
		if (!Character.isLetter(moduleName.charAt(0))) {
			throw new BadRequestException("MODULE_NAME_MUST_BE_CHARACTER", null);
		}
		List<String> list = List.of(
				"Schedules", "Schedule", "DiscoveryMaps", "DiscoveryMap", "UnApprovedDiscoveryMaps",
				"NormalizationRules", "NormalizationRule", "SamFileRule", "SamFileRules", "Dashboard", "Dashboards",
				"ScoreCardValue", "DistinctValues", "Module", "Modules", "Currency", "EnterpriseSearch",
				"EnterpriseSearchs", "RoleLayout", "RoleLayouts", "SignatureDocument", "SignatureDocuments", "Task",
				"Tasks", "Notification", "Notifications", "Catalogues", "Catalogue", "Reports", "Report", "Form",
				"Forms", "Sla", "Slas", "KbSection", "KbSections", "ChatChannel", "ChatChannels", "ChatPrompt",
				"ChatPrompts", "Workflows", "Workflow", "KbArticle", "KbArticles", "KbCategory", "KbCategories",
				"Escalation", "Escalations", "Currencies");
		if (list.contains(moduleName)) {
			String[] vars = { moduleName };
			throw new BadRequestException("DAO_VARIABLE_CANNOT_BE_GIVEN", vars);
		}
		
	
	}
}
