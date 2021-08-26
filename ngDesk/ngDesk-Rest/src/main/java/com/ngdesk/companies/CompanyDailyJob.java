package com.ngdesk.companies;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.email.SendEmail;

@Component
public class CompanyDailyJob {

	private final static Logger log = LoggerFactory.getLogger(CompanyDailyJob.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	Global global;
	
	@Value("${email.host}")
	private String host;
	
	@Value("${env}")
	private String environment;

	//@Scheduled(cron = "0 0 0 * * *")
	public void executeJob() {
		log.trace("Enter CompanyDailyJob.executeJob()");
		try {
		MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
		List<Document> companyList = companiesCollection.find().into(new ArrayList<Document>());

		String defaultModules[]= {"Tickets","Accounts","Chat","Teams","Users"};
		String defaultRoles[]= {"SystemAdmin","Agent","Customers","Public"};
		String defaultTeams[]= {"Global","Ghost Team","SystemAdmin","Agent","Customers","Public"};
		
		String companiesMissingDefaultModulesMessage = " <span> No record found for missing modules!! </span>";
		String companiesMissingDefaultRolesMessage = " <span> No record found for missing roles!! </span>";
		String companiesMissingDefaultTeamsMessage = " <span> No record found for missing teams!! </span>";
		String companiesHavingDuplicateDefaultTeamsMessage = " <span> No record found for duplicate teams!! </span>";
		String companiesHavingSubdomainInUppercaseMessage = " <span> No record found for subdomain in uppercase!! </span>";
		String companiesHavingDuplicateSubdomainMessage = " <span> No record found for duplicate subdomain!! </span>";
		
		Set<String> companiesMissingDefaultModules = new HashSet<String>();
		Set<String> companiesMissingDefaultRoles = new HashSet<String>();
		Set<String> companiesMissingDefaultTeams = new HashSet<String>();
		Set<String> companiesHavingDuplicateDefaultTeams = new HashSet<String>();
		Set<String> companiesHavingSubdomainInUppercase = new HashSet<String>();
		Set<String> companiesHavingDuplicateSubdomain = new HashSet<String>();
		
		String htmlMailTemplate = global.getFile("dailyjob_email_message.html");

		for (Document companyDoc : companyList) {

			String companyId = companyDoc.getObjectId("_id").toString();
			Document company = companiesCollection.find(Filters.eq("_id", new ObjectId(companyId))).first();
			String companySubdomain = company.getString("COMPANY_SUBDOMAIN");
			
			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			
			//CHECK FOR DEFAULT MODULES
			for(String moduleName: defaultModules) {
				Document module = modulesCollection.find(Filters.eq("NAME",moduleName)).first();
				if(module == null) {
					companiesMissingDefaultModules.add(companySubdomain);
				}
			}
			
			//CHECK FOR DEFAULT ROLES
			for(String roleName: defaultRoles) {
				Document role = rolesCollection.find(Filters.eq("NAME", roleName)).first();
				if(role == null) {
					companiesMissingDefaultRoles.add(companySubdomain);
				}
			}
			
			for(String teamName: defaultTeams) {
				//CHECK FOR DEFAULT TEAMS
				Document team = teamsCollection.find(Filters.eq("NAME", teamName)).first();
				if(team == null) {
					companiesMissingDefaultTeams.add(companySubdomain);
				}
				//CHECK FOR DUPLICATE TEAMS
				List<Document> teams= teamsCollection.find(Filters.eq("NAME", teamName)).into(new ArrayList<Document>());
				if(teams.size() > 1) {
					companiesHavingDuplicateDefaultTeams.add(companySubdomain);
				}
			}
			
			// CHECK FOR SUBDOMAIN IN UPPERCASE 
			if (!companySubdomain.equals(companySubdomain.toLowerCase())) {
				companiesHavingSubdomainInUppercase.add(companySubdomain);
			}
			
			// CHECK FOR DUPLICATE SUBDOMAIN
			List<Document> companyListBySubdomain= companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", companySubdomain)).into(new ArrayList<Document>());
			if(companyListBySubdomain.size() > 1) {
				companiesHavingDuplicateSubdomain.add(companySubdomain);
			}
		}

		// IF SET IS EMPTY, UPDATING THE MESSAGES 
		
		if (!companiesMissingDefaultModules.isEmpty()) {
			companiesMissingDefaultModulesMessage = companiesMissingDefaultModules.toString();
		}
		if (!companiesMissingDefaultRoles.isEmpty()) {
			companiesMissingDefaultRolesMessage = companiesMissingDefaultRoles.toString();
		}
		if (!companiesMissingDefaultTeams.isEmpty()) {
			companiesMissingDefaultTeamsMessage = companiesMissingDefaultTeams.toString();
		}
		if (!companiesHavingDuplicateDefaultTeams.isEmpty()) {
			companiesHavingDuplicateDefaultTeamsMessage = companiesHavingDuplicateDefaultTeams.toString();
		}
		if (!companiesHavingSubdomainInUppercase.isEmpty()) {
			companiesHavingSubdomainInUppercaseMessage = companiesHavingSubdomainInUppercase.toString();
		}
		if (!companiesHavingDuplicateSubdomain.isEmpty()) {
			companiesHavingDuplicateSubdomainMessage = companiesHavingDuplicateSubdomain.toString();
		}
	
		// REPLACING HTML MESSAGE CONTENT
		
		htmlMailTemplate = htmlMailTemplate.replace("MISSING_DEFAULT_MODULES_REPLACE",
				companiesMissingDefaultModulesMessage);
		htmlMailTemplate = htmlMailTemplate.replace("MISSING_DEFAULT_ROLES_REPLACE",
				companiesMissingDefaultRolesMessage);
		htmlMailTemplate = htmlMailTemplate.replace("MISSING_DEFAULT_TEAMS_REPLACE",
				companiesMissingDefaultTeamsMessage);
		htmlMailTemplate = htmlMailTemplate.replace("DEFAULT_TEAMS_HAS_DUPLICATE_ENTRY_REPLACE",
				companiesHavingDuplicateDefaultTeamsMessage);
		htmlMailTemplate = htmlMailTemplate.replace("SUBDOMAIN_HAS_UPPERCASE_CHARACTERS_REPLACE",
				companiesHavingSubdomainInUppercaseMessage);
		htmlMailTemplate = htmlMailTemplate.replace("SUBDOMAIN_HAS_DUPLICATE_ENTRY_REPLACE",
				companiesHavingDuplicateSubdomainMessage);

		notifyAdmin(htmlMailTemplate);
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
	log.trace("Exit CompanyDailyJob.executeJob()");
	}
	public void notifyAdmin(String data) {
		log.trace("Enter CompanyDailyJob.notifyAdmin()");

		String emailBody = data;

		// SEND EMAIL TO SPENCER AND SHASHANK IF INVALID DATA PRESENT
		SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com", "support@ngdesk.com",
				"Company Data Validation Report", emailBody, host);
		sendEmailToSpencer.sendEmail();

		SendEmail sendEmailToShashank = new SendEmail("shashank.shankaranand@allbluesolutions.com",
				"support@ngdesk.com", "Company Data Validation Report", emailBody, host);
		sendEmailToShashank.sendEmail();

		log.trace("Exit CompanyDailyJob.notifyAdmin()");
	}
}
