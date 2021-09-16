package com.ngdesk.data.csvimport.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.data.dao.DataService;
import com.ngdesk.data.dao.Phone;
import com.ngdesk.data.dao.Relationship;
import com.ngdesk.data.modules.dao.DataType;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.data.sam.dao.DataProxy;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;
import com.ngdesk.repositories.roles.RolesRepository;

@Service
public class CsvImportService {

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	DataProxy dataAPI;

	@Autowired
	DataService dataService;

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	ModuleService moduleService;

	public boolean accountExists(String accountName, String companyId) {

		Optional<Map<String, Object>> optionalAccountEntry = moduleEntryRepository.findEntryByFieldName("ACCOUNT_NAME",
				accountName, moduleService.getCollectionName("Accounts", companyId));

		if (optionalAccountEntry.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public Map<String, Object> createAccount(String accountName, String companyId, String globalTeamId,
			String userUuid) {
		Map<String, Object> accountEntry = new HashMap<String, Object>();
		try {

			Optional<Module> optionalAccountModule = modulesRepository.findIdbyModuleName("Accounts",
					moduleService.getCollectionName("modules", companyId));
			Module accountModule = optionalAccountModule.get();

			HashMap<String, Object> account = new HashMap<String, Object>();
			account.put("ACCOUNT_NAME", accountName);
			account.put("DATE_CREATED", new Date());
			account.put("DELETED", false);

			List<Relationship> teams = new ArrayList<Relationship>();
			Relationship relationship = new Relationship(globalTeamId,
					getPrimaryDisplayFieldValue("TEAMS", accountModule, companyId, globalTeamId).toString());
			teams.add(relationship);
			account.put("TEAMS", teams);
			System.out.println("hit 1");
			accountEntry = dataAPI.postModuleEntry(account, accountModule.getModuleId(), true, companyId, userUuid);

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException(e.getMessage());
		}
		return accountEntry;
	}

	public HashMap<String, Object> createUser(String email, String companyId, String password, boolean inviteAccepted,
			String subdomain, String notification, int loginAttempts, String language, String role, boolean disabled,
			String globalTeamId, String userUuid) {

		HashMap<String, Object> userEntry = new HashMap<String, Object>();
		try {
			Optional<Module> optionalUserModule = modulesRepository.findIdbyModuleName("Users",
					moduleService.getCollectionName("modules", companyId));
			Module userModule = optionalUserModule.get();

			HashMap<String, Object> user = new HashMap<String, Object>();
			user.put("USER_UUID", UUID.randomUUID().toString());
			user.put("EMAIL_ADDRESS", email.toLowerCase());
			if (!password.contains("V1_PASSWORD")) {
				user.put("PASSWORD", dataService.hashAttachment(password));
			} else {
				user.put("PASSWORD", password);
			}

			user.put("DATE_CREATED", new Date());
			user.put("DISABLED", disabled);
			user.put("LANGUAGE", language);
			user.put("ROLE", role);
			user.put("INVITE_ACCEPTED", inviteAccepted);
			user.put("NOTIFICATION_SOUND", notification);
			user.put("EMAIL_VERIFIED", false);
			user.put("DEFAULT_CONTACT_METHOD", "Email");
			user.put("LOGIN_ATTEMPTS", loginAttempts);
			user.put("DELETED", false);

			List<Relationship> teams = new ArrayList<Relationship>();
			Relationship relationship = new Relationship(globalTeamId,
					getPrimaryDisplayFieldValue("TEAMS", userModule, companyId, globalTeamId).toString());
			teams.add(relationship);
			user.put("TEAMS", teams);
			System.out.println("hit 2");
			userEntry.putAll(dataAPI.postModuleEntry(user, userModule.getModuleId(), true, companyId, userUuid));

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException(e.getMessage());
		}
		return userEntry;
	}

	public Map<String, Object> createContact(String firstName, String lastName, String accountId, Phone phone,
			Module contactModule, String companyId, String globalTeamId, String userId, String userUuid) {

		HashMap<String, Object> contactEntry = new HashMap<String, Object>();
		try {
			HashMap<String, Object> contact = new HashMap<String, Object>();
			contact.put("FIRST_NAME", firstName);
			contact.put("LAST_NAME", lastName);
			contact.put("SUBSCRIPTION_ON_MARKETING_EMAIL", true);
			contact.put("PHONE_NUMBER", phone);
			contact.put("DELETED", false);
			contact.put("DATE_CREATED", new Date());
			contact.put("DATE_UPDATED", new Date());

			Relationship accountRelationship = new Relationship(accountId,
					getPrimaryDisplayFieldValue("ACCOUNT", contactModule, companyId, accountId).toString());
			if (accountRelationship != null) {
				contact.put("ACCOUNT", accountRelationship);
			}

			Relationship userRelationship = new Relationship(userId,
					getPrimaryDisplayFieldValue("USER", contactModule, companyId, userId).toString());
			if (userRelationship != null) {
				contact.put("USER", userRelationship);
			}

			List<Relationship> teams = new ArrayList<Relationship>();
			Relationship teamsRelationship = new Relationship(globalTeamId,
					getPrimaryDisplayFieldValue("TEAMS", contactModule, companyId, globalTeamId).toString());
			teams.add(teamsRelationship);
			contact.put("TEAMS", teams);

			String fullName = firstName;
			if (lastName != null && !lastName.isBlank()) {
				fullName += " " + lastName;
			}

			contact.put("FULL_NAME", fullName);
			System.out.println("hit 3");
			contactEntry
					.putAll(dataAPI.postModuleEntry(contact, contactModule.getModuleId(), true, companyId, userUuid));
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException(e.getMessage());
		}
		return contactEntry;

	}

	public Map<String, Object> createModuleData(String companyId, String moduleName, Map<String, Object> body,
			String userUuid) {
		Map<String, Object> data = new HashMap<String, Object>();

		try {
			Optional<Module> optionalModule = modulesRepository.findIdbyModuleName(moduleName,
					moduleService.getCollectionName("modules", companyId));
			if (optionalModule.isPresent()) {
				Module module = optionalModule.get();

				// INSERT RECORD
				HashMap<String, Object> bodyHashMap = new HashMap<String, Object>();
				bodyHashMap.putAll(body);
				System.out.println("hit 4");
				System.out.println(body);
				data = dataAPI.postModuleEntry(bodyHashMap, module.getModuleId(), true, companyId, userUuid);

				String dataId = data.get("DATA_ID").toString();

				List<ModuleField> fields = module.getFields();
//				for (ModuleField field : fields) {
//					String name = field.getName();
//					DataType dataType = field.getDataType();
//					if (dataType.getDisplay().equalsIgnoreCase("Relationship")) {
//						if (data.containsKey(name)) {
//							Optional<Module> optionalRelatedModule = modulesRepository.findById(field.getModule(),
//									moduleService.getCollectionName("modules", companyId));
//
//							if (optionalRelatedModule.isPresent()) {
//								Module relationModule = optionalRelatedModule.get();
//								String relationModuleName = relationModule.getName();
//								List<ModuleField> relationFields = relationModule.getFields();
//
//								String relationFieldName = null;
//								for (ModuleField relationField : relationFields) {
//
//									DataType datatype = relationField.getDataType();
//									if (datatype.getDisplay().equals("Relationship")) {
//										if (field.getRelationshipField() != null) {
//											if (field.getRelationshipField().equals(relationField.getFieldId())) {
//												relationFieldName = relationField.getName();
//												break;
//											}
//										}
//									}
//								}
//
//								String relationshipType = field.getRelationshipType();
//								if (relationshipType.equals("One to One")) {
//									if (relationFieldName != null) {
//										System.out.println(relationModuleName);
//										System.out.println(field.getPrimaryDisplayField());
//										String value = data.get(name).toString();
//										Optional<Map<String, Object>> optionalEntry = moduleEntryRepository.findById(
//												value, moduleService.getCollectionName(relationModuleName, companyId));
//										HashMap<String, Object> entry = new HashMap<String, Object>();
//										entry.putAll(optionalEntry.get());
//										entry.put(relationFieldName, dataId);
//										dataAPI.putModuleEntry(entry, relationModule.getModuleId(), true, companyId,
//												userUuid, false);
//									}
//								} else if (relationshipType.equals("Many to Many")) {
//									if (!data.get(name).getClass().getSimpleName().toString().equals("ArrayList")) {
//										String[] vars = { name };
//										throw new BadRequestException("INVALID_INPUT_FORMAT", vars);
//									}
//									List<String> values = mapper.readValue(mapper.writeValueAsString(data.get(name)),
//											mapper.getTypeFactory().constructCollectionType(List.class, String.class));
//									for (String value : values) {
//										if (relationFieldName != null) {
//											Optional<Map<String, Object>> optionalEntry = moduleEntryRepository
//													.findById(value, moduleService.getCollectionName(relationModuleName,
//															companyId));
//											HashMap<String, Object> entry = new HashMap<String, Object>();
//											entry.putAll(optionalEntry.get());
//											List<String> relationFieldValues = new ArrayList<String>();
//											if (entry.get(relationFieldName) != null) {
//												relationFieldValues = mapper.readValue(
//														mapper.writeValueAsString(entry.get(relationFieldName)),
//														mapper.getTypeFactory().constructCollectionType(List.class,
//																String.class));
//											}
//											relationFieldValues.add(dataId);
//											entry.put(relationFieldName, relationFieldValues);
//											dataAPI.putModuleEntry(entry, relationModule.getModuleId(), true, companyId,
//													userUuid, false);
//										}
//									}
//								}
//							}
//						}
//					}
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException(e.getMessage());
		}
		return data;
	}

	public Object getPrimaryDisplayFieldValue(String fieldName, Module module, String companyId, String dataId) {

		Optional<ModuleField> optionalField = module.getFields().stream()
				.filter(field -> field.getName().equals(fieldName)).findFirst();
		if (optionalField.isEmpty()) {
			return null;
		}
		ModuleField field = optionalField.get();

		Optional<Module> optionalRelationshipModule = modulesRepository.findById(field.getModule(),
				moduleService.getCollectionName("modules", companyId));
		if (optionalRelationshipModule.isEmpty()) {
			return null;
		}

		Module relationshipModule = optionalRelationshipModule.get();
		String relationshipModuleName = relationshipModule.getName();
		Optional<ModuleField> optionalRelationshipField = optionalRelationshipModule.get().getFields().stream()
				.filter(moduleField -> moduleField.getFieldId().equals(field.getPrimaryDisplayField())).findFirst();
		if (optionalRelationshipField.isEmpty()) {
			return null;
		}

		ModuleField relationshipField = optionalRelationshipField.get();
		String relationshipFieldName = relationshipField.getName();
		Optional<Map<String, Object>> optionalRelationshipEntry = moduleEntryRepository.findEntryById(dataId,
				moduleService.getCollectionName(relationshipModuleName, companyId));
		if (optionalRelationshipEntry.isEmpty()) {
			return null;
		}

		Map<String, Object> relationshipEntry = optionalRelationshipEntry.get();
		return relationshipEntry.get(relationshipFieldName);
	}

	public List<Relationship> getListRelationshipValue(String fieldname, Module module, String companyId,
			List<String> entryIds) {
		List<Relationship> relationshipList = new ArrayList<Relationship>();
		for (String entryId : entryIds) {
			Relationship relationship = new Relationship(entryId,
					getPrimaryDisplayFieldValue(fieldname, module, companyId, entryId).toString());
			relationshipList.add(relationship);
		}
		return relationshipList;
	}

	public void updateUsersInTeamsEntry(String value, String userId, Module teamsModule, String companyId,
			HashMap<String, Object> entry, String userUuid) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			List<String> users = mapper.readValue(value,
					mapper.getTypeFactory().constructCollectionType(List.class, String.class));
			users.add(userId);

			List<Relationship> usersRelationship = getListRelationshipValue("USERS", teamsModule, companyId, users);
			entry.put("USERS", usersRelationship);
			dataAPI.putModuleEntry(entry, teamsModule.getModuleId(), true, companyId, userUuid, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
