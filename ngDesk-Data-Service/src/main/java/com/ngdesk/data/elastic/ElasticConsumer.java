package com.ngdesk.data.elastic;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.data.dao.DataService;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;

@Component
@RabbitListener(queues = "post-module-data-to-elastic", concurrency = "5")
public class ElasticConsumer {

	@Autowired
	ModulesRepository moduleRepository;

	@Autowired
	DataService dataService;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	ModuleService moduleService;

	@RabbitHandler
	public void postEntriesToElastic(String companyId) {
		
		// FIND ALL MODULES
		Optional<List<Module>> optionalModules = moduleRepository.findAllModules("modules_" + companyId);
		
		if (optionalModules.isEmpty()) {
			return;
		}
		
		List<Module> modules = optionalModules.get();
		
		for (Module module : modules) {
			String collectionName = moduleService.getCollectionName(module.getName(), companyId);
			
			Optional<List<Map<String, Object>>> optionalPayloads = entryRepository.findAllEntries(collectionName);
			if (optionalPayloads.isEmpty()) {
				continue;
			}
			for (Map<String, Object> payload : optionalPayloads.get()) {
				String id = payload.remove("_id").toString();
				payload.put("_id", id);
				dataService.postIntoElastic(module, companyId, payload);
			}
		}
	}

}
