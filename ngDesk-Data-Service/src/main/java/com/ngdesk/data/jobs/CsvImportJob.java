package com.ngdesk.data.jobs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
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
import com.ngdesk.data.csvimport.dao.CsvHeaders;
import com.ngdesk.data.csvimport.dao.CsvImport;
import com.ngdesk.data.csvimport.dao.CsvImportData;
import com.ngdesk.data.csvimport.dao.CsvImportLog;
import com.ngdesk.data.csvimport.dao.CsvImportService;
import com.ngdesk.data.dao.BasePhone;
import com.ngdesk.data.dao.DataService;
import com.ngdesk.data.dao.Phone;
import com.ngdesk.data.modules.dao.DataType;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.roles.dao.Role;
import com.ngdesk.data.sam.dao.DataProxy;
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
	DataProxy dataAPI;

	@Autowired
	CsvImportService csvImportService;

	@Autowired
	SendMail sendMail;

	@Value("${env}")
	private String environment;

	@Scheduled(fixedRate = 1000)
	public void importCsv() {
		BufferedReader br = null;
		InputStream is = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			List<CsvImport> csvImports = csvImportRepository.findEntriesByVariable("status", "QUEUED", "csv_import");

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

					Optional<Map<String, Object>> optionalUserEntry = moduleEntryRepository.findEntryById(
							csvDocument.getCreatedBy(), csvImportService.generateCollectionName("Users", companyId));
					Map<String, Object> user = optionalUserEntry.get();
					String userUuid = user.get("USER_UUID").toString();
					System.out.println(userUuid);

					Optional<Map<String, Object>> optionalGlobalTeam = moduleEntryRepository.findEntryByFieldName(
							"NAME", "Global", csvImportService.generateCollectionName("Teams", companyId));

					HashMap<String, Object> globalTeam = new HashMap<String, Object>();
					globalTeam.putAll(optionalGlobalTeam.get());
					String globalTeamId = globalTeam.get("_id").toString();

					Optional<Role> optionalCustomerRole = rolesRepository.findRoleName("Customers",
							csvImportService.generateCollectionName("roles", companyId));
					Role customerRole = optionalCustomerRole.get();
					String customerRoleId = customerRole.getId();

					csvImportRepository.updateEntry(csvDocument.getCsvImportId(), "status", "PROCESSING", "csv_import");

					CsvImportData body = csvDocument.getCsvImportData();
					String moduleId = csvDocument.getModuleId();

					Optional<Module> optionalModule = modulesRepository.findById(moduleId,
							csvImportService.generateCollectionName("modules", companyId));

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
						List<CsvHeaders> headersList = body.getHeaders();
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
						if (isEmpty) {
							csvImportRepository.updateEntry(csvDocument.getCsvImportId(), "status", "FAILED",
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
								String collectionName = csvImportService.generateCollectionName(moduleName, companyId);
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
													.createAccount(accountName, companyId, globalTeamId, userUuid);
											accountId = accountEntry.get("_id").toString();
											inputMessage.put("ACCOUNT", accountId);
										} else {
											Optional<Map<String, Object>> optionalAccount = moduleEntryRepository
													.findEntryByFieldName("ACCOUNT_NAME", accountName, csvImportService
															.generateCollectionName("Accounts", companyId));
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

											if (!picklistValues.contains(value)) {
												CsvImportLog log = new CsvImportLog();
												log.setLineNumber(i);
												log.setErrorMessage("Picklist values are incorrect");
												csvImportRepository.addToEntrySet(csvDocument.getCsvImportId(), "logs",
														log, "csv_import");
												error = true;
												break;
											}
										}
									}

									// VALIDATE IF THE MULTI PICKLIST VALUES ARE MATCHING WITH EXISTING PICKLISTS OR
									// ELSE
									if (inputMessage.containsKey(fieldName)) {
										if (dataType.getDisplay().equalsIgnoreCase("Picklist (Multi-Select)")) {
											List<String> picklistValues = field.getPicklistValues();
											List<String> values = mapper.readValue(
													mapper.writeValueAsString(inputMessage.get(fieldName)),
													mapper.getTypeFactory().constructCollectionType(List.class,
															String.class));
											List<String> selectedtValues = new ArrayList<String>();
											for (String value : values) {
												if (!picklistValues.contains(value)) {
													Map<String, Object> log = new HashMap<String, Object>();
													log.put("LINE_NUMBER", i);
													log.put("ERROR_MESSAGE", "Picklist values are incorrect");
													String logString = new ObjectMapper().writeValueAsString(log);
													csvImportRepository.addToEntrySet(csvDocument.getCsvImportId(),
															"LOGS", logString, "csv_import");
													error = true;
													break;
												} else {
													selectedtValues.add(value);
												}

											}
											if (!selectedtValues.isEmpty()) {
												inputMessage.put(fieldName, selectedtValues);
											}

										}
									}
									// LIST TEXT DATA TYPE HANDLED
									if (inputMessage.containsKey(fieldName)) {
										if (dataType.getDisplay().equalsIgnoreCase("LIST_TEXT")) {

											List<String> values = mapper.readValue(
													mapper.writeValueAsString(inputMessage.get(fieldName)),
													mapper.getTypeFactory().constructCollectionType(List.class,
															String.class));
											if (values.size() != 0) {
												inputMessage.put(fieldName, values);
											}
										}
									}
									// date, date/time ,time data type handled
									if (inputMessage.containsKey(fieldName)) {

										if (dataType.getDisplay().equalsIgnoreCase("Date/Time")
												|| dataType.getDisplay().equalsIgnoreCase("Date")
												|| dataType.getDisplay().equalsIgnoreCase("Time")) {

											String value = inputMessage.get(fieldName).toString();
											try {
												Date date = new Date();
												SimpleDateFormat df = new SimpleDateFormat(
														"yyyy-MM-dd'T'hh:mm:ss.SSSX");
												date = df.parse(value);
												inputMessage.put(fieldName, date);
											} catch (ParseException e) {

												try {
													Date parsedDate = new Date(Long.valueOf(value));

													inputMessage.put(fieldName,
															new SimpleDateFormat("HH:mm a").format(parsedDate));
												} catch (NumberFormatException e1) {
													Map<String, Object> log = new HashMap<String, Object>();
													log.put("LINE_NUMBER", i);
													log.put("ERROR_MESSAGE", fieldName + " values are invalid");
													String logString = new ObjectMapper().writeValueAsString(log);
													csvImportRepository.addToEntrySet(csvDocument.getCsvImportId(),
															"LOGS", logString, "csv_import");
													error = true;
													break;
												}

											}
										}
									}

									// Phone Data TYPE HANDLED
									if (inputMessage.containsKey(fieldName)) {
										BasePhone phone = new BasePhone();
										if (dataType.getDisplay().equalsIgnoreCase("Phone")) {
											String value = inputMessage.get(fieldName).toString();
											if (value != null) {
												if (value.contains("-")) {

													String[] split = value.split("-");

													phone = csvImportService.getPhoneObj(split[0], split[1], phone);

												} else if (value.contains(" ")) {
													String[] split = value.split(" ");

													phone = csvImportService.getPhoneObj(split[0], split[1], phone);

												}
											}
										}
										inputMessage.put(fieldName, phone);
									}

									if (inputMessage.containsKey(fieldName)) {
										if (dataType.getDisplay().equalsIgnoreCase("Chronometer")) {
											if (inputMessage.get(fieldName) != null) {
												String value = inputMessage.get(fieldName).toString();
												String valueWithoutSpace = value.replaceAll("\\s+", "");
												if (valueWithoutSpace.length() == 0
														|| valueWithoutSpace.charAt(0) == '-') {
													inputMessage.put(fieldName, 0);
												} else if (valueWithoutSpace.length() != 0) {
													inputMessage.put(fieldName, valueWithoutSpace);
												}
											}
										}
									}

									boolean checkRequired = true;
									// check
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
																		csvImportService.generateCollectionName(
																				"Contacts", companyId));
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

