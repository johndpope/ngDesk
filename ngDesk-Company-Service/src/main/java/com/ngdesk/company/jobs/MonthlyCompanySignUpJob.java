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
public class MonthlyCompanySignUpJob {
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

	//@Scheduled(fixedRate = 600000)
	// will run at starting of every month
	 @Scheduled(cron = "0 0 0 1 1/1 *")
	public void signupCompanies() {

		List<String> emailIds = List.of("kumar.mukesh@subscribeit.com", "spencer@allbluesolutions.com",
				"madeleine.fontein@subscribeit.com", "sharath.satish@allbluesolutions.com");

		// RUN ONLY ON PROD
		if (!env.getProperty("env").equalsIgnoreCase("prd") && !env.getProperty("env").equalsIgnoreCase("devnew")) {
			return;
		}

		// staring from 1 day of Month(00:00:00) to last day of month(23:59:59)
		// end of month
		Calendar calendarEndDate = Calendar.getInstance();
		calendarEndDate.setTime(new Date());
		calendarEndDate.add(Calendar.MONTH, -1);
		calendarEndDate.set(Calendar.DAY_OF_MONTH, calendarEndDate.getActualMaximum(Calendar.DAY_OF_MONTH));
		calendarEndDate.set(Calendar.HOUR_OF_DAY, 23);
		calendarEndDate.set(Calendar.MINUTE, 59);
		calendarEndDate.set(Calendar.SECOND, 59);
		Date endDate = calendarEndDate.getTime();

		// start of month
		Calendar calendarStartDate = Calendar.getInstance();
		calendarStartDate.setTime(new Date());
		calendarStartDate.add(Calendar.MONTH, -1);
		calendarStartDate.set(Calendar.DAY_OF_MONTH, calendarStartDate.getActualMinimum(Calendar.DAY_OF_MONTH));
		calendarStartDate.set(Calendar.HOUR_OF_DAY, 00);
		calendarStartDate.set(Calendar.MINUTE, 00);
		calendarStartDate.set(Calendar.SECOND, 00);
		Date startDate = calendarStartDate.getTime();

		List<Company> companyList = companyRepository
				.findAllCompaniesWithStartAndEndDate("companies", startDate, endDate).get();

		// SETTING HTML TABLE
		String totalDetails = companySignUpservice.getTotalDetails(companyList);
		totalDetails = totalDetails + "</table>";
		companySignUpservice.sendEmail(totalDetails, emailIds);
	}
}
