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
	CompanySignUpservice companySignUpservice;

	private final Logger log = LoggerFactory.getLogger(MonthlyCompanySignUpJob.class);

	// @Scheduled(fixedRate = 600000)

	// will run at starting of every month
	@Scheduled(cron = "0 0 0 1 1/1 *")
	public void signupCompanies() {
		List<String> emailIds = List.of("kumar.mukesh@subscribeit.com", "spencer@allbluesolutions.com",
				"madeleine.fontein@subscribeit.com", "sharath.satish@allbluesolutions.com");

		log.trace("Enter DailyCompanySignup.signupCompanies()");
		try {

			// RUN ONLY ON PROD

			String environment = env.getProperty("env");
			System.out.println("environment===" + environment);
			if (!environment.equalsIgnoreCase("prd") && !environment.equalsIgnoreCase("devnew")) {

				return;
			}

			// end of month
			Calendar calendarStartDate = Calendar.getInstance();
			calendarStartDate.setTime(new Date());
			calendarStartDate.add(Calendar.MONTH, -1);
			calendarStartDate.set(Calendar.DAY_OF_MONTH, calendarStartDate.getActualMaximum(Calendar.DAY_OF_MONTH));
			calendarStartDate.set(Calendar.HOUR_OF_DAY, 23);
			calendarStartDate.set(Calendar.MINUTE, 59);
			calendarStartDate.set(Calendar.SECOND, 59);
			Date startDate = calendarStartDate.getTime();

			// start of month
			Calendar calendarEndDate = Calendar.getInstance();
			calendarEndDate.setTime(new Date());
			calendarEndDate.add(Calendar.MONTH, -1);
			calendarEndDate.set(Calendar.DAY_OF_MONTH, calendarEndDate.getActualMinimum(Calendar.DAY_OF_MONTH));
			calendarEndDate.set(Calendar.HOUR_OF_DAY, 00);
			calendarEndDate.set(Calendar.MINUTE, 00);
			calendarEndDate.set(Calendar.SECOND, 00);
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

			if (env.getProperty("env").equals("prd")) {

				companySignUpservice.sendErrorMessage(sStackTrace, emailIds);

			}
		}
		log.trace("Exit DailyCompanySignup.signupCompanies()");
	}

}
