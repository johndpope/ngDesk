package com.ngdesk.data.jobs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.ngdesk.commons.Global;
import com.ngdesk.data.company.dao.Company;
import com.ngdesk.data.csvimport.dao.CsvImport;
import com.ngdesk.data.csvimport.dao.CsvImportData;
import com.ngdesk.data.dao.Phone;
import com.ngdesk.data.modules.dao.DataType;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.roles.dao.Role;
import com.ngdesk.repositories.companies.CompanyRepository;
import com.ngdesk.repositories.csvimport.CsvImportRepository;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;
import com.ngdesk.repositories.roles.RolesRepository;
import com.opencsv.CSVReader;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Component
public class CsvImportJob {

	@Autowired
	CsvImportRepository csvImportRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	ModulesRepository modulesRepository;
	
	@Autowired
	Global global;

	@Scheduled(fixedRate = 30000)
	public void importCsv() {
		BufferedReader br = null;
		InputStream is = null;

		try {
			List<CsvImport> csvImports = csvImportRepository.findEntriesByVariable("STATUS", "QUEUED", "csv_import");

			for (CsvImport csvDocument : csvImports) {

				String companyId = csvDocument.getCompanyId();
				Optional<Company> optionalCompany = companyRepository.findById(companyId, "companies");

				if (optionalCompany.isEmpty()) {
					continue;
				}

				Company company = optionalCompany.get();
				String companySubdomain = company.getCompanySubdomain();
				String language = company.getLanguage();

				try {
					Optional<Map<String, Object>> optionalGlobalTeam = moduleEntryRepository
							.findEntryByFieldName("NAME", "Global", "Teams_" + companyId);

					Map<String, Object> globalTeam = optionalGlobalTeam.get();
					String globalTeamId = globalTeam.get("_id").toString();

					Optional<Role> optionalRole = rolesRepository.findRoleName("Customers", "roles_" + companyId);
					Role customerRole = optionalRole.get();
					String customerRoleId = customerRole.getId();

					csvImportRepository.updateEntry(csvDocument.getCsvImportId(), "STATUS", "PROCESSING", "csv_import");

					CsvImportData body = csvDocument.getCsvImportData();
					String moduleId = csvDocument.getModuleId();

					Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);

					if (optionalModule.isPresent()) {
						// CREATE THE ENTRY
						Module module = optionalModule.get();
						String moduleName = module.getName();
						List<ModuleField> fields = module.getFields();

						Map rowMap = new HashMap<Integer, HashMap<String, String>>();
						List<String> headers = new ArrayList<String>();
						int i = 0;
						Base64.Decoder dec = Base64.getDecoder();
						byte[] decbytes = dec.decode(body.getFile());
						is = new ByteArrayInputStream(decbytes);
						Map<String, String> headerMap = body.getHeaders();
						boolean isEmpty = true;

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

									Map colMap = new HashMap<String, Object>();
									for (ModuleField field : fields) {
										String fieldId = field.getFieldId();
										if (headerMap.containsKey(fieldId)) {
											String csvDisplayLabel = headerMap.get(fieldId).toString();
											colMap.put(field.getName(),
													fieldValues.get(headers.indexOf(csvDisplayLabel)));
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
								Map colMap = new HashMap<String, Object>();
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
											if (headerMap.containsKey(fieldId)) {
												String csvDisplayLabel = headerMap.get(fieldId).toString();
												colMap.put(field.getName(),
														values.get(headers.indexOf(csvDisplayLabel)));
											}
										}
										rowMap.put(z, colMap);
									}
								}
								z++;
							}
							workbook.close();
						}
						if (isEmpty) {
							csvImportRepository.updateEntry(csvDocument.getCsvImportId(), "STATUS", "FAILED",
									"csv_import");
							continue;
						} else {
							Iterator<Map.Entry<Integer, HashMap<String, Object>>> itr = rowMap.entrySet().iterator();
							i = 0;
							while (itr.hasNext()) {
								i++;
								Map.Entry<Integer, HashMap<String, Object>> entry = itr.next();
								Map<String, Object> inputMessage = new HashMap<String, Object>();
								try {
									inputMessage = entry.getValue();
								} catch (Exception e) {
									continue;
								}

								// SimpleDateFormat format = new
								// SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

								inputMessage.put("DATE_CREATED", new Date());
								inputMessage.put("DATE_UPDATED", new Date());
								inputMessage.put("CREATED_BY", csvDocument.getCreatedBy());
								inputMessage.put("LAST_UPDATED_BY", csvDocument.getCreatedBy());
								inputMessage.put("DELETED", false);
								inputMessage.put("SOURCE_TYPE", "web");

								// GET THE ENTRY COLLECTION BASED ON SELECTED MODULE
								String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
								Map<String, String> fieldIdNameMap = new HashMap<String, String>();
								String phoneNumber = "";
								if (moduleName.equals("Users")) {
									if (inputMessage.get("PHONE_NUMBER") != null) {
										phoneNumber = inputMessage.get("PHONE_NUMBER").toString();
										inputMessage.remove("PHONE_NUMBER");
									}
									if (inputMessage.get("EMAIL_ADDRESS") != null) {
										String userEmailAddress = inputMessage.get("EMAIL_ADDRESS").toString();
										String[] splitEmail = userEmailAddress.split("@");
										String accountName = "";
										if (splitEmail.length > 1) {
											accountName = splitEmail[1];
										}
										String accountId = null;

										if (!account.accountExists(accountName, companyId)) {
											Document accountDocument = account.createAccount(accountName, companyId,
													globalTeamId);
											accountId = accountDocument.getObjectId("_id").toString();
											inputMessage.put("ACCOUNT", accountId);
										} else {
											Optional<Map<String, Object>> optionalAccount = moduleEntryRepository
													.findEntryByFieldName("ACCOUNT_NAME", accountName,
															"Accounts_" + companyId);
											Map<String, Object> accountEntry = optionalAccount.get();
											accountId = accountEntry.get("_id").toString();
											inputMessage.put("ACCOUNT", accountId);
										}
									}
									if (inputMessage.get("DEFAULT_CONTACT_METHOD") == null
											|| inputMessage.get("DEFAULT_CONTACT_METHOD").toString().equals("")) {
										inputMessage.put("DEFAULT_CONTACT_METHOD", "Email");
									} else {
										inputMessage.put("DEFAULT_CONTACT_METHOD", "Email");
									}

									inputMessage.put("ROLE", customerRole);
									inputMessage.put("IS_LOGIN_ALLOWED", false);
									inputMessage.put("INVITE_ACCEPTED", false);

								}

								boolean error = false;
								for (ModuleField field : fields) {
									String fieldName = field.getName();
									String displayLabel = field.getDisplayLabel();
									fieldIdNameMap.put(field.getFieldId(), displayLabel);
									DataType dataType = field.getDataType();
									String backendDataType = dataType.getBackend();

									// VALIDATE IF THE PICKLIST VALUES ARE MATCHING WITH EXISTING PICKLISTS OR ELSE
									// THROW ERROR
									if (inputMessage.get(fieldName) != null) {
										if (dataType.getDisplay().equalsIgnoreCase("Picklist")) {
											List<String> picklistValues = field.getPicklistValues();
											String value = inputMessage.get(fieldName).toString();
											boolean valueExists = false;
											for (int f = 0; f < picklistValues.size(); f++) {
												if (picklistValues.get(f).equals(value)) {
													valueExists = true;
													break;
												}
											}
											if (!valueExists) {
												Map<String, Object> log = new HashMap<String, Object>();
												log.put("LINE_NUMBER", i);
												log.put("ERROR_MESSAGE", "Picklist values are incorrect");
												String logString = new ObjectMapper().writeValueAsString(log); 
												csvImportRepository.addToEntrySet(csvDocument.getCsvImportId(), "LOGS", logString, "csv_import");
												error = true;
												break;
											}
										}
									}
									if (inputMessage.get(fieldName) != null) {
										if (dataType.getDisplay().equalsIgnoreCase("Chronometer")) {
											long chronometerValueInSecond = 0;
											if (inputMessage.get(fieldName) != null) {
												String value = inputMessage.get(fieldName).toString();
												String valueWithoutSpace = value.replaceAll("\\s+", "");
												if (valueWithoutSpace.length() == 0
														|| valueWithoutSpace.charAt(0) == '-') {
													inputMessage.put(fieldName, 0);
												} else if (valueWithoutSpace.length() != 0) {
													chronometerValueInSecond = global
															.chronometerValueConversionInSeconds(valueWithoutSpace);
													inputMessage.put(fieldName, chronometerValueInSecond);
												}
											}
										}
									}

									boolean checkRequired = true;
									if (moduleName.equals("Tickets")) {
										if (dataType.getDisplay().equalsIgnoreCase("Discussion")
												|| dataType.getDisplay().equalsIgnoreCase("Relationship")) {
											checkRequired = false;
										}
									}
									if (inputMessage.has("ACCOUNT") && fieldName.equals("ACCOUNT")
											&& inputMessage.get(fieldName) != null) {
										checkRequired = false;
									}
									if (dataType.getString("DISPLAY").equalsIgnoreCase("Auto Number")) {
										int autoNumber = (int) field.get("AUTO_NUMBER_STARTING_NUMBER");
										if (collection.countDocuments() == 0) {
											inputMessage.put(fieldName, autoNumber);
										} else {
											Document document = collection.find().sort(Sorts.descending(fieldName))
													.first();
											if (document.get(fieldName) != null) {
												autoNumber = Integer.parseInt(document.get(fieldName).toString());
												autoNumber++;
											}
											inputMessage.put(fieldName, autoNumber);
										}
									}
									if (field.getBoolean("REQUIRED")) {
										if (inputMessage.isNull(fieldName)) {
											if (dataType.getString("DISPLAY").equalsIgnoreCase("ID")) {
												inputMessage.put(fieldName, UUID.randomUUID().toString());
											}
										}

										if (field.getString("NAME").equals("TEAMS")) {
											List<String> teams = new ArrayList<String>();
											if (globalTeamId != null) {
												teams.add(globalTeamId);
											}
											inputMessage.put("TEAMS", teams);
										}

										if (!inputMessage.has(fieldName) || inputMessage.get(fieldName) == null) {

											if (field.containsKey("DEFAULT_VALUE")
													&& field.get("DEFAULT_VALUE") != null) {
												String defaultValue = field.getString("DEFAULT_VALUE");

												Pattern pattern = Pattern.compile("\\{\\{(.*)\\}\\}");
												Matcher matcher = pattern.matcher(defaultValue);

												if (matcher.find()) {

													if (matcher.group(1).equals("CURRENT_CONTACT")) {
														MongoCollection<Document> Contactcollection = mongoTemplate
																.getCollection("Contacts_" + companyId);
														ObjectId currentContactId = Contactcollection
																.find(Filters.eq("USER",
																		csvDocument.getString("CREATED_BY")))
																.first().getObjectId("_id");
														defaultValue = defaultValue.replaceAll(
																"\\{\\{CURRENT_CONTACT\\}\\}",
																currentContactId.toString());

													} else if (matcher.group(1).equals("CURRENT_USER")) {
														defaultValue = defaultValue.replaceAll(
																"\\{\\{CURRENT_USER\\}\\}",
																csvDocument.getString("CREATED_BY"));

													}
												}

												if (backendDataType.equalsIgnoreCase("Array")) {
													inputMessage.put(fieldName, new JSONArray().put(defaultValue));
												} else if (backendDataType.equalsIgnoreCase("Boolean")) {
													if (defaultValue.equals("true")) {
														inputMessage.put(fieldName, true);
													} else {
														inputMessage.put(fieldName, false);
													}
												} else {
													inputMessage.put(fieldName, defaultValue);
												}
											}
										}

									}
								}
								if (error) {
									continue;
								}
								boolean isValidBaseType = false;

								try {

									isValidBaseType = validator.isValidBaseTypes(inputMessage, companyId, moduleName,
											"POST");
								} catch (Exception e) {
									JSONObject log = new JSONObject();
									log.put("LINE_NUMBER", i);
									log.put("ERROR_MESSAGE", e.getMessage());
									csvCollection.findOneAndUpdate(
											Filters.eq("_id", new ObjectId(csvDocument.getObjectId("_id").toString())),
											Updates.addToSet("LOGS", Document.parse(log.toString())));
									continue;
								}

								if (moduleName.equalsIgnoreCase("Users")) {
									Document userDocument = usersCollection
											.find(Filters.eq("EMAIL_ADDRESS", inputMessage.getString("EMAIL_ADDRESS")))
											.first();
									if (userDocument == null || userDocument.getBoolean("DELETED")) {
										try {
											String existingRoleName = rolesCollection
													.find(Filters.eq("_id",
															new ObjectId(inputMessage.getString("ROLE"))))
													.first().getString("NAME");
											Document roleTeam = teamsCollection
													.find(Filters.eq("NAME", existingRoleName)).first();
											String roleTeamId = roleTeam.getObjectId("_id").toString();

											Document teamsModule = modulecollection.find(Filters.eq("NAME", "Teams"))
													.first();
											String teamsModuleId = teamsModule.getObjectId("_id").toString();

											if (userDocument != null) {
												String teamName = userDocument.getString("FIRST_NAME") + " "
														+ userDocument.getString("LAST_NAME");
												Document personalTeam = teamsCollection
														.find(Filters.and(Filters.eq("DELETED", true),
																Filters.eq("NAME", teamName),
																Filters.eq("IS_PERSONAL", true)))
														.first();
												List<String> users = new ArrayList<String>();
												users.add(userDocument.getObjectId("_id").toString());
												personalTeam.put("DELETED", false);
												personalTeam.put("USERS", users);
												wrapper.putData(companyId, teamsModuleId, "Teams",
														personalTeam.toJson(),
														personalTeam.getObjectId("_id").toString());

												userDocument.put("DELETED", false);
												List<String> existingTeams = (List<String>) userDocument.get("TEAMS");
												if (!existingTeams
														.contains(personalTeam.getObjectId("_id").toString())) {
													existingTeams.add(personalTeam.getObjectId("_id").toString());
												}
												userDocument.put("TEAMS", existingTeams);
												wrapper.putData(companyId, moduleId, moduleName, userDocument.toJson(),
														userDocument.getObjectId("_id").toString());
											} else {

												userDocument = userDAO.createUser(
														inputMessage.getString("EMAIL_ADDRESS"), companyId, "", false,
														company.getString("COMPANY_SUBDOMAIN"), "alarm_classic", 0,
														language, inputMessage.getString("ROLE"), false, globalTeamId);

												String userId = userDocument.getObjectId("_id").toString();

												Document contactsModule = modulecollection
														.find(Filters.eq("NAME", "Contacts")).first();
												String contactsModuleId = contactsModule.getObjectId("_id").toString();

												Document contactDocument = userDAO.createContact(
														inputMessage.getString("FIRST_NAME"),
														inputMessage.getString("LAST_NAME"),
														inputMessage.getString("ACCOUNT"),
														new Phone("us", "+1", phoneNumber, "us.svg"), contactsModuleId,
														companyId, globalTeamId, userId);

												String teamJson = global.getFile("DefaultTeam.json");
												teamJson = teamJson.replaceAll("USER_ID_REPLACE",
														userDocument.getObjectId("_id").toString());

												JSONObject team = new JSONObject(teamJson);
												team.put("NAME", inputMessage.getString("FIRST_NAME") + " "
														+ inputMessage.getString("LAST_NAME"));
												team.put("DESCRIPTION",
														"Personal team for " + inputMessage.getString("FIRST_NAME")
																+ " " + inputMessage.getString("LAST_NAME"));
												team.put("DATE_CREATED", new Date());
												team.put("DATE_UPDATED", new Date());
												team.put("IS_PERSONAL", true);
												String personalTeam = dataService.createModuleData(companyId, "Teams",
														team.toString());
												String personalTeamId = Document.parse(personalTeam)
														.getString("DATA_ID");

												List<String> teams = new ArrayList<String>();
												teams.add(personalTeamId);
												teams.add(globalTeamId);

												teams.add(roleTeamId);
												userDocument.put("TEAMS", teams);
												userDocument.put("IS_LOGIN_ALLOWED", false);
												userDocument.put("CONTACT",
														contactDocument.getObjectId("_id").toString());

												wrapper.putData(companyId, moduleId, moduleName, userDocument.toJson(),
														userDocument.getObjectId("_id").toString());
											}

											List<String> globalUsers = (List<String>) globalTeam.get("USERS");
											globalUsers.add(userDocument.getObjectId("_id").toString());
											globalTeam.put("USERS", globalUsers);
											wrapper.putData(companyId, teamsModuleId, "Teams", globalTeam.toJson(),
													globalTeamId);

											List<String> roleTeamUsers = (List<String>) roleTeam.get("USERS");
											roleTeamUsers.add(userDocument.getObjectId("_id").toString());
											roleTeam.put("USERS", roleTeamUsers);
											wrapper.putData(companyId, teamsModuleId, "Teams", roleTeam.toJson(),
													roleTeamId);

										} catch (Exception e) {
											continue;
										}
									}
								} else {
									try {
										if (moduleName.equals("Accounts")) {
											if (!account.accountExists(inputMessage.getString("ACCOUNT_NAME"),
													companyId)) {
												dataService.createModuleData(companyId, moduleName,
														inputMessage.toString());
											}
										} else {
											dataService.createModuleData(companyId, moduleName,
													inputMessage.toString());
										}
									} catch (Exception e) {
										continue;
									}
								}
							}
							csvCollection.findOneAndUpdate(
									Filters.eq("_id", new ObjectId(csvDocument.getObjectId("_id").toString())),
									Updates.set("STATUS", "COMPLETED"));

						}
					}

				} catch (Exception e) {

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
