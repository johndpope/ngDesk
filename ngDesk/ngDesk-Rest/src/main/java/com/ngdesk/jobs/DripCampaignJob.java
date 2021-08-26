package com.ngdesk.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
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
import com.ngdesk.Global;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.InternalErrorException;

@Component
public class DripCampaignJob {

	private final static Logger log = LoggerFactory.getLogger(DripCampaignJob.class);
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@Value("${email.host}")
	private String host;
	
	@Value("${env}")
	private String environment;

	@Scheduled(cron = "0 0 0 * * *")
	public void campaingnJob() {
		try {
			log.trace("Enter Jobs.campaingnJob()");
			// COMPANIES CREATED BEFORE 1, 3, 5, 7, 10 days
			int[] days = { 1, 3, 5, 7, 10 };
			Document campaignDocumentInviteUsers = new Document();
			Document campaignDocumentEscalation = new Document();
			Document campaignDocumentSchedule = new Document();
			Document campaignDocumentChat = new Document();
			MongoCollection<Document> dripCampaignCollection = mongoTemplate.getCollection("drip_campaigns");

			// INSERT THE DOCUMENT TO CAMPAIGN COLLECTION
			dripCampaignCollection.insertOne(campaignDocumentInviteUsers);
			dripCampaignCollection.insertOne(campaignDocumentEscalation);
			dripCampaignCollection.insertOne(campaignDocumentSchedule);
			dripCampaignCollection.insertOne(campaignDocumentChat);

			String campaignInviteUsersId = campaignDocumentInviteUsers.remove("_id").toString();
			String campaignEscalationId = campaignDocumentEscalation.remove("_id").toString();
			String campaignSchedulesId = campaignDocumentSchedule.remove("_id").toString();
			String campaignChatId = campaignDocumentChat.remove("_id").toString();
			for (int day : days) {
				List<Document> emailInviteUserDocs = new ArrayList<Document>();
				List<Document> emailScheduleDocs = new ArrayList<Document>();
				List<Document> emailEscalationDocs = new ArrayList<Document>();
				List<Document> emailChatDocs = new ArrayList<Document>();

				// GET FROM AND TO DATE FOR FILTERS
				JSONObject date = getFromAndToDate(day);

				MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");

				List<Document> companyList = companiesCollection
						.find(Filters.and(Filters.gte("DATE_CREATED", date.getString("FROM_DATE")),
								Filters.lt("DATE_CREATED", date.getString("TO_DATE"))))
						.into(new ArrayList<Document>());

				for (Document company : companyList) {
					Document usagetypeDoc = (Document) company.get("USAGE_TYPE");
					if (usagetypeDoc == null) {
						continue;
					}
					Boolean pager = usagetypeDoc.getBoolean("PAGER");
					if (!pager) {
						continue;
					}

					String companyId = company.getObjectId("_id").toString();

					MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
					Document role = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first();
					String roleId = role.getObjectId("_id").toString();

					MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
					List<Document> users = usersCollection
							.find(Filters.and(Filters.eq("DELETED", false), Filters.eq("ROLE", roleId)))
							.into(new ArrayList<Document>());

					MongoCollection<Document> eventTrackingCollection = mongoTemplate.getCollection("event_tracking");

					// CHECK FOR ESCALATION
					Document escalationEvent = eventTrackingCollection.find(Filters
							.and(Filters.eq("EVENT_NAME", "Created Escalation"), Filters.eq("COMPANY_ID", companyId)))
							.first();
					if (escalationEvent != null) {
						continue;
					}
					for (Document user : users) {

						String userUUID = user.getString("USER_UUID").toString();

						if (user.getBoolean("SUBSCRIPTION_ON_MARKETING_EMAIL")) {
							// SEND SURVEY EMAIL
							Document emailDoc = new Document();
							emailDoc.put("USER_UUID", userUUID);
							emailDoc.put("COMPANY_ID", companyId);
							emailDoc.put("EMAIL_OPENED", false);

							emailEscalationDocs.add(emailDoc);
							// SEND EMAIL TO ALL THE SYSTEM ADMINS
							sendCampaignEmail(user.getString("EMAIL_ADDRESS"), company.getString("COMPANY_SUBDOMAIN"),
									user.getString("FIRST_NAME"), user.getString("LAST_NAME"), userUUID,
									"marketing_escalation.html", campaignEscalationId, String.valueOf(day));
						}
					}
				}

				// CREATE A KEY WITH SPECIFIC DAYS
				campaignDocumentEscalation.put("CAMPAIGN_DAYS_" + day, emailEscalationDocs);
				campaignDocumentEscalation.put("CAMPAIGN_NAME", "ESCALATION");

				// SCHEDULE
				for (Document company : companyList) {
					Document usagetypeDoc = (Document) company.get("USAGE_TYPE");
					if (usagetypeDoc == null) {
						continue;
					}
					Boolean pager = usagetypeDoc.getBoolean("PAGER");
					if (!pager) {
						continue;
					}

					String companyId = company.getObjectId("_id").toString();

					MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
					Document role = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first();
					String roleId = role.getObjectId("_id").toString();

					MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
					List<Document> users = usersCollection
							.find(Filters.and(Filters.eq("DELETED", false), Filters.eq("ROLE", roleId)))
							.into(new ArrayList<Document>());

					MongoCollection<Document> eventTrackingCollection = mongoTemplate.getCollection("event_tracking");

					// CHECK FOR SCHEDULE
					Document scheduleEvent = eventTrackingCollection.find(Filters
							.and(Filters.eq("EVENT_NAME", "Created Schedule"), Filters.eq("COMPANY_ID", companyId)))
							.first();
					if (scheduleEvent != null) {
						continue;
					}
					for (Document user : users) {

						String userUUID = user.getString("USER_UUID").toString();

						if (user.getBoolean("SUBSCRIPTION_ON_MARKETING_EMAIL")) {
							// SEND SURVEY EMAIL
							Document emailDoc = new Document();
							emailDoc.put("USER_UUID", userUUID);
							emailDoc.put("COMPANY_ID", companyId);
							emailDoc.put("EMAIL_OPENED", false);

							emailScheduleDocs.add(emailDoc);
							// SEND EMAIL TO ALL THE SYSTEM ADMINS
							sendCampaignEmail(user.getString("EMAIL_ADDRESS"), company.getString("COMPANY_SUBDOMAIN"),
									user.getString("FIRST_NAME"), user.getString("LAST_NAME"), userUUID,
									"marketing_schedule.html", campaignSchedulesId, String.valueOf(day));
						}
					}
				}

				// CREATE A KEY WITH SPECIFIC DAYS
				campaignDocumentSchedule.put("CAMPAIGN_DAYS_" + day, emailScheduleDocs);
				campaignDocumentSchedule.put("CAMPAIGN_NAME", "SCHEDULE");

				// INVITE USERS
				for (Document company : companyList) {

					String companyId = company.getObjectId("_id").toString();

					MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
					Document role = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first();
					String roleId = role.getObjectId("_id").toString();

					MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
					List<Document> users = usersCollection
							.find(Filters.and(Filters.eq("DELETED", false), Filters.eq("ROLE", roleId)))
							.into(new ArrayList<Document>());

					MongoCollection<Document> eventTrackingCollection = mongoTemplate.getCollection("event_tracking");

					// CHECK IF THE COMPANY INVITED USERS
					Document inviteUserEvent = eventTrackingCollection.find(
							Filters.and(Filters.eq("EVENT_NAME", "Invited users"), Filters.eq("COMPANY_ID", companyId)))
							.first();
					if (inviteUserEvent != null) {
						continue;
					}
					for (Document user : users) {

						String userUUID = user.getString("USER_UUID").toString();

						if (user.getBoolean("SUBSCRIPTION_ON_MARKETING_EMAIL")) {
							// SEND SURVEY EMAIL
							Document emailDoc = new Document();
							emailDoc.put("USER_UUID", userUUID);
							emailDoc.put("COMPANY_ID", companyId);
							emailDoc.put("EMAIL_OPENED", false);

							emailInviteUserDocs.add(emailDoc);
							// SEND EMAIL TO ALL THE SYSTEM ADMINS
							sendCampaignEmail(user.getString("EMAIL_ADDRESS"), company.getString("COMPANY_SUBDOMAIN"),
									user.getString("FIRST_NAME"), user.getString("LAST_NAME"), userUUID,
									"marketing_invite_users.html", campaignInviteUsersId, String.valueOf(day));
						}
					}
				}

				// CREATE A KEY WITH SPECIFIC DAYS
				campaignDocumentInviteUsers.put("CAMPAIGN_DAYS_" + day, emailInviteUserDocs);
				campaignDocumentInviteUsers.put("CAMPAIGN_NAME", "INVITE_USERS");

				// CHAT
				for (Document company : companyList) {
					Document usagetypeDoc = (Document) company.get("USAGE_TYPE");
					if (usagetypeDoc == null) {
						continue;
					}
					Boolean chat = usagetypeDoc.getBoolean("CHAT");
					if (chat) {

						String companyId = company.getObjectId("_id").toString();

						MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
						Document role = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first();
						String roleId = role.getObjectId("_id").toString();

						MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
						List<Document> users = usersCollection
								.find(Filters.and(Filters.eq("DELETED", false), Filters.eq("ROLE", roleId)))
								.into(new ArrayList<Document>());

						for (Document user : users) {

							String userUUID = user.getString("USER_UUID").toString();
							if (user.getBoolean("SUBSCRIPTION_ON_MARKETING_EMAIL")) {
								// SEND SURVEY EMAIL
								Document emailDoc = new Document();
								emailDoc.put("USER_UUID", userUUID);
								emailDoc.put("COMPANY_ID", companyId);
								emailDoc.put("EMAIL_OPENED", false);

								emailChatDocs.add(emailDoc);
								// SEND EMAIL TO ALL THE SYSTEM ADMINS
								sendCampaignEmail(user.getString("EMAIL_ADDRESS"),
										company.getString("COMPANY_SUBDOMAIN"), user.getString("FIRST_NAME"),
										user.getString("LAST_NAME"), userUUID, "marketing_chat.html", campaignChatId,
										String.valueOf(day));
							}
						}

					}
				}

				// CREATE A KEY WITH SPECIFIC DAYS
				campaignDocumentInviteUsers.put("CAMPAIGN_DAYS_" + day, emailChatDocs);
				campaignDocumentInviteUsers.put("CAMPAIGN_NAME", "CHAT");
			}

			// UPDATE THE CAMPAIGN COLLECTION
			campaignDocumentEscalation.put("CAMPAIGN_DATE",
					global.getFormattedDate(new Timestamp(new Date().getTime())));
			dripCampaignCollection.findOneAndReplace(Filters.eq("_id", new ObjectId(campaignEscalationId)),
					campaignDocumentEscalation);

			campaignDocumentSchedule.put("CAMPAIGN_DATE", global.getFormattedDate(new Timestamp(new Date().getTime())));
			dripCampaignCollection.findOneAndReplace(Filters.eq("_id", new ObjectId(campaignSchedulesId)),
					campaignDocumentSchedule);

			campaignDocumentInviteUsers.put("CAMPAIGN_DATE",
					global.getFormattedDate(new Timestamp(new Date().getTime())));
			dripCampaignCollection.findOneAndReplace(Filters.eq("_id", new ObjectId(campaignInviteUsersId)),
					campaignDocumentInviteUsers);

			campaignDocumentChat.put("CAMPAIGN_DATE", global.getFormattedDate(new Timestamp(new Date().getTime())));
			dripCampaignCollection.findOneAndReplace(Filters.eq("_id", new ObjectId(campaignChatId)),
					campaignDocumentChat);

		} catch (Exception e) {
			e.printStackTrace();

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();

			if (environment.equals("prd")) {
				SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com", "support@ngdesk.com",
						"Internal Error: Stack Trace :Drip campaign", sStackTrace, host);
				sendEmailToSpencer.sendEmail();

				SendEmail sendEmailToShashank = new SendEmail("shashank.shankaranand@allbluesolutions.com",
						"support@ngdesk.com", "Internal Error: Stack Trace :Drip campaign", sStackTrace, host);
				sendEmailToShashank.sendEmail();
			}
		}
		log.trace("Exit Jobs.campaingnJob()");
	}

	public JSONObject getFromAndToDate(int days) {
		try {
			Date now = new Date();
			DateFormat outputFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(now);
			calendar.add(Calendar.DATE, -days);
			calendar.add(Calendar.HOUR, 0);
			Date previousDate = calendar.getTime();
			String output = outputFormatter.format(previousDate);

			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTime(now);
			calendar1.add(Calendar.DATE, -days);
			calendar1.add(Calendar.HOUR, 23);
			calendar1.add(Calendar.MINUTE, 59);
			calendar1.add(Calendar.SECOND, 59);
			Date previousDate1 = calendar1.getTime();
			String output1 = outputFormatter.format(previousDate1);
			JSONObject date = new JSONObject();
			date.put("FROM_DATE", output);
			date.put("TO_DATE", output1);
			return date;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new JSONObject();
	}

	public void sendCampaignEmail(String emailAddress, String subdomain, String firstName, String lastName,
			String userUUID, String fileName, String campaignId, String campaignDays) {
		try {
			log.trace("Enter Global.sendSurveyEmail() emailAddress: " + emailAddress + ", subdomain: " + subdomain
					+ ",firstName:  " + firstName + ",lastName:  " + lastName);
			String from = "support@" + subdomain + ".ngdesk.com";

			// SET EMAIL BODY
			String messageSurvey = global.getFile(fileName);
			messageSurvey = messageSurvey.replaceAll("FIRST_NAME", firstName).replaceAll("LAST_NAME", lastName)
					.replaceAll("SUBDOMAIN", subdomain).replaceAll("EMAIL", emailAddress)
					.replaceAll("USER_UUID", userUUID).replaceAll("TYPE", "user").replaceAll("CAMPAIGN_ID", campaignId)
					.replaceAll("CAMPAIGN_DAYS", campaignDays);

			String subjectSurvey = "Getting more from ngDesk";

			SendEmail sendSurveyEmail = new SendEmail(emailAddress, from, subjectSurvey, messageSurvey, host);
			log.trace("Exit Global.sendSurveyEmail() emailAddress: " + emailAddress + ", subdomain: " + subdomain
					+ ",firstName:  " + firstName + ",lastName:  " + lastName);
			sendSurveyEmail.sendEmail();
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}
}
