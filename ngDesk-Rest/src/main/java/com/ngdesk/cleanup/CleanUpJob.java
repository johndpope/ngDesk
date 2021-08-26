package com.ngdesk.cleanup;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.companies.CompanyService;
import com.ngdesk.email.SendEmail;

@Component
public class CleanUpJob {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	CompanyService companyService;

	@Value("${email.host}")
	private String host;

	@Value("${env}")
	private String environment;

	private final Logger log = LoggerFactory.getLogger(CleanUpJob.class);

	@Scheduled(fixedRate = 86400000)
	public void executeJob() {
		try {
			log.trace("Enter CleanUpJob.executeJob()");

			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			List<Document> companies = companiesCollection.find(Filters.eq("VERSION", "v2"))
					.into(new ArrayList<Document>());

			Map<String, String> companiesToArchive = new HashMap<String, String>();

			Date date = new Date();
			String formattedToday = new SimpleDateFormat("yyyy.MM.dd").format(date);

			for (Document company : companies) {
				String companyId = company.getObjectId("_id").toString();
				String subdomain = company.getString("COMPANY_SUBDOMAIN");

				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);

				List<Document> users = usersCollection
						.find(Filters.and(Filters.eq("DELETED", false), Filters.exists("LAST_SEEN")))
						.into(new ArrayList<Document>());

				boolean archiveCompany = true;
				boolean emailAdmins = true;
				Timestamp latestLastSeen = null;

				for (Document user : users) {
					Date parsedDate = user.getDate("LAST_SEEN");
					Timestamp lastSeen = new java.sql.Timestamp(parsedDate.getTime());

					if (latestLastSeen == null) {
						latestLastSeen = lastSeen;
					} else if (lastSeen.after(latestLastSeen)) {
						latestLastSeen = lastSeen;
					}
				}

				if (latestLastSeen == null) {
					companiesToArchive.put(companyId, subdomain);
				} else {
					Timestamp today = new Timestamp(new Date().getTime());

					Calendar calendar = Calendar.getInstance();
					calendar.setTime(today);
					calendar.add(Calendar.MONTH, -6);
					Timestamp sixMonthsAgo = new Timestamp(calendar.getTime().getTime());

					Calendar currentCalander = Calendar.getInstance();
					currentCalander.setTime(today);
					currentCalander.add(Calendar.MONTH, -5);
					Timestamp fiveMonthsAgo = new Timestamp(currentCalander.getTime().getTime());

					if (latestLastSeen.after(sixMonthsAgo)) {
						archiveCompany = false;
					}

					if (latestLastSeen.after(fiveMonthsAgo)) {
						emailAdmins = false;
					}
				}

				String formattedLatestLastSeen = null;
				if (latestLastSeen != null) {
					Date latestLastSeenDate = new Date(latestLastSeen.getTime());
					formattedLatestLastSeen = new SimpleDateFormat("yyyy.MM.dd").format(latestLastSeenDate);
				}

				if (archiveCompany) {
					companiesToArchive.put(companyId, subdomain);
				} else if (formattedLatestLastSeen != null && formattedLatestLastSeen.equals(formattedToday)
						&& emailAdmins) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(latestLastSeen);
					cal.add(Calendar.DAY_OF_WEEK, 180);

					Timestamp dateToBeArchived = new Timestamp(cal.getTime().getTime());

					MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
					Document systemAdminRole = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first();

					if (systemAdminRole != null) {

						String systemAdminRoleId = systemAdminRole.getObjectId("_id").toString();
						List<Document> adminUsers = usersCollection
								.find(Filters.and(Filters.eq("DELETED", false), Filters.eq("ROLE", systemAdminRoleId)))
								.into(new ArrayList<Document>());
						for (Document user : adminUsers) {

							if (user.containsKey("EMAIL_ADDRESS") && user.get("EMAIL_ADDRESS") != null) {
								String emailAddress = user.getString("EMAIL_ADDRESS");

								String name = user.getString("FIRST_NAME");

								if (user.containsKey("LAST_NAME") && user.get("LAST_NAME") != null) {
									name = name + " " + user.getString("LAST_NAME");
								}

								String subject = subdomain + ".ngdesk.com will be archived";
								String body = "Hi " + name + ",<br/><br/>" + "Your subdomain on ngdesk " + subdomain
										+ ".ngdesk.com will be archived on " + dateToBeArchived.toString()
										+ ", since there has been "
										+ "no user activity for more than 5 months. Please login within the next month if you wish to continue using ngdesk."
										+ "<br/>For any questions please mail support@ngdesk.com<br/><br/>Regards,<br/>ngDesk Support Team.";

								new SendEmail(emailAddress, "support@ngdesk.com", subject, body, host).sendEmail();

								new SendEmail("shashank@allbluesolutions.com", "support@ngdesk.com", subject, body,
										host).sendEmail();

								new SendEmail("spencer@allbluesolutions.com", "support@ngdesk.com", subject, body, host)
										.sendEmail();

							}
						}
					}
				}

			}

//			for (String companyId : companiesToArchive.keySet()) {
//				String subdomain = companiesToArchive.get(companyId);
//				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
//				List<Document> users = usersCollection.find().into(new ArrayList<Document>());
//				MongoCollection<Document> archivedUsersCollection = mongoTemplate.getCollection("archived_users");
//
//				for (Document user : users) {
//					user.remove("_id");
//					user.put("COMPANY_SUBDOMAIN", subdomain);
//				}
//				if (users.size() > 0) {
//					archivedUsersCollection.insertMany(users);
//				}
//				companyService.dropCompany(companyId, subdomain);
//			}

			log.trace("Enter CleanUpJob.executeJob()");
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
