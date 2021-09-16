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
import com.ngdesk.data.dao.DataService;
import com.ngdesk.data.dao.Phone;
import com.ngdesk.data.dao.Relationship;
import com.ngdesk.data.modules.dao.DataType;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
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

	@Autowired
	ModuleService moduleService;

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
				if (language.equalsIgnoreCase("EN")) {
					language = "English";
				}

				try {

					List<Module> modules = modulesRepository
							.getAllModules(moduleService.getCollectionName("modules", companyId));

					Optional<Map<String, Object>> optionalUserEntry = moduleEntryRepository.findEntryById(
							csvDocument.getCreatedBy(), moduleService.getCollectionName("Users", companyId));
					Map<String, Object> user = optionalUserEntry.get();
					String userUuid = user.get("USER_UUID").toString();

					Optional<Map<String, Object>> optionalGlobalTeam = moduleEntryRepository.findEntryByFieldName(
							"NAME", "Global", moduleService.getCollectionName("Teams", companyId));

					HashMap<String, Object> globalTeam = new HashMap<String, Object>();
					globalTeam.putAll(optionalGlobalTeam.get());
					String globalTeamId = globalTeam.get("_id").toString();

					Optional<Role> optionalCustomerRole = rolesRepository.findRoleName("Customers",
							moduleService.getCollectionName("roles", companyId));
					Role customerRole = optionalCustomerRole.get();
					String customerRoleId = customerRole.getId();

					csvImportRepository.updateEntry(csvDocument.getCsvImportId(), "status", "PROCESSING", "csv_import");

					CsvImportData body = csvDocument.getCsvImportData();
					String moduleId = csvDocument.getModuleId();

					Optional<Module> optionalModule = modulesRepository.findById(moduleId,
							moduleService.getCollectionName("modules", companyId));

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
									e.printStackTrace();
									CsvImportLog log = new CsvImportLog();
									log.setLineNumber(i);
									log.setErrorMessage(e.getMessage());
									csvImportRepository.addToEntrySet(csvDocument.getCsvImportId(), "logs", log,
											"csv_import");
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

//								inputMessage.put("DATE_CREATED", new Date());
//								inputMessage.put("DATE_UPDATED", new Date());
//								inputMessage.put("CREATED_BY", csvDocument.getCreatedBy());
//								inputMessage.put("LAST_UPDATED_BY", csvDocument.getCreatedBy());
//								inputMessage.put("DELETED", false);
//								inputMessage.put("SOURCE_TYPE", "web");
								inputMessage.put("FIRST_NAME", "xxxx");
								inputMessage.put("LAST_NAME", "yyyy" + i);

								// GET THE ENTRY COLLECTION BASED ON SELECTED MODULE
								Map<String, String> fieldIdNameMap = new HashMap<String, String>();
								String phoneNumber = "";
								boolean error = false;

								if (moduleName.equals("Users") || moduleName.equals("Contacts")) {
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
											accountId = accountEntry.get("DATA_ID").toString();
											inputMessage.put("ACCOUNT", accountId);
										} else {
											Optional<Map<String, Object>> optionalAccount = moduleEntryRepository
													.findEntryByFieldName("ACCOUNT_NAME", accountName,
															moduleService.getCollectionName("Accounts", companyId));
											Map<String, Object> accountEntry = optionalAccount.get();
											accountId = accountEntry.get("_id").toString();
											inputMessage.put("ACCOUNT", accountId);
										}
									} else {
										CsvImportLog log = new CsvImportLog();
										log.setLineNumber(i);
										log.setErrorMessage("Email address is required");
										csvImportRepository.addToEntrySet(csvDocument.getCsvImportId(), "logs", log,
												"csv_import");
										error = true;
									}

									if (moduleName.equals("Users")) {
										if (inputMessage.containsKey("PHONE_NUMBER")) {
											phoneNumber = inputMessage.get("PHONE_NUMBER").toString();
											inputMessage.remove("PHONE_NUMBER");
										}
										if (inputMessage.containsKey("DEFAULT_CONTACT_METHOD")) {
											if (inputMessage.get("DEFAULT_CONTACT_METHOD") == null || inputMessage
													.get("DEFAULT_CONTACT_METHOD").toString().equals("")) {
												inputMessage.put("DEFAULT_CONTACT_METHOD", "Email");
											}
										} else {
											inputMessage.put("DEFAULT_CONTACT_METHOD", "Email");
										}

										inputMessage.put("ROLE", customerRoleId);
										inputMessage.put("IS_LOGIN_ALLOWED", false);
										inputMessage.put("INVITE_ACCEPTED", false);
									}

								}

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

