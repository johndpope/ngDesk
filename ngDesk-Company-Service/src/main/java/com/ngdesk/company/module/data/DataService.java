package com.ngdesk.company.module.data;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.company.dao.Company;
import com.ngdesk.company.dao.Phone;
import com.ngdesk.company.knowledgebase.dao.ArticleService;
import com.ngdesk.repositories.ModuleEntryRepository;

@Component
public class DataService {

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	ArticleService articleService;

	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	Global global;

	public String postDefaultModuleEntries(Company company, Map<String, String> rolesMap, ObjectId globalTeamId,
			ObjectId adminTeamId, ObjectId salesTeamId, ObjectId agentTeamId, ObjectId customersTeamId,
			ObjectId spenderTeamId, ObjectId accountingManagerTeamId, ObjectId accountantTeamId) {

		String companyId = company.getCompanyId();
		String emailAddress = company.getEmailAddress();
		String firstName = company.getFirstName();
		String lastName = company.getLastName();
		Phone phone = company.getPhone();

		String password = company.getPassword();
		String email = emailAddress.toLowerCase().replaceAll("@", "*") + "*" + company.getCompanySubdomain();
		String ha1Password = email + ":" + company.getCompanySubdomain() + ".ngdesk.com:" + password;

		String hashedPassword = passwordHash(ha1Password);

		String accountName = emailAddress.split("@")[1];
		String nameReplace = firstName + " " + lastName;
		String discussionMessageReplace = " Hi " + firstName
				+ ",<br><br>This is your first ticket. Any customer request sent to your email will become a Support ticket, just like this one. Respond to this ticket by typing a message above and clicking Save. You can also see how an email becomes a ticket by emailing your new account, <a href='mailto:support@"
				+ company.getCompanySubdomain() + ".ngdesk.com'>support@" + company.getCompanySubdomain()
				+ ".ngdesk.com.</a>You can view all your tickets by clicking Tickets on the sidebar.";

		ObjectId personalTeamId = new ObjectId();
		ObjectId ghostTeamId = new ObjectId();
		ObjectId publicTeamId = new ObjectId();

		ObjectId contactId = new ObjectId();

		String newAccountId = postNewAccount(accountName, globalTeamId, companyId);
		String defaultAccountId = postDefaultAccount(globalTeamId, companyId);

		String userId = postNewUser(emailAddress, globalTeamId.toString(), personalTeamId.toString(),
				adminTeamId.toString(), rolesMap.get("SystemAdmin"), companyId, hashedPassword, contactId.toString());

		postContact(userId, phone, contactId, companyId, firstName, lastName, newAccountId, globalTeamId.toString());

		ObjectId ghostContactId = new ObjectId();
		String ghostUserId = postGhostUser(companyId, ghostTeamId.toString(), rolesMap.get("Customers"),
				ghostContactId.toString());
		postContact(ghostUserId, new Phone(), ghostContactId, companyId, "Ghost", "User", defaultAccountId,
				ghostTeamId.toString());

		ObjectId systemContactId = new ObjectId();
		String systemUserId = postSystemUser(companyId, customersTeamId.toString(), rolesMap.get("Customers"),
				systemContactId.toString());
		postContact(systemUserId, new Phone(), systemContactId, companyId, "System", "User", defaultAccountId,
				customersTeamId.toString());

		postDefaultTeams(companyId, systemUserId, ghostUserId, userId, globalTeamId, customersTeamId, agentTeamId,
				adminTeamId, ghostTeamId, personalTeamId, nameReplace, publicTeamId, salesTeamId);

		if (company.getPlugins().contains("Expenses")) {
			postExpensesTeams(companyId, userId, spenderTeamId, accountingManagerTeamId, accountantTeamId);
		}

		postDefaultTicket(systemUserId, rolesMap.get("Customers"), discussionMessageReplace, globalTeamId.toString(),
				companyId, systemContactId.toString());

		postKnowledgeBaseDefaults(systemUserId, globalTeamId.toString(), companyId, publicTeamId.toString(),
				adminTeamId.toString(), agentTeamId.toString());

		return globalTeamId.toString();
	}

