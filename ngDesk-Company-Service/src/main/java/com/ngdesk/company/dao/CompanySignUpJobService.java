package com.ngdesk.company.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.mail.SendMail;
import com.ngdesk.company.role.dao.Role;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.RoleRepository;

@Component
public class CompanySignUpJobService {
	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	Global global;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	SendMail sendMail;

	public void sendEmail(String table, List<String> emailIds) {
		for (String emailAddress : emailIds) {
			String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			String from = "support@ngdesk.com";
			String subject = "List of companies " + formattedDate;
			String body = global.getFile("daily_company_signup.html");
			body = body.replace("TABLE_REPLACE", table);
			sendMail.send(emailAddress, from, subject, body);
		}
	}

	public String buildCompanyDetailsRow(Company company, Map<String, Object> user,
			Map<String, Object> firstAdminUserContact) {

		String firstName = firstAdminUserContact.get("FIRST_NAME").toString();
		String lastName = firstAdminUserContact.get("LAST_NAME").toString();
		ObjectMapper mapper = new ObjectMapper();
		Phone phone = null;
		try {
			phone = mapper.readValue(mapper.writeValueAsString(firstAdminUserContact.get("PHONE_NUMBER")), Phone.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		UsageType usageType = company.isUsageType();
		if (user != null) {
			// GET PHONE NUMBER FROM COMPANY
			String phoneNumber = phone.getDialCode() + phone.getPhoneNumber();

			List<String> usageTypeDetails = new ArrayList<String>();
			if (usageType != null) {
				if (usageType.isTickets()) {
					usageTypeDetails.add("Tickets");
				}
				if (usageType.isChat()) {
					usageTypeDetails.add("Chat");
				}
				if (usageType.isPager()) {
					usageTypeDetails.add("Pager");
				}
			}
			// GET DETAILS FROM COMPANY AND USER
			String detailsRow = "<tr><td>" + company.getCompanyName() + "</td><td>" + company.getCompanySubdomain()
					+ "</td><td>" + firstName + "</td><td>" + lastName + "</td><td>" + user.get("EMAIL_ADDRESS")
					+ "</td><td>" + phoneNumber + "</td><td>" + company.getTimezone() + "</td><td>"
					+ company.getIndustry() + "</td><td>" + String.join("/", usageTypeDetails) + "</td></tr>";
			return detailsRow;
		}
		return "";
	}

	public String getTotalDetails(List<Company> companyList) {
		String totalDetails = "<table border=1>";
		String header = "<tr><th>Company Name</th><th>Company Subdomain</th><th>First Name</th><th>Last Name</th>"
				+ "<th>Email Address</th><th>Phone Number</th><th>Timezone</th><th>Industry</th><th>Signed up for Tickets/Pager/Chat</th></tr>";
		totalDetails = totalDetails + header;
		for (Company company : companyList) {
			String companyId = company.getCompanyId().toString();
			List<Role> roles = roleRepository.findAllRolesByCollectionName("roles_" + companyId).get();
			Role systemAdminRole = roles.stream().filter(role -> role.getName().equalsIgnoreCase("SystemAdmin"))
					.findFirst().orElse(null);
			String systemAdminId = systemAdminRole.getId().toString();
			List<Map<String, Object>> users = moduleEntryRepository.getAllEntries("Users_" + companyId).get();
			// GET FIRST SYSTEM ADMIN
			Map<String, Object> firstAdminUser = users.stream()
					.filter(user -> user.get("ROLE").toString().equals(systemAdminId)).findFirst().orElse(null);
			String contactId = firstAdminUser.get("CONTACT").toString();
			List<Map<String, Object>> contact = moduleEntryRepository.getAllEntries("Contacts_" + companyId).get();
			Map<String, Object> contactModule = contact.stream()
					.filter(contacts -> contacts.get("_id").toString().equals(contactId)).findFirst().orElse(null);
			// ADDING INDIVIDUAL ROWS
			totalDetails = totalDetails + buildCompanyDetailsRow(company, firstAdminUser, contactModule);
		}
		return totalDetails;
	}

}
