package com.ngdesk.data.elastic;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.data.dao.DataService;
import com.ngdesk.data.modules.dao.DataType;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.roles.dao.Role;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;
import com.ngdesk.repositories.roles.RolesRepository;

@Component
public class Wrapper {

	@Autowired
	DataService dataService;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	ModulesRepository modulesRepository;

	public Map<String, Object> postData(String companyId, Module module, Map<String, Object> body) {
		// RestHighLevelClient elasticClient = null;

		try {
			String collectionName = module.getName().replaceAll("\\s+", "_") + "_" + companyId;

			// elasticClient = new RestHighLevelClient(RestClient.builder(new
			// HttpHost(elasticHost, 9200, "http")));

			Optional<Role> optionalCustomerRole = rolesRepository.findRoleName("Customers", "roles_" + companyId);
			Role customerRole = optionalCustomerRole.get();
			String customerRoleId = customerRole.getId();

			Map<String, Object> entry = new HashMap<String, Object>();
			entry.putAll(body);
			entry.put("DATE_CREATED", new Date());
			entry.put("DATE_UPDATED", new Date());
			entry.put("EFFECTIVE_FROM", new Date());
			if (customerRoleId != null) {
				if ((entry.get("ROLE")) != null && !entry.get("ROLE").equals(customerRoleId)) {
					entry.put("LAST_SEEN", new Date());
				}
			}

			Map<String, Object> updatedEntry = moduleEntryRepository.save(entry, collectionName);
			dataService.postIntoElastic(module, companyId, updatedEntry);
			return updatedEntry;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// elasticClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public void putData(String companyId, Module module, Map<String, Object> body, String dataId) {
		// RestHighLevelClient elasticClient = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			String collectionName = module.getName().replaceAll("\\s+", "_") + "_" + companyId;

			// elasticClient = new RestHighLevelClient(RestClient.builder(new
			// HttpHost(elasticHost, 9200, "http")));

			Map<String, Object> entry = new HashMap<String, Object>();
			entry.putAll(body);

			Optional<Map<String, Object>> optionalExistingEntry = moduleEntryRepository.findById(dataId,
					collectionName);
			Map<String, Object> existingEntry = optionalExistingEntry.get();

			if (module.getName().equals("Users")) {

				// CHECK IF ROLE CHANGED
				if (!entry.get("ROLE").toString().equals(existingEntry.get("ROLE").toString())) {
					// IF CHANGED UPDATE DEFAULT TEAMS

					Optional<Role> optionalExistingRole = rolesRepository.findById(existingEntry.get("ROLE").toString(),
							"roles_" + companyId);
					Optional<Role> optionalNewRole = rolesRepository.findById(entry.get("ROLE").toString(),
							"roles_" + companyId);

					Role existingRole = optionalExistingRole.get();
					Role newRole = optionalNewRole.get();

					String existingRoleName = existingRole.getName();
					String newRoleName = newRole.getName();

					Optional<Map<String, Object>> optionalOldTeam = moduleEntryRepository.findEntryByFieldName("NAME",
							existingRoleName, "Teams_" + companyId);
					Optional<Map<String, Object>> optionalNewTeam = moduleEntryRepository.findEntryByFieldName("NAME",
							newRoleName, "Teams_" + companyId);

					Map<String, Object> oldTeam = optionalOldTeam.get();
					Map<String, Object> newTeam = optionalNewTeam.get();

					String oldTeamId = oldTeam.get("_id").toString();
					String newTeamId = newTeam.get("_id").toString();

					List<String> teams = mapper.readValue(mapper.writeValueAsString(entry.get("TEAMS")),
							mapper.getTypeFactory().constructCollectionType(List.class, String.class));

					teams.remove(oldTeamId);
					teams.add(newTeamId);

					newTeam.put("TEAMS", teams);

					moduleEntryRepository.pullDataByVariable("NAME", existingRoleName, "USERS", dataId,
							"Teams_" + companyId);
					moduleEntryRepository.addDataToSetByVariable("NAME", newRoleName, "USERS", dataId,
							"Teams_" + companyId);

				}
			}

			List<ModuleField> fields = module.getFields();
			String discussionFieldName = null;

			for (ModuleField field : fields) {
				DataType dataType = field.getDataType();
				String display = dataType.getDisplay();
				if (display.equals("Discussion")) {
					discussionFieldName = field.getName();
					break;
				}
			}

			if (entry.containsKey(discussionFieldName)) {
				entry.put(discussionFieldName, existingEntry.get(discussionFieldName));
			}

			if (entry.containsKey("IS_CHANGED")) {

				entry.remove("IS_CHANGED");
				if (!module.getName().equalsIgnoreCase("teams")) {
					entry.put("EFFECTIVE_FROM", new Date());
					existingEntry.put("EFFECTIVE_TO", new Date());
					existingEntry.put("DATA_ID", dataId);
					existingEntry.remove("_id");

					moduleEntryRepository.updateEntry(entry, collectionName);
					moduleEntryRepository.save(existingEntry, collectionName);
				}

			} else {
				moduleEntryRepository.updateEntry(entry, collectionName);
			}

			dataService.postIntoElastic(module, companyId, entry);

			return;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// elasticClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		throw new InternalErrorException("INTERNAL_ERROR");

	}
}