	public String postNewAccount(String accountName, ObjectId globalTeamId, String companyId) {
		try {
			String newAccountJson = "{\"ACCOUNT_NAME\":\"ACCOUNT_NAME_REPLACE\",\"TEAMS\":[\"GLOBAL_TEAM_REPLACE\"],\"DELETED\":false}";
			newAccountJson = newAccountJson.replaceAll("ACCOUNT_NAME_REPLACE", accountName);
			newAccountJson = newAccountJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId.toString());

			Map<String, Object> newAccount = mapper.readValue(newAccountJson, Map.class);

			entryRepository.save(newAccount, "Accounts_" + companyId);

			return newAccount.get("_id").toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new BadRequestException("POST_NEW_ACCOUNT_FAILED", null);
	}

	public String postDefaultAccount(ObjectId globalTeamId, String companyId) {
		try {
			String accountJson = "{\"ACCOUNT_NAME\":\"ngdesk.com\",\"TEAMS\":[\"GLOBAL_TEAM_REPLACE\"],\"DELETED\":false}";
			accountJson = accountJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId.toString());

			Map<String, Object> newAccount = mapper.readValue(accountJson, Map.class);

			entryRepository.save(newAccount, "Accounts_" + companyId);

			return newAccount.get("_id").toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new BadRequestException("POST_DEFAULT_ACCOUNT_FAILED", null);
	}

