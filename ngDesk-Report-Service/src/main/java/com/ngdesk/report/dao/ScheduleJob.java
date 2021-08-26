package com.ngdesk.report.dao;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.email.SendEmail;
import com.ngdesk.report.company.dao.Company;
import com.ngdesk.report.graphql.dao.GraphqlProxy;
import com.ngdesk.report.graphql.dao.ReportInput;
import com.ngdesk.report.graphql.dao.RoleLayoutCondition;
import com.ngdesk.report.module.dao.Module;
import com.ngdesk.report.module.dao.ModuleField;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.repositories.ReportRepository;

@Component
public class ScheduleJob {

	@Autowired
	Global global;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	ReportRepository reportRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	GraphqlProxy graphqlProxy;

	@Value("${email.host}")
	private String host;

	@Value("${env}")
	private String environment;

	@Scheduled(cron = "0 0,30 * ? * *")
	public void schedule() {
		try {

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
			Timestamp cronParseTime = new Timestamp(new Date().getTime()); // Get the current time up here so that even
																			// if the method takes time to run, current
																			// time will still be the same and schedules
																			// will be respected
			ZonedDateTime now = ZonedDateTime.now();
			Calendar cal = Calendar.getInstance();
			cal.setTime(cronParseTime);
			cal.add(Calendar.SECOND, -10); // Subtract 10 seconds from current time to get the next run time of cron
											// expression properly (Reason: the next method of cron expression gives the
											// next run date from current date, since the job is scheduled to run at a
											// perfect time, cron expression will return the next run date of the next
											// schedule instead of the current one)
			cronParseTime.setTime(cal.getTime().getTime());

			Optional<List<Company>> optionalCompanies = companyRepository.findAllCompanies("companies");
			if (optionalCompanies.isPresent()) {
				for (Company company : optionalCompanies.get()) {
					String companyId = company.getCompanyId();
					Optional<List<Report>> optionalReports = reportRepository
							.findByCollectionName("reports_" + companyId);

					if (optionalReports.isPresent()) {
						for (Report report : optionalReports.get()) {
							List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);

							Module currentModule = modules.stream()
									.filter(module -> module.getModuleId().equals(report.getModule())).findFirst()
									.orElse(null);

							List<ReportField> reportFields = report.getFields();

							List<String> reportFieldNames = new ArrayList<String>();
							List<String> fieldNamesWithOneToMany = new ArrayList<String>();
							List<String> relatedFieldNames = new ArrayList<String>();

							for (ReportField reportField : reportFields) {
								String[] arrayOfFieldIds = reportField.getFieldId().split("[.]");

								ModuleField moduleField = currentModule.getFields().stream()
										.filter(filter -> filter.getFieldId().equals(arrayOfFieldIds[0])).findFirst()
										.orElse(null);
								reportFieldNames.add(moduleField.getName());
								reportFieldNames = reportFieldNames.stream().distinct().collect(Collectors.toList());

								if (moduleField.getDataType().getDisplay().equalsIgnoreCase("Relationship")
										&& moduleField.getRelationshipType().equalsIgnoreCase("one to many")) {

									ModuleField relatedModuleField = validateRelatedOneToManyField(moduleField,
											arrayOfFieldIds, companyId);

									relatedFieldNames.add(moduleField.getName() + "." + relatedModuleField.getName());
									relatedFieldNames = relatedFieldNames.stream().distinct()
											.collect(Collectors.toList());

									fieldNamesWithOneToMany
											.add(moduleField.getName() + "." + relatedModuleField.getName());
								} else {
									fieldNamesWithOneToMany.add(moduleField.getName());
								}

							}
							String userId = report.getCreatedBy();
							if (userId != null) {
								Map<String, Object> user = moduleEntryRepository.findById(userId, "Users_" + companyId)
										.orElse(null);
								if (user != null && report.getSchedules() != null) {
									ReportSchedule schedules = report.getSchedules();
									String cronExpression = schedules.getCron();

									String timeZone = "UTC";

									if (company.getTimezone() != null) {
										// All the schdules are set in company time zone, UTC if absent
										timeZone = company.getTimezone();
									}

									now = now.toInstant().atZone(ZoneId.of(timeZone)); // current time in company time
																						// zone
									// Day, Hour and Minute to be compared to the Schedule time
									int currentDay = now.getDayOfMonth();
									int currentHour = now.getHour();
									int currentMinute = now.getMinute();
									CronSequenceGenerator generator = new CronSequenceGenerator(cronExpression);
									ZonedDateTime z = cronParseTime.toInstant().atZone(ZoneId.of(timeZone));
									String cronTime = z.format(fmt);
									Date cronparsedDate = dateFormat.parse(cronTime);
									Date nextRunDate = generator.next(cronparsedDate);
									LocalDateTime nextRun = LocalDateTime.ofInstant(nextRunDate.toInstant(),
											ZoneId.systemDefault());
									int scheduleDay = nextRun.getDayOfMonth();
									int scheduleHour = nextRun.getHour();
									int scheduleMinute = nextRun.getMinute();

									// create a report only if the schedule matches the current time
									if (scheduleDay == currentDay && scheduleHour == currentHour
											&& scheduleMinute == currentMinute) {
										String query = buildQuery(reportFieldNames, currentModule, modules,
												relatedFieldNames, report);
										List<RoleLayoutCondition> conditions = new ArrayList<RoleLayoutCondition>();
										List<Filter> filters = report.getFilters();
										for (Filter filter : filters) {
											RoleLayoutCondition condition = new RoleLayoutCondition();
											condition.setCondition(filter.getField().getFieldId());
											condition.setConditionValue(filter.getValue());
											condition.setOperator(filter.getOperator());
											condition.setRequirementType(filter.getRequirementType());
											conditions.add(condition);
										}

										ReportInput reportInput = new ReportInput(query, conditions,
												report.getReportName(), fieldNamesWithOneToMany,
												report.getSchedules().getEmails());
										graphqlProxy.reportGenerate(reportInput, companyId,
												user.get("USER_UUID").toString());

									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();

			if (environment.equals("prd")) {
				SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com", "error@ngdesk.com",
						"Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToSpencer.sendEmail();

				SendEmail sendEmailToShashank = new SendEmail("shashank.shankaranand@allbluesolutions.com",
						"error@ngdesk.com", "Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToShashank.sendEmail();
			}

		}
	}

	public ModuleField validateRelatedOneToManyField(ModuleField field, String[] arrayOfFieldIds, String companyId) {

		Optional<Module> optionalRelatedModule = modulesRepository.findById(field.getModule(), "modules_" + companyId);
		if (optionalRelatedModule.isEmpty()) {
			String[] vars = {};
			throw new BadRequestException("INVALID_RELATED_MODULE", vars);
		}
		Optional<ModuleField> optionalRelatedField = optionalRelatedModule.get().getFields().stream()
				.filter(relatedField -> relatedField.getFieldId().equalsIgnoreCase(arrayOfFieldIds[1])).findFirst();

		return optionalRelatedField.get();

	}

	public String buildQuery(List<String> reportFieldNames, Module module, List<Module> modules,
			List<String> relatedFieldNames, Report report) {
		String reportQuery = "\n" + "DATA_ID: _id";
		String moduleName = module.getName().replaceAll("\\s", "_");

		for (String fieldId : reportFieldNames) {
			ModuleField moduleField = module.getFields().stream().filter(filter -> filter.getName().equals(fieldId))
					.findFirst().orElse(null);

			if (moduleField.getDataType().getDisplay().equalsIgnoreCase("Relationship")
					&& moduleField.getRelationshipType().equalsIgnoreCase("one to many")) {
				List<String> formatedRelatedFieldNames = new ArrayList<String>();
				for (String relatedFieldName : relatedFieldNames) {
					String[] arrayOfFieldIds = relatedFieldName.split("[.]");
					if (fieldId.equals(arrayOfFieldIds[0])) {

						formatedRelatedFieldNames.add(arrayOfFieldIds[1]);
					}
				}
				String sortBy = "DATE_CREATED";
				if (report.getSortBy().getFieldId() != null) {
					ModuleField sortByField = module.getFields().stream()
							.filter(filter -> filter.getFieldId().equals(report.getSortBy().getFieldId())).findFirst()
							.orElse(null);

					sortBy = sortByField.getName();
				}
				String orderBy = "asc";
				if (report.getOrder() != null) {
					orderBy = report.getOrder();
				}

				String relatedOneToManyFieldNames = String.join(" ", formatedRelatedFieldNames);
				reportQuery += "\n" + moduleField.getName() + "( pageNumber:0 pageSize: 10 moduleId:" + "\""
						+ module.getModuleId() + "\"" + "sortBy:" + "\"" + sortBy + "\"" + "orderBy:" + "\"" + orderBy
						+ "\"" + "){" + relatedOneToManyFieldNames + "}";

			} else if (moduleField.getDataType().getDisplay().equalsIgnoreCase("Aggregate")) {
				reportQuery += "\n" + moduleField.getName();

			} else if (moduleField.getDataType().getDisplay().equalsIgnoreCase("Phone")) {
				reportQuery += "\n" + moduleField.getName() + "{ COUNTRY_CODE DIAL_CODE PHONE_NUMBER COUNTRY_FLAG }"
						+ "\n";

			} else if (moduleField.getDataType().getDisplay().equalsIgnoreCase("Discussion")) {
				reportQuery += "\n" + moduleField.getName() + "{ COUNTRY_CODE DIAL_CODE PHONE_NUMBER COUNTRY_FLAG }"
						+ "\n";

			} else if (moduleField.getDataType().getDisplay().equalsIgnoreCase("Relationship")
					&& moduleField.getRelationshipType().equalsIgnoreCase("many to one")) {
				Module relatedModule = modules.stream()
						.filter(rlModule -> rlModule.getModuleId().equals(moduleField.getModule())).findFirst()
						.orElse(null);

				ModuleField primaryDisplayFeild = relatedModule.getFields().stream()
						.filter(filter -> filter.getFieldId().equals(moduleField.getPrimaryDisplayField())).findFirst()
						.orElse(null);
				String primaryDisplayFieldName = primaryDisplayFeild.getName();
				reportQuery += "\n" + moduleField.getName() + "{ DATA_ID: _id  PRIMARY_DISPLAY_FIELD: "
						+ primaryDisplayFieldName + " }";

			} else {
				reportQuery += "\n" + moduleField.getName();
			}

		}
		String query = "{  CSV:getCsvFor" + moduleName + "(moduleId:" + "\"" + module.getModuleId() + "\"" + ") { "
				+ reportQuery + "   } }";
		return query;
	}
}
