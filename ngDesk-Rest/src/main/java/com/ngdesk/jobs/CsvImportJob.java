package com.ngdesk.jobs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.accounts.Account;
import com.ngdesk.email.SendEmail;
import com.ngdesk.modules.DataService;
import com.ngdesk.users.Phone;
import com.ngdesk.users.UserDAO;
import com.ngdesk.validation.Validator;
import com.ngdesk.wrapper.Wrapper;
import com.opencsv.CSVReader;

@Component
public class CsvImportJob {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private DataService dataService;

	@Autowired
	Account account;

	@Autowired
	UserDAO userDAO;

	@Value("${email.host}")
	private String host;

	@Value("${env}")
	private String environment;

	@Autowired
	private Global global;

	@Autowired
	private Wrapper wrapper;

	@Autowired
	private Validator validator;

	private final Logger log = LoggerFactory.getLogger(SendCampaignJob.class);

	@Scheduled(fixedRate = 30000)
	public void importCsv() {
		BufferedReader br = null;
		InputStream is = null;

		try {
			log.trace("Enter CsvImportJob.importCsv()");
			MongoCollection<Document> csvCollection = mongoTemplate.getCollection("csv_import");
			List<Document> csvDocuments = csvCollection.find(Filters.eq("STATUS", "QUEUED"))
					.into(new ArrayList<Document>());
			for (Document csvDocument : csvDocuments) {

				String companyId = csvDocument.getString("COMPANY_ID");
				MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
				Document company = companiesCollection.find(Filters.eq("_id", new ObjectId(companyId))).first();

				if (company == null) {
					continue;
				}
				String companySubdomain = company.getString("COMPANY_SUBDOMAIN");
				try {

					MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
					MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);

					Document globalTeam = teamsCollection.find(Filters.eq("NAME", "Global")).first();
					String globalTeamId = globalTeam.getObjectId("_id").toString();

					MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
					Document customerRoleDocument = rolesCollection.find(Filters.eq("NAME", "Customers")).first();
					String customerRole = customerRoleDocument.getObjectId("_id").toString();

					String language = company.getString("LANGUAGE");

					csvCollection.findOneAndUpdate(
							Filters.eq("_id", new ObjectId(csvDocument.getObjectId("_id").toString())),
							Updates.set("STATUS", "PROCESSING"));

					Document body = (Document) csvDocument.get("CSV_IMPORT_DATA");
					String moduleId = csvDocument.getString("MODULE_ID");
					String moduleCollectionName = "modules_" + companyId;
					MongoCollection<Document> modulecollection = mongoTemplate.getCollection(moduleCollectionName);
					Document moduledoc = modulecollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

					if (moduledoc != null) {
						// CREATE THE ENTRY
						String moduleName = moduledoc.getString("NAME");
						List<Document> allFields = (List<Document>) moduledoc.get("FIELDS");
						Map rowMap = new HashMap<Integer, HashMap<String, String>>();
						List<String> headers = new ArrayList<String>();
						int i = 0;
						Base64.Decoder dec = Base64.getDecoder();
						byte[] decbytes = dec.decode(body.getString("FILE"));
						is = new ByteArrayInputStream(decbytes);
						Map headerMap = (Map) body.get("HEADERS");
						boolean isEmpty = true;
						if (body.getString("FILE_TYPE").equals("csv")) {
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
									for (Document field : allFields) {
										String fieldId = field.getString("FIELD_ID");
										if (headerMap.containsKey(fieldId)) {
											String csvDisplayLabel = headerMap.get(fieldId).toString();
											colMap.put(field.getString("NAME"),
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
						} else if (body.getString("FILE_TYPE").equals("xlsx")
								|| body.getString("FILE_TYPE").equals("xls")) {
							Workbook workbook;
							if (body.getString("FILE_TYPE").equals("xlsx")) {
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
										for (Document field : allFields) {
											String fieldId = field.getString("FIELD_ID");
											if (headerMap.containsKey(fieldId)) {
												String csvDisplayLabel = headerMap.get(fieldId).toString();
												colMap.put(field.getString("NAME"),
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
							csvCollection.findOneAndUpdate(
									Filters.eq("_id", new ObjectId(csvDocument.getObjectId("_id").toString())),
									Updates.set("STATUS", "FAILED"));
							continue;
						} else {
							Iterator<Map.Entry<Integer, HashMap<String, Object>>> itr = rowMap.entrySet().iterator();
							i = 0;
							while (itr.hasNext()) {
								i++;
								Map.Entry<Integer, HashMap<String, Object>> entry = itr.next();
								JSONObject inputMessage = new JSONObject();
								try {
									inputMessage = new JSONObject(entry.getValue());
								} catch (Exception e) {
									continue;
								}

								if (inputMessage.has("DATE_CREATED") || inputMessage.has("DATE_UPDATED")
										|| inputMessage.has("LAST_UPDATED_BY") || inputMessage.has("CREATED_BY")) {
									inputMessage.remove("DATE_CREATED");
									inputMessage.remove("DATE_UPDATED");
									inputMessage.remove("LAST_UPDATED_BY");
									inputMessage.remove("CREATED_BY");
								}

								SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

								inputMessage.put("DATE_CREATED", format.format(new Date(new Date().getTime())));
								inputMessage.put("DATE_UPDATED", format.format(new Date(new Date().getTime())));
								inputMessage.put("CREATED_BY", csvDocument.getString("CREATED_BY"));
								inputMessage.put("LAST_UPDATED_BY", csvDocument.getString("CREATED_BY"));
								inputMessage.put("DELETED", false);
								inputMessage.put("SOURCE_TYPE", "web");

								// GET THE ENTRY COLLECTION BASED ON SELECTED MODULE
								String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
								MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
								Map<String, String> fieldIdNameMap = new HashMap<String, String>();
								String phoneNumber = "";
								if (moduleName.equals("Users")) {
									if (inputMessage.has("PHONE_NUMBER")) {
										phoneNumber = inputMessage.getString("PHONE_NUMBER");
										inputMessage.remove("PHONE_NUMBER");
									}
									if (inputMessage.has("EMAIL_ADDRESS")) {
										String userEmailAddress = inputMessage.getString("EMAIL_ADDRESS");
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
											MongoCollection<Document> accountsCollection = mongoTemplate
													.getCollection("Accounts_" + companyId);
											accountId = accountsCollection.find(Filters.eq("ACCOUNT_NAME", accountName))
													.first().getObjectId("_id").toString();
											inputMessage.put("ACCOUNT", accountId);
										}
									}
									if (inputMessage.has("DEFAULT_CONTACT_METHOD")) {
										if (inputMessage.getString("DEFAULT_CONTACT_METHOD") == null
												|| inputMessage.getString("DEFAULT_CONTACT_METHOD").equals("")) {
											inputMessage.put("DEFAULT_CONTACT_METHOD", "Email");
										}
									} else {
										inputMessage.put("DEFAULT_CONTACT_METHOD", "Email");
									}

									inputMessage.put("ROLE", customerRole);
									inputMessage.put("IS_LOGIN_ALLOWED", false);
									inputMessage.put("INVITE_ACCEPTED", false);

								}

								boolean error = false;
								for (Document field : allFields) {
									String fieldName = field.getString("NAME");
									String displayLabel = field.getString("DISPLAY_LABEL");
									fieldIdNameMap.put(field.getString("FIELD_ID"), displayLabel);
									Document dataType = (Document) field.get("DATA_TYPE");
									String backendDataType = dataType.getString("BACKEND");

									// VALIDATE IF THE PICKLIST VALUES ARE MATCHING WITH EXISTING PICKLISTS OR ELSE
									// THROW ERROR
									if (inputMessage.has(fieldName)) {
										if (dataType.getString("DISPLAY").equalsIgnoreCase("Picklist")) {
											List<String> picklistValues = (List<String>) field.get("PICKLIST_VALUES");
											String value = inputMessage.getString(fieldName);
											boolean valueExists = false;
											for (int f = 0; f < picklistValues.size(); f++) {
												if (picklistValues.get(f).equals(value)) {
													valueExists = true;
													break;
												}
											}
											if (!valueExists) {
												JSONObject log = new JSONObject();
												log.put("LINE_NUMBER", i);
												log.put("ERROR_MESSAGE", "Picklist values are incorrect");
												csvCollection
														.findOneAndUpdate(
																Filters.eq("_id",
																		new ObjectId(csvDocument.getObjectId("_id")
																				.toString())),
																Updates.addToSet("LOGS",
																		Document.parse(log.toString())));
												error = true;
												break;
											}
										}
									}
									if (inputMessage.has(fieldName)) {
										if (dataType.getString("DISPLAY").equalsIgnoreCase("Chronometer")) {
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
										if (dataType.getString("DISPLAY").equalsIgnoreCase("Discussion")
												|| dataType.getString("DISPLAY").equalsIgnoreCase("Relationship")) {
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
					} else {
						csvCollection.findOneAndUpdate(
								Filters.eq("_id", new ObjectId(csvDocument.getObjectId("_id").toString())),
								Updates.set("STATUS", "FAILED"));
					}
				} catch (Exception e) {
					e.printStackTrace();

					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					String sStackTrace = sw.toString();
					String info = "<br>Subdomain: " + companySubdomain + "<br>File Name: "
							+ csvDocument.getString("NAME");

					if (environment.equals("prd")) {
						SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com",
								"support@ngdesk.com", "Internal Error: Stack Trace", sStackTrace + info, host);
						sendEmailToSpencer.sendEmail();

						SendEmail sendEmailToShashank = new SendEmail("shashank.shankaranand@allbluesolutions.com",
								"support@ngdesk.com", "Internal Error: Stack Trace", sStackTrace, host);
						sendEmailToShashank.sendEmail();
					}

					csvCollection.findOneAndUpdate(
							Filters.eq("_id", new ObjectId(csvDocument.getObjectId("_id").toString())),
							Updates.set("STATUS", "FAILED"));
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
