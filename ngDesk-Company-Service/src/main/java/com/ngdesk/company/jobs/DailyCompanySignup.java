package com.ngdesk.company.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.Global;
import com.ngdesk.commons.mail.SendMail;
import com.ngdesk.company.dao.Company;
import com.ngdesk.company.role.dao.Role;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.RoleRepository;

@Component
public class DailyCompanySignup {

	private final Logger log = LoggerFactory.getLogger(DailyCompanySignup.class);

	@Autowired
	Global global;

	@Autowired
	SendMail sendMail;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	private Environment env;

	@Scheduled(fixedRate = 6000)
	// 13:00 UTC = 9AM EST
	// @Scheduled(cron = "0 0 13 * * *")

	public void signupCompanies() {
		System.out.println("entering********");
		log.trace("Enter DailyCompanySignup.signupCompanies()");
		try {
			List<String> emailIds = new ArrayList<String>();
			emailIds.add("kumar.mukesh@subscribeit.com");
			emailIds.add("spencer@allbluesolutions.com");
			emailIds.add("madeleine.fontein@subscribeit.com");
			emailIds.add("sharath.satish@allbluesolutions.com");

			// RUN ONLY ON PROD

			String environment = env.getProperty("env");
			System.out.println("environment===" + environment);
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

			List<Company> companyList = companyRepository
					.findAllCompaniesWithStartAndEndDate("companies", startDate, endDate).get();

			// SETTING HTML TABLE
			String totalDetails = "<table border=1>";

			String header = "<tr><th>Company Name</th><th>Company Subdomain</th><th>First Name</th><th>Last Name</th>"
					+ "<th>Email Address</th><th>Phone Number</th><th>Timezone</th><th>Industry</th><th>Pricing Tier</th></tr>";

			totalDetails = totalDetails + header;

			log.trace("Total number of companies" + companyList.size());

			for (Company company : companyList) {
				System.out.println("company===" + company);
				String companyId = company.getCompanyId().toString();

				List<Role> rolesCollection = roleRepository.findAllRolesByCollectionName("roles_" + companyId).get();
				Role role = rolesCollection.stream().filter(roles -> roles.getName().equalsIgnoreCase("SystemAdmin"))
						.findFirst().orElse(null);
				String systemAdminId = role.getId().toString();

				List<Map<String, Object>> usersCollection = moduleEntryRepository.getAllEntries("Users_" + companyId)
						.get();

				// GET FIRST SYSTEM ADMIN
				Map<String, Object> firstAdminUser = usersCollection.stream()
						.filter(users -> users.get("ROLE").toString().equalsIgnoreCase("SystemAdmin")).findFirst()
						.orElse(null);
				String contactId = firstAdminUser.get("CONTACT").toString();

				List<Map<String, Object>> contactCollection = moduleEntryRepository
						.getAllEntries("Contacts_" + companyId).get();

				Map<String, Object> contactModule = contactCollection.stream()
						.filter(contacts -> contacts.get("_id").toString().equalsIgnoreCase(contactId)).findFirst()
						.orElse(null);

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

			if (env.getProperty("env").equals("prd")) {
				sendMail.send("spencer@allbluesolutions.com", "support@ngdesk.com", "DailyCompanySignup: Stack Trace",
						sStackTrace);
				sendMail.send("sharath.satish@allbluesolutions.com", "support@ngdesk.com",
						"DailyCompanySignup: Stack Trace", sStackTrace);
				sendMail.send("kumar.mukesh@subscribeit.com", "support@ngdesk.com", "DailyCompanySignup: Stack Trace",
						sStackTrace);
				sendMail.send("madeleine.fontein@subscribeit.com", "support@ngdesk.com",
						"DailyCompanySignup: Stack Trace", sStackTrace);
			}
		}
		log.trace("Exit DailyCompanySignup.signupCompanies()");
	}

	private String buildCompanyDetailsRow(Company company, Map<String, Object> user,
			Map<String, Object> contactModule) {
		try {
			Map<String, Object> firstAdminUser = moduleEntryRepository
					.findById(user.get("CONTACT").toString(), "Contacts_" + company.getCompanyId().toString()).get();

			String firstName = firstAdminUser.get("FIRST_NAME").toString();
			String lastName = firstAdminUser.get("LAST_NAME").toString();
			if (user != null) {
				// GET PHONE NUMBER FROM COMPANY
				String phoneNumber = ((Document) contactModule.get("PHONE_NUMBER")).getString("DIAL_CODE")
						+ ((Document) contactModule.get("PHONE_NUMBER")).getString("PHONE_NUMBER");

				String pricingTire;
				if (company.getPricing() == null) {
					pricingTire = "free";
				} else {
					pricingTire = company.getPricing();
				}

				// GET DETAILS FROM COMPANY AND USER
				String detailsRow = "<tr><td>" + company.getCompanyName() + "</td><td>" + company.getCompanySubdomain()
						+ "</td><td>" + firstName + "</td><td>" + lastName + "</td><td>" + user.get("EMAIL_ADDRESS")
						+ "</td><td>" + phoneNumber + "</td><td>" + company.getTimezone() + "</td><td>"
						+ company.getIndustry() + "</td><td>" + pricingTire + "</td></tr>";
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
				sendMail.send(emailAddress, from, subject, body);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
