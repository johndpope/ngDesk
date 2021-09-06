package com.ngdesk.data.jobs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.mail.SendMail;
import com.ngdesk.data.company.dao.Company;
import com.ngdesk.data.csvimport.dao.CsvImport;
import com.ngdesk.data.csvimport.dao.CsvImportData;
import com.ngdesk.data.csvimport.dao.CsvImportService;
import com.ngdesk.data.dao.DataService;
import com.ngdesk.data.dao.Phone;
import com.ngdesk.data.elastic.Wrapper;
import com.ngdesk.data.modules.dao.DataType;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.roles.dao.Role;
import com.ngdesk.data.validator.Validator;
import com.ngdesk.repositories.companies.CompanyRepository;
import com.ngdesk.repositories.csvimport.CsvImportRepository;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;
import com.ngdesk.repositories.roles.RolesRepository;
import com.opencsv.CSVReader;

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
	DataService dataService;

	@Autowired
	Validator validator;

	@Autowired
	Wrapper wrapper;

	@Autowired
	CsvImportService csvImportService;

	@Autowired
	SendMail sendMail;

	@Value("${env}")
	private String environment;

	@Scheduled(fixedRate = 30000)
	public void importCsv() {
		BufferedReader br = null;
		InputStream is = null;
		ObjectMapper mapper = new ObjectMapper();

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

					Optional<Role> optionalCustomerRole = rolesRepository.findRoleName("Customers",
							"roles_" + companyId);
					Role customerRole = optionalCustomerRole.get();
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

								if (inputMessage.containsKey("DATE_CREATED") || inputMessage.containsKey("DATE_UPDATED")
										|| inputMessage.containsKey("LAST_UPDATED_BY")
										|| inputMessage.containsKey("CREATED_BY")) {
									inputMessage.remove("DATE_CREATED");
									inputMessage.remove("DATE_UPDATED");
									inputMessage.remove("LAST_UPDATED_BY");
									inputMessage.remove("CREATED_BY");
								}

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
									if (inputMessage.containsKey("PHONE_NUMBER")) {
										phoneNumber = inputMessage.get("PHONE_NUMBER").toString();
										inputMessage.remove("PHONE_NUMBER");
									}
									if (inputMessage.containsKey("EMAIL_ADDRESS")) {
										String userEmailAddress = inputMessage.get("EMAIL_ADDRESS").toString();
										String[] splitEmail = userEmailAddress.split("@");
										String accountName = "";
										if (splitEmail.length > 1) {
											accountName = splitEmail[1];
										}
										String accountId = null;

										if (!csvImportService.accountExists(accountName, companyId)) {
											Map<String, Object> accountEntry = csvImportService
													.createAccount(accountName, companyId, globalTeamId);
											accountId = accountEntry.get("_id").toString();
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
									if (inputMessage.containsKey("DEFAULT_CONTACT_METHOD")) {
										if (inputMessage.get("DEFAULT_CONTACT_METHOD") == null
												|| inputMessage.get("DEFAULT_CONTACT_METHOD").toString().equals("")) {
											inputMessage.put("DEFAULT_CONTACT_METHOD", "Email");
										}
									} else {
										inputMessage.put("DEFAULT_CONTACT_METHOD", "Email");
									}

									inputMessage.put("ROLE", customerRoleId);
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
									if (inputMessage.containsKey(fieldName)) {
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
												csvImportRepository.addToEntrySet(csvDocument.getCsvImportId(), "LOGS",
														logString, "csv_import");
												error = true;
												break;
											}
										}
									}
									if (inputMessage.containsKey(fieldName)) {
										if (dataType.getDisplay().equalsIgnoreCase("Chronometer")) {
											long chronometerValueInSecond = 0;
											if (inputMessage.get(fieldName) != null) {
												String value = inputMessage.get(fieldName).toString();
												String valueWithoutSpace = value.replaceAll("\\s+", "");
												if (valueWithoutSpace.length() == 0
														|| valueWithoutSpace.charAt(0) == '-') {
													inputMessage.put(fieldName, 0);
												} else if (valueWithoutSpace.length() != 0) {
													chronometerValueInSecond = dataService
															.getChronometerValueInMinutes(valueWithoutSpace);
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
									if (inputMessage.containsKey("ACCOUNT") && fieldName.equals("ACCOUNT")
											&& inputMessage.get(fieldName) != null) {
										checkRequired = false;
									}
									if (dataType.getDisplay().equalsIgnoreCase("Auto Number")) {
										int autoNumber = (int) field.getAutoNumberStartingNumber();
										int count = (int) moduleEntryRepository.findCountOfEntries(collectionName);
										if (count == 0) {
											inputMessage.put(fieldName, autoNumber);
										} else {
											Optional<Map<String, Object>> optionalEntry = moduleEntryRepository
													.findBySortingField(fieldName, collectionName);
											Map<String, Object> sortedEntry = optionalEntry.get();
											if (sortedEntry.get(fieldName) != null) {
												autoNumber = Integer.parseInt(sortedEntry.get(fieldName).toString());
												autoNumber++;
											}
											inputMessage.put(fieldName, autoNumber);
										}
									}
									if (field.getRequired()) {
										if (inputMessage.get(fieldName) == null) {
											if (dataType.getDisplay().equalsIgnoreCase("ID")) {
												inputMessage.put(fieldName, UUID.randomUUID().toString());
											}
										}

										if (field.getName().equals("TEAMS")) {
											List<String> teams = new ArrayList<String>();
											if (globalTeamId != null) {
												teams.add(globalTeamId);
											}
											inputMessage.put("TEAMS", teams);
										}

										if (!inputMessage.containsKey(fieldName)
												|| inputMessage.get(fieldName) == null) {

											if (field.getDefaultValue() != null) {
												String defaultValue = field.getDefaultValue();

												Pattern pattern = Pattern.compile("\\{\\{(.*)\\}\\}");
												Matcher matcher = pattern.matcher(defaultValue);

												if (matcher.find()) {

													if (matcher.group(1).equals("CURRENT_CONTACT")) {

														Optional<Map<String, Object>> optionalContactEntry = moduleEntryRepository
																.findEntryByFieldName("USER",
																		csvDocument.getCreatedBy(),
																		"Contacts_" + companyId);
														Map<String, Object> contactEntry = optionalContactEntry.get();

														defaultValue = defaultValue.replaceAll(
																"\\{\\{CURRENT_CONTACT\\}\\}",
																contactEntry.get("_id").toString());

													} else if (matcher.group(1).equals("CURRENT_USER")) {
														defaultValue = defaultValue.replaceAll(
																"\\{\\{CURRENT_USER\\}\\}", csvDocument.getCreatedBy());
													}
												}
												if (backendDataType.equalsIgnoreCase("Array")) {
													inputMessage.put(fieldName, Arrays.asList(defaultValue));
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

								try {
									validator.validateBaseTypes(module, inputMessage, companyId);
								} catch (Exception e) {
									Map<String, Object> log = new HashMap<String, Object>();
									log.put("LINE_NUMBER", i);
									log.put("ERROR_MESSAGE", e.getMessage());
									String logString = new ObjectMapper().writeValueAsString(log);
									csvImportRepository.addToEntrySet(csvDocument.getCsvImportId(), "LOGS", logString,
											"csv_import");
									continue;
								}

								if (moduleName.equalsIgnoreCase("Users")) {
									Optional<Map<String, Object>> optionalUser = moduleEntryRepository
											.findEntryByFieldName("EMAIL_ADDRESS", inputMessage.get("EMAIL_ADDRESS"),
													"Users_" + companyId);

									Map<String, Object> userEntry = new HashMap<String, Object>();
									boolean isDeleted = false;

									if (optionalUser.isPresent()) {
										isDeleted = Boolean.valueOf(optionalUser.get().get("DELETED").toString());
										userEntry = optionalUser.get();
									}

									if (optionalUser.isEmpty() || isDeleted) {
										try {
											Optional<Role> optionalRole = rolesRepository.findById(
													inputMessage.get("ROLE").toString(), "roles_" + companyId);
											Role role = optionalRole.get();
											String existingRoleName = role.getName();

											Optional<Map<String, Object>> optionalTeamsEntry = moduleEntryRepository
													.findEntryByFieldName("NAME", existingRoleName,
															"Teams_" + companyId);
											Map<String, Object> roleTeam = optionalTeamsEntry.get();
											String roleTeamId = roleTeam.get("_id").toString();

											Optional<Module> optionalTeamsModule = modulesRepository
													.findIdbyModuleName("Teams", "modules_" + companyId);
											Module teamsModule = optionalTeamsModule.get();
											String teamsModuleId = teamsModule.getModuleId();

											if (optionalUser.isPresent()) {
												String teamName = userEntry.get("FIRST_NAME") + " "
														+ userEntry.get("LAST_NAME");

												Optional<Map<String, Object>> optionalPersonalTeam = moduleEntryRepository
														.findTeamsByVariableForIsPersonal("NAME", teamName,
																"Teams_" + companyId);
												Map<String, Object> personalTeam = optionalPersonalTeam.get();
												List<String> users = new ArrayList<String>();
												users.add(userEntry.get("_id").toString());
												personalTeam.put("DELETED", false);
												personalTeam.put("USERS", users);
												wrapper.putData(companyId, teamsModule, personalTeam,
														personalTeam.get("_id").toString());

												userEntry.put("DELETED", false);
												List<String> existingTeams = mapper.readValue(
														mapper.writeValueAsString(userEntry.get("TEAMS")),
														mapper.getTypeFactory().constructCollectionType(List.class,
																String.class));
												if (!existingTeams.contains(personalTeam.get("_id").toString())) {
													existingTeams.add(personalTeam.get("_id").toString());
												}
												userEntry.put("TEAMS", existingTeams);
												wrapper.putData(companyId, module, userEntry,
														userEntry.get("_id").toString());
											} else {

												userEntry = csvImportService.createUser(
														inputMessage.get("EMAIL_ADDRESS").toString(), companyId, "",
														false, company.getCompanySubdomain(), "alarm_classic", 0,
														language, inputMessage.get("ROLE").toString(), false,
														globalTeamId);

												String userId = userEntry.get("_id").toString();

												Optional<Module> optionalContactModule = modulesRepository
														.findIdbyModuleName("Contacts", "modules_" + companyId);
												Module contactModule = optionalContactModule.get();

												Map<String, Object> contactEntry = csvImportService.createContact(
														inputMessage.get("FIRST_NAME").toString(),
														inputMessage.get("LAST_NAME").toString(),
														inputMessage.get("ACCOUNT").toString(),
														new Phone("us", "+1", phoneNumber, "us.svg"), contactModule,
														companyId, globalTeamId, userId);

												String teamJson = "{\"NAME\":\"Global\",\"DESCRIPTION\":\"Default Team\",\"USERS\":[\"USER_ID_REPLACE\"],\"DELETED\":false,\"IS_PERSONAL\":false}";
												teamJson = teamJson.replaceAll("USER_ID_REPLACE", userId);

												Map<String, Object> team = new HashMap<String, Object>();
												team.putAll(mapper.readValue(teamJson, Map.class));
												team.put("NAME", inputMessage.get("FIRST_NAME") + " "
														+ inputMessage.get("LAST_NAME"));
												team.put("DESCRIPTION",
														"Personal team for " + inputMessage.get("FIRST_NAME") + " "
																+ inputMessage.get("LAST_NAME"));
												team.put("DATE_CREATED", new Date());
												team.put("DATE_UPDATED", new Date());
												team.put("IS_PERSONAL", true);
												Map<String, Object> personalTeam = csvImportService
														.createModuleData(companyId, "Teams", team);
												String personalTeamId = personalTeam.get("DATA_ID").toString();

												List<String> teams = new ArrayList<String>();
												teams.add(personalTeamId);
												teams.add(globalTeamId);

												teams.add(roleTeamId);
												userEntry.put("TEAMS", teams);
												userEntry.put("IS_LOGIN_ALLOWED", false);
												userEntry.put("CONTACT", contactEntry.get("_id").toString());

												wrapper.putData(companyId, module, userEntry,
														userEntry.get("_id").toString());
											}
											List<String> globalUsers = mapper.readValue(
													mapper.writeValueAsString(globalTeam.get("USERS")),
													mapper.getTypeFactory().constructCollectionType(List.class,
															String.class));

											globalUsers.add(userEntry.get("_id").toString());
											globalTeam.put("USERS", globalUsers);
											wrapper.putData(companyId, teamsModule, globalTeam, globalTeamId);

											List<String> roleTeamUsers = mapper.readValue(
													mapper.writeValueAsString(roleTeam.get("USERS")),
													mapper.getTypeFactory().constructCollectionType(List.class,
															String.class));
											roleTeamUsers.add(userEntry.get("_id").toString());
											roleTeam.put("USERS", roleTeamUsers);
											wrapper.putData(companyId, teamsModule, roleTeam, roleTeamId);

										} catch (Exception e) {
											continue;
										}
									}
								} else {
									try {
										if (moduleName.equals("Accounts")) {
											if (!csvImportService.accountExists(
													inputMessage.get("ACCOUNT_NAME").toString(), companyId)) {
												csvImportService.createModuleData(companyId, moduleName, inputMessage);
											}
										} else {
											csvImportService.createModuleData(companyId, moduleName, inputMessage);
										}
									} catch (Exception e) {
										continue;
									}
								}
							}
							csvImportRepository.updateEntry(csvDocument.getCsvImportId(), "STATUS", "COMPLETED",
									"csv_import");
						}
					} else {
						csvImportRepository.updateEntry(csvDocument.getCsvImportId(), "STATUS", "FAILED", "csv_import");
					}

				} catch (Exception e) {

					e.printStackTrace();

					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					String sStackTrace = sw.toString();
					String info = "<br>Subdomain: " + companySubdomain + "<br>File Name: " + csvDocument.getName();

					if (environment.equals("prd")) {
						sendMail.send("spencer@allbluesolutions.com", "support@ngdesk.com",
								"Internal Error: Stack Trace", sStackTrace + info);

						sendMail.send("shashank.shankaranand@allbluesolutions.com", "support@ngdesk.com",
								"Internal Error: Stack Trace", sStackTrace);

					}

					csvImportRepository.updateEntry(csvDocument.getCsvImportId(), "STATUS", "FAILED", "csv_import");
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
