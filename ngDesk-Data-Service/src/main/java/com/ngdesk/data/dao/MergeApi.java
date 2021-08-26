package com.ngdesk.data.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.data.roles.dao.RolesService;
import com.ngdesk.data.validator.Validator;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RefreshScope
public class MergeApi {
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
	MergeService mergeService;

	@Autowired
	DataService dataService;

	@Autowired
	DeleteDataService deleteDataService;

	@PostMapping("/modules/{module_id}/merge")
	@Operation(summary = "Merge Module Entries", description = "Merge multiple entries")
	public Map<String, Object> mergeModuleEntries(@Valid @RequestBody Merge merge,
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

		entry = mergeService.getChronometerFieldsValue(module, merge, entry);
		entry = mergeService.getCurrencyFieldValue(module, merge, entry);
		entry = mergeService.getListTextFieldValue(module, merge, entry);
		entry = mergeService.getRelationshipManyToManyValue(module, merge, entry);
		entry = mergeService.getRelationshipFieldOneToManyValue(module, merge, entry);
		entry = mergeService.getTextAreaFieldsValue(module, merge, entry);
		entry = mergeService.getNumberFieldsValue(module, merge, entry);
		entry = mergeService.getFormulaFieldsValue(module, merge, entry);

		// Merge discussion Field
		if (discussionField != null) {
			List<DiscussionMessage> messages = mergeService.getDiscussionMessage(discussionField, entry);
			List<DiscussionMessage> mergedMessages = mergeService.mergeDiscussionMessages(merge.getMergeEntryIds(),
					module, discussionField);

			messages.addAll(mergedMessages);
			messages = mergeService.sortDiscussion(messages);
			messages.add(mergeService.mergeMetaData(module));
			entry.put(discussionField.getName(), messages);
		}

		entry.put("DATE_UPDATED", new Date());
		entry.put("LAST_UPDATED_BY", authManager.getUserDetails().getUserId());

		entryRepository.updateEntry(entry, collectionName);
		dataService.updateIntoElasticSearch(module, entry);

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

}
