package com.ngdesk.reporting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.ngdesk.Global;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.BadRequestException;
import com.opencsv.CSVWriter;

@Component
public class ScheduleJob {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@Autowired
	private ReportService reportService;

	@Value("${email.host}")
	private String host;

	@Value("${env}")
	private String environment;

	private final Logger log = LoggerFactory.getLogger(ScheduleJob.class);

//	@Scheduled(cron = "0 0,30 * ? * *")
	public void schedule() {
		try {

			log.trace("Entered schedule()");
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

			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			List<Document> companies = companiesCollection.find(Filters.eq("VERSION", "v2"))
					.into(new ArrayList<Document>());

			for (Document company : companies) {
				String companyId = company.getObjectId("_id").toString();
				MongoCollection<Document> reportsCollection = mongoTemplate.getCollection("reports_" + companyId);
				List<Document> reports = reportsCollection.find().into(new ArrayList<Document>());
				String companySubdomain = company.getString("COMPANY_SUBDOMAIN");

				for (Document reportDocument : reports) {
					String reportId = reportDocument.remove("_id").toString();

					if (reportDocument.containsKey("SCHEDULES") && reportDocument.get("SCHEDULES") != null) {
						JSONObject schedules = new JSONObject(
								new ObjectMapper().writeValueAsString(reportDocument.get("SCHEDULES")));
						JSONArray emails = schedules.getJSONArray("EMAILS");
						String cronExpression = schedules.getString("CRON");

						String timeZone = "UTC";
						LocalDateTime dateTime = null;
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss.SSSX");
						if (company.containsKey("TIMEZONE") && company.get("TIMEZONE") != null) {
							timeZone = company.get("TIMEZONE").toString(); // All the schdules are set in company time
																			// zone, UTC
																			// if absent
						}
						ZoneId fromTimeZone = ZoneId.of("UTC"); // Source timezone
						ZoneId toTimeZone = ZoneId.of(timeZone); // Target timezone
						now = now.toInstant().atZone(ZoneId.of(timeZone)); // current time in company time zone
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
							// Generate CSV file
							String modulesCollectionName = "modules_" + companyId;
							MongoCollection<Document> modulesCollection = mongoTemplate
									.getCollection(modulesCollectionName);
							Report report = new ObjectMapper().readValue(reportDocument.toJson(), Report.class);

							if (global.isDocumentIdExists(report.getModule(), modulesCollectionName)) {

								if (new ObjectId().isValid(report.getModule())) {

									Document moduleDocument = modulesCollection
											.find(Filters.eq("_id", new ObjectId(report.getModule()))).first();
									ArrayList<Document> fieldDocuments = (ArrayList) moduleDocument.get("FIELDS");

									List<String> AllfieldNames = new ArrayList<String>();
									Map<String, Document> relationFields = new HashMap<String, Document>();
									Map<String, Document> fieldsMap = new HashMap<String, Document>();

									for (Document field : fieldDocuments) {
										AllfieldNames.add(field.getString("NAME"));
										fieldsMap.put(field.getString("FIELD_ID"), field);

										Document dataType = (Document) field.get("DATA_TYPE");
										String displayDataType = dataType.getString("DISPLAY");

										if (displayDataType.equals("Relationship")) {
											if (field.getString("RELATIONSHIP_TYPE").equals("One to One")
													|| field.getString("RELATIONSHIP_TYPE").equals("Many to One")) {
												relationFields.put(field.getString("FIELD_ID"), field);
											}
										}
									}

									List<String> fieldNames = new ArrayList<String>();
									List<String> fieldDisplays = new ArrayList<String>();
									List<String> fieldsToAdd = new ArrayList<String>();

									for (Field field : report.getFields()) {
										String fieldName = fieldsMap.get(field.getId()).getString("NAME");
										String displayLabel = fieldsMap.get(field.getId()).getString("DISPLAY_LABEL");
										if (!AllfieldNames.contains(fieldName)) {
											throw new BadRequestException("FIELD_INVALID");
										}
										fieldNames.add(fieldName);
										fieldDisplays.add(displayLabel);
										if (relationFields.containsKey(field.getId())) {
											fieldsToAdd.add(field.getId());
										}
									}

									// INSERT HEADERS
									Writer writer = new StringWriter();
									CSVWriter csvWriter = new CSVWriter(writer);
									csvWriter.writeNext(fieldDisplays.toArray(new String[fieldDisplays.size()]));

									String moduleName = moduleDocument.getString("NAME");

									String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
									MongoCollection<Document> dataCollection = mongoTemplate
											.getCollection(collectionName);

									List<Bson> filters = reportService.generateFilter(report, fieldsMap);

									String sortBy = fieldsMap.get(report.getSortBy().getId()).getString("NAME");
									String orderBy = report.getOrder();

									Bson sortFilter = null;

									if (orderBy.equals("asc")) {
										sortFilter = Sorts.ascending(sortBy);
									} else if (orderBy.equals("desc")) {
										sortFilter = Sorts.descending(sortBy);
									}

									List<Document> reportDocuments = new ArrayList<Document>();

									if (filters.size() != 0) {
										reportDocuments = dataCollection
												.find(Filters.and(filters)).sort(sortFilter).projection(Filters
														.and(Projections.include(fieldNames), Projections.excludeId()))
												.into(new ArrayList<Document>());
									} else {
										reportDocuments = dataCollection
												.find().sort(sortFilter).projection(Filters
														.and(Projections.include(fieldNames), Projections.excludeId()))
												.into(new ArrayList<Document>());
									}

									MongoCollection<Document> rolesCollection = mongoTemplate
											.getCollection("roles_" + companyId);
									List<Document> roles = rolesCollection.find().into(new ArrayList<Document>());

									Map<String, String> roleIds = new HashMap<String, String>();
									for (Document role : roles) {
										String roleId = role.getObjectId("_id").toString();
										String roleName = role.getString("NAME");
										roleIds.put(roleId, roleName);
									}

									for (Document document : reportDocuments) {
										List<String> row = new ArrayList<String>();
										for (Field field : report.getFields()) {
											String fieldName = fieldsMap.get(field.getId()).getString("NAME");
											Document dataType = (Document) fieldsMap.get(field.getId())
													.get("DATA_TYPE");

											String value = "";
											if (document.get(fieldName) != null) {
												if (dataType.get("DISPLAY").toString().equalsIgnoreCase("Discussion")) {
													List<Document> messages = (List<Document>) document.get(fieldName);
													String exportMessages = "";
													for (Document message : messages) {
														exportMessages = exportMessages + message.getString("MESSAGE");
													}
													String text = Jsoup.parse(exportMessages).text();
													row.add(text);

												} else if (dataType.get("DISPLAY").toString()
														.equalsIgnoreCase("Date/Time")) {

													Date unconvertedDate = document.getDate(fieldName);
													dateTime = LocalDateTime.ofInstant(unconvertedDate.toInstant(), ZoneId.systemDefault());
													
													// Zoned date time at source timezone
													ZonedDateTime sourceTime = dateTime.atZone(fromTimeZone);
													// Zoned date time at target timezone
													ZonedDateTime targetTime = sourceTime
															.withZoneSameInstant(toTimeZone);
													// Format date time
													value = formatter.format(targetTime).toString();

												} else {
													value = document.get(fieldName).toString();
												}
											}
											if (fieldsToAdd.contains(field.getId())) {

												if (value != null && value.length() > 0) {
													// RELATIONFIELD
													Document fieldDoc = relationFields.get(field.getId());
													String primaryDisplayField = fieldDoc
															.getString("PRIMARY_DISPLAY_FIELD");
													Document relationModule = modulesCollection
															.find(Filters.eq("_id",
																	new ObjectId(fieldDoc.getString("MODULE"))))
															.first();
													List<Document> relationModuleFields = (List<Document>) relationModule
															.get("FIELDS");
													String relationModuleName = relationModule.getString("NAME");

													MongoCollection<Document> relationEntries = mongoTemplate
															.getCollection(relationModuleName + "_" + companyId);
													Document entryDoc = relationEntries
															.find(Filters.eq("_id", new ObjectId(value))).first();
													String rowValue = null;

													for (Document relationField : relationModuleFields) {
														if (relationField.getString("FIELD_ID")
																.equals(primaryDisplayField)) {
															rowValue = entryDoc
																	.getString(relationField.getString("NAME"));
															break;
														}
													}
													row.add(rowValue);
												} else {
													row.add(null);
												}

											} else {
												if (fieldName.equalsIgnoreCase("ROLE")) {
													value = roleIds.get(value);
												} else if (!dataType.get("DISPLAY").toString().equals("Discussion")) {
													row.add(value);
												}

											}
										}
										csvWriter.writeNext(row.toArray(new String[row.size()]));
									}
									csvWriter.close();

									String reportName = report.getReportName() + "-"
											+ now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
									// Create a common collection to store the reports witha a unique UUID
									MongoCollection<Document> generatedReportsCollection = mongoTemplate
											.getCollection("generated_reports");
									JSONObject reportToInsert = new JSONObject();
									String reportUuid = UUID.randomUUID().toString();
									reportToInsert.put("UUID", reportUuid);
									reportToInsert.put("FILE", writer.toString());
									reportToInsert.put("REPORT_ID", reportId);
									reportToInsert.put("COMPANY_ID", companyId);
									reportToInsert.put("FILE_NAME", reportName);
									// add company id and report id
									generatedReportsCollection.insertOne(Document.parse(reportToInsert.toString()));
									String link = "https://" + companySubdomain
											+ ".ngdesk.com/ngdesk-rest/ngdesk/reports/schedules/download?uuid="
											+ reportUuid;

									// send email with above uuid
									for (int i = 0; i < emails.length(); i++) {
										// make resources file
										String messageHTML = global.getFile("report_schedule_email.html");
										messageHTML = messageHTML.replaceAll("REPORT_NAME", reportName);
										messageHTML = messageHTML.replaceAll("LINK", link);
										SendEmail email = new SendEmail(emails.getString(i), "support@ngdesk.com",
												"Scheduled Report", messageHTML, host);
										email.sendEmail();
									}
									log.trace("Exit schedule()");
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
				SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com", "support@ngdesk.com",
						"Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToSpencer.sendEmail();

				SendEmail sendEmailToShashank = new SendEmail("shashank.shankaranand@allbluesolutions.com",
						"support@ngdesk.com", "Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToShashank.sendEmail();
			}

		}
	}

}
