package com.ngdesk.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.social.facebook.api.Page.PriceRange;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.email.SendEmail;

@Component
public class DailyCompanySignup {

	private final static Logger log = LoggerFactory.getLogger(DailyCompanySignup.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@Value("${email.host}")
	private String host;

	@Value("${env}")
	private String environment;

	// @Scheduled(fixedRate = 3600000)
	// 13:00 UTC = 9AM EST
	@Scheduled(cron = "0 0 13 * * *")
	public void signupCompanies() {

		try {
			log.trace("Enter DailyCompanySignup.signupCompanies()");

			List<String> emailIds = new ArrayList<String>();
			emailIds.add("rob@allbluesolutions.com");
			emailIds.add("spencer@allbluesolutions.com");
			emailIds.add("des@allbluesolutions.com");
			emailIds.add("sharath.satish@allbluesolutions.com");
			emailIds.add("kumar.mukesh@allbluesolutions.com");

			// RUN ONLY ON PROD

			if (!environment.equalsIgnoreCase("prd") && !environment.equalsIgnoreCase("devnew")) {

				return;
			}

			Date now = new Date();
			Calendar calendarStartDate = Calendar.getInstance();
			calendarStartDate.setTime(now);
			calendarStartDate.add(Calendar.HOUR, 13);
			Date startDate = calendarStartDate.getTime();

			Calendar calendarEndDate = Calendar.getInstance();
			calendarEndDate.setTime(now);
			calendarEndDate.add(Calendar.DATE, -1);
			calendarEndDate.add(Calendar.HOUR, 13);
			calendarEndDate.add(Calendar.MINUTE, 00);
			calendarEndDate.add(Calendar.SECOND, 01);
			Date endDate = calendarEndDate.getTime();

			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");

			List<Document> companyList = companiesCollection
					.find(Filters.and(Filters.gte("DATE_CREATED", endDate), Filters.lt("DATE_CREATED", startDate)))
					.into(new ArrayList<Document>());

			// SETTING HTML TABLE
			String totalDetails = "<table border=1>";
			String header = "<tr><th>Company Name</th><th>Company Subdomain</th><th>First Name</th><th>Last Name</th>"
					+ "<th>Email Address</th><th>Phone Number</th><th>Timezone</th><th>Industry</th><th>Pricing Tier</th></tr>";
			totalDetails = totalDetails + header;

			log.trace("Total number of companies" + companyList.size());
			for (Document company : companyList) {

				String companyId = company.getObjectId("_id").toString();

				MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
				Document role = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first();
				String systemAdminId = role.getObjectId("_id").toString();

				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
				// GET FIRST SYSTEM ADMIN
				Document firstAdminUser = usersCollection.find(Filters.eq("ROLE", systemAdminId)).first();
				String contactId = (String) firstAdminUser.get("CONTACT");

				MongoCollection<Document> contactCollection = mongoTemplate.getCollection("Contacts_" + companyId);

				Document contactModule = contactCollection.find(Filters.eq("_id", new ObjectId(contactId))).first();

				// ADDING INDIVIDUAL ROWS
				totalDetails = totalDetails + buildCompanyDetailsRow(company, firstAdminUser, contactModule);

			}
			totalDetails = totalDetails + "</table>";
			sendEmailTo(emailIds, totalDetails);

			log.trace("Exit DailyCompanySignup.signupCompanies()");

		} catch (Exception e) {
			e.printStackTrace();

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();

			if (environment.equals("prd")) {
				SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com", "support@ngdesk.com",
						"DailyCompanySignup: Stack Trace", sStackTrace, host);
				sendEmailToSpencer.sendEmail();

				SendEmail sendEmailToShashank = new SendEmail("shashank.shankaranand@allbluesolutions.com",
						"support@ngdesk.com", "DailyCompanySignup: Stack Trace", sStackTrace, host);
				sendEmailToShashank.sendEmail();

				SendEmail sendEmailToSharath = new SendEmail("sharath.satish@allbluesolutions.com",
						"support@ngdesk.com", "DailyCompanySignup: Stack Trace", sStackTrace, host);
				sendEmailToSharath.sendEmail();
			}
		}
		log.trace("Exit DailyCompanySignup.signupCompanies()");
	}

	public String buildCompanyDetailsRow(Document company, Document user, Document contactModule) {
		try {
			MongoCollection<Document> contactsCollection = mongoTemplate
					.getCollection("Contacts_" + company.getObjectId("_id").toString());
			Document firstAdminUser = contactsCollection.find(Filters.eq("USER", user.getObjectId("_id").toString()))
					.first();
			String firstName = (String) firstAdminUser.get("FIRST_NAME");
			String lastName = (String) firstAdminUser.get("LAST_NAME");
			if (user != null) {
				// GET PHONE NUMBER FROM COMPANY
				String phoneNumber = ((Document) contactModule.get("PHONE_NUMBER")).getString("DIAL_CODE")
						+ ((Document) contactModule.get("PHONE_NUMBER")).getString("PHONE_NUMBER");

				String pricingTire;
				if (company.getString("PRICING_TIER") == null) {
					pricingTire = "free";
				} else {
					pricingTire = company.getString("PRICING_TIER");
				}

				// GET DETAILS FROM COMPANY AND USER
				String detailsRow = "<tr><td>" + company.getString("COMPANY_NAME") + "</td><td>"
						+ company.getString("COMPANY_SUBDOMAIN") + "</td><td>" + firstName + "</td><td>" + lastName
						+ "</td><td>" + user.getString("EMAIL_ADDRESS") + "</td><td>" + phoneNumber + "</td><td>"
						+ company.getString("TIMEZONE") + "</td><td>" + company.getString("INDUSTRY") + "</td><td>"
						+ pricingTire + "</td></tr>";
				return detailsRow;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public void sendEmailTo(List<String> emails, String table) {
		try {

			for (String emailAddress : emails) {
				String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
				String from = "support@ngdesk.com";
				String subject = "List of companies " + formattedDate;

				String body = global.getFile("daily_company_signup.html");
				body = body.replace("TABLE_REPLACE", table);

				SendEmail sendemail = new SendEmail(emailAddress, from, subject, body, host);
				sendemail.sendEmail();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
