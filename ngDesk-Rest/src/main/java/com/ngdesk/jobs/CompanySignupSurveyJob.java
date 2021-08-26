package com.ngdesk.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import com.ngdesk.Global;
import com.ngdesk.email.SendEmail;

@Component
public class CompanySignupSurveyJob {
	private final static Logger log = LoggerFactory.getLogger(CompanySignupSurveyJob.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	Global global;
	
	@Value("${email.host}")
	private String host;
	
	@Value("${env}")
	private String environment;
	
	@Scheduled(cron = "0 0 0 * * *")
	public void companySignUp() {

		try {
			log.trace("Enter CompanySignupSurveyJob.companySignUp()");
			Date now = new Date();
			DateFormat outputFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(now);
			calendar.add(Calendar.DATE, -1);
			calendar.add(Calendar.HOUR, 0);
			Date previousDate = calendar.getTime();
			String output = outputFormatter.format(previousDate);

			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTime(now);
			calendar1.add(Calendar.DATE, -1);
			calendar1.add(Calendar.HOUR, 23);
			calendar1.add(Calendar.MINUTE, 59);
			calendar1.add(Calendar.SECOND, 59);
			Date previousDate1 = calendar1.getTime();
			String output1 = outputFormatter.format(previousDate1);
			
			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			List<Document> companyList = companiesCollection
					.find(Filters.and(Filters.gte("DATE_CREATED", output), Filters.lt("DATE_CREATED", output1)))
					.into(new ArrayList<Document>());

			for (Document company : companyList) {

				String companyId = company.getObjectId("_id").toString();
				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
				List<Document> users = usersCollection.find(Filters.eq("DELETED", false))
						.into(new ArrayList<Document>());

				MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
				Document role = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first();
				String roleId = role.getObjectId("_id").toString();

				for (Document user : users) {

					String userUUID = user.getString("USER_UUID").toString();
					String userRole = user.getString("ROLE");

					if (userRole.equals(roleId)) {
						// Send Survey Email
						global.sendSurveyEmail(user.getString("EMAIL_ADDRESS"), company.getString("COMPANY_SUBDOMAIN"),
								user.getString("FIRST_NAME"), user.getString("LAST_NAME"), userUUID);
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
				SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com",
						"support@ngdesk.com", "Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToSpencer.sendEmail();

				SendEmail sendEmailToShashank = new SendEmail(
						"shashank.shankaranand@allbluesolutions.com", "support@ngdesk.com",
						"Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToShashank.sendEmail();
			}
		}
		log.trace("Exit CompanySignupSurveyJob.companySignUp()");
	}

}
