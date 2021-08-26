package com.ngdesk.data.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;

@Component
@RabbitListener(queues = "many-to-many-updates", concurrency = "25")
public class ManyToManyHandler {

	@Autowired
	ModulesRepository modulesRepository;
	
	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	ModuleService moduleService;

	@RabbitHandler
	public void run(ManyToManyReceiver receiver) {

		Map<String, Object> entry = receiver.getEntry();
		Map<String, Object> existingEntry = receiver.getExistingEntry();
		String companyId = receiver.getCompanyId();
		String currentModuleId = receiver.getModuleId();

		List<Module> modules = modulesRepository.getAllModules("modules_" + companyId);

		try {

			Optional<Module> optionalModule = modulesRepository.findById(currentModuleId, "modules_" + companyId);
			
			if (optionalModule.isPresent()) {
				
				Module currentModule = optionalModule.get();
				
				
				String entryId = receiver.getEntryId();
				List<ModuleField> manyToManyFields = currentModule.getFields().stream()
						.filter(field -> field.getDataType().getDisplay().equals("Relationship")
								&& field.getRelationshipType().equalsIgnoreCase("Many to Many"))
						.collect(Collectors.toList());

				Map<String, List<String>> modulesEntriesToBeUpdated = new HashMap<String, List<String>>();

				for (ModuleField manyToManyField : manyToManyFields) {
					
					try {
						String fieldName = manyToManyField.getName();

						Module relatedModule = null;
						String relatedModuleId = manyToManyField.getModule();
						String relatedFieldId = manyToManyField.getRelationshipField();

						if (relatedModuleId == null || relatedFieldId == null || relatedModuleId.isBlank()
								|| relatedFieldId.isBlank()) {
							continue;
						}

						relatedModule = modules.stream().filter(module -> module.getModuleId().equals(relatedModuleId))
								.findFirst().orElse(null);

						if (relatedModule == null) {
							continue;
						}

						ModuleField relatedModuleField = relatedModule.getFields().stream()
								.filter(field -> field.getFieldId().equals(relatedFieldId)).findFirst().orElse(null);

						if (relatedModuleField == null) {
							continue;
						}
						
						
						String collectionName = moduleService.getCollectionName(relatedModule.getName(), companyId);
						// CURRENTLY NULL
						if (entry.get(fieldName) == null) {
							// CHECK IF PREVIOUS COPY HAS FIELD
							if (existingEntry.get(fieldName) != null) {
								List<String> relatedEntryIds = (List<String>) existingEntry.get(fieldName);
								for (String id: relatedEntryIds) {
									entryRepository.pull(relatedModuleField.getName(), id, collectionName, entryId);
								}
							}
						} else {
							List<String> relatedEntryIds = (List<String>) entry.get(fieldName);
							if (existingEntry.get(fieldName) != null) {
								List<String> previousRelatedEntryIds = (List<String>) existingEntry.get(fieldName);
								for (String id: relatedEntryIds) {
									if (!previousRelatedEntryIds.contains(id)) {
										entryRepository.push(relatedModuleField.getName(), id, collectionName, entryId);
									} 
								}
								for (String id: previousRelatedEntryIds) {
									if (!relatedEntryIds.contains(id)) {
										entryRepository.pull(relatedModuleField.getName(), id, collectionName, entryId);
									} 
								}
							} else {
								for (String id: relatedEntryIds) {
									entryRepository.push(relatedModuleField.getName(), id, collectionName, entryId);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
