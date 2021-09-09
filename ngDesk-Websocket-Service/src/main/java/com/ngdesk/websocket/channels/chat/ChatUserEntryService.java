package com.ngdesk.websocket.channels.chat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.data.elastic.ElasticMessage;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.repositories.RolesRepository;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.companies.dao.Phone;
import com.ngdesk.websocket.modules.dao.Module;
import com.ngdesk.websocket.roles.dao.Role;

@Component
public class ChatUserEntryService {

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	CompaniesRepository companiesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	public void ChatUserEntryCreation(ChatUser chatUserEntry) {
		try {
			Optional<Company> optionalCompany = companiesRepository
					.findCompanyBySubdomain(chatUserEntry.getSubDomain());
			if (optionalCompany.isPresent()) {
				Company company = optionalCompany.get();
				if (chatUserEntry.getEmailAddress() != null) {
					createOrGetUser(company, chatUserEntry);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public Map<String, Object> createOrGetUser(Company company, ChatUser message) {
		String companyId = company.getId();
		Map<String, Object> customer = new HashMap<String, Object>();

		Optional<Module> optionalTeamsModule = modulesRepository.findModuleByName("Teams", "modules_" + companyId);
		Module teamsModule = optionalTeamsModule.get();
		Optional<Map<String, Object>> optionalGlobalTeam = entryRepository.findTeamByName("Global",
				"Teams_" + companyId);

		Optional<Map<String, Object>> optionalCustomersTeam = entryRepository.findTeamByName("Customers",
				"Teams_" + companyId);
		customer = entryRepository
				.findUserByEmailAddressIncludingDeleted(message.getEmailAddress().toLowerCase(), "Users_" + companyId)
				.orElse(null);
		Optional<Role> optionalCustomerRole = rolesRepository.findRoleName("Customers", "roles_" + companyId);

		if (customer == null) {
			if (optionalGlobalTeam.isPresent()) {
				Map<String, Object> globalTeam = optionalGlobalTeam.get();
				Map<String, Object> account = getAccountPayload(message.getEmailAddress().toLowerCase(),
						globalTeam.get("_id").toString());
				String accountId = createOrGetAccountId(companyId, message.getEmailAddress().toLowerCase(), account);
				if (optionalCustomerRole.isPresent()) {
					Role customerRole = optionalCustomerRole.get();
					Map<String, Object> newCustomer = getUserPayload(message.getEmailAddress().toLowerCase(),
							globalTeam.get("_id").toString(), customerRole.getId());
					Optional<Module> optionalUsersModule = modulesRepository.findModuleByName("Users",
							"modules_" + companyId);
					if (optionalUsersModule.isPresent()) {
						Module usersModule = optionalUsersModule.get();
						customer = postData(companyId, usersModule.getModuleId(), "Users", newCustomer);
						String customerId = customer.get("_id").toString();

						Map<String, Object> newContact = getContactPayload(message.getFirstName(),
								message.getLastName(), company.getPhone(), accountId, globalTeam.get("_id").toString(),
								customerId);
						Optional<Module> optionalContactsModule = modulesRepository.findModuleByName("Contacts",
								"modules_" + companyId);
						if (optionalContactsModule.isPresent()) {
							Module contactsModule = optionalContactsModule.get();
							Map<String, Object> contact = postData(companyId, contactsModule.getModuleId(), "Contacts",
									newContact);

							customer.put("CONTACT", contact.get("_id").toString());

							Map<String, Object> personalTeam = new HashMap<String, Object>();

							personalTeam.put("DESCRIPTION",
									"Personal Team for " + message.getFirstName() + " " + message.getLastName());
							personalTeam.put("NAME", message.getFirstName() + " " + message.getLastName());
							personalTeam.put("IS_PERSONAL", true);

							List<String> users = new ArrayList<String>();
							users.add(customerId);
							personalTeam.put("USERS", users);
							personalTeam.put("DELETED", false);
							Map<String, Object> personalTeamMap = postData(companyId, teamsModule.getModuleId(),
									"Teams", personalTeam);

							List<String> globalTeamUsers = (List<String>) globalTeam.get("USERS");
							globalTeamUsers.add(customerId);
							globalTeam.put("USERS", globalTeamUsers);
							putData(companyId, teamsModule.getModuleId(), "Teams", globalTeam,
									globalTeam.get("_id").toString());

							if (optionalCustomersTeam.isPresent()) {
								Map<String, Object> customersTeam = optionalCustomersTeam.get();
								List<String> customerTeamUsers = (List<String>) customersTeam.get("USERS");
								customerTeamUsers.add(customerId);
								customersTeam.put("USERS", customerTeamUsers);
								putData(companyId, teamsModule.getModuleId(), "Teams", customersTeam,
										customersTeam.get("_id").toString());
							}

							Optional<Map<String, Object>> optionalCustomerTeam = entryRepository
									.findTeamByName("Customers", "Teams_" + companyId);
							Optional<Map<String, Object>> optionalUpdatedUserWithTeams = entryRepository
									.findById(customerId, "Users_" + companyId);
							Map<String, Object> updatedUserWithTeams = optionalUpdatedUserWithTeams.get();
							List<String> teams = (List<String>) updatedUserWithTeams.get("TEAMS");
							if (optionalCustomerTeam.isPresent()) {
								Map<String, Object> customerTeam = optionalCustomerTeam.get();
								String customerTeamId = customerTeam.get("_id").toString();
								teams.add(customerTeamId);
							}
							teams.add(personalTeam.get("_id").toString());
							updatedUserWithTeams.put("TEAMS", teams);
							updatedUserWithTeams.put("CONTACT", contact.get("_id").toString());
							putData(companyId, usersModule.getModuleId(), "Users", updatedUserWithTeams, customerId);
						}

					}
				}

			}

		} else {
			if (customer.containsKey("DELETED") && (boolean) customer.get("DELETED")) {
				if (company.getPhone() != null) {
					entryRepository.setUserPhoneNumberAndDeletedToFalse(message.getEmailAddress().toLowerCase(),
							company.getPhone(), "Users_" + companyId);
				} else {
					entryRepository.setUserDeletedToFalse(message.getEmailAddress(), "Users_" + companyId);
				}
			}

		}

		return customer;
	}

	public Map<String, Object> getAccountPayload(String emailAddress, String teamId) {
		Map<String, Object> account = new HashMap<String, Object>();

		List<String> teams = new ArrayList<String>();
		teams.add(teamId);

		String accountName = emailAddress.split("@")[1];
		account.put("ACCOUNT_NAME", accountName);
		account.put("DELETED", false);
		account.put("TEAMS", teams);
		account.put("DATE_CREATED", new Date());
		account.put("DATE_UPDATED", new Date());
		account.put("EFFECTIVE_FROM", new Date());

		return account;
	}

	public String createOrGetAccountId(String companyId, String emailAddress, Map<String, Object> account) {
		String accountId = null;
		// CHECK IF ACCOUNT EXISTS AND GET ACCOUNT IF EXISTS
		String accountName = emailAddress.split("@")[1];

		Optional<Module> optionalAccountModule = modulesRepository.findModuleByName("Accounts", "modules_" + companyId);
		if (optionalAccountModule.isPresent()) {
			Module accountModule = optionalAccountModule.get();
			Optional<Map<String, Object>> optionalExistingAccount = entryRepository.findAccountByName(accountName,
					"Accounts_" + companyId);

			// IF ACCOUNT DOCUMENT NULL, CREATE NEW ONE
			if (optionalExistingAccount.isEmpty()) {
				Map<String, Object> accountToReturn = postData(companyId, accountModule.getModuleId(), "Accounts",
						account);
				accountId = accountToReturn.get("_id").toString();
			} else {
				Map<String, Object> existingAccount = optionalExistingAccount.get();
				accountId = existingAccount.get("_id").toString();
			}
		}

		return accountId;

	}

	public void postDataIntoElastic(String moduleId, String companyId, Map<String, Object> payload) {
		try {
			ElasticMessage message = new ElasticMessage(moduleId, companyId, payload);
			rabbitTemplate.convertAndSend("elastic-updates", message);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Map<String, Object> postData(String companyId, String moduleId, String moduleName,
			Map<String, Object> entry) {

		String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
		entry.put("DATE_CREATED", new Date());
		entry.put("DATE_UPDATED", new Date());
		entry.put("EFFECTIVE_FROM", new Date());
		entry = entryRepository.save(entry, collectionName);
		postDataIntoElastic(moduleId, companyId, entry);
		return entry;

	}

	public Map<String, Object> getUserPayload(String emailAddress, String teamId, String role) {
		Map<String, Object> customer = new HashMap<String, Object>();

		List<String> teams = new ArrayList<String>();
		teams.add(teamId);

		customer.put("TEAMS", teams);
		customer.put("EMAIL_ADDRESS", emailAddress);
		customer.put("PASSWORD", "");

		customer.put("DATE_CREATED", new Date());
		customer.put("EFFECTIVE_FROM", new Date());
		customer.put("DATE_UPDATED", new Date());
		customer.put("DISABLED", false);
		customer.put("LANGUAGE", "English");
		customer.put("USER_UUID", UUID.randomUUID().toString());
		customer.put("ROLE", role);
		customer.put("LOGIN_ATTEMPTS", 0);
		customer.put("DELETED", false);
		customer.put("DEFAULT_CONTACT_METHOD", "Email");

		return customer;
	}

	public Map<String, Object> getContactPayload(String firstName, String lastName, Phone phone, String accountId,
			String teamId, String userId) {

		Map<String, Object> contact = new HashMap<String, Object>();

		List<String> teams = new ArrayList<String>();
		teams.add(teamId);

		contact.put("TEAMS", teams);

		// HANDLING QUOTES IN NAME
		if (firstName.contains("\"")) {
			firstName = firstName.replace("\"", "");
		}

		if (lastName.contains("\"")) {
			lastName = lastName.replace("\"", "");
		}

		contact.put("FIRST_NAME", firstName);
		contact.put("LAST_NAME", lastName);
		contact.put("DATE_CREATED", new Date());
		contact.put("EFFECTIVE_FROM", new Date());
		contact.put("DATE_UPDATED", new Date());
		contact.put("ACCOUNT", accountId);
		contact.put("DELETED", false);
		contact.put("SUBSCRIPTION_ON_MARKETING_EMAIL", true);
		if (phone != null) {
			contact.put("PHONE_NUMBER", phone);
		}
		contact.put("USER", userId);

		String fullName = firstName;
		if (lastName != null && !lastName.isBlank()) {
			fullName += " " + lastName;
		}

		contact.put("FULL_NAME", fullName);
		return contact;
	}

	public Map<String, Object> putData(String companyId, String moduleId, String moduleName, Map<String, Object> entry,
			String dataId) {

		String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;

		if (moduleName.equals("Users")) {
			Optional<Map<String, Object>> optionalExistingEntry = entryRepository.findById(dataId,
					"Users_" + companyId);
			Map<String, Object> existingEntry = optionalExistingEntry.get();

			// CHECK IF ROLE CHANGED
			if (!entry.get("ROLE").toString().equals(existingEntry.get("ROLE").toString())) {
				// IF CHANGED UPDATE DEFAULT TEAMS
				Optional<Role> optionalExistingRole = rolesRepository.findById(existingEntry.get("_id").toString(),
						"roles_" + companyId);
				Optional<Role> optionalNewRole = rolesRepository.findById(entry.get("_id").toString(),
						"roles_" + companyId);
				Role newRole = optionalNewRole.get();
				if (optionalExistingRole.isPresent()) {
					Role existingRole = optionalExistingRole.get();
					String existingRoleName = existingRole.getName();

					if (newRole != null) {
						String newRoleName = newRole.getName();
						Map<String, Object> oldTeam = entryRepository
								.findTeamByName(existingRoleName, "Teams_" + companyId).orElse(null);

						Map<String, Object> newTeam = entryRepository.findTeamByName(newRoleName, "Teams_" + companyId)
								.orElse(null);

						String oldTeamId = oldTeam.get("_id").toString();
						String newTeamId = newTeam.get("_id").toString();
						List<String> teams = (List<String>) entry.get("TEAMS");
						teams.remove(oldTeamId);
						teams.add(newTeamId);
						newTeam.put("TEAMS", teams);
						entryRepository.updateTeamUser(dataId, existingRoleName, "Teams_" + companyId);
						entryRepository.removeTeamUser(dataId, newRoleName, "Teams_" + companyId);

					}

				}

			}
		}

		Optional<Map<String, Object>> optionalExistingEntry = entryRepository.findById(dataId, collectionName);
		if (optionalExistingEntry.isPresent()) {
			Map<String, Object> existingEntry = optionalExistingEntry.get();
			existingEntry.put("DATA_ID", existingEntry.remove("_id").toString());
			if (!moduleName.equalsIgnoreCase("teams")) {
				existingEntry.put("EFFECTIVE_TO", new Date());
				entryRepository.save(existingEntry, collectionName);
			}
			entry.put("_id", new ObjectId(dataId));
			postDataIntoElastic(moduleId, companyId, entry);
		}

		return entry;

	}

}
