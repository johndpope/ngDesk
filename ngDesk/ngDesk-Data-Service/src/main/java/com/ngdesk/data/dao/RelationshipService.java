package com.ngdesk.data.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.data.validator.Validator;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;

@Component
public class RelationshipService {

	@Autowired
	DataService dataService;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	ModuleService moduleService;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	AuthManager manager;

	@Autowired
	Validator validator;

	@Autowired
	RelationshipService relationshipService;

	public void postOneToManyEntries(Map<String, Object> entry, Module module) {
		String companyId = manager.getUserDetails().getCompanyId();

		List<ModuleField> oneToManyFields = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equals("Relationship")
						&& field.getRelationshipType().equalsIgnoreCase("One to Many"))
				.collect(Collectors.toList());

		oneToManyFields.forEach(field -> {
			String fieldName = field.getName();

			if (entry.get(fieldName) != null) {

				Optional<Module> optionalModule = modulesRepository.findById(field.getModule(), "modules_" + companyId);

				Module relatedModule = optionalModule.get();
				ModuleField relatedField = relatedModule.getFields().stream()
						.filter(moduleField -> moduleField.getFieldId().equals(field.getRelationshipField()))
						.findFirst().orElse(null);

				List<Map<String, Object>> entries = (List<Map<String, Object>>) entry.get(fieldName);

				List<Map<String, Object>> existingEntries = entries.stream()
						.filter(existingEntry -> existingEntry.get("DATA_ID") != null
								&& ObjectId.isValid(existingEntry.get("DATA_ID").toString()))
						.collect(Collectors.toList());

				String collectionName = moduleService.getCollectionName(relatedModule.getName(), companyId);
				existingEntries.forEach(existingEntry -> {
					Optional<Map<String, Object>> optionalRelatedEntry = entryRepository
							.findEntryById(existingEntry.get("DATA_ID").toString(), collectionName);
					if (optionalRelatedEntry.isPresent()) {
						Map<String, Object> relatedEntry = optionalRelatedEntry.get();
						relatedEntry.put(relatedField.getName(), entry.get("_id").toString());
						entryRepository.updateEntry(relatedEntry, collectionName);
					}
				});

				List<Map<String, Object>> newEntries = entries.stream()
						.filter(existingEntry -> existingEntry.get("DATA_ID") == null).collect(Collectors.toList());

				newEntries.forEach(newEntry -> {
					Map<String, Object> payload = new HashMap<String, Object>();

					// TODO: HANDLE INHERITANCE

					newEntry = dataService.formatRelationship(relatedModule, newEntry);

					payload = dataService.formatDateAndTimeField(newEntry, relatedModule);
					payload = dataService.buildEntryPayload(relatedModule, newEntry);
					payload = dataService
							.addFieldsWithDefaultValue(moduleService.getAllFields(relatedModule, companyId), payload);
					payload = dataService.addInternalFields(relatedModule, payload,
							manager.getUserDetails().getUserId(), companyId);
					newEntry.put("_id", payload.get("_id"));

					payload = dataService.addAutoNumberFields(relatedModule, payload);
					payload = dataService.setInheritanceValue(relatedModule, payload);
					payload = dataService.formatPayload(relatedModule, payload);
					payload = dataService.setInheritanceValue(relatedModule, payload);
					payload = dataService.formatDiscussion(relatedModule, newEntry, payload);

					if (dataService.requiredFieldsCheckRequired(payload)) {
						validator.requiredFieldsPresent(relatedModule, payload);
					}

					validator.validateBaseTypes(relatedModule, payload, companyId);
					validator.validateModuleValidations(relatedModule, payload, "POST",
							manager.getUserDetails().getRole(), companyId);
					payload = dataService.setSlas(relatedModule, payload, new HashMap<String, Object>(), "POST",
							companyId);
					payload.put(relatedField.getName(), entry.get("_id").toString());

					Map<String, Map<String, Object>> payloadMap = new HashMap<String, Map<String, Object>>();
					List<Module> moduleFamily = moduleService.getModuleFamily(relatedModule.getModuleId(),
							manager.getUserDetails().getCompanyId());

					entryRepository.save(payload, collectionName);
					payloadMap.put(relatedModule.getModuleId(), payload);

					dataService.insertIntoElasticSearch(payloadMap, moduleFamily, companyId);
					relationshipService.postOneToManyEntries(newEntry, relatedModule);

				});
			}
		});

	}

	public void updateManyToOneEntry(Module module, Map<String, Object> existingEntry, String fieldId) {

		String companyId = manager.getUserDetails().getCompanyId();
		String collectionName = moduleService.getCollectionName(module.getName(), companyId);
		Optional<ModuleField> optionalField = dataService.getFieldByFieldId(module, fieldId);

		if (optionalField.isEmpty()) {

			String[] vars = { module.getName() };
			throw new BadRequestException("RELATIONSHIP_FIELD_INVALID", vars);

		}

		ModuleField field = optionalField.get();

		if (field.getDataType().getDisplay().equals("Relationship")
				&& field.getRelationshipType().equalsIgnoreCase("Many to One")) {

			existingEntry.remove(field.getName());
			entryRepository.updateEntry(existingEntry, collectionName);

		} else {

			throw new BadRequestException("NOT_RELATIONSHIP_FIELD_MANY_TO_ONE", null);

		}
	}

}
