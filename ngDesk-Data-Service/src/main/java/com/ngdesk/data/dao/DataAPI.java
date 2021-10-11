package com.ngdesk.data.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.lang3.math.NumberUtils;
import org.bson.types.ObjectId;
import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.mail.EmailService;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.modules.dao.Condition;
import com.ngdesk.data.modules.dao.ListFormulaField;
import com.ngdesk.data.modules.dao.ListLayout;
import com.ngdesk.data.modules.dao.ListMobileLayout;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.data.rolelayout.dao.LayoutModule;
import com.ngdesk.data.rolelayout.dao.RoleLayout;
import com.ngdesk.data.roles.dao.RolesService;
import com.ngdesk.data.validator.Validator;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;
import com.ngdesk.repositories.roles.RoleLayoutRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RefreshScope
public class DataAPI {

	@Autowired
	Validator validator;

	@Autowired
	AuthManager authManager;

	@Autowired
	RolesService rolesService;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleService moduleService;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	DataService dataService;

	@Autowired
	DataAPI dataApi;

	@Value("${manager.host}")
	String managerHost;

	@Autowired
	MongoTransactionManager mongoTransactionManager;

	@Autowired
	BulkUpdateService bulkUpdateService;

	@Autowired
	MergeService mergeService;

	@Autowired
	RelationshipService relationshipService;

	@Autowired
	RoleLayoutRepository roleLayoutRepository;

	@Autowired
	EmailService emailService;

	@Autowired
	EntryChangeLogService entryChangeLogService;

	@Autowired
	DeleteDataService deleteDataService;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	RolesFieldPermissionsService rolesFieldPermissionsService;

