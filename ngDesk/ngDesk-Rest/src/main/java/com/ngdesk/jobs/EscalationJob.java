package com.ngdesk.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.redisson.api.RMap;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.Notify;
import com.ngdesk.email.SendEmail;
import com.ngdesk.escalations.EscalateTo;
import com.ngdesk.escalations.EscalationRule;
import com.ngdesk.modules.monitors.MonitorJob;
import com.ngdesk.schedules.OnCallUser;

@Component
public class EscalationJob {

	@Value("${email.host}")
	private String host;

	@Value("${env}")
	private String environment;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private OnCallUser callUser;

	@Autowired
	private Notify notify;

	@Autowired
	MonitorJob monitorJob;

	@Autowired
	Global global;

	@Autowired
	RedissonClient redisson;

	private final Logger log = LoggerFactory.getLogger(EscalationJob.class);

	@Scheduled(fixedRate = 60000)
	public void executeJob() {

		try {

			log.trace("Enter EscalationJob.executeJob()");

			String epochDate = "01/01/1970";
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
			Timestamp epoch = new Timestamp(date.getTime());

			Timestamp today = new Timestamp(new Date().getTime());
			long currentTimeDiff = today.getTime() - epoch.getTime();
			RSortedSet<Long> escalationTimes = redisson.getSortedSet("escalationTimes");
			RMap<Long, String> escalationRules = redisson.getMap("escalationRules");
			HashMap<Long, JSONObject> timesToRemove = new HashMap<Long, JSONObject>();
			String companyId = null;

			for (Long timeDiff : escalationTimes) {

				log.debug("Has EscalationTime");

				if (currentTimeDiff >= timeDiff) {

					log.debug("CurrentTimeDiff: " + currentTimeDiff);
					log.debug("TimeDiff: " + timeDiff);

					JSONObject ruleJson = new JSONObject(escalationRules.get(timeDiff));

					companyId = ruleJson.getString("COMPANY_ID");
					String escalationId = ruleJson.getString("ESCALATION_ID");
					String entryId = ruleJson.getString("ENTRY_ID");

					log.debug("companyId: " + companyId);
					log.debug("escalationId: " + escalationId);
					log.debug("entryId: " + entryId);

					ruleJson.remove("COMPANY_ID");
					ruleJson.remove("ESCALATION_ID");
					ruleJson.remove("ENTRY_ID");

					JSONObject timetoRemove = new JSONObject();
					timetoRemove.put("ESCALATION_ID", escalationId);
					timetoRemove.put("ENTRY_ID", entryId);
					timetoRemove.put("CLEAR_ENTRIES", false);
					timesToRemove.put(timeDiff, timetoRemove);

					MongoCollection<Document> escalations = mongoTemplate.getCollection("escalations_" + companyId);
					Document escalation = escalations.find(Filters.eq("_id", new ObjectId(escalationId))).first();

					if (escalation != null) {

						List<Document> ruleDocuments = (List<Document>) escalation.get("RULES");
						Document nextRule = null;

						Integer currentOrder = ruleJson.getInt("ORDER");

						Document currentRule = ruleDocuments.stream()
								.filter(ruleDocument -> ruleDocument.getInteger("ORDER") == currentOrder).findFirst()
								.orElse(null);

						if (currentRule != null) {
							ruleJson = new JSONObject(currentRule.toJson());
						}

						Integer nextOrder = currentOrder + 1;
						nextRule = ruleDocuments.stream()
								.filter(ruleDocument -> ruleDocument.getInteger("ORDER") == nextOrder).findFirst()
								.orElse(null);

						if (nextRule != null) {
							monitorJob.addEscalationToRedis(escalationId, nextRule, companyId, entryId);
						} else {
							timesToRemove.get(timeDiff).put("CLEAR_ENTRIES", true);
						}

						MongoCollection<Document> escalationcollection = mongoTemplate
								.getCollection("escalated_entries_" + companyId);
						Document escalatedEntry = escalationcollection.find(
								Filters.and(Filters.eq("ESCALATION_ID", escalationId), Filters.eq("ENTRY_ID", entryId)))
								.first();

						MongoCollection<Document> userscollection = mongoTemplate.getCollection("Users_" + companyId);
						EscalationRule rule = new ObjectMapper().readValue(ruleJson.toString(), EscalationRule.class);

						if (escalatedEntry != null) {

							String moduleId = escalatedEntry.getString("MODULE_ID");
							String subject = escalatedEntry.getString("SUBJECT");
							String body = escalatedEntry.getString("BODY");
							JSONObject mobileParams = new JSONObject();

							MongoCollection<Document> modulesCollection = mongoTemplate
									.getCollection("modules_" + companyId);
							Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

							if (module != null) {

								String moduleName = module.getString("NAME");
								MongoCollection<Document> entriesCollection = mongoTemplate
										.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);

								Document moduleEntry = entriesCollection.find(Filters
										.and(Filters.eq("_id", new ObjectId(entryId)), Filters.eq("DELETED", false)))
										.first();

								if (moduleEntry != null) {
									moduleEntry.remove("_id");
									moduleEntry.put("DATA_ID", entryId);
									String entryString = new ObjectMapper().writeValueAsString(moduleEntry);
									HashMap<String, Object> inputMessage = new ObjectMapper().readValue(entryString,
											HashMap.class);

									String reg = "\\{\\{(?i)(inputMessage[_a-zA-Z\\.\\-]+)\\}\\}";
									Pattern r = Pattern.compile(reg);
									Matcher matcherSubject = r.matcher(subject);
									Matcher matcherBody = r.matcher(body);

									if (body.length() == 0) {
										body = "";
									}

									while (matcherSubject.find()) {
										try {
											String value = global.getValue(
													matcherSubject.group(1).split("(?i)inputMessage\\.")[1],
													inputMessage, companyId, moduleId, entryId, false);
											if (value != null) {
												subject = subject.replaceAll(
														"\\{\\{" + matcherSubject.group(1) + "\\}\\}", value);

											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
									while (matcherBody.find()) {
										try {
											String value = global.getValue(
													matcherBody.group(1).split("(?i)inputMessage\\.")[1], inputMessage,
													companyId, moduleId, entryId, false);
											if (value != null) {
												body = body.replaceAll("\\{\\{" + matcherBody.group(1) + "\\}\\}",
														value);
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									}

									EscalateTo escalateTo = rule.getEscalateTo();
									List<String> scheduleIds = escalateTo.getScheduleIds();
									List<String> userIds = escalateTo.getUserIds();
									List<String> teamIds = escalateTo.getTeamIds();

									String metadataHtml = global.getFile("escalation_metadata.html");

									metadataHtml = metadataHtml.replace("ESCALATION_RULE_NUMBER",
											Integer.toString(rule.getOrder()));
									metadataHtml = metadataHtml.replaceAll("ESCALATION_NAME_REPLACE",
											escalation.getString("NAME"));

									String htmlToReplace = "";

									mobileParams.put("MODULE_ID", moduleId);
									mobileParams.put("DATA_ID", entryId);

									for (String teamId : teamIds) {
										String collectionName = "Teams_" + companyId;
										MongoCollection<Document> collection = mongoTemplate
												.getCollection(collectionName);
										Document teamDoc = collection.find(Filters.eq("_id", new ObjectId(teamId)))
												.first();

										if (teamDoc != null && teamDoc.get("USERS") != null) {
											List<String> users = (List<String>) teamDoc.get("USERS");
											for (String userId : users) {
												userIds.add(userId);
											}
										}
									}

									for (String scheduleId : scheduleIds) {
										String collectionName = "schedules_" + companyId;
										MongoCollection<Document> collection = mongoTemplate
												.getCollection(collectionName);
										Document scheduleDocument = collection
												.find(Filters.eq("_id", new ObjectId(scheduleId))).first();

										if (scheduleDocument != null) {
											String scheduleName = scheduleDocument.getString("name");
											String userId = callUser.CallUser(scheduleName, companyId);
											if (userId != null && userId.length() > 0) {
												Document userDocument = userscollection
														.find(Filters.eq("_id", new ObjectId(userId))).first();

												String contactMethod = "Email";
												if (userDocument.get("DEFAULT_CONTACT_METHOD") != null) {
													contactMethod = userDocument.getString("DEFAULT_CONTACT_METHOD");
												} else if (userDocument.get("DEFAULT_CONTACT_METHOD") == null
														&& userDocument.get("CONTACT") != null) {
													String contactId = userDocument.getString("CONTACT");
													MongoCollection<Document> contactsCollection = mongoTemplate
															.getCollection("Contacts_" + companyId);
													Document contact = contactsCollection
															.find(Filters.eq("_id", new ObjectId(contactId))).first();
													if (contact != null) {
														contactMethod = contact.getString("DEFAULT_CONTACT_METHOD");
													}
												}

												if (notify.notifyUser(companyId, userId, contactMethod, escalationId,
														subject, body, mobileParams)) {

													String filename = null;
													if (contactMethod.equalsIgnoreCase("Email")) {
														filename = "mail-icon.html";
													} else {
														filename = "bell-icon.html";
													}
													htmlToReplace += global.getFile(filename).replaceAll(
															"EMAIL_ADDRESS", userDocument.getString("EMAIL_ADDRESS"))
															+ "<br/>";
												}
											}
										}
									}
									for (String userId : userIds) {
										if (userId != null && userId.length() > 0) {
											Document userDocument = userscollection
													.find(Filters.eq("_id", new ObjectId(userId))).first();
											if (userDocument != null) {
												String contactMethod = "Email";
												if (userDocument.get("DEFAULT_CONTACT_METHOD") != null) {
													contactMethod = userDocument.getString("DEFAULT_CONTACT_METHOD");
												} else if (userDocument.get("DEFAULT_CONTACT_METHOD") == null
														&& userDocument.get("CONTACT") != null) {
													String contactId = userDocument.getString("CONTACT");
													MongoCollection<Document> contactsCollection = mongoTemplate
															.getCollection("Contacts_" + companyId);
													Document contact = contactsCollection
															.find(Filters.eq("_id", new ObjectId(contactId))).first();
													if (contact != null) {
														contactMethod = contact.getString("DEFAULT_CONTACT_METHOD");
													}
												}
												if (notify.notifyUser(companyId, userId, contactMethod, escalationId,
														subject, body, mobileParams)) {
													String filename = null;
													if (contactMethod.equalsIgnoreCase("Email")) {
														filename = "mail-icon.html";
													} else {
														filename = "bell-icon.html";
													}
													htmlToReplace += global.getFile(filename).replaceAll(
															"EMAIL_ADDRESS", userDocument.getString("EMAIL_ADDRESS"))
															+ "<br/>";
												}
											}
										}
									}

									metadataHtml = metadataHtml.replaceAll("REPLACE_TO_LIST", htmlToReplace);
									MongoCollection<Document> usersCollection = mongoTemplate
											.getCollection("Users_" + companyId);

									Document systemUser = usersCollection
											.find(Filters.eq("EMAIL_ADDRESS", "system@ngdesk.com")).first();

									String contactId = systemUser.getString("CONTACT");
									MongoCollection<Document> contactsCollection = mongoTemplate
											.getCollection("Contacts_" + companyId);

									String firstName = "";
									String lastName = "";

									if (contactId != null) {
										Document contact = contactsCollection
												.find(Filters.eq("_id", new ObjectId(contactId))).first();
										if (contact != null) {
											firstName = contact.getString("FIRST_NAME");
											lastName = contact.getString("LAST_NAME");
										}
									}

									JSONObject sender = new JSONObject();

									if (systemUser != null) {
										sender.put("FIRST_NAME", firstName);
										sender.put("LAST_NAME", lastName);
										sender.put("ROLE", systemUser.getString("ROLE"));
										sender.put("USER_UUID", systemUser.getString("USER_UUID"));
									}

									JSONObject message = new JSONObject();
									message.put("MESSAGE_ID", UUID.randomUUID().toString());
									message.put("MESSAGE_TYPE", "META_DATA");
									message.put("MESSAGE", metadataHtml);
									message.put("SENDER", sender);
									log.debug("message: " + message);
									Document discussionMessage = Document.parse(message.toString());
									discussionMessage.put("DATE_CREATED", new Date());
									String discussionFieldName = null;
									List<Document> fields = (List<Document>) module.get("FIELDS");
									for (Document field : fields) {
										Document dataType = (Document) field.get("DATA_TYPE");
										if (dataType.getString("DISPLAY").equals("Discussion")) {
											discussionFieldName = field.getString("NAME");
											break;
										}
									}

									if (discussionFieldName != null) {
										entriesCollection.updateOne(Filters.eq("_id", new ObjectId(entryId)),
												Updates.addToSet(discussionFieldName, discussionMessage));
									}

									entriesCollection.updateOne(Filters.eq("_id", new ObjectId(entryId)),
											Updates.addToSet("META_DATA.EVENTS", discussionMessage));
								}
							}
						}
					} else {
						timesToRemove.get(timeDiff).put("CLEAR_ENTRIES", true);
					}
				} else {
					break;
				}
			}

			for (Long time : timesToRemove.keySet()) {
				escalationTimes.remove(time);
				escalationRules.remove(time);
				JSONObject timeToRemove = timesToRemove.get(time);
				String escalationId = timeToRemove.getString("ESCALATION_ID");
				String entryId = timeToRemove.getString("ENTRY_ID");

				if (timeToRemove.getBoolean("CLEAR_ENTRIES")) {
					String collectionName = "escalated_entries_" + companyId;
					MongoCollection<Document> escalationCollection = mongoTemplate.getCollection(collectionName);
					escalationCollection.deleteOne(
							Filters.and(Filters.eq("ESCALATION_ID", escalationId), Filters.eq("ENTRY_ID", entryId)));
				}
			}
			timesToRemove.clear();
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

		log.trace("Exit EscalationJob.executeJob()");
	}

}
