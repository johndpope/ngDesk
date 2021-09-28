package com.ngdesk.data.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
						int i = 0;
						boolean isEmpty = true;

						Map<Integer, Map<String, Object>> rowMap = csvImportService.decodeFile(body, fields);
						isEmpty = (rowMap != null) ? false : true;

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
								error = (inputMessage == null) ? true : false;
								
								if (error) {
									continue;
								}

								inputMessage = dataService.addInternalFields(module, inputMessage,
										csvDocument.getCreatedBy(), companyId);

								if (moduleName.equals("Users") || moduleName.equals("Contacts")) {

									String accountId = csvImportService.getAccountId(inputMessage, companyId, modules,
											globalTeamId, userUuid, csvDocument, i);
									error = (accountId == null) ? true : false;
									
									if (error) {
										continue;
									}

									inputMessage.put("ACCOUNT", accountId);
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

								

								if (moduleName.equalsIgnoreCase("Users")) {
									boolean flag = csvImportService.handleUserModule(inputMessage, companyId, modules,
											userUuid, globalTeamId, module, company, csvDocument, globalTeam, language,
											phoneNumber, i);
									if (flag) {
										continue;
									}
								} else {
									try {
										if (moduleName.equals("Accounts")) {
											if (!csvImportService.accountExists(
													inputMessage.get("ACCOUNT_NAME").toString(), companyId)) {
												csvImportService.createModuleData(companyId, moduleName, inputMessage,
														userUuid, modules);
											}
										} else if (moduleName.equals("Contacts")) {
											String accountId = inputMessage.get("ACCOUNT").toString();
											Relationship accountRelationship = new Relationship(accountId,
													csvImportService.getPrimaryDisplayFieldValue("ACCOUNT", module,
															companyId, accountId).toString());
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

										csvImportService.addToSet(i,
												csvImportService.formatErrorMessage(e.getMessage()),
												csvDocument.getCsvImportId());
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
		}
	}
}
