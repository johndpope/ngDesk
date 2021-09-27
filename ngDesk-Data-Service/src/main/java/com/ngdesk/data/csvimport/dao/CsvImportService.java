package com.ngdesk.data.csvimport.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.data.dao.BasePhone;
import com.ngdesk.data.dao.DataService;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.Phone;
import com.ngdesk.data.dao.Relationship;
import com.ngdesk.data.dao.Sender;
import com.ngdesk.data.modules.dao.DataType;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.data.sam.dao.DataProxy;
import com.ngdesk.repositories.csvimport.CsvImportRepository;
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

	@Autowired
	CsvImportRepository csvImportRepository;

	@Autowired
	Global global;

	public boolean accountExists(String accountName, String companyId) {

		Optional<Map<String, Object>> optionalAccountEntry = moduleEntryRepository.findEntryByFieldName("ACCOUNT_NAME",
				accountName, moduleService.getCollectionName("Accounts", companyId));

		if (optionalAccountEntry.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public Map<String, Object> createAccount(String accountName, String companyId, String globalTeamId, String userUuid,
			Module accountModule) {
		Map<String, Object> accountEntry = new HashMap<String, Object>();
		try {
			HashMap<String, Object> account = new HashMap<String, Object>();
			account.put("ACCOUNT_NAME", accountName);
			account.put("DATE_CREATED", new Date());
			account.put("DELETED", false);

			List<Relationship> teams = new ArrayList<Relationship>();
			Relationship relationship = new Relationship(globalTeamId,
					getPrimaryDisplayFieldValue("TEAMS", accountModule, companyId, globalTeamId).toString());
			teams.add(relationship);
			account.put("TEAMS", teams);
			accountEntry = dataAPI.postModuleEntry(account, accountModule.getModuleId(), true, companyId, userUuid);

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException(e.getMessage());
		}
		return accountEntry;
	}

	public HashMap<String, Object> createUser(String email, String companyId, String password, boolean inviteAccepted,
			String subdomain, String notification, int loginAttempts, String language, String role, boolean disabled,
			String globalTeamId, String userUuid, Module userModule) {

		HashMap<String, Object> userEntry = new HashMap<String, Object>();
		try {
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
			contactEntry
					.putAll(dataAPI.postModuleEntry(contact, contactModule.getModuleId(), true, companyId, userUuid));
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException(e.getMessage());
		}
		return contactEntry;

	}

	public Map<String, Object> createModuleData(String companyId, String moduleName, Map<String, Object> body,
			String userUuid, List<Module> modules) {
		Map<String, Object> data = new HashMap<String, Object>();

		try {
			Optional<Module> optionalModule = modules.stream().filter(mod -> mod.getName().equals(moduleName))
					.findFirst();
			if (optionalModule.isPresent()) {
				Module module = optionalModule.get();

				// INSERT RECORD
				HashMap<String, Object> bodyHashMap = new HashMap<String, Object>();
				bodyHashMap.putAll(body);
				data = dataAPI.postModuleEntry(bodyHashMap, module.getModuleId(), true, companyId, userUuid);
				String dataId = data.get("DATA_ID").toString();

				List<ModuleField> fields = module.getFields();
				for (ModuleField field : fields) {
					String name = field.getName();
					DataType dataType = field.getDataType();
					if (dataType.getDisplay().equalsIgnoreCase("Relationship")
							&& field.getRelationshipType().equalsIgnoreCase("One to One")) {
						if (data.containsKey(name)) {
							Optional<Module> optionalRelatedModule = modulesRepository.findById(field.getModule(),
									moduleService.getCollectionName("modules", companyId));

							if (optionalRelatedModule.isPresent()) {
								Module relationModule = optionalRelatedModule.get();
								List<ModuleField> relationFields = relationModule.getFields();
								ModuleField relatedField = relationFields.stream().filter(relationField -> relationField
										.getFieldId().equals(field.getRelationshipField())).findFirst().orElse(null);
								if (relatedField != null) {
									String value = getPrimaryDisplayFieldValue(relatedField.getName(), relationModule,
											companyId, dataId).toString();
									Relationship relationship = new Relationship(dataId, value);

									HashMap<String, Object> entry = new HashMap<String, Object>();
									entry.put("DATA_ID", data.get(name));
									entry.put(relatedField.getName(), relationship);
									dataAPI.putModuleEntry(entry, relationModule.getModuleId(), true, companyId,
											userUuid, false);
								}
							}
						}
					}
				}
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
			Map<String, Object> entry, String userUuid) {
		try {
			HashMap<String, Object> mapEntry = new HashMap<String, Object>();
			mapEntry.putAll(entry);
			List<String> users = parseString(value);

			List<Relationship> usersRelationship = getListRelationshipValue("USERS", teamsModule, companyId, users);
			entry.put("USERS", usersRelationship);
			dataAPI.putModuleEntry(mapEntry, teamsModule.getModuleId(), true, companyId, userUuid, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getRelationshipId(ModuleField field, String companyId, Object value) {

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
		Optional<Map<String, Object>> optionalRelationshipEntry = moduleEntryRepository.findEntryByVariable(
				relationshipFieldName, value, moduleService.getCollectionName(relationshipModuleName, companyId));

		if (optionalRelationshipEntry.isEmpty()) {
			return null;
		}

		Map<String, Object> relationshipEntry = optionalRelationshipEntry.get();
		return relationshipEntry.get("_id").toString();
	}

	public boolean checkRelationshipStatus(ModuleField field, String dataId, String companyId) {
		Optional<Module> optionalRelationshipModule = modulesRepository.findById(field.getModule(),
				moduleService.getCollectionName("modules", companyId));
		if (optionalRelationshipModule.isEmpty()) {
			return false;
		}

		Module relationshipModule = optionalRelationshipModule.get();
		String relationshipModuleName = relationshipModule.getName();

		Optional<ModuleField> optionalRelationshipField = optionalRelationshipModule.get().getFields().stream()
				.filter(moduleField -> moduleField.getFieldId().equals(field.getRelationshipField())).findFirst();
		if (optionalRelationshipField.isEmpty()) {
			return false;
		}

		ModuleField relationshipField = optionalRelationshipField.get();
		String relationshipFieldName = relationshipField.getName();

		Optional<Map<String, Object>> optionalRelationshipEntry = moduleEntryRepository.findById(dataId,
				moduleService.getCollectionName(relationshipModuleName, companyId));

		if (optionalRelationshipEntry.isEmpty()) {
			return false;
		}

		Map<String, Object> relationshipEntry = optionalRelationshipEntry.get();
		if (relationshipEntry.get(relationshipFieldName) == null) {
			return true;
		}
		return false;
	}

	public List<String> parseString(String string) {
		List<String> list = new ArrayList<String>();
		String str[] = Arrays.stream(string.replaceAll("\\[|\\]", "").split(",")).map(String::trim)
				.toArray(String[]::new);
		list = Arrays.asList(str);
		return list;
	}

	public void addToSet(int i, String message, String id) {
		CsvImportLog log = new CsvImportLog();
		log.setLineNumber(i);
		log.setErrorMessage(message);
		csvImportRepository.addToEntrySet(id, "logs", log, "csv_import");
	}

	public BasePhone createPhoneObject(String countryDialCode, String phoneNumber, BasePhone phone) {
		try {
			String countriesJson = global.getFile("countriesWithDialCode.json");
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> mapCountries = mapper.readValue(countriesJson, Map.class);
			List<Map<String, Object>> countries = mapper.readValue(
					mapper.writeValueAsString(mapCountries.get("COUNTRIES")),
					mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

			String updatedCode = countryDialCode.replace("+", "").trim();
			Map<String, Object> country = countries.stream()
					.filter(countryList -> countryList.get("DIAL_CODE").toString().equals(updatedCode)).findFirst()
					.orElse(null);
			if (country != null) {
				phone.setCountryCode(country.get("COUNTRY_CODE").toString());
				phone.setPhoneNumber(phoneNumber);
				phone.setCountryFlag(country.get("COUNTRY_FLAG").toString());
				countryDialCode = (!countryDialCode.contains("+")) ? "+" + countryDialCode : countryDialCode;
				phone.setDialCode(countryDialCode);
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new InternalErrorException(e.getMessage());
		}
		return phone;
	}

	public Map<String, Object> formatDataTypes(List<ModuleField> fields, Map<String, Object> inputMessage,
			CsvImport csvDocument, int i, String companyId, Map<String, Object> user, String globalTeamId,
			Module module) {
		Map<String, String> fieldIdNameMap = new HashMap<String, String>();
		boolean error = false;
		for (ModuleField field : fields) {
			String fieldName = field.getName();
			String displayLabel = field.getDisplayLabel();
			fieldIdNameMap.put(field.getFieldId(), displayLabel);
			DataType dataType = field.getDataType();

			// VALIDATE IF THE PICKLIST VALUES ARE MATCHING WITH EXISTING PICKLISTS
			if (inputMessage.containsKey(fieldName)) {

				if (dataType.getDisplay().equalsIgnoreCase("Picklist")) {
					List<String> picklistValues = field.getPicklistValues();
					String value = inputMessage.get(fieldName).toString().trim();

					String filteredValue = picklistValues.stream().filter(list -> list.equalsIgnoreCase(value))
							.findFirst().orElse(null);

					if (filteredValue != null) {
						inputMessage.put(fieldName, filteredValue);
					} else {
						addToSet(i, fieldName + " picklist values are incorrect", csvDocument.getCsvImportId());
						error = true;
						break;
					}
				}

				if (dataType.getDisplay().equalsIgnoreCase("Chronometer")) {
					if (inputMessage.get(fieldName) != null) {
						String value = inputMessage.get(fieldName).toString().toLowerCase();
						String valueWithoutSpace = value.replaceAll("\\s+", "");
						if (valueWithoutSpace.length() == 0 || valueWithoutSpace.charAt(0) == '-') {
							inputMessage.put(fieldName, 0);
						} else if (valueWithoutSpace.length() != 0) {
							inputMessage.put(fieldName, valueWithoutSpace);
						}
					}
				}

				if (dataType.getDisplay().equalsIgnoreCase("Picklist (Multi-Select)")) {
					List<String> picklistValues = field.getPicklistValues();
					List<String> values = parseString(inputMessage.get(fieldName).toString());
					List<String> selectedtValues = new ArrayList<String>();
					for (String value : values) {
						String filteredValue = picklistValues.stream().filter(list -> list.equalsIgnoreCase(value))
								.findFirst().orElse(null);

						if (filteredValue != null) {
							selectedtValues.add(filteredValue);
						} else {
							addToSet(i, fieldName + " picklist values are incorrect", csvDocument.getCsvImportId());
							error = true;
							break;
						}
					}
					if (error) {
						break;
					} else if (!selectedtValues.isEmpty()) {
						inputMessage.put(fieldName, selectedtValues);
					}
				}

				if (dataType.getDisplay().equalsIgnoreCase("Discussion")) {
					String value = inputMessage.get(fieldName).toString().trim();
					Optional<Map<String, Object>> optionalContact = moduleEntryRepository.findEntryById(
							user.get("CONTACT").toString(), moduleService.getCollectionName("Contacts", companyId));
					Map<String, Object> contactEntry = optionalContact.get();
					String firstName = contactEntry.get("FIRST_NAME").toString();
					String lastName = contactEntry.get("LAST_NAME").toString();

					Sender sender = new Sender(firstName, lastName, user.get("USER_UUID").toString(),
							user.get("ROLE").toString());

					DiscussionMessage message = new DiscussionMessage(value, new Date(), UUID.randomUUID().toString(),
							"MESSAGE", null, sender);

					List<DiscussionMessage> messages = Arrays.asList(message);
					inputMessage.put(fieldName, messages);
				}

				// LIST TEXT DATA TYPE HANDLED
				if (dataType.getDisplay().equalsIgnoreCase("List Text")) {
					List<String> values = parseString(inputMessage.get(fieldName).toString().trim());
					if (values.size() != 0) {
						inputMessage.put(fieldName, values);
					}
				}

				// Phone Data TYPE HANDLED
				if (dataType.getDisplay().equalsIgnoreCase("Phone")) {
					BasePhone phone = new BasePhone();
					try {
						String value = inputMessage.get(fieldName).toString().trim();
						if (value != null) {
							if (csvDocument.getSeparator() != null) {
								String separator = (csvDocument.getSeparator().equals("Blank space")) ? " " : "-";
								String[] split = value.split(separator);
								if (split.length == 2) {
									phone = createPhoneObject(split[0].trim(), split[1].trim(), phone);
								} else {
									error = true;
								}
							} else {
								String separator = "-";
								String[] split = value.split(separator);
								if (split.length == 2) {
									phone = createPhoneObject(split[0].trim(), split[1].trim(), phone);
								} else {
									error = true;
								}
							}
						}
						if (error || phone == null) {
							addToSet(i, fieldName + " values are incorrect", csvDocument.getCsvImportId());
							break;
						} else {
							inputMessage.put(fieldName, phone);
						}
					} catch (Exception e) {
						addToSet(i, e.getMessage(), csvDocument.getCsvImportId());
						break;
					}
				}

				// Date, date/time ,time data type handled
				if (dataType.getDisplay().equalsIgnoreCase("Date/Time")
						|| dataType.getDisplay().equalsIgnoreCase("Date")
						|| dataType.getDisplay().equalsIgnoreCase("Time")) {

					String value = inputMessage.get(fieldName).toString().trim();
					try {
						Date date = new Date();
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSX");
						date = df.parse(value);
						inputMessage.put(fieldName, date);
					} catch (Exception e) {
						addToSet(i, fieldName + " values are invalid", csvDocument.getCsvImportId());
						error = true;
						break;
					}
				}

				List<String> ignoredFields = List.of("CREATED_BY", "LAST_UPDATED_BY");
				if (dataType.getDisplay().equalsIgnoreCase("Relationship") && !ignoredFields.contains(fieldName)) {
					if (field.getRelationshipType().equalsIgnoreCase("One To One")) {
						String relationshipId = getRelationshipId(field, companyId, inputMessage.get(fieldName));

						if (relationshipId != null && checkRelationshipStatus(field, relationshipId, companyId)) {
							Relationship relationship = new Relationship(relationshipId,
									inputMessage.get(fieldName).toString());
							inputMessage.put(fieldName, relationship);
						} else {
							String message = (relationshipId == null) ? " relationship value is not valid"
									: " relationship already exist";
							addToSet(i, fieldName + message, csvDocument.getCsvImportId());
							error = true;
							break;
						}
					} else if (field.getRelationshipType().equalsIgnoreCase("Many To One")) {
						String relationshipId = getRelationshipId(field, companyId, inputMessage.get(fieldName));
						if (relationshipId != null) {
							Relationship relationship = new Relationship(relationshipId,
									inputMessage.get(fieldName).toString());
							inputMessage.put(fieldName, relationship);
						} else {
							addToSet(i, fieldName + " relationship value is not valid", csvDocument.getCsvImportId());
							error = true;
							break;
						}
					} else if (field.getRelationshipType().equalsIgnoreCase("Many To Many")) {

						List<String> values = parseString(inputMessage.get(fieldName).toString());
						if (values != null) {
							List<Relationship> relationshipList = new ArrayList<Relationship>();
							for (String value : values) {
								String relationshipId = getRelationshipId(field, companyId, value);
								if (relationshipId != null) {
									Relationship relationship = new Relationship(relationshipId,
											inputMessage.get(fieldName).toString());
									relationshipList.add(relationship);
								} else {
									addToSet(i, fieldName + " relationship value is not valid",
											csvDocument.getCsvImportId());
									error = true;
									break;
								}
							}
							if (error) {
								break;
							} else {
								inputMessage.put(fieldName, relationshipList);
							}
						} else {
							addToSet(i, fieldName + " relationship value is not valid", csvDocument.getCsvImportId());
							error = true;
							break;
						}
					}
				}
			}

			if (field.getRequired()) {
				if (inputMessage.get(fieldName) == null) {
					if (dataType.getDisplay().equalsIgnoreCase("ID")) {
						inputMessage.put(fieldName, UUID.randomUUID().toString());
					}
				}
				if (field.getName().equals("TEAMS")) {
					List<Relationship> teams = new ArrayList<Relationship>();
					if (globalTeamId != null) {
						Relationship relationship = new Relationship(globalTeamId,
								getPrimaryDisplayFieldValue("TEAMS", module, companyId, globalTeamId).toString());
						teams.add(relationship);
					}
					inputMessage.put("TEAMS", teams);
				}
			}
		}
		if (error) {
			return null;
		} else {
			return inputMessage;
		}
	}

}