//									if (dataType.getDisplay().equalsIgnoreCase("Auto Number")) {
//										int autoNumber = (int) field.getAutoNumberStartingNumber();
//										int count = (int) moduleEntryRepository.findCountOfEntries(collectionName);
//										if (count == 0) {
//											inputMessage.put(fieldName, autoNumber);
//										} else {
//											Optional<Map<String, Object>> optionalEntry = moduleEntryRepository
//													.findBySortingField(fieldName, collectionName);
//											Map<String, Object> sortedEntry = optionalEntry.get();
//											if (sortedEntry.get(fieldName) != null) {
//												autoNumber = Integer.parseInt(sortedEntry.get(fieldName).toString());
//												autoNumber++;
//											}
//											inputMessage.put(fieldName, autoNumber);
//										}
//									}
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
														csvImportService.getPrimaryDisplayFieldValue("TEAMS", module,
																companyId, globalTeamId).toString());
												teams.add(relationship);
											}
											inputMessage.put("TEAMS", teams);
										}

//										if (!inputMessage.containsKey(fieldName)
//												|| inputMessage.get(fieldName) == null) {
//
//											if (field.getDefaultValue() != null) {
//												String defaultValue = field.getDefaultValue();
//
//												Pattern pattern = Pattern.compile("\\{\\{(.*)\\}\\}");
//												Matcher matcher = pattern.matcher(defaultValue);
//
//												if (matcher.find()) {
//
//													if (matcher.group(1).equals("CURRENT_CONTACT")) {
//
//														Optional<Map<String, Object>> optionalContactEntry = moduleEntryRepository
//																.findEntryByFieldName("USER",
//																		csvDocument.getCreatedBy(),
//																		moduleService.getCollectionName(
//																				"Contacts", companyId));
//														Map<String, Object> contactEntry = optionalContactEntry.get();
//
//														defaultValue = defaultValue.replaceAll(
//																"\\{\\{CURRENT_CONTACT\\}\\}",
//																contactEntry.get("_id").toString());
//
//													} else if (matcher.group(1).equals("CURRENT_USER")) {
//														defaultValue = defaultValue.replaceAll(
//																"\\{\\{CURRENT_USER\\}\\}", csvDocument.getCreatedBy());
//													}
//												}
//												if (backendDataType.equalsIgnoreCase("Array")) {
//													inputMessage.put(fieldName, Arrays.asList(defaultValue));
//												} else if (backendDataType.equalsIgnoreCase("Boolean")) {
//													if (defaultValue.equals("true")) {
//														inputMessage.put(fieldName, true);
//													} else {
//														inputMessage.put(fieldName, false);
//													}
//												} else {
//													inputMessage.put(fieldName, defaultValue);
//												}
//											}
//										}
									}
								}
								inputMessage = dataService.addFieldsWithDefaultValue(
										moduleService.getAllFields(module, companyId), inputMessage);
								inputMessage = dataService.addInternalFields(module, inputMessage,
										csvDocument.getCreatedBy(), companyId);

								if (error) {
									continue;
								}

