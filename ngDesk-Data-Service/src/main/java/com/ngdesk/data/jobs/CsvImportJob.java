package com.ngdesk.data.jobs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.ngdesk.data.csvimport.dao.CsvImportService;
import com.ngdesk.data.dao.DataService;
import com.ngdesk.data.dao.Relationship;
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

					Map<String, Object> globalTeam = optionalGlobalTeam.get();
					String globalTeamId = globalTeam.get("_id").toString();

					Optional<Role> optionalCustomerRole = rolesRepository.findRoleName("Customers",
							moduleService.getCollectionName("roles", companyId));
					Role customerRole = optionalCustomerRole.get();
					String customerRoleId = customerRole.getId();

					csvImportRepository.updateEntry(csvDocument.getCsvImportId(), "status", "PROCESSING", "csv_import");

					CsvImportData body = csvDocument.getCsvImportData();
					String moduleId = csvDocument.getModuleId();

					Optional<Module> optionalModule = modules.stream()
							.filter(module -> module.getModuleId().equals(moduleId)).findFirst();

					if (optionalModule.isPresent()) {
						// CREATE THE ENTRY
						Module module = optionalModule.get();
						String moduleName = module.getName();
						List<ModuleField> fields = module.getFields();

						Map<Integer, Map<String, Object>> rowMap = new HashMap<Integer, Map<String, Object>>();
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

						if (isEmpty) {
							csvImportRepository.updateEntry(csvDocument.getCsvImportId(), "status", "FAILED",
									"csv_import");
							continue;
						} else {

							i = 0;
							for (Integer key : rowMap.keySet()) {
								Map<String, Object> inputMessage = rowMap.get(key);
								i++;

								if (inputMessage.containsKey("DATE_CREATED") || inputMessage.containsKey("DATE_UPDATED")
										|| inputMessage.containsKey("LAST_UPDATED_BY")
										|| inputMessage.containsKey("CREATED_BY")) {
									inputMessage.remove("DATE_CREATED");
									inputMessage.remove("DATE_UPDATED");
									inputMessage.remove("LAST_UPDATED_BY");
									inputMessage.remove("CREATED_BY");
								}

								String phoneNumber = "";
								boolean error = false;

								inputMessage = csvImportService.formatDataTypes(fields, inputMessage, csvDocument, i,
										companyId, user, globalTeamId, module);

								if (inputMessage == null) {
									error = true;
								}
								inputMessage = dataService.addInternalFields(module, inputMessage,
										csvDocument.getCreatedBy(), companyId);

								if (moduleName.equals("Users") || moduleName.equals("Contacts")) {
									if (inputMessage.containsKey("EMAIL_ADDRESS")) {
										String userEmailAddress = inputMessage.get("EMAIL_ADDRESS").toString();
										String[] splitEmail = userEmailAddress.split("@");
										String accountName = "";
										if (splitEmail.length > 1) {
											accountName = splitEmail[1].trim();
										}
										String accountId = null;

										if (!csvImportService.accountExists(accountName, companyId)) {
											Module accountModule = modules.stream()
													.filter(mod -> mod.getName().equals("Accounts")).findFirst()
													.orElse(null);
											Map<String, Object> accountEntry = csvImportService.createAccount(
													accountName, companyId, globalTeamId, userUuid, accountModule);
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
										csvImportService.addToSet(i, "Email address is required",
												csvDocument.getCsvImportId());
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

								if (error) {
									continue;
								}

								if (moduleName.equalsIgnoreCase("Users")) {

								} else {
									try {
										if (moduleName.equals("Accounts")) {
											if (!csvImportService.accountExists(
													inputMessage.get("ACCOUNT_NAME").toString(), companyId)) {
												csvImportService.createModuleData(companyId, moduleName, inputMessage,
														userUuid, modules);
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
											csvImportService.createModuleData(companyId, moduleName, inputMessage,
													userUuid, modules);
										} else {
											csvImportService.createModuleData(companyId, moduleName, inputMessage,
													userUuid, modules);
										}
									} catch (Exception e) {
										e.printStackTrace();
										csvImportService.addToSet(i, e.getMessage(), csvDocument.getCsvImportId());
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
					csvImportService.addToSet(0, e.getMessage(), csvDocument.getCsvImportId());

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
