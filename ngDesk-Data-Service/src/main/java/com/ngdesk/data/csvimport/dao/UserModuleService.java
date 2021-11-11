package com.ngdesk.data.csvimport.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.data.company.dao.Company;
import com.ngdesk.data.dao.Phone;
import com.ngdesk.data.dao.Relationship;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.data.roles.dao.Role;
import com.ngdesk.data.sam.dao.DataProxy;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.roles.RolesRepository;

@Service
public class UserModuleService {

	@Autowired
	CsvImportService csvImportService;

	@Autowired
	ModuleService moduleService;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	DataProxy dataAPI;

	@Autowired
	RolesRepository rolesRepository;

	public Map<String, Object> handleUserModule(Map<String, Object> inputMessage, List<Module> modules, String userUuid,
			Module module, Company company, Map<String, Object> globalTeam, String accountId) {

		Map<String, Object> entry = new HashMap<String, Object>();
		String companyId = company.getCompanyId();
		String globalTeamId = globalTeam.get("_id").toString();
		Optional<Map<String, Object>> optionalUser = moduleEntryRepository.findEntryByFieldName("EMAIL_ADDRESS",
				inputMessage.get("EMAIL_ADDRESS"), moduleService.getCollectionName("Users", companyId));

		HashMap<String, Object> userEntry = new HashMap<String, Object>();
		boolean isDeleted = false;
		ObjectMapper mapper = new ObjectMapper();

		if (optionalUser.isPresent()) {
			isDeleted = Boolean.valueOf(optionalUser.get().get("DELETED").toString());
			userEntry.putAll(optionalUser.get());
		}
		try {
			if (optionalUser.isEmpty() || isDeleted) {
				Optional<Role> optionalRole = rolesRepository.findById(inputMessage.get("ROLE").toString(),
						moduleService.getCollectionName("roles", companyId));
				if (optionalRole.isEmpty()) {
					return null;
				}
				Role role = optionalRole.get();
				String existingRoleName = role.getName();

				Optional<Map<String, Object>> optionalTeamsEntry = moduleEntryRepository.findEntryByFieldName("NAME",
						existingRoleName, moduleService.getCollectionName("Teams", companyId));
				if (optionalTeamsEntry.isEmpty()) {
					return null;
				}
				Map<String, Object> roleTeam = optionalTeamsEntry.get();
				String roleTeamId = roleTeam.get("_id").toString();

				Optional<Module> optionalTeamsModule = modules.stream().filter(mod -> mod.getName().equals("Teams"))
						.findFirst();
				if (optionalTeamsModule.isEmpty()) {
					return null;
				}
				Module teamsModule = optionalTeamsModule.get();
				String userId = "";

				if (optionalUser.isPresent()) {
					userId = userEntry.get("_id").toString();
					String teamName = userEntry.get("FIRST_NAME") + " " + userEntry.get("LAST_NAME");

					Optional<Map<String, Object>> optionalPersonalTeam = moduleEntryRepository
							.findTeamsByVariableForIsPersonal("NAME", teamName,
									moduleService.getCollectionName("Teams", companyId));
					if (optionalPersonalTeam.isEmpty()) {
						return null;
					}
					Map<String, Object> personalTeam = optionalPersonalTeam.get();
					personalTeam.put("DELETED", false);
					userEntry.put("DELETED", false);

					updateUsersInTeamsEntry(Arrays.asList().toString(), userId, teamsModule, companyId, personalTeam,
							userUuid);

					List<String> existingTeams = mapper.readValue(mapper.writeValueAsString(userEntry.get("TEAMS")),
							mapper.getTypeFactory().constructCollectionType(List.class, String.class));
					if (!existingTeams.contains(personalTeam.get("_id").toString())) {
						existingTeams.add(personalTeam.get("_id").toString());
					}

					List<Relationship> teams = csvImportService.getListRelationshipValue("TEAMS", module, companyId,
							existingTeams);
					userEntry.put("TEAMS", teams);
					entry = dataAPI.putModuleEntry(userEntry, module.getModuleId(), true, companyId, userUuid, false);
				} else {
					String userEmailAddress = inputMessage.get("EMAIL_ADDRESS").toString();
					String[] splitEmail = userEmailAddress.split("@");
					String[] names = splitEmail[0].split("\\.");
					String firstName = names[0].trim();
					String lastName = "";
					if (names.length > 1) {
						lastName = names[1].trim(); 
					}

					Module userModule = modules.stream().filter(mod -> mod.getName().equals("Users")).findFirst()
							.orElse(null);
					if (userModule == null) {
						return null;
					}

					userEntry = csvImportService.createUser(inputMessage.get("EMAIL_ADDRESS").toString(), companyId, "",
							false, company.getCompanySubdomain(), "alarm_classic", 0,
							inputMessage.get("ROLE").toString(), false, globalTeamId, userUuid, userModule);
					userId = userEntry.get("DATA_ID").toString();

					Module contactModule = modules.stream().filter(mod -> mod.getName().equals("Contacts")).findFirst()
							.orElse(null);
					if (contactModule == null) {
						return null;
					}

					Phone phone = new Phone("us", "+1", "", "us.svg");
					Map<String, Object> contactEntry = csvImportService.createContact(firstName, lastName, accountId,
							phone, contactModule, companyId, globalTeamId, userId, userUuid);

					HashMap<String, Object> team = new HashMap<String, Object>();
					team.put("NAME", firstName + " " + lastName);
					team.put("DESCRIPTION", "Personal team for " + firstName + " " + lastName);

					List<Relationship> users = new ArrayList<Relationship>();
					String primaryDisplayFieldValue = csvImportService
							.getPrimaryDisplayFieldValue("USERS", teamsModule, companyId, userId).toString();
					Relationship userRelationship = new Relationship(userId, primaryDisplayFieldValue);
					users.add(userRelationship);
					team.put("USERS", users);
					team.put("DELETED", false);
					team.put("DATE_CREATED", new Date());
					team.put("DATE_UPDATED", new Date());
					team.put("IS_PERSONAL", true);
					Map<String, Object> personalTeam = csvImportService.createModuleData(companyId, "Teams", team,
							userUuid, modules);
					String personalTeamId = personalTeam.get("DATA_ID").toString();

					List<String> teamsList = Arrays.asList(personalTeamId, globalTeamId, roleTeamId);
					List<Relationship> teams = csvImportService.getListRelationshipValue("TEAMS", module, companyId,
							teamsList);
					userEntry.put("TEAMS", teams);
					userEntry.put("IS_LOGIN_ALLOWED", false);

					Relationship contactRelationship = new Relationship(contactEntry.get("DATA_ID").toString(),
							csvImportService.getPrimaryDisplayFieldValue("CONTACT", module, companyId,
									contactEntry.get("DATA_ID").toString()).toString());
					if (contactRelationship != null) {
						userEntry.put("CONTACT", contactRelationship);
					}
					entry = dataAPI.putModuleEntry(userEntry, module.getModuleId(), true, companyId, userUuid, false);
				}

				updateUsersInTeamsEntry(globalTeam.get("USERS").toString(), userId, teamsModule, companyId, globalTeam,
						userUuid);

				updateUsersInTeamsEntry(roleTeam.get("USERS").toString(), userId, teamsModule, companyId, roleTeam,
						userUuid);
			}
		} catch (Exception e) {
			e.printStackTrace();
			String errorMessage = csvImportService.formatErrorMessage(e.getMessage());
			throw new InternalErrorException(errorMessage);
		}
		return entry;
	}

	public void updateUsersInTeamsEntry(String value, String userId, Module teamsModule, String companyId,
			Map<String, Object> entry, String userUuid) {

		HashMap<String, Object> mapEntry = new HashMap<String, Object>();
		mapEntry.putAll(entry);
		List<String> users = csvImportService.parseString(value);

		List<Relationship> usersRelationship = csvImportService.getListRelationshipValue("USERS", teamsModule,
				companyId, users);
		entry.put("USERS", usersRelationship);
		try {
			dataAPI.putModuleEntry(mapEntry, teamsModule.getModuleId(), true, companyId, userUuid, false);
		} catch (Exception e) {
			String errorMessage = csvImportService.formatErrorMessage(e.getMessage());
			throw new InternalErrorException(errorMessage);
		}
	}
}