//								try {
//									if (dataService.requiredFieldsCheckRequired(inputMessage)) {
//										validator.requiredFieldsPresent(module, inputMessage);
//									}
//									validator.validateBaseTypes(module, inputMessage, companyId);
//								} catch (Exception e) {
//									e.printStackTrace(); 
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
													moduleService.getCollectionName("Users", companyId));

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
													moduleService.getCollectionName("roles", companyId));
											Role role = optionalRole.get();
											String existingRoleName = role.getName();

											Optional<Map<String, Object>> optionalTeamsEntry = moduleEntryRepository
													.findEntryByFieldName("NAME", existingRoleName,
															moduleService.getCollectionName("Teams", companyId));
											HashMap<String, Object> roleTeam = new HashMap<String, Object>();
											roleTeam.putAll(optionalTeamsEntry.get());
											String roleTeamId = roleTeam.get("_id").toString();

											Optional<Module> optionalTeamsModule = modulesRepository.findIdbyModuleName(
													"Teams", moduleService.getCollectionName("modules", companyId));
											Module teamsModule = optionalTeamsModule.get();
											String userId = "";

											if (optionalUser.isPresent()) {
												userId = userEntry.get("_id").toString();
												String teamName = userEntry.get("FIRST_NAME") + " "
														+ userEntry.get("LAST_NAME");

												Optional<Map<String, Object>> optionalPersonalTeam = moduleEntryRepository
														.findTeamsByVariableForIsPersonal("NAME", teamName,
																moduleService.getCollectionName("Teams", companyId));
												HashMap<String, Object> personalTeam = new HashMap<String, Object>();
												personalTeam.putAll(optionalPersonalTeam.get());
												personalTeam.put("DELETED", false);

												csvImportService.updateUsersInTeamsEntry(Arrays.asList().toString(),
														userId, teamsModule, companyId, personalTeam, userUuid);

												userEntry.put("DELETED", false);
												List<String> existingTeams = mapper.readValue(
														mapper.writeValueAsString(userEntry.get("TEAMS")),
														mapper.getTypeFactory().constructCollectionType(List.class,
																String.class));
												if (!existingTeams.contains(personalTeam.get("_id").toString())) {
													existingTeams.add(personalTeam.get("_id").toString());
												}

												List<Relationship> teams = csvImportService.getListRelationshipValue(
														"TEAMS", module, companyId, existingTeams);

												userEntry.put("TEAMS", teams);
												System.out.println(" csv hit 2");
												dataAPI.putModuleEntry(userEntry, module.getModuleId(), true, companyId,
														userUuid, false);
											} else {

												userEntry = csvImportService.createUser(
														inputMessage.get("EMAIL_ADDRESS").toString(), companyId, "",
														false, company.getCompanySubdomain(), "alarm_classic", 0,
														language, inputMessage.get("ROLE").toString(), false,
														globalTeamId, userUuid);

												userId = userEntry.get("DATA_ID").toString();

												Optional<Module> optionalContactModule = modulesRepository
														.findIdbyModuleName("Contacts",
																moduleService.getCollectionName("modules", companyId));
												Module contactModule = optionalContactModule.get();

												Map<String, Object> contactEntry = csvImportService.createContact(
														inputMessage.get("FIRST_NAME").toString(),
														inputMessage.get("LAST_NAME").toString(),
														inputMessage.get("ACCOUNT").toString(),
														new Phone("us", "+1", phoneNumber, "us.svg"), contactModule,
														companyId, globalTeamId, userId, userUuid);

												HashMap<String, Object> team = new HashMap<String, Object>();
												team.put("NAME", inputMessage.get("FIRST_NAME") + " "
														+ inputMessage.get("LAST_NAME"));
												team.put("DESCRIPTION",
														"Personal team for " + inputMessage.get("FIRST_NAME") + " "
																+ inputMessage.get("LAST_NAME"));

												List<Relationship> users = new ArrayList<Relationship>();
												String primaryDisplayFieldValue = csvImportService
														.getPrimaryDisplayFieldValue("USERS", teamsModule, companyId,
																userId)
														.toString();
												Relationship userRelationship = new Relationship(userId,
														primaryDisplayFieldValue);
												users.add(userRelationship);
												team.put("USERS", users);
												team.put("DELETED", false);
												team.put("DATE_CREATED", new Date());
												team.put("DATE_UPDATED", new Date());
												team.put("IS_PERSONAL", true);
												System.out.println("csv hit a");
												Map<String, Object> personalTeam = csvImportService
														.createModuleData(companyId, "Teams", team, userUuid);
												String personalTeamId = personalTeam.get("DATA_ID").toString();

												List<String> teamsList = Arrays.asList(personalTeamId, globalTeamId,
														roleTeamId);

												List<Relationship> teams = csvImportService.getListRelationshipValue(
														"TEAMS", module, companyId, teamsList);

												userEntry.put("TEAMS", teams);
												userEntry.put("IS_LOGIN_ALLOWED", false);

												Relationship contactRelationship = new Relationship(
														contactEntry.get("DATA_ID").toString(),
														csvImportService.getPrimaryDisplayFieldValue("CONTACT", module,
																companyId, contactEntry.get("DATA_ID").toString())
																.toString());
												if (contactRelationship != null) {
													userEntry.put("CONTACT", contactRelationship);
												}
												System.out.println("csv hit 3");
												dataAPI.putModuleEntry(userEntry, module.getModuleId(), true, companyId,
														userUuid, false);
											}

											csvImportService.updateUsersInTeamsEntry(globalTeam.get("USERS").toString(),
													userId, teamsModule, companyId, globalTeam, userUuid);

											csvImportService.updateUsersInTeamsEntry(roleTeam.get("USERS").toString(),
													userId, teamsModule, companyId, roleTeam, userUuid);

										} catch (Exception e) {
											e.printStackTrace();
											CsvImportLog log = new CsvImportLog();
											log.setLineNumber(i);
											log.setErrorMessage(e.getMessage());
											csvImportRepository.addToEntrySet(csvDocument.getCsvImportId(), "logs", log,
													"csv_import");
											continue;
										}
									}
								} else {
									try {
										if (moduleName.equals("Accounts")) {
											if (!csvImportService.accountExists(
													inputMessage.get("ACCOUNT_NAME").toString(), companyId)) {
												System.out.println("csv hit b");
												csvImportService.createModuleData(companyId, moduleName, inputMessage,
														userUuid);
											}
										} else if (moduleName.equals("Contacts")) {
											Module contactModule = modules.stream()
													.filter(mod -> mod.getName().equals("Contacts")).findFirst()
													.orElse(null);
											String accountId = inputMessage.get("ACCOUNT").toString();
											Relationship accountRelationship = new Relationship(accountId,
													csvImportService.getPrimaryDisplayFieldValue("ACCOUNT",
															contactModule, companyId, accountId).toString());
											if (accountRelationship != null) {
												inputMessage.put("ACCOUNT", accountRelationship);
											}
											System.out.println("csv hit c");
											csvImportService.createModuleData(companyId, moduleName, inputMessage,
													userUuid);
										} else {
											System.out.println("csv hit d");
											csvImportService.createModuleData(companyId, moduleName, inputMessage,
													userUuid);
										}
									} catch (Exception e) {
										e.printStackTrace();
										CsvImportLog log = new CsvImportLog();
										log.setLineNumber(i);
										log.setErrorMessage(e.getMessage());
										csvImportRepository.addToEntrySet(csvDocument.getCsvImportId(), "logs", log,
												"csv_import");
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
					CsvImportLog log = new CsvImportLog();
					log.setErrorMessage(e.getMessage());
					csvImportRepository.addToEntrySet(csvDocument.getCsvImportId(), "logs", log, "csv_import");

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
