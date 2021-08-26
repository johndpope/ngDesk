package com.ngdesk.data.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.data.roles.dao.RolesService;
import com.ngdesk.data.validator.Validator;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;

@Component
public class BulkUpdateService {

	@Autowired
	AuthManager authManager;

	@Autowired
	RolesService rolesService;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	DataService dataService;

	@Autowired
	Validator validator;

	@Autowired
	ModuleService moduleService;

	@Autowired
	EntryChangeLogService entryChangeLogService;

	@Autowired
	RolesFieldPermissionsService rolesFieldPermissionsService;

	// BULK UPDATE FUNCTIONS

	public boolean checkAuthorityForAllEntries(List<String> entryIds, Map<String, Object> entry, String moduleId) {
		for (String entryId : entryIds) {
			entry.put("DATA_ID", entryId);
			rolesFieldPermissionsService.isAuthorized(authManager.getUserDetails().getRole(), moduleId, entry, "PUT");
		}
		return true;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Map<String, Object>> updateEntries(List<String> entryIds, Map<String, Object> update, Module module) {

		String collectionName = moduleService.getCollectionName(module.getName(),
				authManager.getUserDetails().getCompanyId());
		List<Map<String, Object>> payloadMap = new ArrayList<Map<String, Object>>();

		for (String entryId : entryIds) {
			update.put("_id", entryId);

			Optional<Map<String, Object>> optionalEntry = entryRepository.findById(entryId, collectionName);
			Map<String, Object> existingEntry = new HashMap<String, Object>();
			existingEntry.putAll(optionalEntry.get());

			Map<String, Object> updatedEntry = updateEntry(update, module, existingEntry);
			payloadMap.add(updatedEntry);
		}
		return payloadMap;
	}

	public Map<String, Object> updateEntry(Map<String, Object> entry, Module module,
			Map<String, Object> existingEntry) {

		String companyId = authManager.getUserDetails().getCompanyId();
		entry = dataService.formatRelationship(module, entry);
		String collectionName = moduleService.getCollectionName(module.getName(), companyId);
		Map<String, Object> payload = new HashMap<String, Object>();

		payload = dataService.buildPutEntryPayload(module, entry, existingEntry);

		payload = dataService.updateInternalFields(module, payload);
		payload = dataService.formatDiscussion(module, entry, payload);
		payload = dataService.setInheritanceValue(module, payload);
		if (dataService.requiredFieldsCheckRequired(payload)) {
			validator.requiredFieldsPresent(module, payload);
		}

		validator.validateUniqueField(module, entry, collectionName, "PUT", companyId);
		payload = dataService.formatDateAndTimeField(payload, module);
		validator.validateBaseTypes(module, payload, companyId);

		validator.validateModuleValidations(module, payload, "PUT", authManager.getUserDetails().getRole(), companyId);

		payload = dataService.formatChronometer(module, payload);

		dataService.postTemporalEntry(module, payload);

		payload = dataService.setSlas(module, payload, existingEntry, "PUT", companyId);

		Optional<Map<String, Object>> optionalPreivousCopy = entryRepository.findById(payload.get("_id").toString(),
				collectionName);
		Map<String, Object> previousCopy = optionalPreivousCopy.get();

		Map<String, Object> updatedEntry = entryChangeLogService.addDiscussionMetadataAfterFieldUpdate(previousCopy,
				module, payload, companyId);
		entryRepository.updateEntry(updatedEntry, collectionName);

		return updatedEntry;
	}

	public List<Map<String, Object>> updateToElasticNotifyAndStartWorkflows(List<Map<String, Object>> payloadMap,
			Module module, Map<String, Map<String, Object>> existingEntriesMap) {
		for (Map<String, Object> payload : payloadMap) {

			dataService.updateIntoElasticSearch(module, payload);

			payload.put("DATA_ID", payload.get("_id").toString());
			payload.remove("_id");
			WorkflowPayload workflowPayload = new WorkflowPayload(authManager.getUserDetails().getUserId(),
					module.getModuleId(), authManager.getUserDetails().getCompanyId(),
					payload.get("DATA_ID").toString(), existingEntriesMap.get(payload.get("DATA_ID")), "PUT",
					new Date());
			dataService.addToWorkflowQueue(workflowPayload);
			dataService.addToNotifyQueue(new NotificationMessage(module.getModuleId(),
					authManager.getUserDetails().getCompanyId(), null, payload.get("DATA_ID").toString()));

		}
		return payloadMap;
	}

	public List<Map<String, Object>> convertDataIdToId(List<Map<String, Object>> entries) {
		entries.forEach(entry -> {
			String dataId = entry.remove("DATA_ID").toString();
			entry.put("_id", dataId);
		});

		return entries;
	}

	public Map<String, Map<String, Object>> existingEntriesMap(Module module, List<String> entryIds) {
		Map<String, Map<String, Object>> existingEntryMap = new HashMap<String, Map<String, Object>>();

		for (String entryId : entryIds) {
			String collectionName = moduleService.getCollectionName(module.getName(),
					authManager.getUserDetails().getCompanyId());

			Optional<Map<String, Object>> optionalEntry = entryRepository.findById(entryId, collectionName);

			if (!optionalEntry.isEmpty()) {
				existingEntryMap.put(entryId, optionalEntry.get());
			}
		}
		return existingEntryMap;

	}

}
