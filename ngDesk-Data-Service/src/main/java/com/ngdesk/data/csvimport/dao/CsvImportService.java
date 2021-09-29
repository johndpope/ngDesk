package com.ngdesk.data.csvimport.dao;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.data.company.dao.Company;
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
import com.ngdesk.data.roles.dao.Role;
import com.ngdesk.data.sam.dao.DataProxy;
import com.ngdesk.repositories.csvimport.CsvImportRepository;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;
import com.ngdesk.repositories.roles.RolesRepository;
import com.opencsv.CSVReader;

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
			throw new InternalErrorException(formatErrorMessage(e.getMessage()));
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
			throw new InternalErrorException(formatErrorMessage(e.getMessage()));
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
			throw new InternalErrorException(formatErrorMessage(e.getMessage()));
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
			throw new InternalErrorException(formatErrorMessage(e.getMessage()));
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
			throw new InternalErrorException(formatErrorMessage(e.getMessage()));
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
		try {
			Map<String, String> fieldIdNameMap = new HashMap<String, String>();
			boolean error = false;
			for (ModuleField field : fields) {
				String fieldName = field.getName();
				String displayLabel = field.getDisplayLabel();
				fieldIdNameMap.put(field.getFieldId(), displayLabel);
				DataType dataType = field.getDataType();

				if (inputMessage.containsKey(fieldName)) {
					String value = inputMessage.get(fieldName).toString().trim();
					List<String> ignoredFields = List.of("CREATED_BY", "LAST_UPDATED_BY");
					List<String> numericDataTypes = List.of("Auto Number", "Currency", "Number");
					if (numericDataTypes.contains(dataType.getDisplay())) {
						if (NumberUtils.isParsable(value)) {
							if (dataType.getDisplay().equalsIgnoreCase("Currency")) {
								inputMessage.put(fieldName, Float.valueOf(value));
							} else {
								inputMessage.put(fieldName, Integer.valueOf(value));
							}
						} else {
							addToSet(i, displayLabel + " value is invalid", csvDocument.getCsvImportId());
							error = true;
							break;
						}

					} else if (dataType.getDisplay().equalsIgnoreCase("Checkbox")) {
						if (value.equalsIgnoreCase("true")) {
							inputMessage.put(fieldName, true);
						} else {
							inputMessage.put(fieldName, false);
						}

					} else if (dataType.getDisplay().equalsIgnoreCase("Picklist")) {
						List<String> picklistValues = field.getPicklistValues();
						String picklistValue = value;

						String filteredValue = picklistValues.stream()
								.filter(list -> list.equalsIgnoreCase(picklistValue)).findFirst().orElse(null);

						if (filteredValue != null) {
							inputMessage.put(fieldName, filteredValue);
						} else {
							addToSet(i, displayLabel + " value is invalid", csvDocument.getCsvImportId());
							error = true;
							break;
						}

					} else if (dataType.getDisplay().equalsIgnoreCase("Chronometer")) {
						if (inputMessage.get(fieldName) != null) {
							value = value.toLowerCase();
							String valueWithoutSpace = value.replaceAll("\\s+", "");
							if (valueWithoutSpace.length() == 0 || valueWithoutSpace.charAt(0) == '-') {
								inputMessage.put(fieldName, 0);
							} else if (valueWithoutSpace.length() != 0) {
								inputMessage.put(fieldName, valueWithoutSpace);
							}
						}

					} else if (dataType.getDisplay().equalsIgnoreCase("Picklist (Multi-Select)")) {
						List<String> picklistValues = field.getPicklistValues();
						List<String> values = parseString(value);
						List<String> selectedtValues = new ArrayList<String>();
						for (String eachValue : values) {
							String filteredValue = picklistValues.stream()
									.filter(list -> list.equalsIgnoreCase(eachValue)).findFirst().orElse(null);

							if (filteredValue != null) {
								selectedtValues.add(filteredValue);
							} else {
								addToSet(i, displayLabel + " values are invalid", csvDocument.getCsvImportId());
								error = true;
								break;
							}
						}
						if (error) {
							break;
						} else if (!selectedtValues.isEmpty()) {
							inputMessage.put(fieldName, selectedtValues);
						}

					} else if (dataType.getDisplay().equalsIgnoreCase("Discussion")) {
						Optional<Map<String, Object>> optionalContact = moduleEntryRepository.findEntryById(
								user.get("CONTACT").toString(), moduleService.getCollectionName("Contacts", companyId));
						Map<String, Object> contactEntry = optionalContact.get();
						String firstName = contactEntry.get("FIRST_NAME").toString();
						String lastName = contactEntry.get("LAST_NAME").toString();

						Sender sender = new Sender(firstName, lastName, user.get("USER_UUID").toString(),
								user.get("ROLE").toString());

						DiscussionMessage message = new DiscussionMessage(value, new Date(),
								UUID.randomUUID().toString(), "MESSAGE", null, sender);

						List<DiscussionMessage> messages = Arrays.asList(message);
						inputMessage.put(fieldName, messages);

					} else if (dataType.getDisplay().equalsIgnoreCase("List Text")) {
						List<String> values = parseString(value);
						if (values.size() != 0) {
							inputMessage.put(fieldName, values);
						}

					} else if (dataType.getDisplay().equalsIgnoreCase("Phone")) {
						BasePhone phone = new BasePhone();
						try {
							if (value != null) {
								String separator = csvDocument.getCsvFormat().getSeparator();
								if (separator != null) {
									separator = (separator.equalsIgnoreCase("Blank space")) ? " " : "-";
									String[] split = value.split(separator);
									if (split.length == 2) {
										phone = createPhoneObject(split[0].trim(), split[1].trim(), phone);
									} else {
										error = true;
									}
								} else {
									separator = "-";
									String[] split = value.split(separator);
									if (split.length == 2) {
										phone = createPhoneObject(split[0].trim(), split[1].trim(), phone);
									} else {
										error = true;
									}
								}
							}
							if (error || phone == null) {
								addToSet(i, displayLabel + " value is invalid", csvDocument.getCsvImportId());
								break;
							} else {
								inputMessage.put(fieldName, phone);
							}
						} catch (Exception e) {
							addToSet(i, e.getMessage(), csvDocument.getCsvImportId());
							break;
						}

					} else if (dataType.getDisplay().equalsIgnoreCase("Date/Time")
							|| dataType.getDisplay().equalsIgnoreCase("Date")
							|| dataType.getDisplay().equalsIgnoreCase("Time")) {

						try {
							Date date = new Date();
							SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSX");
							date = df.parse(value);
							inputMessage.put(fieldName, date);
						} catch (Exception e) {
							addToSet(i, displayLabel + " value is invalid", csvDocument.getCsvImportId());
							error = true;
							break;
						}

					} else if (dataType.getDisplay().equalsIgnoreCase("Relationship")
							&& !ignoredFields.contains(fieldName)) {
						if (field.getRelationshipType().equalsIgnoreCase("One To One")) {
							String relationshipId = getRelationshipId(field, companyId, value);

							if (relationshipId != null && checkRelationshipStatus(field, relationshipId, companyId)) {
								Relationship relationship = new Relationship(relationshipId, value);
								inputMessage.put(fieldName, relationship);
							} else {
								String message = (relationshipId == null) ? " relationship value is invalid"
										: " relationship already exist";
								addToSet(i, displayLabel + message, csvDocument.getCsvImportId());
								error = true;
								break;
							}

						} else if (field.getRelationshipType().equalsIgnoreCase("Many To One")) {
							String relationshipId = getRelationshipId(field, companyId, value);
							if (relationshipId != null) {
								Relationship relationship = new Relationship(relationshipId, value);
								inputMessage.put(fieldName, relationship);
							} else {
								addToSet(i, displayLabel + " relationship value is invalid",
										csvDocument.getCsvImportId());
								error = true;
								break;
							}

						} else if (field.getRelationshipType().equalsIgnoreCase("Many To Many")) {
							List<String> values = parseString(value);
							if (values != null) {
								List<Relationship> relationshipList = new ArrayList<Relationship>();
								for (String eachValue : values) {
									String relationshipId = getRelationshipId(field, companyId, eachValue);
									if (relationshipId != null) {
										Relationship relationship = new Relationship(relationshipId, value);
										relationshipList.add(relationship);
									} else {
										addToSet(i, displayLabel + " relationship value is invalid",
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
								addToSet(i, displayLabel + " relationship value is invalid",
										csvDocument.getCsvImportId());
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
			}
		} catch (Exception e) {
			throw new InternalErrorException(e.getMessage());
		}
		return inputMessage;
	}

	public void handleUserModule(Map<String, Object> inputMessage, List<Module> modules, String userUuid, Module module,
			Company company, Map<String, Object> globalTeam, String language, String phoneNumber) {
		try {
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

			if (optionalUser.isEmpty() || isDeleted) {
				Optional<Role> optionalRole = rolesRepository.findById(inputMessage.get("ROLE").toString(),
						moduleService.getCollectionName("roles", companyId));
				Role role = optionalRole.get();
				String existingRoleName = role.getName();

				Optional<Map<String, Object>> optionalTeamsEntry = moduleEntryRepository.findEntryByFieldName("NAME",
						existingRoleName, moduleService.getCollectionName("Teams", companyId));
				Map<String, Object> roleTeam = optionalTeamsEntry.get();
				String roleTeamId = roleTeam.get("_id").toString();

				Optional<Module> optionalTeamsModule = modules.stream().filter(mod -> mod.getName().equals("Teams"))
						.findFirst();
				Module teamsModule = optionalTeamsModule.get();
				String userId = "";

				if (optionalUser.isPresent()) {
					userId = userEntry.get("_id").toString();
					String teamName = userEntry.get("FIRST_NAME") + " " + userEntry.get("LAST_NAME");

					Optional<Map<String, Object>> optionalPersonalTeam = moduleEntryRepository
							.findTeamsByVariableForIsPersonal("NAME", teamName,
									moduleService.getCollectionName("Teams", companyId));
					Map<String, Object> personalTeam = optionalPersonalTeam.get();
					personalTeam.put("DELETED", false);

					updateUsersInTeamsEntry(Arrays.asList().toString(), userId, teamsModule, companyId, personalTeam,
							userUuid);

					userEntry.put("DELETED", false);
					List<String> existingTeams = mapper.readValue(mapper.writeValueAsString(userEntry.get("TEAMS")),
							mapper.getTypeFactory().constructCollectionType(List.class, String.class));
					if (!existingTeams.contains(personalTeam.get("_id").toString())) {
						existingTeams.add(personalTeam.get("_id").toString());
					}

					List<Relationship> teams = getListRelationshipValue("TEAMS", module, companyId, existingTeams);

					userEntry.put("TEAMS", teams);
					dataAPI.putModuleEntry(userEntry, module.getModuleId(), true, companyId, userUuid, false);
				} else {
					Module userModule = modules.stream().filter(mod -> mod.getName().equals("Users")).findFirst()
							.orElse(null);
					userEntry = createUser(inputMessage.get("EMAIL_ADDRESS").toString(), companyId, "", false,
							company.getCompanySubdomain(), "alarm_classic", 0, language,
							inputMessage.get("ROLE").toString(), false, globalTeamId, userUuid, userModule);

					userId = userEntry.get("DATA_ID").toString();

					String userEmailAddress = inputMessage.get("EMAIL_ADDRESS").toString();
					String[] splitEmail = userEmailAddress.split("@");
					String[] names = splitEmail[0].split("\\.");
					String firstName = names[0].trim();
					String lastName = "";
					if (names.length > 1) {
						lastName = names[1].trim();
					}

					Module contactModule = modules.stream().filter(mod -> mod.getName().equals("Contacts")).findFirst()
							.orElse(null);

					Map<String, Object> contactEntry = createContact(firstName, lastName,
							inputMessage.get("ACCOUNT").toString(), new Phone("us", "+1", phoneNumber, "us.svg"),
							contactModule, companyId, globalTeamId, userId, userUuid);

					HashMap<String, Object> team = new HashMap<String, Object>();
					team.put("NAME", inputMessage.get("FIRST_NAME") + " " + inputMessage.get("LAST_NAME"));
					team.put("DESCRIPTION", "Personal team for " + inputMessage.get("FIRST_NAME") + " "
							+ inputMessage.get("LAST_NAME"));

					List<Relationship> users = new ArrayList<Relationship>();
					String primaryDisplayFieldValue = getPrimaryDisplayFieldValue("USERS", teamsModule, companyId,
							userId).toString();
					Relationship userRelationship = new Relationship(userId, primaryDisplayFieldValue);
					users.add(userRelationship);
					team.put("USERS", users);
					team.put("DELETED", false);
					team.put("DATE_CREATED", new Date());
					team.put("DATE_UPDATED", new Date());
					team.put("IS_PERSONAL", true);
					Map<String, Object> personalTeam = createModuleData(companyId, "Teams", team, userUuid, modules);
					String personalTeamId = personalTeam.get("DATA_ID").toString();

					List<String> teamsList = Arrays.asList(personalTeamId, globalTeamId, roleTeamId);

					List<Relationship> teams = getListRelationshipValue("TEAMS", module, companyId, teamsList);

					userEntry.put("TEAMS", teams);
					userEntry.put("IS_LOGIN_ALLOWED", false);

					Relationship contactRelationship = new Relationship(contactEntry.get("DATA_ID").toString(),
							getPrimaryDisplayFieldValue("CONTACT", module, companyId,
									contactEntry.get("DATA_ID").toString()).toString());
					if (contactRelationship != null) {
						userEntry.put("CONTACT", contactRelationship);
					}
					dataAPI.putModuleEntry(userEntry, module.getModuleId(), true, companyId, userUuid, false);
				}

				updateUsersInTeamsEntry(globalTeam.get("USERS").toString(), userId, teamsModule, companyId, globalTeam,
						userUuid);

				updateUsersInTeamsEntry(roleTeam.get("USERS").toString(), userId, teamsModule, companyId, roleTeam,
						userUuid);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException(formatErrorMessage(e.getMessage()));
		}
	}

	public String formatErrorMessage(String message) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			Map<String, String> error = mapper.readValue(message, Map.class);
			message = error.get("ERROR").toString();
		} catch (Exception e) {
			return message;
		}
		return message;
	}

	public Map<Integer, Map<String, Object>> decodeFile(CsvImportData body, List<ModuleField> fields) {
		Map<Integer, Map<String, Object>> rowMap = new HashMap<Integer, Map<String, Object>>();
		boolean isEmpty = true;
		BufferedReader br = null;
		InputStream is = null;
		try {
			List<String> headers = new ArrayList<String>();
			int i = 0;
			Base64.Decoder dec = Base64.getDecoder();
			byte[] decbytes = dec.decode(body.getFile());
			is = new ByteArrayInputStream(decbytes);
			List<CsvHeaders> headersList = body.getHeaders();

			if (body.getFileType().equals("csv")) {

				// DECODING THE BYTE STRING SENT FROM FRONT-END
				br = new BufferedReader(new InputStreamReader(is));
				boolean isHeader = true;
				CSVReader csvReader = new CSVReader(br);
				List<String[]> list = new ArrayList<>();
				list = csvReader.readAll();
				csvReader.close();
				for (String[] column : list) {
					List<String> fieldValues = new ArrayList<String>();
					for (String row : column) {
						if (isHeader) {
							headers.add(row);
						} else {
							fieldValues.add(row);
						}
					}

					if (!isHeader) {

						Map<String, Object> colMap = new HashMap<String, Object>();
						for (ModuleField field : fields) {
							String fieldId = field.getFieldId();
							for (CsvHeaders csvHeader : headersList) {
								if (csvHeader.getFieldId().equals(fieldId)) {
									colMap.put(field.getName(),
											fieldValues.get(headers.indexOf(csvHeader.getHeaderName())));
								}
							}
						}
						rowMap.put(i, colMap);
						i++;
					}
					isHeader = false;
					if (list.indexOf(column) == list.size() - 1) {
						isEmpty = false;
					}
				}

			} else if (body.getFileType().equals("xlsx") || body.getFileType().equals("xls")) {
				Workbook workbook = null;
				if (body.getFileType().equals("xlsx")) {
					workbook = new XSSFWorkbook(is);
				} else {
					workbook = new HSSFWorkbook(is);
				}
				Sheet datatypeSheet = workbook.getSheetAt(0);
				Iterator<Row> iterator = datatypeSheet.iterator();
				int z = 0;
				int lastColumn = 0;
				while (iterator.hasNext()) {
					List<String> values = new ArrayList<String>();
					Map<String, Object> colMap = new HashMap<String, Object>();
					Row currentRow = iterator.next();
					if (z == 0) {
						lastColumn = Math.max(currentRow.getLastCellNum(), 1);
					}
					for (int cn = 0; cn < lastColumn; cn++) {
						Cell currentCell = currentRow.getCell(cn);
						if (z == 0) {
							headers.add(currentCell.toString());
						} else {
							if (currentCell == null || currentCell.toString().isEmpty()) {
								values.add("");
							} else {
								isEmpty = false;
								currentCell.setCellType(CellType.STRING);
								values.add(currentCell.toString());
							}
						}
					}
					if (z > 0) {
						if (!isEmpty) {
							for (ModuleField field : fields) {
								String fieldId = field.getFieldId();
								for (CsvHeaders csvHeader : headersList) {
									if (csvHeader.getFieldId().equals(fieldId)) {
										colMap.put(field.getName(),
												values.get(headers.indexOf(csvHeader.getHeaderName())));
									}
								}
							}
							rowMap.put(z, colMap);
						}
					}
					z++;
				}
				workbook.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalErrorException(e.getMessage());
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new InternalErrorException(e.getMessage());
				}
			}
		}

		if (isEmpty) {
			return null;
		}
		return rowMap;
	}

	public String getAccountId(Map<String, Object> inputMessage, String companyId, List<Module> modules,
			String globalTeamId, String userUuid, CsvImport csvDocument, int i) {

		String accountId = null;
		try {
			if (inputMessage.containsKey("EMAIL_ADDRESS")) {
				String userEmailAddress = inputMessage.get("EMAIL_ADDRESS").toString();
				String[] splitEmail = userEmailAddress.split("@");
				String accountName = "";
				if (splitEmail.length > 1) {
					accountName = splitEmail[1].trim();
				}

				if (!accountExists(accountName, companyId)) {
					Module accountModule = modules.stream().filter(mod -> mod.getName().equals("Accounts")).findFirst()
							.orElse(null);
					Map<String, Object> accountEntry = createAccount(accountName, companyId, globalTeamId, userUuid,
							accountModule);
					accountId = accountEntry.get("DATA_ID").toString();
				} else {
					Optional<Map<String, Object>> optionalAccount = moduleEntryRepository.findEntryByFieldName(
							"ACCOUNT_NAME", accountName, moduleService.getCollectionName("Accounts", companyId));
					Map<String, Object> accountEntry = optionalAccount.get();
					accountId = accountEntry.get("_id").toString();
				}
			} else {
				addToSet(i, "Email address is required", csvDocument.getCsvImportId());
				return null;
			}
		} catch (Exception e) {
			throw new InternalErrorException(e.getMessage());
		}

		return accountId;
	}

}