//								try {
//									validator.validateBaseTypes(module, inputMessage, companyId);
//								} catch (Exception e) {
//									CsvImportLog log = new CsvImportLog();
//									log.setLineNumber(i);
//									log.setErrorMessage(e.getMessage());
//									csvImportRepository.addToEntrySet(csvDocument.getCsvImportId(), "logs", log,
//											"csv_import");
//									continue;
//								}

								if (moduleName.equalsIgnoreCase("Users")) {
									Optional<Map<String, Object>> optionalUser = moduleEntryRepository
											.findEntryByFieldName("EMAIL_ADDRESS", inputMessage.get("EMAIL_ADDRESS"),
													csvImportService.generateCollectionName("Users", companyId));

									HashMap<String, Object> userEntry = new HashMap<String, Object>();
									boolean isDeleted = false;

									if (optionalUser.isPresent()) {
										isDeleted = Boolean.valueOf(optionalUser.get().get("DELETED").toString());
										userEntry.putAll(optionalUser.get());
									}

									if (optionalUser.isEmpty() || isDeleted) {
										try {
											Optional<Role> optionalRole = rolesRepository.findById(
													inputMessage.get("ROLE").toString(),
													csvImportService.generateCollectionName("roles", companyId));
											Role role = optionalRole.get();
											String existingRoleName = role.getName();

											Optional<Map<String, Object>> optionalTeamsEntry = moduleEntryRepository
													.findEntryByFieldName("NAME", existingRoleName, csvImportService
															.generateCollectionName("Teams", companyId));
											HashMap<String, Object> roleTeam = new HashMap<String, Object>();
											roleTeam.putAll(optionalTeamsEntry.get());
											String roleTeamId = roleTeam.get("_id").toString();

											Optional<Module> optionalTeamsModule = modulesRepository.findIdbyModuleName(
													"Teams",
													csvImportService.generateCollectionName("modules", companyId));
											Module teamsModule = optionalTeamsModule.get();
											String teamsModuleId = teamsModule.getModuleId();

											if (optionalUser.isPresent()) {
												String teamName = userEntry.get("FIRST_NAME") + " "
														+ userEntry.get("LAST_NAME");

												Optional<Map<String, Object>> optionalPersonalTeam = moduleEntryRepository
														.findTeamsByVariableForIsPersonal("NAME", teamName,
																csvImportService.generateCollectionName("Teams",
																		companyId));
												HashMap<String, Object> personalTeam = new HashMap<String, Object>();
												personalTeam.putAll(optionalPersonalTeam.get());
												List<String> users = new ArrayList<String>();
												users.add(userEntry.get("_id").toString());
												personalTeam.put("DELETED", false);
												personalTeam.put("USERS", users);
												dataAPI.putModuleEntry(personalTeam, teamsModuleId, true, companyId,
														userUuid, false);

												userEntry.put("DELETED", false);
												List<String> existingTeams = mapper.readValue(
														mapper.writeValueAsString(userEntry.get("TEAMS")),
														mapper.getTypeFactory().constructCollectionType(List.class,
																String.class));
												if (!existingTeams.contains(personalTeam.get("_id").toString())) {
													existingTeams.add(personalTeam.get("_id").toString());
												}
												userEntry.put("TEAMS", existingTeams);
												dataAPI.putModuleEntry(userEntry, module.getModuleId(), true, companyId,
														userUuid, false);
											} else {

												userEntry = csvImportService.createUser(
														inputMessage.get("EMAIL_ADDRESS").toString(), companyId, "",
														false, company.getCompanySubdomain(), "alarm_classic", 0,
														language, inputMessage.get("ROLE").toString(), false,
														globalTeamId, userUuid);

												String userId = userEntry.get("_id").toString();

												Optional<Module> optionalContactModule = modulesRepository
														.findIdbyModuleName("Contacts", csvImportService
																.generateCollectionName("modules", companyId));
												Module contactModule = optionalContactModule.get();

												Map<String, Object> contactEntry = csvImportService.createContact(
														inputMessage.get("FIRST_NAME").toString(),
														inputMessage.get("LAST_NAME").toString(),
														inputMessage.get("ACCOUNT").toString(),
														new Phone("us", "+1", phoneNumber, "us.svg"), contactModule,
														companyId, globalTeamId, userId, userUuid);

												String teamJson = "{\"NAME\":\"Global\",\"DESCRIPTION\":\"Default Team\",\"USERS\":[\"USER_ID_REPLACE\"],\"DELETED\":false,\"IS_PERSONAL\":false}";
												teamJson = teamJson.replaceAll("USER_ID_REPLACE", userId);

												HashMap<String, Object> team = new HashMap<String, Object>();
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
														.createModuleData(companyId, "Teams", team, userUuid);
												String personalTeamId = personalTeam.get("DATA_ID").toString();

												List<String> teams = new ArrayList<String>();
												teams.add(personalTeamId);
												teams.add(globalTeamId);

												teams.add(roleTeamId);
												userEntry.put("TEAMS", teams);
												userEntry.put("IS_LOGIN_ALLOWED", false);
												userEntry.put("CONTACT", contactEntry.get("_id").toString());

												dataAPI.putModuleEntry(userEntry, module.getModuleId(), true, companyId,
														userUuid, false);
											}
											List<String> globalUsers = mapper.readValue(
													mapper.writeValueAsString(globalTeam.get("USERS")),
													mapper.getTypeFactory().constructCollectionType(List.class,
															String.class));

											globalUsers.add(userEntry.get("_id").toString());
											globalTeam.put("USERS", globalUsers);
											dataAPI.putModuleEntry(globalTeam, teamsModuleId, true, companyId, userUuid,
													false);

											List<String> roleTeamUsers = mapper.readValue(
													mapper.writeValueAsString(roleTeam.get("USERS")),
													mapper.getTypeFactory().constructCollectionType(List.class,
															String.class));
											roleTeamUsers.add(userEntry.get("_id").toString());
											roleTeam.put("USERS", roleTeamUsers);
											dataAPI.putModuleEntry(roleTeam, teamsModuleId, true, companyId, userUuid,
													false);
										} catch (Exception e) {
											continue;
										}
									}
								} else {
									try {
										if (moduleName.equals("Accounts")) {
											if (!csvImportService.accountExists(
													inputMessage.get("ACCOUNT_NAME").toString(), companyId)) {
												csvImportService.createModuleData(companyId, moduleName, inputMessage,
														userUuid);
											}
										} else {
											csvImportService.createModuleData(companyId, moduleName, inputMessage,
													userUuid);
										}
									} catch (Exception e) {
										continue;
									}
								}
							}
							csvImportRepository.updateEntry(csvDocument.getCsvImportId(), "status", "COMPLETED",
									"csv_import");
						}
					} else {
						csvImportRepository.updateEntry(csvDocument.getCsvImportId(), "status", "FAILED", "csv_import");
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

					csvImportRepository.updateEntry(csvDocument.getCsvImportId(), "status", "FAILED", "csv_import");
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
