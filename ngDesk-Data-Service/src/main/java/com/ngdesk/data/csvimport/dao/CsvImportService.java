package com.ngdesk.data.csvimport.dao;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.stream.Collectors;

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
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.data.dao.DataService;
import com.ngdesk.data.dao.Phone;
import com.ngdesk.data.dao.Relationship;
import com.ngdesk.data.modules.dao.DataType;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
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

		HashMap<String, Object> account = new HashMap<String, Object>();
		account.put("ACCOUNT_NAME", accountName);
		account.put("DATE_CREATED", new Date());
		account.put("DELETED", false);

		List<Relationship> teams = new ArrayList<Relationship>();
		Relationship relationship = new Relationship(globalTeamId,
				getPrimaryDisplayFieldValue("TEAMS", accountModule, companyId, globalTeamId).toString());
		teams.add(relationship);
		account.put("TEAMS", teams);
		try {
			accountEntry = dataAPI.postModuleEntry(account, accountModule.getModuleId(), true, companyId, userUuid);
		} catch (Exception e) {
			throw new InternalErrorException(formatErrorMessage(e.getMessage()));
		}
		return accountEntry;
	}

	public HashMap<String, Object> createUser(String email, String companyId, String password, boolean inviteAccepted,
			String subdomain, String notification, int loginAttempts, String role, boolean disabled,
			String globalTeamId, String userUuid, Module userModule) {

		HashMap<String, Object> userEntry = new HashMap<String, Object>();

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
		user.put("LANGUAGE", "English");
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

		Map<String, Object> responseMap = new HashMap<String, Object>();
		try {
			responseMap = dataAPI.postModuleEntry(user, userModule.getModuleId(), true, companyId, userUuid);
		} catch (Exception e) {
			throw new InternalErrorException(formatErrorMessage(e.getMessage()));
		}
		userEntry.putAll(responseMap);
		return userEntry;
	}

	public Map<String, Object> createContact(String firstName, String lastName, String accountId, Phone phone,
			Module contactModule, String companyId, String globalTeamId, String userId, String userUuid) {

		HashMap<String, Object> contactEntry = new HashMap<String, Object>();

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
		contact.put("ACCOUNT", accountRelationship);

		Relationship userRelationship = new Relationship(userId,
				getPrimaryDisplayFieldValue("USER", contactModule, companyId, userId).toString());
		contact.put("USER", userRelationship);

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

		Map<String, Object> responseMap = new HashMap<String, Object>();
		try {
			responseMap = dataAPI.postModuleEntry(contact, contactModule.getModuleId(), true, companyId, userUuid);
		} catch (Exception e) {
			throw new InternalErrorException(formatErrorMessage(e.getMessage()));
		}
		contactEntry.putAll(responseMap);
		return contactEntry;
	}

	public Map<String, Object> createModuleData(String companyId, String moduleName, Map<String, Object> body,
			String userUuid, List<Module> modules) {
		Map<String, Object> data = new HashMap<String, Object>();

		Optional<Module> optionalModule = modules.stream().filter(mod -> mod.getName().equals(moduleName)).findFirst();
		if (optionalModule.isPresent()) {
			Module module = optionalModule.get();

			HashMap<String, Object> bodyHashMap = new HashMap<String, Object>();
			bodyHashMap.putAll(body);

			try {
				data = dataAPI.postModuleEntry(bodyHashMap, module.getModuleId(), true, companyId, userUuid);
			} catch (Exception e) {
				throw new InternalErrorException(formatErrorMessage(e.getMessage()));
			}

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
							ModuleField relatedField = relationFields.stream().filter(
									relationField -> relationField.getFieldId().equals(field.getRelationshipField()))
									.findFirst().orElse(null);
							if (relatedField != null) {
								String value = getPrimaryDisplayFieldValue(relatedField.getName(), relationModule,
										companyId, dataId).toString();
								Relationship relationship = new Relationship(dataId, value);

								HashMap<String, Object> entry = new HashMap<String, Object>();
								entry.put("DATA_ID", data.get(name));
								entry.put(relatedField.getName(), relationship);
								try {
									dataAPI.putModuleEntry(entry, relationModule.getModuleId(), true, companyId,
											userUuid, false);
								} catch (Exception e) {
									throw new InternalErrorException(formatErrorMessage(e.getMessage()));
								}
							}
						}
					}
				}
			}
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
		Optional<ModuleField> optionalRelationshipField = relationshipModule.getFields().stream()
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
		if (relationshipEntry.get(relationshipFieldName) == null) {
			return null;
		}
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

	public String getRelationshipId(ModuleField field, String companyId, Object value) {

		Optional<Module> optionalRelationshipModule = modulesRepository.findById(field.getModule(),
				moduleService.getCollectionName("modules", companyId));
		if (optionalRelationshipModule.isEmpty()) {
			return null;
		}

		Module relationshipModule = optionalRelationshipModule.get();
		String relationshipModuleName = relationshipModule.getName();
		Optional<ModuleField> optionalRelationshipField = relationshipModule.getFields().stream()
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
		if (relationshipEntry.get("_id") == null) {
			return null;
		}
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

		Optional<ModuleField> optionalRelationshipField = relationshipModule.getFields().stream()
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
			try {
				list = csvReader.readAll();
				csvReader.close();
			} catch (IOException e) {
				throw new InternalErrorException("File corrupted");
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
					}
				}
			}

			list = list.stream().filter(item -> !(item.length == 1 && item[0].isBlank())).collect(Collectors.toList());

			List<String[]> updatedList = new ArrayList<>();
			for (String[] column : list) {
				boolean Invalid = true;
				for (String value : column) {
					if (!value.trim().isBlank()) {
						Invalid = false;
					}
				}
				if (!Invalid) {
					updatedList.add(column);
				}
			}

			for (String[] column : updatedList) {
				List<String> fieldValues = new ArrayList<String>();
				for (String row : column) {
					if (isHeader) {
						headers.add(row.trim());
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
				if (updatedList.indexOf(column) == updatedList.size() - 1) {
					isEmpty = false;
				}
			}
		} else if (body.getFileType().equals("xlsx") || body.getFileType().equals("xls")) {
			Workbook workbook = null;
			Sheet datatypeSheet;
			try {
				if (body.getFileType().equals("xlsx")) {
					workbook = new XSSFWorkbook(is);
				} else {
					workbook = new HSSFWorkbook(is);
				}
				datatypeSheet = workbook.getSheetAt(0);
				workbook.close();
			} catch (IOException e) {
				throw new InternalErrorException("File corrupted");
			}

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
									colMap.put(field.getName(), values.get(headers.indexOf(csvHeader.getHeaderName())));
								}
							}
						}
						rowMap.put(z, colMap);
					}
				}
				z++;
			}
		}

		if (isEmpty) {
			return null;
		}
		return rowMap;
	}

	public String getAccountId(String moduleName, Map<String, Object> inputMessage, String companyId,
			List<Module> modules, String globalTeamId, String userUuid, CsvImport csvDocument, int i) {

		String accountId = null;
		String accountName = "";

		if (moduleName.equals("Users") && inputMessage.containsKey("EMAIL_ADDRESS")) {
			String userEmailAddress = inputMessage.get("EMAIL_ADDRESS").toString();
			String[] splitEmail = userEmailAddress.split("@");
			if (splitEmail.length > 1) {
				accountName = splitEmail[1].trim();
			}
		} else if (moduleName.equals("Contacts") && inputMessage.containsKey("ACCOUNT")) {
			accountName = inputMessage.get("ACCOUNT").toString().trim();
		}

		if (!accountName.isBlank()) {
			if (!accountExists(accountName, companyId)) {
				Module accountModule = modules.stream().filter(mod -> mod.getName().equals("Accounts")).findFirst()
						.orElse(null);
				if (accountModule != null) {
					Map<String, Object> accountEntry = new HashMap<String, Object>();
					try {
						accountEntry = createAccount(accountName, companyId, globalTeamId, userUuid, accountModule);
						accountId = accountEntry.get("DATA_ID").toString();
					} catch (Exception e) {
						throw new InternalErrorException(e.getMessage());
					}
				}
			} else {
				Optional<Map<String, Object>> optionalAccount = moduleEntryRepository.findEntryByFieldName(
						"ACCOUNT_NAME", accountName, moduleService.getCollectionName("Accounts", companyId));
				if (optionalAccount.isPresent()) {
					Map<String, Object> accountEntry = optionalAccount.get();
					accountId = accountEntry.get("_id").toString();
				}
			}
		}
		return accountId;
	}

	public void updateCsvEntry(boolean error, int totalLines, int i, String dataId) {
		int completedCount;
		int failedCount;
		String status;
		if (error == false) {
			completedCount = i;
			status = "COMPLETED";
		} else {
			completedCount = Math.abs(i - 1);
			status = "FAILED";
		}
		failedCount = totalLines - completedCount;

		Map<String, Object> entry = new HashMap<String, Object>();
		entry.put("status", status);
		entry.put("completedCount", completedCount);
		entry.put("failedCount", failedCount);

		csvImportRepository.updateEntry(dataId, entry, "csv_import");
	}

}
