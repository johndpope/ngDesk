package com.ngdesk.data.csvimport.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.data.dao.DataService;
import com.ngdesk.data.dao.Phone;
import com.ngdesk.data.elastic.Wrapper;
import com.ngdesk.data.modules.dao.DataType;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.roles.dao.Role;
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
	Wrapper wrapper;

	@Autowired
	DataService dataService;
	
	@Autowired
	RolesRepository rolesRepository;

	public boolean accountExists(String accountName, String companyId) {

		String collectionName = "Accounts_" + companyId;
		Optional<Map<String, Object>> optionalAccountEntry = moduleEntryRepository.findEntryByFieldName("ACCOUNT_NAME",
				accountName, collectionName);

		if (optionalAccountEntry.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public Map<String, Object> createAccount(String accountName, String companyId, String globalTeamId) {
		Map<String, Object> accountEntry = new HashMap<String, Object>();
		try {

			Optional<Module> optionalAccountModule = modulesRepository.findIdbyModuleName("Accounts",
					"modules_" + companyId);
			Module accountModule = optionalAccountModule.get();

			Map<String, Object> account = new HashMap<String, Object>();
			account.put("ACCOUNT_NAME", accountName);
			account.put("DATE_CREATED", new Date());
			account.put("TEAMS", Arrays.asList(globalTeamId));
			account.put("DELETED", false);

			accountEntry = wrapper.postData(companyId, accountModule, account);

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		return accountEntry;
	}

	public Map<String, Object> createUser(String email, String companyId, String password, boolean inviteAccepted,
			String subdomain, String notification, int loginAttempts, String language, String role, boolean disabled,
			String globalTeamId) {

		Map<String, Object> userEntry = new HashMap<String, Object>();
		try {
			Optional<Module> optionalUserModule = modulesRepository.findIdbyModuleName("Users", "modules_" + companyId);
			Module userModule = optionalUserModule.get();

			Map<String, Object> user = new HashMap<String, Object>();
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
			user.put("TEAMS", Arrays.asList(globalTeamId));

			userEntry = wrapper.postData(companyId, userModule, user);

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		return userEntry;
	}

	public Map<String, Object> createContact(String firstName, String lastName, String accountId, Phone phone,
			Module contactModule, String companyId, String globalTeamId, String userId) {

		Map<String, Object> contact = new HashMap<String, Object>();
		contact.put("FIRST_NAME", firstName);
		contact.put("LAST_NAME", lastName);
		contact.put("ACCOUNT", accountId);
		contact.put("SUBSCRIPTION_ON_MARKETING_EMAIL", true);
		contact.put("PHONE_NUMBER", phone);
		contact.put("TEAMS", Arrays.asList(globalTeamId));
		contact.put("DELETED", false);
		contact.put("DATE_CREATED", new Date());
		contact.put("DATE_UPDATED", new Date());
		contact.put("USER", userId);

		String fullName = firstName;
		if (lastName != null && !lastName.isBlank()) {
			fullName += " " + lastName;
		}

		contact.put("FULL_NAME", fullName);

		contact = wrapper.postData(companyId, contactModule, contact);
		return contact;
	}

	public Map<String, Object> createModuleData(String companyId, String moduleName, Map<String, Object> body) {
		Map<String, Object> data = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();

		try {
			Optional<Module> optionalModule = modulesRepository.findIdbyModuleName(moduleName, "modules_" + companyId);
			if (optionalModule.isPresent()) {
				Module module = optionalModule.get();
				
				// INSERT RECORD
				data = wrapper.postData(companyId, module, body);

				String dataId = data.get("_id").toString();
				data.remove("_id");
				data.put("DATA_ID", dataId);

				List<ModuleField> fields = module.getFields();
				for (ModuleField field : fields) {
					String name = field.getName();
					DataType dataType = field.getDataType();
					if (dataType.getDisplay().equalsIgnoreCase("Relationship")) {
						if (data.containsKey(name)) {
							Optional<Module> optionalRelatedModule = modulesRepository.findById(field.getModule(),
									"modules_" + companyId);

							if (optionalRelatedModule.isPresent()) {
								Module relationModule = optionalRelatedModule.get();
								String relationModuleName = relationModule.getName();
								List<ModuleField> relationFields = relationModule.getFields();

								String relationFieldName = null;
								for (ModuleField relationField : relationFields) {

									DataType datatype = relationField.getDataType();
									if (datatype.getDisplay().equals("Relationship")) {
										if (field.getRelationshipField() != null) {
											if (field.getRelationshipField().equals(relationField.getFieldId())) {
												relationFieldName = relationField.getName();
												break;
											}
										}
									}
								}

								String relationshipType = field.getRelationshipType();
								if (relationshipType.equals("One to One")) {
									if (relationFieldName != null) {
										String value = data.get(name).toString();
										Optional<Map<String, Object>> optionalEntry = moduleEntryRepository
												.findById(value, relationModuleName + "_" + companyId);
										Map<String, Object> entry = optionalEntry.get();
										entry.put(relationFieldName, dataId);
										wrapper.putData(companyId, relationModule, entry, value);
									}
								} else if (relationshipType.equals("Many to Many")) {
									if (!data.get(name).getClass().getSimpleName().toString().equals("ArrayList")) {
										String[] vars = { name };
										throw new BadRequestException("INVALID_INPUT_FORMAT", vars);
									}
									List<String> values = mapper.readValue(mapper.writeValueAsString(data.get(name)),
											mapper.getTypeFactory().constructCollectionType(List.class, String.class));
									for (String value : values) {
										if (relationFieldName != null) {
											Optional<Map<String, Object>> optionalEntry = moduleEntryRepository
													.findById(value, relationModuleName + "_" + companyId);
											Map<String, Object> entry = optionalEntry.get();
											List<String> relationFieldValues = new ArrayList<String>();
											if (entry.get(relationFieldName) != null) {
												relationFieldValues = mapper.readValue(
														mapper.writeValueAsString(entry.get(relationFieldName)),
														mapper.getTypeFactory().constructCollectionType(List.class,
																String.class));
											}
											relationFieldValues.add(dataId);
											entry.put(relationFieldName, relationFieldValues);
											wrapper.putData(companyId, relationModule, entry, value);
										}
									}
								}
							}
						}
					}
				}
				return data;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