	@Transactional
	@PostMapping("/modules/{module_id}/data")
	@Operation(summary = "Post Module Entry", description = "Post a single entry for a module")
	public Map<String, Object> postModuleEntry(@RequestBody HashMap<String, Object> entry,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
			@Parameter(description = "Is Trigger", required = false) @RequestParam(value = "is_trigger", required = false) boolean isTrigger,
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

		List<Module> moduleFamily = moduleService.getModuleFamily(moduleId,
				authManager.getUserDetails().getCompanyId());

		// TODO: Handle Discovered Software, Applications & Accounts Duplicate repost
		Map<String, Object> payload = dataApi.saveEntry(entry, moduleId, moduleFamily);

		payload.put("DATA_ID", payload.get("_id").toString());
		payload.remove("_id");

		dataService.addToOCRQueue(payload, optionalModule.get());

		if (!isTrigger) {
			WorkflowPayload workflowPayload = new WorkflowPayload(authManager.getUserDetails().getUserId(), moduleId,
					authManager.getUserDetails().getCompanyId(), payload.get("DATA_ID").toString(),
					new HashMap<String, Object>(), "POST", new Date());
			dataService.addToWorkflowQueue(workflowPayload);
		}

		moduleFamily.forEach(module -> {
			dataService.addToNotifyQueue(new NotificationMessage(module.getModuleId(),
					authManager.getUserDetails().getCompanyId(), null, null));
		});

		return payload;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Map<String, Object> saveEntry(Map<String, Object> entry, String moduleId, List<Module> moduleFamily) {

		String companyId = authManager.getUserDetails().getCompanyId();

		Map<String, String> moduleEntryMap = new HashMap<String, String>();
		Map<String, Object> payload = new HashMap<String, Object>();
		Map<String, Map<String, Object>> payloadMap = new HashMap<String, Map<String, Object>>();
		for (Module module : moduleFamily) {
			String collectionName = module.getName().replaceAll("\\s+", "_") + "_" + companyId;
			entry = dataService.formatRelationship(module, entry);

			payload = dataService.formatDateAndTimeField(entry, module);
			payload = dataService.buildEntryPayload(module, entry);

			payload = dataService.addFieldsWithDefaultValue(moduleService.getAllFields(module, companyId), payload);

			payload = dataService.addInternalFields(module, payload, authManager.getUserDetails().getUserId(),
					companyId);

			// TODO: FIX THIS
			entry.put("_id", payload.get("_id"));

			payload = dataService.addAutoNumberFields(module, payload);
			payload = dataService.setInheritanceValue(module, payload);
			payload = dataService.formatPayload(module, payload);
			payload = dataService.setInheritanceValue(module, payload);
			payload = dataService.formatDiscussion(module, entry, payload);
			payload = dataService.encryptPassword(module, payload);

			moduleEntryMap.put(module.getModuleId(), payload.get("_id").toString());
			if (module.getParentModule() != null) {
				Module parentModule = moduleFamily.stream()
						.filter(mod -> mod.getModuleId().equals(module.getParentModule())).findFirst().orElse(null);
				payload.put(parentModule.getSingularName().toUpperCase().replaceAll("\\s+", "_"),
						moduleEntryMap.get(parentModule.getModuleId()));
			}
			if (dataService.requiredFieldsCheckRequired(payload)) {
				validator.requiredFieldsPresent(module, payload);
			}
			validator.validateAlternatePrimaryKeys(module, entry, collectionName);
			validator.validateUniqueField(module, entry, collectionName, "POST", companyId);

			validator.validateBaseTypes(module, payload, companyId);
			payload = dataService.formatChronometerInCreateLayout(module, payload);
			validator.validateModuleValidations(module, payload, "POST", authManager.getUserDetails().getRole(),
					companyId);

			List<ModuleField> formulaFields = module.getFields().stream()
					.filter(field -> field.getDataType().getDisplay().equals("Formula")).collect(Collectors.toList());
			for (ModuleField field : formulaFields) {
				String value = dataService.getFormulaFieldValue(module, payload, field, field.getFormula());
				if (NumberUtils.isParsable(value)) {
					payload.put(field.getName(), Float.valueOf(value));
				} else {
					payload.put(field.getName(), value);
				}
			}

			List<ModuleField> listFormulaFields = module.getFields().stream()
					.filter(field -> field.getDataType().getDisplay().equals("List Formula"))
					.collect(Collectors.toList());

			payload = dataService.getListFormulaField(listFormulaFields, entry, payload, module);

			Optional<ModuleField> optionalReceiptCaptureField = module.getFields().stream()
					.filter(field -> field.getDataType().getDisplay().equals("Receipt Capture")).findFirst();

			if (optionalReceiptCaptureField.isPresent()) {

				ModuleField receiptCaptureField = optionalReceiptCaptureField.get();
				payload = dataService.formatReceiptCapture(module, payload, receiptCaptureField);
			}

			payload = dataService.setSlas(module, payload, new HashMap<String, Object>(), "POST", companyId);
			entryRepository.save(payload, collectionName);

			relationshipService.postOneToManyEntries(entry, module);
			payloadMap.put(module.getModuleId(), payload);

			dataService.addMetadataEntryQueue(new MetadataPayload(companyId, module.getModuleId(),
					payload.get("_id").toString(), authManager.getUserDetails().getUserId(), null, payload));

		}
		dataService.insertIntoElasticSearch(payloadMap, moduleFamily, companyId);
		return payload;
	}

	@Transactional
	@PutMapping("/modules/{module_id}/data")
	@Operation(summary = "Put Module Entry", description = "Edit a single entry of a module")
	public Map<String, Object> putModuleEntry(@RequestBody HashMap<String, Object> entry,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
			@Parameter(description = "Trigger", required = false) @RequestParam(value = "is_trigger", required = false) boolean isTrigger,
			@Parameter(description = "Company ID", required = false) @RequestParam(value = "company_id", required = false) String companyId,
			@Parameter(description = "User UUID", required = false) @RequestParam(value = "user_uuid", required = false) String userUuid,
			@Parameter(description = "Probe", required = false) @RequestParam(value = "is_probe", required = false) boolean isProbe) {
		companyId = authManager.getUserDetails().getCompanyId();
		String roleId = authManager.getUserDetails().getRole();

		if (!validator.isValidObjectId(moduleId)) {

			throw new BadRequestException("INVALID_MODULE", null);
		}

		Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
		if (optionalModule.isEmpty()) {

			throw new BadRequestException("INVALID_MODULE", null);
		}

		Module module = optionalModule.get();
		HashMap<String, Object> existingEntry = new HashMap<String, Object>();
		Optional<Map<String, Object>> optionalEntry = Optional.empty();
		if (!rolesService.isAuthorizedForRecord(roleId, "PUT", moduleId)) {
			throw new ForbiddenException("FORBIDDEN");
		}

		rolesFieldPermissionsService.isAuthorized(roleId, moduleId, entry, "PUT");

		if (entry.get("DATA_ID") != null && !ObjectId.isValid(entry.get("DATA_ID").toString())) {

			throw new BadRequestException("INVALID_ENTRY", null);
		}

		String collectionName = moduleService.getCollectionName(module.getName(), companyId);

		if (entry.get("DATA_ID") == null) {

			List<ModuleField> alternatePrimaryFields = dataService.getAlternatePrimaryKeys(module);
			Map<String, Object> keyValuePairs = new HashMap<String, Object>();
			for (ModuleField alternatePrimaryField : alternatePrimaryFields) {
				String alternatePrimaryFieldName = alternatePrimaryField.getName();
				if (entry.get(alternatePrimaryFieldName) != null) {
					keyValuePairs.put(alternatePrimaryFieldName, entry.get(alternatePrimaryFieldName));
				}
			}
			if (keyValuePairs.size() > 0) {
				optionalEntry = entryRepository.findEntryByAlternatePrimaryKeys(keyValuePairs, collectionName);
			}
			if (optionalEntry.isEmpty()) {
				throw new BadRequestException("INVALID_ENTRY", null);
			}
			existingEntry = new HashMap<String, Object>(optionalEntry.get());

		} else {
			entry.put("_id", entry.get("DATA_ID").toString());
			entry.remove("DATA_ID");
			optionalEntry = entryRepository.findById(entry.get("_id").toString(), collectionName);
			if (optionalEntry.isEmpty()) {

				throw new BadRequestException("INVALID_ENTRY", null);
			}
			existingEntry.putAll(optionalEntry.get());

		}

		if (module.getName().equals("Teams")) {
			dataService.isEditableTeam(entry.get("_id").toString(), entry.get("NAME").toString());
		}

		if (module.getName().equals("Users")) {
			dataService.isEditableUser(entry.get("_id").toString());
			dataService.roleChangeCheck(entry, existingEntry);
		}

		Map<String, Object> payload = dataApi.updateEntry(entry, module, existingEntry);

		payload.put("DATA_ID", payload.get("_id").toString());
		payload.remove("_id");

		dataService.addToOCRQueue(payload, module);
		if (!isTrigger) {
			WorkflowPayload workflowPayload = new WorkflowPayload(authManager.getUserDetails().getUserId(), moduleId,
					authManager.getUserDetails().getCompanyId(), payload.get("DATA_ID").toString(), optionalEntry.get(),
					"PUT", new Date());
			dataService.addToWorkflowQueue(workflowPayload);
		}
		if (!isProbe) {
			dataService.addToNotifyQueue(
					new NotificationMessage(module.getModuleId(), companyId, null, payload.get("DATA_ID").toString()));
		}

		return payload;

	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Map<String, Object> updateEntry(Map<String, Object> entry, Module module,
			Map<String, Object> existingEntry) {

		String companyId = authManager.getUserDetails().getCompanyId();

		entry = dataService.formatRelationship(module, entry);

		String collectionName = moduleService.getCollectionName(module.getName(), companyId);
		Map<String, Object> payload = new HashMap<String, Object>();

		payload = dataService.formatDateAndTimeField(entry, module);

		payload = dataService.buildPutEntryPayload(module, entry, existingEntry);

		payload = dataService.updateInternalFields(module, payload);
		payload = dataService.formatDiscussion(module, entry, payload);
		payload = dataService.setInheritanceValue(module, payload);
		payload = dataService.encryptPassword(module, payload);

		if (dataService.requiredFieldsCheckRequired(payload)) {
			validator.requiredFieldsPresent(module, payload);
		}
		// TODO: CHRONOMETER WHEN THE EXISTING VALUE IS PRESENT THE CHRONOMETER BASE
		// VALIDATION THROWS ERROR

		validator.validateUniqueField(module, existingEntry, collectionName, "PUT", companyId);

		validator.validateBaseTypes(module, payload, companyId);

		validator.validateModuleValidations(module, payload, "PUT", authManager.getUserDetails().getRole(), companyId);
		payload = dataService.formatChronometer(module, payload);

		dataService.postTemporalEntry(module, payload);
		Optional<ModuleField> optionalReceiptCaptureField = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equals("Receipt Capture")).findFirst();

		if (optionalReceiptCaptureField.isPresent()) {

			ModuleField receiptCaptureField = optionalReceiptCaptureField.get();
			payload = dataService.formatReceiptCapture(module, payload, receiptCaptureField);
		}
		payload = dataService.setSlas(module, payload, existingEntry, "PUT", companyId);

		List<ModuleField> formulaFields = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equals("Formula")).collect(Collectors.toList());
		for (ModuleField field : formulaFields) {
			String value = dataService.getFormulaFieldValue(module, payload, field, field.getFormula());
			if (NumberUtils.isParsable(value)) {
				payload.put(field.getName(), Float.valueOf(value));
			} else {
				payload.put(field.getName(), value);
			}
		}
		List<ModuleField> listFormulaFields = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equals("List Formula")).collect(Collectors.toList());

