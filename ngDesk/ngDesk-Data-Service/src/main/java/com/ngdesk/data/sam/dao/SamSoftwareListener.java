package com.ngdesk.data.sam.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.data.dao.DataService;
import com.ngdesk.data.dao.RelationshipService;
import com.ngdesk.data.dao.SamPayload;
import com.ngdesk.data.dao.WorkflowPayload;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.data.roles.dao.RolesService;
import com.ngdesk.data.validator.Validator;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;

@Component
@RabbitListener(queues = "new-sam-entry", concurrency = "5")
public class SamSoftwareListener {

	private final Logger log = LoggerFactory.getLogger(SamSoftwareListener.class);

	@Autowired
	Validator validator;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	RolesService rolesService;

	@Autowired
	ModuleService moduleService;

	@Autowired
	DataService dataService;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	RelationshipService relationshipService;

	@Autowired
	DataProxy dataProxy;

	@RabbitHandler
	public void postSamEntries(SamPayload samPayload) {
		try {

			log.debug("Received Sam Payload");

			String moduleId = samPayload.getModuleId();
			String companyId = samPayload.getCompanyId();
			String userId = samPayload.getUserId();
			String roleId = samPayload.getRoleId();
			String userUuid = samPayload.getUserUuid();

			Map<String, Object> entryToPost = samPayload.getEntry();

			Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);

			List<Module> moduleFamily = moduleService.getModuleFamily(moduleId, samPayload.getCompanyId());

			Optional<List<Module>> optionalModules = modulesRepository.findAllModules("modules_" + companyId);
			List<Module> allModules = optionalModules.get();

			Module accountsModule = allModules.stream().filter(module -> module.getName().equals("Accounts"))
					.findFirst().orElse(null);
			Module productsModule = allModules.stream().filter(module -> module.getName().equals("Software Products"))
					.findFirst().orElse(null);
			Module standardizedSoftwareInstallationModule = allModules.stream()
					.filter(module -> module.getName().equals("Standardized Software Installation")).findFirst()
					.orElse(null);

			if (optionalModule.get().getName().equals("Software Installation")) {

				Map<String, Object> account = new HashMap<String, Object>();
				account.put("ACCOUNT_NAME", entryToPost.get("PUBLISHER").toString());

				account = saveEntry(account, accountsModule.getModuleId(),
						moduleService.getModuleFamily(accountsModule.getModuleId(), companyId), companyId, userId,
						roleId, userUuid);
				String accountId = account.get("DATA_ID").toString();

				Map<String, Object> product = new HashMap<String, Object>();
				product.put("NAME", entryToPost.get("DISPLAY_NAME").toString());
				product.put("ACCOUNT", accountId);

				product = saveEntry(product, productsModule.getModuleId(),
						moduleService.getModuleFamily(productsModule.getModuleId(), companyId), companyId, userId,
						roleId, userUuid);
				String productId = product.get("DATA_ID").toString();

				Map<String, Object> standardizedSoftwareInstallation = new HashMap<String, Object>();

				String displayName = entryToPost.get("PUBLISHER").toString() + " "
						+ entryToPost.get("DISPLAY_NAME").toString() + " " + entryToPost.get("VERSION").toString();

				standardizedSoftwareInstallation.put("DISCOVERED_PUBLISHER", entryToPost.get("PUBLISHER").toString());
				standardizedSoftwareInstallation.put("DISCOVERED_PRODUCT", productId);
				standardizedSoftwareInstallation.put("DISCOVERED_VERSION", entryToPost.get("VERSION").toString());
				standardizedSoftwareInstallation.put("NORMALIZATION_STATUS", "New");
				standardizedSoftwareInstallation.put("DISPLAY_NAME", displayName);
				standardizedSoftwareInstallation.put("PLATFORM", entryToPost.get("PLATFORM").toString());

				standardizedSoftwareInstallation = saveEntry(standardizedSoftwareInstallation,
						standardizedSoftwareInstallationModule.getModuleId(),
						moduleService.getModuleFamily(standardizedSoftwareInstallationModule.getModuleId(), companyId),
						companyId, userId, roleId, userUuid);

				String standardizedSoftwareInstallationId = standardizedSoftwareInstallation.get("DATA_ID").toString();
				entryToPost.put("STANDARDIZED_SOFTWARE_INSTALLATION", standardizedSoftwareInstallationId);

			}
			Map<String, Object> payload = saveEntry(entryToPost, moduleId, moduleFamily, companyId, userId, roleId,
					userUuid);
			WorkflowPayload workflowPayload = new WorkflowPayload(samPayload.getUserId(), moduleId,
					samPayload.getCompanyId(), payload.get("DATA_ID").toString(), new HashMap<String, Object>(),
					"POST", new Date());
			dataService.addToWorkflowQueue(workflowPayload);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Map<String, Object> saveEntry(Map<String, Object> entry, String moduleId, List<Module> moduleFamily,
			String companyId, String userId, String roleId, String userUuid) {
		try {

			Map<String, String> moduleEntryMap = new HashMap<String, String>();

			Map<String, Object> payload = new HashMap<String, Object>();
			Map<String, Map<String, Object>> payloadMap = new HashMap<String, Map<String, Object>>();
			for (Module module : moduleFamily) {
				String collectionName = module.getName().replaceAll("\\s+", "_") + "_" + companyId;

				if (module.getAlternatePrimaryKeys() != null && module.getAlternatePrimaryKeys().size() > 0) {

					List<ModuleField> alternatePrimaryFields = new ArrayList<ModuleField>();

					module.getAlternatePrimaryKeys().forEach(primaryField -> {
						ModuleField alternatePrimaryField = module.getFields().stream()
								.filter(field -> field.getFieldId().equals(primaryField)).findFirst().orElse(null);
						if (alternatePrimaryField != null) {
							alternatePrimaryFields.add(alternatePrimaryField);
						}
					});

					Map<String, Object> keyValuePairs = new HashMap<String, Object>();
					for (ModuleField alternatePrimaryField : alternatePrimaryFields) {
						String alternatePrimaryFieldName = alternatePrimaryField.getName();
						if (entry.get(alternatePrimaryFieldName) != null) {
							keyValuePairs.put(alternatePrimaryFieldName, entry.get(alternatePrimaryFieldName));
						}
					}

					if (keyValuePairs.size() > 0) {
						Optional<Map<String, Object>> optionalEntry = entryRepository
								.findEntryByAlternatePrimaryKeys(keyValuePairs, collectionName);

						if (optionalEntry.isPresent()) {
							HashMap<String, Object> copy = new HashMap<String, Object>(entry);
							copy.put("DATA_ID", optionalEntry.get().get("_id").toString());

							return dataProxy.putModuleEntry(copy, module.getModuleId(), false, companyId, userUuid,
									true);
						}
					}
				}

				entry = dataService.formatRelationship(module, entry);
				payload = dataService.buildEntryPayload(module, entry);
				payload = dataService.addFieldsWithDefaultValue(moduleService.getAllFields(module, companyId), payload);
				payload = dataService.addInternalFields(module, payload, userId, companyId);

				// TODO: FIX THIS
				entry.put("_id", payload.get("_id"));

				payload = dataService.addAutoNumberFields(module, payload);
				payload = dataService.setInheritanceValue(module, payload);
				payload = dataService.formatPayload(module, payload);
				payload = dataService.setInheritanceValue(module, payload);
				payload = dataService.formatDiscussion(module, entry, payload);

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
				validator.validateModuleValidations(module, payload, "POST", roleId, companyId);

				payload = dataService.setSlas(module, payload, new HashMap<String, Object>(), "POST", companyId);
				entryRepository.save(payload, collectionName);

				payloadMap.put(module.getModuleId(), payload);

			}
			dataService.insertIntoElasticSearch(payloadMap, moduleFamily, companyId);
			payload.put("DATA_ID", payload.get("_id").toString());
			payload.remove("_id");
			return payload;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
