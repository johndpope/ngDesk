package com.ngdesk.company.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.mail.SendMail;
import com.ngdesk.company.dao.Company;
import com.ngdesk.company.dao.CompanySignUpservice;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.ModuleEntryRepository;

@Component
public class DailyCompanySignupJob {

	@Autowired
	SendMail sendMail;

	@Autowired
	private Environment env;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	CompanySignUpservice companySignUpservice;

	private final Logger log = LoggerFactory.getLogger(DailyCompanySignupJob.class);

	// @Scheduled(fixedRate = 6000)
	// 13:00 UTC = 9AM EST
	@Scheduled(cron = "0 0 13 * * *")
	public void signupCompanies() {

		log.trace("Enter DailyCompanySignup.signupCompanies()");

		List<String> emailIds = List.of("kumar.mukesh@subscribeit.com", "spencer@allbluesolutions.com",
				"madeleine.fontein@subscribeit.com", "sharath.satish@allbluesolutions.com");

		try {

			// RUN ONLY ON PROD
			if (!env.getProperty("env").equalsIgnoreCase("prd") && !env.getProperty("env").equalsIgnoreCase("devnew")) {
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
			calendarEndDate.add(Calendar.MINUTE, 0);
			calendarEndDate.add(Calendar.SECOND, 1);
			Date endDate = calendarEndDate.getTime();

			List<Company> companyList = companyRepository
					.findAllCompaniesWithStartAndEndDate("companies", startDate, endDate).get();

			// SETTING HTML TABLE
			String totalDetails = companySignUpservice.getTotalDetails(companyList);

			totalDetails = totalDetails + "</table>";
			companySignUpservice.sendEmail(totalDetails, emailIds);

			log.trace("Exit DailyCompanySignup.signupCompanies()");

		} catch (Exception e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();

			if (env.getProperty("env").equalsIgnoreCase("prd")) {
				companySignUpservice.sendErrorMessage(sStackTrace, emailIds);
			}
		}
		log.trace("Exit DailyCompanySignup.signupCompanies()");
	}

}