		payload = dataService.getListFormulaField(listFormulaFields, entry, payload, module);

		Optional<Map<String, Object>> optionalPreivousCopy = entryRepository.findById(payload.get("_id").toString(),
				collectionName);

		Map<String, Object> previousCopy = optionalPreivousCopy.get();
		Map<String, Object> updatedEntry = entryChangeLogService.addDiscussionMetadataAfterFieldUpdate(previousCopy,
				module, payload, companyId);
		entryRepository.updateEntry(updatedEntry, collectionName);
		relationshipService.postOneToManyEntries(entry, module);

		ManyToManyReceiver receiver = new ManyToManyReceiver(module.getModuleId(), payload.get("_id").toString(),
				companyId, updatedEntry, previousCopy);
		rabbitTemplate.convertAndSend("many-to-many-updates", receiver);

		dataService.addMetadataEntryQueue(new MetadataPayload(companyId, module.getModuleId(),
				payload.get("_id").toString(), authManager.getUserDetails().getUserId(), previousCopy, updatedEntry));

		dataService.updateIntoElasticSearch(module, updatedEntry);

		return updatedEntry;
	}

	@GetMapping("/modules/{module_id}/data/{data_id}")
	@Operation(summary = "Get Module Single Entry", description = "Get a single entry of a module")
	public Map<String, Object> getModuleEntry(
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
			@Parameter(description = "Data Id", required = true) @PathVariable("data_id") String dataId,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "filter_fieldid", required = false) String filterFieldId) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String roleId = authManager.getUserDetails().getRole();

		if (!validator.isValidObjectId(moduleId)) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Module module = optionalModule.get();

		if (!rolesService.isAuthorizedForRecord(roleId, "GET", moduleId)) {
			throw new ForbiddenException("FORBIDDEN");
		}

		if (dataId != null && !ObjectId.isValid(dataId)) {
			throw new BadRequestException("INVALID_ENTRY", null);
		}

		Optional<Map<String, Object>> optionalEntry = entryRepository.findEntryWithAggregationRelationship(module,
				dataId);
		if (optionalEntry.isEmpty()) {
			throw new BadRequestException("INVALID_ENTRY", null);
		}

		Map<String, Object> entry = optionalEntry.get();

		if (module.getName().equalsIgnoreCase("Users")
				&& rolesService.isCustomer(authManager.getUserDetails().getRole())) {
			validator.isAutorizedForUserRecord(entry, companyId);
		}

		if (!module.getName().equalsIgnoreCase("Teams") && !rolesService.isSystemAdmin(roleId)) {
			List<Map<String, Object>> teams = (List<Map<String, Object>>) entry.get("TEAMS");
			List<String> teamIds = new ArrayList<String>();

			teams.forEach(team -> {
				if (team.get("DATA_ID") != null) {
					teamIds.add(team.get("DATA_ID").toString());
				}
			});

			// TODO: Revisit function name
			validator.isAutorizedForRecord(teamIds, companyId, authManager.getUserDetails().getUserId());
		}
		return entry;
	}

	@GetMapping("/modules/{module_id}/data")
	@PageableAsQueryParam
	@Operation(summary = "Get All Entries", description = "Get a single entry of a module")
	public Page<Map<String, Object>> getAllData(
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
			@Parameter(description = "Search", required = false) @RequestParam(value = "search", required = false) String search) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String roleId = authManager.getUserDetails().getRole();

		if (!validator.isValidObjectId(moduleId)) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Module module = optionalModule.get();

		if (!rolesService.isAuthorizedForRecord(roleId, "GET", moduleId)) {
			throw new ForbiddenException("FORBIDDEN");
		}

		if (rolesService.isCustomer(roleId) && module.getName().equals("Users")) {
			List<String> entryIds = new ArrayList<String>();
			entryIds.add(authManager.getUserDetails().getUserId());
			return entryRepository.findAllPaginatedEntriesByIds(pageable, module, entryIds);
		}

		List<String> entryIdsFromSearch = new ArrayList<String>();
		if (search != null && !search.isBlank()) {
			Set<String> teamIds = dataService.getAllTeamIds();
			entryIdsFromSearch = dataService.getIdsFromGlobalSearch(search, module, teamIds, null);
			return entryRepository.findAllPaginatedEntriesByIds(pageable, module, entryIdsFromSearch);
		}

		return entryRepository.findAllPaginatedEntries(pageable, module);
	}

