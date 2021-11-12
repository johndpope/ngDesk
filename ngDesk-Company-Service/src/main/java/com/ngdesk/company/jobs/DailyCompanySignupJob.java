package com.ngdesk.company.jobs;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.mail.SendMail;
import com.ngdesk.company.dao.Company;
import com.ngdesk.company.dao.CompanySignUpJobService;
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
	CompanySignUpJobService companySignUpservice;

	// @Scheduled(fixedRate = 600000)
	// 13:00 UTC = 9AM EST
	@Scheduled(cron = "0 0 13 * * *")
	public void signupCompanies() {

		List<String> emailIds = List.of("kumar.mukesh@subscribeit.com", "spencer@allbluesolutions.com",
				"madeleine.fontein@subscribeit.com", "sharath.satish@allbluesolutions.com");

		// RUN ONLY ON PROD
		if (!env.getProperty("env").equalsIgnoreCase("prd") && !env.getProperty("env").equalsIgnoreCase("devnew")) {
			return;
		}

		// (11hours(previous day)+13hours(current day)
		// current day
		Date now = new Date();
		Calendar calendarEndDate = Calendar.getInstance();
		calendarEndDate.setTime(now);
		calendarEndDate.add(Calendar.HOUR, 13);
		Date endDate = calendarEndDate.getTime();

		// previous day
		Calendar calendarStartDate = Calendar.getInstance();
		calendarStartDate.setTime(now);
		calendarStartDate.add(Calendar.DATE, -1);
		calendarStartDate.add(Calendar.HOUR, 13);
		calendarStartDate.add(Calendar.MINUTE, 0);
		calendarStartDate.add(Calendar.SECOND, 1);
		Date startDate = calendarStartDate.getTime();

		List<Company> companyList = companyRepository
				.findAllCompaniesWithStartAndEndDate("companies", startDate, endDate).get();

		// SETTING HTML TABLE
		String totalDetails = companySignUpservice.getTotalDetails(companyList);
		totalDetails = totalDetails + "</table>";
		companySignUpservice.sendEmail(totalDetails, emailIds);
	}
}