	public String postNewUser(String emailAddress, String globalTeamId, String personalTeamId, String adminTeamId,
			String adminRoleId, String companyId, String password, String contactId) {
		String userJson = "{\"LANGUAGE\":\"English\",\"NOTIFICATION_SOUND\":\"alarm_classic\",\"DELETED\":false,\"DISABLED\":false,\"ROLE\":\"SystemAdminRole\",\"EMAIL_VERIFIED\":false,\"EMAIL_ADDRESS\":\"EMAIL_ADDRESS_REPLACE\",\"PASSWORD\":\"\",\"DEFAULT_CONTACT_METHOD\":\"Email\",\"TEAMS\":[\"GLOBAL_TEAM_REPLACE\",\"SYSTEM_ADMIN_TEAM_REPLACE\",\"PERSONAL_TEAM_REPLACE\"],\"USER_UUID\":\"8f3ce655-95f8-4ec8-b55b-0e05b60ec449\",\"LOGIN_ATTEMPTS\":0,\"INVITE_ACCEPTED\":true}";

		try {
			userJson = userJson.replaceAll("SystemAdminRole", adminRoleId);
			userJson = userJson.replaceAll("EMAIL_ADDRESS_REPLACE", emailAddress);
			userJson = userJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
			userJson = userJson.replaceAll("SYSTEM_ADMIN_TEAM_REPLACE", adminTeamId);
			userJson = userJson.replaceAll("PERSONAL_TEAM_REPLACE", personalTeamId);

			Map<String, Object> entry = mapper.readValue(userJson, Map.class);
			entry.put("PASSWORD", password);
			entry.put("CONTACT", contactId);
			entryRepository.save(entry, "Users_" + companyId);

			return entry.get("_id").toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new BadRequestException("POST_NEW_USER_FAILED", null);
	}

	public void postContact(String userId, Phone phone, ObjectId contactId, String companyId, String firstName,
			String lastName, String accountId, String globalTeamId) {
		try {
			Map<String, Object> phoneEntry = mapper.readValue(mapper.writeValueAsString(phone), Map.class);

			Map<String, Object> entry = new HashMap<String, Object>();

			String fullName = firstName;
			if (lastName != null && !lastName.isBlank()) {
				fullName += " " + lastName;
			}

			entry.put("FULL_NAME", fullName);
			entry.put("ACCOUNT", accountId);
			entry.put("FIRST_NAME", firstName);
			entry.put("LAST_NAME", lastName);
			entry.put("DELETED", false);
			entry.put("TEAMS", Arrays.asList(globalTeamId));
			entry.put("SUBSCRIPTION_ON_MARKETING_EMAIL", true);
			entry.put("DATE_CREATED", new Date());
			entry.put("DATE_UPDATED", new Date());
			entry.put("USER", userId);
			entry.put("PHONE_NUMBER", phoneEntry);
			entry.put("_id", contactId);

			entryRepository.save(entry, "Contacts_" + companyId);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String postGhostUser(String companyId, String ghostTeamId, String customersRoleId, String contactId) {
		try {
			String userJson = "{\"LANGUAGE\":\"English\",\"NOTIFICATION_SOUND\":\"alarm_classic\",\"DELETED\":false,\"DISABLED\":true,\"ROLE\":\"CustomersRole\",\"EMAIL_VERIFIED\":false,\"EMAIL_ADDRESS\":\"ghost@ngdesk.com\",\"PASSWORD\":\"\",\"DEFAULT_CONTACT_METHOD\":\"Email\",\"TEAMS\":[\"TEAM_ID_REPLACE\"],\"USER_UUID\":\"5ecdc88d-ba93-4772-9733-5217cc5b3ab7\",\"LOGIN_ATTEMPTS\":0,\"INVITE_ACCEPTED\":true}";
			userJson = userJson.replaceAll("CustomersRole", customersRoleId);
			userJson = userJson.replaceAll("TEAM_ID_REPLACE", ghostTeamId);
			Map<String, Object> entry = mapper.readValue(userJson, Map.class);
			entry.put("CONTACT", contactId);
			entryRepository.save(entry, "Users_" + companyId);
			return entry.get("_id").toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new BadRequestException("POST_DEFAULT_USERS_FAILED", null);
	}

	public String postSystemUser(String companyId, String customerTeamId, String customersRoleId, String contactId) {
		try {
			String systemUserJson = "{\"LANGUAGE\":\"English\",\"NOTIFICATION_SOUND\":\"alarm_classic\",\"DELETED\":false,\"DISABLED\":false,\"ROLE\":\"CustomersRole\",\"EMAIL_VERIFIED\":false,\"EMAIL_ADDRESS\":\"system@ngdesk.com\",\"PASSWORD\":\"\",\"DEFAULT_CONTACT_METHOD\":\"Email\",\"TEAMS\":[\"TEAM_ID_REPLACE\"],\"USER_UUID\":\"3af20730-5da4-4244-b9e4-a6767ff875c7\",\"LOGIN_ATTEMPTS\":0,\"INVITE_ACCEPTED\":true}";
			systemUserJson = systemUserJson.replaceAll("CustomersRole", customersRoleId);
			systemUserJson = systemUserJson.replaceAll("TEAM_ID_REPLACE", customerTeamId);
			Map<String, Object> systemEntry = mapper.readValue(systemUserJson, Map.class);
			systemEntry.put("CONTACT", contactId);
			entryRepository.save(systemEntry, "Users_" + companyId);
			return systemEntry.get("_id").toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new BadRequestException("POST_DEFAULT_USERS_FAILED", null);
	}

	public void postDefaultTeams(String companyId, String systemUserId, String ghostUserId, String userId,
			ObjectId globalTeamId, ObjectId customerTeamId, ObjectId agentTeamId, ObjectId adminTeamId,
			ObjectId ghostTeamId, ObjectId personalTeamId, String name, ObjectId publicTeamId, ObjectId salesTeamId) {
		try {
			String globalTeamJson = "{\"DESCRIPTION\":\"Default Team\",\"USERS\":[\"SYSTEM_USER_ID_REPLACE\",\"GHOST_USER_ID_REPLACE\",\"NEW_USER_ID_REPLACE\"],\"DELETED\":false,\"IS_PERSONAL\":false,\"NAME\":\"Global\"}";
			globalTeamJson = globalTeamJson.replaceAll("SYSTEM_USER_ID_REPLACE", systemUserId);
			globalTeamJson = globalTeamJson.replaceAll("GHOST_USER_ID_REPLACE", ghostUserId);
			globalTeamJson = globalTeamJson.replaceAll("NEW_USER_ID_REPLACE", userId);

			Map<String, Object> globalTeam = mapper.readValue(globalTeamJson, Map.class);
			globalTeam.put("_id", globalTeamId);
			entryRepository.save(globalTeam, "Teams_" + companyId);

			String customerTeamJson = "{\"DESCRIPTION\":\"Default team for Customers\",\"USERS\":[\"SYSTEM_USER_ID_REPLACE\"],\"DELETED\":false,\"IS_PERSONAL\":false,\"NAME\":\"Customers\"}";
			customerTeamJson = customerTeamJson.replaceAll("SYSTEM_USER_ID_REPLACE", systemUserId);

			Map<String, Object> customerTeam = mapper.readValue(customerTeamJson, Map.class);
			customerTeam.put("_id", customerTeamId);
			entryRepository.save(customerTeam, "Teams_" + companyId);

			String agentTeamJson = "{\"DESCRIPTION\":\"Default team for agents\",\"USERS\":[],\"DELETED\":false,\"IS_PERSONAL\":false,\"NAME\":\"Agent\"}";
			Map<String, Object> agentTeam = mapper.readValue(agentTeamJson, Map.class);
			agentTeam.put("_id", agentTeamId);
			entryRepository.save(agentTeam, "Teams_" + companyId);

			String adminTeamJson = "{\"DESCRIPTION\":\"Default team for admins\",\"USERS\":[\"USER_ID_REPLACE\"],\"DELETED\":false,\"IS_PERSONAL\":false,\"NAME\":\"SystemAdmin\"}";
			adminTeamJson = adminTeamJson.replaceAll("USER_ID_REPLACE", userId);
			Map<String, Object> adminTeam = mapper.readValue(adminTeamJson, Map.class);
			adminTeam.put("_id", adminTeamId);
			entryRepository.save(adminTeam, "Teams_" + companyId);

			String ghostTeamJson = "{\"DESCRIPTION\":\"I take over the teams that have been deleted\",\"USERS\":[\"GHOST_USER_ID_REPLACE\"],\"DELETED\":false,\"IS_PERSONAL\":false,\"NAME\":\"Ghost Team\"}";
			ghostTeamJson = ghostTeamJson.replaceAll("GHOST_USER_ID_REPLACE", ghostUserId);
			Map<String, Object> ghostTeam = mapper.readValue(ghostTeamJson, Map.class);
			ghostTeam.put("_id", ghostTeamId);
			entryRepository.save(ghostTeam, "Teams_" + companyId);

			String personalTeamJson = "{\"DESCRIPTION\":\"Personal team for NAME_REPLACE\",\"USERS\":[\"USER_ID_REPLACE\"],\"DELETED\":false,\"IS_PERSONAL\":true,\"NAME\":\"NAME_REPLACE\"}";
			personalTeamJson = personalTeamJson.replaceAll("NAME_REPLACE", name);
			personalTeamJson = personalTeamJson.replaceAll("USER_ID_REPLACE", userId);
			Map<String, Object> personalTeam = mapper.readValue(personalTeamJson, Map.class);
			personalTeam.put("_id", personalTeamId);
			entryRepository.save(personalTeam, "Teams_" + companyId);

			String publicTeamJson = "{\"DESCRIPTION\":\"Team for all the public\",\"USERS\":[],\"DELETED\":false,\"IS_PERSONAL\":false,\"NAME\":\"Public\"}";
			Map<String, Object> publicTeam = mapper.readValue(publicTeamJson, Map.class);
			publicTeam.put("_id", publicTeamId);
			entryRepository.save(publicTeam, "Teams_" + companyId);

			String salesTeamJson = "{\"DESCRIPTION\":\"Default team for sales\",\"USERS\":[],\"DELETED\":false,\"IS_PERSONAL\":false,\"NAME\":\"Sales\"}";
			salesTeamJson = salesTeamJson.replaceAll("USER_ID_REPLACE", userId);
			Map<String, Object> salesTeam = mapper.readValue(salesTeamJson, Map.class);
			salesTeam.put("_id", salesTeamId);
			entryRepository.save(salesTeam, "Teams_" + companyId);

		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("POST_DEFAULT_TEAMS_FAILED", null);
		}
	}

	public void postExpensesTeams(String companyId, String userId, Object spenderTeamId, Object accountantTeamId,
			Object accountingManagerTeamId) {
		try {
			String spenderTeamJson = "{\"DESCRIPTION\":\"Default team for Spender\",\"USERS\":[],\"DELETED\":false,\"IS_PERSONAL\":false,\"NAME\":\"Spender\"}";
			spenderTeamJson = spenderTeamJson.replaceAll("USER_ID_REPLACE", userId);
			Map<String, Object> spenderTeam = mapper.readValue(spenderTeamJson, Map.class);
			spenderTeam.put("_id", spenderTeamId);
			entryRepository.save(spenderTeam, "Teams_" + companyId);

			String accountantTeamJson = "{\"DESCRIPTION\":\"Default team for Accountant\",\"USERS\":[],\"DELETED\":false,\"IS_PERSONAL\":false,\"NAME\":\"Accountant\"}";
			accountantTeamJson = accountantTeamJson.replaceAll("USER_ID_REPLACE", userId);
			Map<String, Object> accountantTeam = mapper.readValue(accountantTeamJson, Map.class);
			accountantTeam.put("_id", accountantTeamId);
			entryRepository.save(accountantTeam, "Teams_" + companyId);

			String accountingManagerTeamJson = "{\"DESCRIPTION\":\"Default team for Accounting Manager\",\"USERS\":[],\"DELETED\":false,\"IS_PERSONAL\":false,\"NAME\":\"AccountingManager\"}";
			accountingManagerTeamJson = accountingManagerTeamJson.replaceAll("USER_ID_REPLACE", userId);
			Map<String, Object> accountingManagerTeam = mapper.readValue(accountingManagerTeamJson, Map.class);
			accountingManagerTeam.put("_id", accountingManagerTeamId);
			entryRepository.save(accountingManagerTeam, "Teams_" + companyId);

		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("POST_EXPENSES_TEAMS_FAILED", null);
		}
	}

	public void postDefaultTicket(String systemUserId, String customersRoleId, String discussionMessage,
			String globalTeamId, String companyId, String systemContactId) {

		try {
			String ticketJson = "{\"CREATED_BY\":\"SYSTEM_USER_ID_REPLACE\",\"LAST_UPDATED_BY\":\"SYSTEM_USER_ID_REPLACE\",\"STATUS\":\"New\",\"SOURCE_TYPE\":\"web\",\"TICKET_ID\":1,\"REQUESTOR\":\"SYSTEM_CONTACT_ID_REPLACE\",\"MESSAGES\":[{\"MESSAGE\":\"MESSAGE_REPLACE\",\"SENDER\":{\"ROLE\":\"CustomersRole\",\"USER_UUID\":\"3af20730-5da4-4244-b9e4-a6767ff875c7\",\"LAST_NAME\":\"User\",\"FIRST_NAME\":\"Sytem\"},\"ATTACHMENTS\":[],\"MESSAGE_ID\":\"1741f731-6efd-40a6-9e91-cc79deb9a2c5\",\"MESSAGE_TYPE\":\"MESSAGE\"}],\"TEAMS\":[\"GLOBAL_TEAM_ID_REPLACE\"],\"SUBJECT\":\"Sample Ticket: Meet the ticket\",\"DELETED\":false,\"DATE_CREATED\":\"DATE_CREATED_REPLACE\",\"DATE_UPDATED\":\"DATE_UPDATED_REPLACE\"}";

			ticketJson = ticketJson.replaceAll("SYSTEM_USER_ID_REPLACE", systemUserId);
			ticketJson = ticketJson.replaceAll("SYSTEM_CONTACT_ID_REPLACE", systemContactId);
			ticketJson = ticketJson.replaceAll("CustomersRole", customersRoleId);
			ticketJson = ticketJson.replaceAll("MESSAGE_REPLACE", discussionMessage);
			ticketJson = ticketJson.replaceAll("GLOBAL_TEAM_ID_REPLACE", globalTeamId);

			Map<String, Object> ticket = mapper.readValue(ticketJson, Map.class);
			ticket.put("DATE_CREATED", new Date());
			ticket.put("DATE_UPDATED", new Date());
			entryRepository.save(ticket, "Tickets_" + companyId);

		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("POST_DEFAULT_TICKETS_FAILED", null);
		}
	}

	// TODO: Use class
	public void postKnowledgeBaseDefaults(String systemUserId, String globalTeamId, String companyId,
			String publicTeamId, String adminTeamId, String agentTeamId) {
		try {
			String categoriesJson = global.getFile("DefaultCategories.json");
			categoriesJson = categoriesJson.replaceAll("GLOBAL_TEAM_ID_REPLACE", globalTeamId);
			categoriesJson = categoriesJson.replaceAll("PUBLIC_TEAM_ID_REPLACE", publicTeamId);
			categoriesJson = categoriesJson.replaceAll("USER_ID_REPLACE", systemUserId);
			Map<String, Object> category = mapper.readValue(categoriesJson, Map.class);
			category.put("DATE_CREATED", new Date());
			category.put("DATE_UPDATED", new Date());
			entryRepository.save(category, "categories_" + companyId);

			String categoryId = category.get("_id").toString();

			String sectionsJson = global.getFile("DefaultSections.json");
			sectionsJson = sectionsJson.replaceAll("USER_ID_REPLACE", systemUserId);
			sectionsJson = sectionsJson.replaceAll("GLOBAL_TEAM_ID_REPLACE", globalTeamId);
			sectionsJson = sectionsJson.replaceAll("PUBLIC_TEAM_ID_REPLACE", publicTeamId);
			sectionsJson = sectionsJson.replaceAll("ADMIN_TEAM", adminTeamId);
			sectionsJson = sectionsJson.replaceAll("AGENT_TEAM", agentTeamId);
			sectionsJson = sectionsJson.replaceAll("CATEOGRY_ID_REPLACE", categoryId);

			String articlesJson = global.getFile("DefaultArticles.json");
			articlesJson = articlesJson.replaceAll("USER_ID_REPLACE", systemUserId);
			articlesJson = articlesJson.replaceAll("GLOBAL_TEAM_ID_REPLACE", globalTeamId);
			articlesJson = articlesJson.replaceAll("PUBLIC_TEAM_ID_REPLACE", publicTeamId);

			Map<String, Object> sectionsObj = mapper.readValue(sectionsJson, Map.class);
			List<Map<String, Object>> sections = (List<Map<String, Object>>) sectionsObj.get("SECTIONS");

			String announcementSectionId = null;
			String faqSectionId = null;

			for (Map<String, Object> section : sections) {
				section.put("DATE_CREATED", new Date());
				section.put("DATE_UPDATED", new Date());
				entryRepository.save(section, "sections_" + companyId);

				if (section.get("NAME").equals("Announcements")) {
					announcementSectionId = section.get("_id").toString();
				} else {
					faqSectionId = section.get("_id").toString();
				}
			}

			articlesJson = articlesJson.replaceAll("Announcements_REPLACE", announcementSectionId);
			articlesJson = articlesJson.replaceAll("FAQ_REPLACE", faqSectionId);

			Map<String, Object> articlesObj = mapper.readValue(articlesJson, Map.class);
			List<Map<String, Object>> articles = (List<Map<String, Object>>) articlesObj.get("ARTICLES");

			for (Map<String, Object> article : articles) {
				article.put("DATE_CREATED", new Date());
				article.put("DATE_UPDATED", new Date());
				entryRepository.save(article, "articles_" + companyId);
			}
			articleService.insertArticlesToElastic(articles, companyId);

		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("POST_KNOWLEDGEBASE_FAILED", null);
		}
	}

	private String passwordHash(String pwd) {

		String hashedPassword = "";

		if (pwd.equals("")) {
			return "";
		}

		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(pwd.getBytes());
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			hashedPassword = bigInt.toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		return hashedPassword;
	}

}