//	 @GetMapping("/modules/{module_id}/data/layouts/{layout_id}")
//	 @PageableAsQueryParam
//	 @Operation(summary = "Get Module List Layout Entries", description = "Get all the entries of a list layout")
//	public Page<Map<String, Object>> getListLayoutData(
//			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable,
//			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
//			@Parameter(description = "Layout ID", required = true) @PathVariable("layout_id") String layoutId) {
//
//		String companyId = authManager.getUserDetails().getCompanyId();
//		String roleId = authManager.getUserDetails().getRole();
//
//		if (!validator.isValidObjectId(moduleId)) {
//			throw new BadRequestException("INVALID_MODULE", null);
//		}
//
//		Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
//		if (optionalModule.isEmpty()) {
//			throw new BadRequestException("INVALID_MODULE", null);
//		}
//
//		Module module = optionalModule.get();
//
//		if (!rolesService.isAuthorizedForRecord(roleId, "GET", moduleId)) {
//			throw new ForbiddenException("FORBIDDEN");
//		}
//
//		ListLayout layout = dataService.getListLayout(module, layoutId, roleId);
//
//		boolean useCustomSort = false;
//
//		if (pageable.getSort().isEmpty()) {
//			useCustomSort = true;
//		} else {
//			Order order = pageable.getSort().get().findFirst().orElse(null);
//			if (order == null) {
//				useCustomSort = true;
//			} else {
//				if (order.getProperty() == null || order.getProperty().equalsIgnoreCase("undefined")) {
//					useCustomSort = true;
//				}
//			}
//		}
//
//		if (useCustomSort) {
//			ModuleField sortByField = module.getFields().stream()
//					.filter(field -> field.getFieldId().equals(layout.getOrderBy().getColumn())).findFirst()
//					.orElse(null);
//			if (sortByField != null) {
//				if (layout.getOrderBy().getOrder().equalsIgnoreCase("asc")) {
//					pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
//							Sort.by(sortByField.getName()).ascending());
//				} else {
//					pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
//							Sort.by(sortByField.getName()).descending());
//				}
//			}
//		}
//
//		ListMobileLayout mobileLayout = dataService.getListMobileLayout(module, layoutId, roleId);
//		if (layout == null && mobileLayout == null) {
//			String[] vars = { module.getName() };
//			throw new BadRequestException("LIST_LAYOUT_DOES_NOT_EXIST", vars);
//		}
//
//		boolean isMobileListLayout = false;
//		if (layout == null && mobileLayout != null) {
//			isMobileListLayout = true;
//		}
//
//		List<String> fieldIds = new ArrayList<String>();
//		List<Condition> conditions = new ArrayList<Condition>();
//		if (isMobileListLayout) {
//			fieldIds = mobileLayout.getFields();
//			conditions = mobileLayout.getConditions();
//		} else {
//			fieldIds = layout.getShowColumns().getFields();
//			conditions = layout.getConditions();
//		}
//		long startTime = System.nanoTime();
//
//		Page<Map<String, Object>> data = entryRepository.findAllListLayoutEntries(pageable, module, conditions,
//				fieldIds);
//		long endTime = System.nanoTime();
//
//		long duration = (endTime - startTime);
//		long durationInSeconds = (duration / 1000000000);
//		if (durationInSeconds >= 10) {
//			try {
//				String emailBody = "ModuleID: " + moduleId + " CompanyID: " + companyId + " LayoutID: " + layoutId
//						+ "\n Pageable: " + new ObjectMapper().writeValueAsString(pageable);
//				emailService.notifyShashankAndSpencerOnError(emailBody);
//			} catch (JsonProcessingException e) {
//				e.printStackTrace();
//			}
//		}
//
//		return data;
//	}

	@GetMapping("/modules/{module_id}/data/layouts/{layout_id}/{field_id}/{entry_id}")
	@PageableAsQueryParam
	@Operation(summary = "Get Module List Layout Entries Related", description = "Get all the entries of a list layout which are related to an entry")
	public Page<Map<String, Object>> getListLayoutDataWithEntryId(
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
			@Parameter(description = "Layout ID", required = true) @PathVariable("layout_id") String layoutId,
			@Parameter(description = "Field Id", required = true) @PathVariable("field_id") String fieldId,
			@Parameter(description = "Entry ID", required = true) @PathVariable("entry_id") String entryId) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String roleId = authManager.getUserDetails().getRole();

		if (!validator.isValidObjectId(moduleId)) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Module module = optionalModule.get();

		if (!rolesService.isAuthorizedForRecord(roleId, "GET", moduleId)) {
			throw new ForbiddenException("FORBIDDEN");
		}

		ListLayout layout = dataService.getListLayout(module, layoutId, roleId);

		boolean useCustomSort = false;

		if (pageable.getSort().isEmpty()) {
			useCustomSort = true;
		} else {
			Order order = pageable.getSort().get().findFirst().orElse(null);
			if (order == null) {
				useCustomSort = true;
			} else {
				if (order.getProperty() == null || order.getProperty().equalsIgnoreCase("undefined")) {
					useCustomSort = true;
				}
			}
		}

		if (useCustomSort) {
			ModuleField sortByField = module.getFields().stream()
					.filter(field -> field.getFieldId().equals(layout.getOrderBy().getColumn())).findFirst()
					.orElse(null);
			if (sortByField != null) {
				if (layout.getOrderBy().getOrder().equalsIgnoreCase("asc")) {
					pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
							Sort.by(sortByField.getName()).ascending());

				} else {
					pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
							Sort.by(sortByField.getName()).descending());
				}
			}
		}

		ListMobileLayout mobileLayout = dataService.getListMobileLayout(module, layoutId, roleId);
		if (layout == null && mobileLayout == null) {
			String[] vars = { module.getName() };
			throw new BadRequestException("LIST_LAYOUT_DOES_NOT_EXIST", vars);
		}

		boolean isMobileListLayout = false;
		if (layout == null && mobileLayout != null) {
			isMobileListLayout = true;
		}

		List<String> fieldIds = new ArrayList<String>();
		List<Condition> conditions = new ArrayList<Condition>();
		Condition filter = new Condition();
		filter.setCondition(fieldId);
		filter.setConditionValue(entryId);
		filter.setOpearator("EQUALS_TO");
		filter.setRequirementType("All");

		if (isMobileListLayout) {
			fieldIds = mobileLayout.getFields();
			conditions = mobileLayout.getConditions();
		} else {
			fieldIds = layout.getShowColumns().getFields();
		}
		conditions.add(filter);
		return entryRepository.findAllListLayoutEntries(pageable, module, conditions, fieldIds);
	}

	@GetMapping("/modules/{module_id}/relationship/data")
	@PageableAsQueryParam
	public Page<Map<String, Object>> getRelationshipData(
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
			@Parameter(description = "Field of Data Type 'Relationship'", required = true) @RequestParam("field_id") String fieldId,
			@Parameter(description = "Search parameter", required = false, example = "'SUBJECT=TEST~~STATUS=New' or 'Test'") @RequestParam(value = "search", required = false) String search) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String roleId = authManager.getUserDetails().getRole();

		if (!validator.isValidObjectId(moduleId)) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		if (!rolesService.isAuthorizedForRecord(roleId, "GET", moduleId)) {
			throw new ForbiddenException("FORBIDDEN");
		}

		Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Module module = optionalModule.get();

		Optional<ModuleField> optionalField = dataService.getFieldByFieldId(module, fieldId);
		if (optionalField.isEmpty()) {
			String[] vars = { module.getName() };
			throw new BadRequestException("RELATIONSHIP_FIELD_INVALID", vars);
		}

		ModuleField field = optionalField.get();
		if (!field.getDataType().getDisplay().equals("Relationship")) {
			throw new BadRequestException("NOT_RELATIONSHIP_FIELD", null);
		}

		Optional<Module> optionalRelationshipModule = modulesRepository.findById(field.getModule(),
				"modules_" + companyId);

		if (optionalRelationshipModule.isEmpty()) {
			String[] vars = { field.getName() };
			throw new BadRequestException("RELATIONSHIP_MODULE_INVALID", vars);
		}
		List<String> entryIds = null;
		if (search != null && !search.isBlank()) {
			Set<String> teamIds = dataService.getAllTeamIds();

			List<Condition> conditions = new ArrayList<Condition>();
			if (field.getRelationshipField() != null) {
				Module relatedModule = optionalRelationshipModule.get();
				Optional<ModuleField> optionalRelationshipField = relatedModule.getFields().stream()
						.filter(moduleField -> moduleField.getFieldId().equals(field.getRelationshipField()))
						.findFirst();
				if (optionalRelationshipField.isPresent()) {
					ModuleField relatedField = optionalRelationshipField.get();
					if (relatedField.getDataFilter() != null) {
						conditions = relatedField.getDataFilter().getConditions();
					}
				}
			}

			entryIds = dataService.getIdsFromGlobalSearch(search, optionalRelationshipModule.get(), teamIds,
					conditions);
		}
		return entryRepository.findAllRelationshipEntries(pageable, optionalRelationshipModule.get(), field, entryIds);
	}

	@Transactional
	@DeleteMapping("/modules/{module_id}/data")
	public DeleteData deleteData(
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
			@Parameter(description = "Entry Ids", required = true) @RequestParam(value = "entry_ids", required = true) List<String> entryIds,
			@Parameter(description = "Is Trigger", required = false) @RequestParam(value = "is_trigger", required = false) boolean isTrigger) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String roleId = authManager.getUserDetails().getRole();

		if (!validator.isValidObjectId(moduleId)) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Module module = optionalModule.get();
		String collectionName = moduleService.getCollectionName(module.getName(),
				authManager.getUserDetails().getCompanyId());

		if (!rolesService.isAuthorizedForRecord(roleId, "DELETE", moduleId)) {
			throw new ForbiddenException("FORBIDDEN");
		}

		validator.validateEntryIds(entryIds, collectionName);

		if (module.getName().equalsIgnoreCase("Teams")) {
			validator.validateDefaultTeams(entryIds, companyId);
		}

		if (module.getName().equalsIgnoreCase("Users")) {
			validator.validateRestrictedUsers(entryIds, companyId);
			validator.validateCurrentUser(entryIds, authManager.getUserDetails().getUserId());
		}

		if (module.getName().equalsIgnoreCase("Contacts")) {
			validator.validateRestrictedContacts(entryIds);
			validator.validateCurrentContacts(entryIds);
		}

		for (String dataId : entryIds) {
			deleteDataService.deleteData(module, dataId, authManager.getUserDetails().getUserId(),
					authManager.getUserDetails().getCompanyId());

			dataService.addMetadataEntryQueue(new MetadataPayload(companyId, module.getModuleId(), dataId,
					authManager.getUserDetails().getUserId(), null, null));

		}
		dataService.addToDeleteEntryQueue(new DeleteEntriesPayload(authManager.getUserDetails().getCompanyId(),
				moduleId, entryIds, authManager.getUserDetails().getUserId()));

		dataService.addToNotifyQueue(new NotificationMessage(module.getModuleId(), companyId, null, null));
		DeleteData deleteData = new DeleteData(entryIds);
		return deleteData;
	}

	@Transactional
	@PutMapping("modules/{module_id}/data/bulk_update")
	@Operation(summary = "Bulk Update Module Entries", description = "Update multiple entries at once")
	public List<Map<String, Object>> bulkUpdate(@Valid @RequestBody BulkUpdate update,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String roleId = authManager.getUserDetails().getRole();

		if (!validator.isValidObjectId(moduleId)) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Module module = optionalModule.get();
		String collectionName = moduleService.getCollectionName(module.getName(),
				authManager.getUserDetails().getCompanyId());

		if (!rolesService.isAuthorizedForRecord(roleId, "PUT", moduleId)) {
			throw new ForbiddenException("FORBIDDEN");
		}

		bulkUpdateService.checkAuthorityForAllEntries(update.getEntryIds(), update.getUpdate(), moduleId);

		validator.validateEntryIds(update.getEntryIds(), collectionName);
		if (module.getName().equalsIgnoreCase("Teams")) {
			validator.validateDefaultTeams(update.getEntryIds(), companyId);
		}

		if (module.getName().equalsIgnoreCase("Users")) {
			validator.validateRestrictedUsers(update.getEntryIds(), companyId);
		}

		Map<String, Map<String, Object>> existingEntriesMap = bulkUpdateService.existingEntriesMap(module,
				update.getEntryIds());

		// ONLY UPDATES THE ENTRY ON MONGO
		List<Map<String, Object>> payloadMap = bulkUpdateService.updateEntries(update.getEntryIds(), update.getUpdate(),
				module);

		// IF EVERYTHING IS SUCCESS THEN UPDATE ELASTIC, NOTIFY FRONTEND, START
		// WORKFLOWS
		payloadMap = bulkUpdateService.updateToElasticNotifyAndStartWorkflows(payloadMap, module, existingEntriesMap);

		return payloadMap;
	}

	@Transactional
	@PostMapping("/modules/{module_id}/data/merge")
	@Operation(summary = "Merge Module Entries", description = "Merge multiple entries")
	public Map<String, Object> mergeEntries(@Valid @RequestBody Merge merge,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String roleId = authManager.getUserDetails().getRole();

		if (!validator.isValidObjectId(moduleId)) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Module module = optionalModule.get();

		ModuleField discussionField = mergeService.getDiscussionField(module);

		if (discussionField == null) {
			String[] vars = { module.getName() };
			throw new BadRequestException("DISCUSSION_FIELD_REQUIRED_FOR_MERGE", vars);
		}

		if (!rolesService.isAuthorizedForRecord(roleId, "POST", moduleId)) {
			throw new ForbiddenException("FORBIDDEN");
		}

		if (merge.getEntryId() == null || !validator.isValidObjectId(merge.getEntryId())) {
			throw new BadRequestException("INVALID_ENTRY", null);
		}

		String collectionName = moduleService.getCollectionName(module.getName(), companyId);
		Optional<Map<String, Object>> optionalEntry = entryRepository.findById(merge.getEntryId(), collectionName);

		if (optionalEntry.isEmpty()) {
			throw new BadRequestException("INVALID_ENTRY", null);
		}

		Map<String, Object> entry = optionalEntry.get();

		validator.validateEntryIds(merge.getMergeEntryIds(), collectionName);

		List<DiscussionMessage> messages = mergeService.getDiscussionMessage(discussionField, entry);
		List<DiscussionMessage> mergedMessages = mergeService.mergeDiscussionMessages(merge.getMergeEntryIds(), module,
				discussionField);

		messages.addAll(mergedMessages);
		messages = mergeService.sortDiscussion(messages);
		messages.add(mergeService.mergeMetaData(module));
		entry.put(discussionField.getName(), messages);
		entry.put("DATE_UPDATED", new Date());
		entry.put("LAST_UPDATED_BY", authManager.getUserDetails().getUserId());

		entryRepository.updateEntry(entry, collectionName);
		dataService.updateIntoElasticSearch(module, entry);

		// DELETE ENTRIES WHICH ARE MERGED
//		dataApi.deleteData(module.getModuleId(), merge.getMergeEntryIds(), false, true);
		merge.getMergeEntryIds().forEach(dataId -> {
			deleteDataService.deleteData(module, dataId, authManager.getUserDetails().getUserId(),
					authManager.getUserDetails().getCompanyId());
		});

		dataService.addToDeleteEntryQueue(new DeleteEntriesPayload(authManager.getUserDetails().getCompanyId(),
				moduleId, merge.getMergeEntryIds(), authManager.getUserDetails().getUserId()));

		String dataId = entry.remove("_id").toString();
		entry.put("DATA_ID", dataId);

		dataService.addToNotifyQueue(new NotificationMessage(module.getModuleId(), companyId, null, dataId));

		return entry;
	}

	@GetMapping("/role_layouts/{role_layout_id}/module/{module_id}/data")
	@PageableAsQueryParam
	public Page<Map<String, Object>> getRoleLayoutData(
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable,
			@Parameter(description = "Role layout id", required = true) @PathVariable("role_layout_id") String roleLayoutId,
			@Parameter(description = "Module id in the role layout", required = true) @PathVariable("module_id") String moduleId) {
		String[] vars = {};

		Optional<RoleLayout> optionalRoleLayout = roleLayoutRepository.findByIdAndCompanyId(roleLayoutId,
				authManager.getUserDetails().getCompanyId(), "role_layouts");

		if (optionalRoleLayout.isEmpty()) {
			throw new BadRequestException("ROLE_LAYOUT_INVALID", vars);
		}

		RoleLayout roleLayout = optionalRoleLayout.get();
		LayoutModule layoutForModule = roleLayout.getModules().stream()
				.filter(module -> module.getModule().equals(moduleId)).findAny().orElse(null);

		if (layoutForModule == null) {
			throw new BadRequestException("ROLE_LAYOUT_MODULE_INVALID", vars);
		}

		Optional<Module> optionalModule = modulesRepository.findById(moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());

		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE", vars);
		}

		Module module = optionalModule.get();

		return entryRepository.findAllListLayoutEntries(pageable, module, layoutForModule.getLayout().getConditions(),
				layoutForModule.getLayout().getColumnsShow());

	}

	@PutMapping("/modules/{module_id}/data/{data_id}/unlink/many_to_one")
	@Operation(summary = "Put Many to One Entry", description = "Unlinks Many to One Entry")
	public void putManyToOneEntry(
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
			@Parameter(description = "Data ID", required = true) @PathVariable("data_id") String dataId,
			@Parameter(description = "Field Id", required = true) @RequestParam("field_id") String fieldId) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String roleId = authManager.getUserDetails().getRole();

		if (!validator.isValidObjectId(moduleId)) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Module module = optionalModule.get();
		String collectionName = moduleService.getCollectionName(module.getName(), companyId);

		Optional<Map<String, Object>> optionalEntry = entryRepository.findById(dataId, collectionName);

		if (optionalEntry.isEmpty()) {
			throw new BadRequestException("INVALID_ENTRY", null);
		}

		if (!rolesService.isAuthorizedForRecord(roleId, "PUT", moduleId)) {
			throw new ForbiddenException("FORBIDDEN");
		}

		Map<String, Object> existingEntry = optionalEntry.get();

		relationshipService.updateManyToOneEntry(module, existingEntry, fieldId);

	}

	@PostMapping("/formula/{module_id}")
	@Operation(summary = "Post formula field value", description = "Post formula field value for a entry")
	public Map<String, Object> generateFormulaFieldValue(@RequestBody HashMap<String, Object> entry,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId) {

		String companyId = authManager.getUserDetails().getCompanyId();
		if (!validator.isValidObjectId(moduleId)) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Module module = optionalModule.get();

		List<ModuleField> formulaFields = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equals("Formula")).collect(Collectors.toList());

		Map<String, Object> formattedEntry = dataService.formatRelationship(module, entry);
		formattedEntry = dataService.formatPayload(module, formattedEntry);
		Map<String, Object> payload = new HashMap<String, Object>();
		String value = "";

		for (ModuleField formulaField : formulaFields) {
			value = dataService.getFormulaFieldValue(module, formattedEntry, formulaField, formulaField.getFormula());
			payload.put(formulaField.getName(), value);
		}

		ObjectMapper mapper = new ObjectMapper();

		List<ModuleField> listFormulaFields = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equals("List Formula")).collect(Collectors.toList());

		for (ModuleField listFormulaField : listFormulaFields) {
			boolean isPresent = false;
			List<ListFormulaFieldValue> finalListFormulaFieldValues = new ArrayList<ListFormulaFieldValue>();
			List<ListFormulaFieldValue> listFormulaFieldValues = new ArrayList<ListFormulaFieldValue>();
			try {
				listFormulaFieldValues = mapper.readValue(
						mapper.writeValueAsString(entry.get(listFormulaField.getName())),
						mapper.getTypeFactory().constructCollectionType(List.class, ListFormulaFieldValue.class));
			} catch (Exception e) {
				e.printStackTrace();

			}
			if (listFormulaFieldValues != null) {
				for (ListFormulaFieldValue listFormulaFieldValue : listFormulaFieldValues) {
					if (listFormulaFieldValue.getFormulaName() != null
							&& !listFormulaFieldValue.getFormulaName().isEmpty()) {
						isPresent = true;
						List<ListFormulaField> listFormulas = listFormulaField.getListFormula();
						ListFormulaField listFormula = listFormulas.stream()
								.filter(lFormula -> lFormula.getFormulaName()
										.equalsIgnoreCase(listFormulaFieldValue.getFormulaName()))
								.findAny().orElse(null);
						if (listFormula == null) {
							String[] vars = { listFormulaFieldValue.getFormulaName() };
							throw new BadRequestException("FORMULA_NAME_INVALID", vars);
						} else {
							String formula = listFormula.getFormula();
							value = dataService.getFormulaFieldValue(module, formattedEntry, listFormulaField, formula);
							listFormulaFieldValue.setValue(value);
							if (NumberUtils.isParsable(value)) {
								listFormulaFieldValue.setValue(Double.valueOf(value));
							}
							finalListFormulaFieldValues.add(listFormulaFieldValue);
						}
					}
				}
				if (isPresent) {
					payload.put(listFormulaField.getName(), finalListFormulaFieldValues);
				}
			}
		}
		return payload;
	}
}
