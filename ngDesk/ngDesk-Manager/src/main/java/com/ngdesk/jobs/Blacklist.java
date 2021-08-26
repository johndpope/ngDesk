package com.ngdesk.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.SendMail;

@Component
public class Blacklist {

	private final Logger log = LoggerFactory.getLogger(Blacklist.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@Autowired
	SendMail sendMail;

	@Autowired
	private Environment env;
	
	@Autowired
	private RedissonClient redisson;

	@Scheduled(fixedRate = 60000)
	public void executeJob() {
		log.trace("Enter Blacklist.executeJob()");
		String subdomain = "";
		
		try {
			
			RMap<String, Map<String, List<Timestamp>>> outgoingMails = redisson.getMap("outgoingMails");
			RMap<String, Map<String, List<Timestamp>>> incomingMails = redisson.getMap("incomingMails");
			
			for (String companyId : incomingMails.keySet()) {
				MongoCollection<Document> blacklistCollection = mongoTemplate
						.getCollection("blacklisted_whitelisted_emails_" + companyId);
				List<String> incomingEmailsToRemove = new ArrayList<String>();
				List<String> outgoingEmailsToRemove = new ArrayList<String>();

				if (incomingMails.containsKey(companyId)) {
					// INCOMING EMAILS
					for (String email : incomingMails.get(companyId).keySet()) {
						Timestamp currentTimestamp = new Timestamp(new Date().getTime());
						long millisec = TimeUnit.MINUTES.toMillis(1);
						long oneMinuteAgo = currentTimestamp.getTime() - millisec;

						if (incomingMails.get(companyId).get(email).size() >= 5) {

							Document blacklistDoc = blacklistCollection
									.find(Filters.and(Filters.eq("STATUS", "BLACKLIST"), Filters.eq("TYPE", "INCOMING"),
											Filters.eq("EMAIL_ADDRESS", email)))
									.first();

							if (blacklistDoc == null) {
								JSONObject blacklistEmail = new JSONObject();
								blacklistEmail.put("EMAIL_ADDRESS", email);
								blacklistEmail.put("TYPE", "INCOMING");
								blacklistEmail.put("DATE_CREATED",
										global.getFormattedDate(new Timestamp(new Date().getTime())));
								blacklistEmail.put("IS_DOMAIN", false);
								blacklistEmail.put("STATUS", "BLACKLIST");
								blacklistCollection.insertOne(Document.parse(blacklistEmail.toString()));

								// EMAIL ALL ADMINS OF COMPANY ABOUT BLACKLISTED EMAIL
								MongoCollection<Document> rolesCollection = mongoTemplate
										.getCollection("roles_" + companyId);
								Document adminRole = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first();
								String roleId = adminRole.getObjectId("_id").toString();

								MongoCollection<Document> usersCollection = mongoTemplate
										.getCollection("Users_" + companyId);
								List<Document> adminUsers = usersCollection
										.find(Filters.and(Filters.eq("ROLE", roleId), Filters.eq("DELETED", false),
												Filters.or(Filters.eq("EFFECTIVE_TO", null),
														Filters.exists("EFFECTIVE_TO", false))))
										.into(new ArrayList<Document>());

								String subject = "An email has been blocked";
								for (Document user : adminUsers) {
									String emailAddress = user.getString("EMAIL_ADDRESS");
									String firstName = user.getString("FIRST_NAME");
									String lastName = "";

									if (user.containsKey("LAST_NAME") && user.get("LAST_NAME") != null) {
										lastName = user.getString("LAST_NAME");
									}

									String body = global.getFile("blacklist-incoming-body.html");
									body = body.replace("FULL_NAME_REPLACE", firstName + " " + lastName);
									body = body.replace("FROM_EMAIL_REPLACE", email);

									sendMail.send(emailAddress, "support@ngdesk.com", subject, body);
								}
							}
							incomingEmailsToRemove.add(email);
						} else {
							List<Timestamp> timestamps = incomingMails.get(companyId).get(email);
							for (int i = timestamps.size() - 1; i >= 0; i--) {
								Timestamp timestamp = timestamps.get(i);
								long timestampInMils = timestamp.getTime();
								if (oneMinuteAgo >= timestampInMils) {
									timestamps.remove(i);
								}
							}

							if (timestamps.size() == 0) {
								incomingEmailsToRemove.add(email);
							} else {
								
								Map<String,List<Timestamp>> userTimestamps = incomingMails.get(companyId);
								userTimestamps.put(email, timestamps);
								
								incomingMails.put(companyId, userTimestamps);
							}

						}
					}
				}

				// OUTGOING EMAILS
				if (outgoingMails.containsKey(companyId)) {
					for (String email : outgoingMails.get(companyId).keySet()) {

						Timestamp currentTimestamp = new Timestamp(new Date().getTime());
						long millisec = TimeUnit.MINUTES.toMillis(1);
						long oneMinuteAgo = currentTimestamp.getTime() + millisec;

						if (outgoingMails.get(companyId).get(email).size() >= 25) {

							Document blacklistDoc = blacklistCollection
									.find(Filters.and(Filters.eq("STATUS", "BLACKLIST"), Filters.eq("TYPE", "OUTGOING"),
											Filters.eq("EMAIL_ADDRESS", email)))
									.first();

							if (blacklistDoc == null) {
								JSONObject blacklistEmail = new JSONObject();
								blacklistEmail.put("EMAIL_ADDRESS", email);
								blacklistEmail.put("TYPE", "OUTGOING");
								blacklistEmail.put("DATE_CREATED",
										global.getFormattedDate(new Timestamp(new Date().getTime())));
								blacklistEmail.put("STATUS", "BLACKLIST");
								blacklistEmail.put("IS_DOMAIN", false);
								blacklistCollection.insertOne(Document.parse(blacklistEmail.toString()));

								// EMAIL ALL ADMINS OF COMPANY ABOUT BLACKLISTED EMAIL
								MongoCollection<Document> rolesCollection = mongoTemplate
										.getCollection("roles_" + companyId);
								Document adminRole = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first();
								String roleId = adminRole.getObjectId("_id").toString();

								MongoCollection<Document> usersCollection = mongoTemplate
										.getCollection("Users_" + companyId);

								MongoCollection<Document> companyCollection = mongoTemplate.getCollection("companies");
								Document companyDocument = companyCollection
										.find(Filters.eq("_id", new ObjectId(companyId))).first();
								subdomain = companyDocument.getString("COMPANY_SUBDOMAIN");

								List<Document> adminUsers = usersCollection
										.find(Filters.and(Filters.eq("ROLE", roleId), Filters.eq("DELETED", false),
												Filters.or(Filters.eq("EFFECTIVE_TO", null),
														Filters.exists("EFFECTIVE_TO", false))))
										.into(new ArrayList<Document>());

								String subject = "An email has been blocked";
								for (Document user : adminUsers) {
									String emailAddress = user.getString("EMAIL_ADDRESS");
									String firstName = user.getString("FIRST_NAME");
									String lastName = "";

									if (user.containsKey("LAST_NAME") && user.get("LAST_NAME") != null) {
										lastName = user.getString("LAST_NAME");
									}

									String body = global.getFile("blacklist-outgoing-body.html");
									body = body.replace("FULL_NAME_REPLACE", firstName + " " + lastName);
									body = body.replace("FROM_EMAIL_REPLACE", email);
									body = body.replace("SUB_DOMAIN", subdomain);

									sendMail.send(emailAddress, "support@ngdesk.com", subject, body);
								}

							}
							outgoingEmailsToRemove.add(email);
						} else {
							List<Timestamp> timestamps = outgoingMails.get(companyId).get(email);
							for (int i = timestamps.size() - 1; i >= 0; i--) {
								Timestamp timestamp = timestamps.get(i);
								long timestampInMils = timestamp.getTime();
								if (oneMinuteAgo >= timestampInMils) {
									timestamps.remove(i);
								}
							}

							if (timestamps.size() == 0) {
								outgoingEmailsToRemove.add(email);
							} else {
								
								Map<String, List<Timestamp>> userTimes = outgoingMails.get(companyId);
								userTimes.put(email, timestamps);
								outgoingMails.put(companyId, userTimes);
							}

						}
					}
				}
				
				Map<String, List<Timestamp>> userTimes = incomingMails.get(companyId);
				for (String email : incomingEmailsToRemove) {
					userTimes.remove(email);
				}
				incomingMails.put(companyId, userTimes);
				
				Map<String, List<Timestamp>> userTimestamps = outgoingMails.get(companyId);
				for (String email : outgoingEmailsToRemove) {
					userTimestamps.remove(email);
				}
				outgoingMails.put(companyId, userTimes);
			}

		} catch (Exception e) {
			e.printStackTrace();

			String subject = "Call Failed on Blacklist for " + subdomain;
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();
			sStackTrace += "<br/><br/>" + sStackTrace;
			String environment = env.getProperty("env");
			if (environment.equals("prd")) {
				sendMail.send("spencer@allbluesolutions.com", "support@ngdesk.com", subject, sStackTrace);
				sendMail.send("shashank@allbluesolutions.com", "support@ngdesk.com", subject, sStackTrace);
			}

		}

		log.trace("Exit Blacklist.executeJob()");
	}
}
