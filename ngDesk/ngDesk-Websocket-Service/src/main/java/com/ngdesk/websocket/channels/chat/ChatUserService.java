package com.ngdesk.websocket.channels.chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpHost;
import org.bson.types.ObjectId;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.data.elastic.ElasticService;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.repositories.RolesRepository;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.companies.dao.Phone;
import com.ngdesk.websocket.modules.dao.Module;
import com.ngdesk.websocket.roles.dao.Role;

@Component
public class ChatUserService {
	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	Environment env;

	@Autowired
	ElasticService elasticService;

	public Map<String, Object> createOrGetUser(Company company, PageLoad message) {

		String companyId = company.getId();

		Map<String, Object> customer = new HashMap<String, Object>();

		Module teamsModule = modulesRepository.findModuleByName("Teams", "modules_" + companyId).orElse(null);

		Map<String, Object> globalTeam = entryRepository.findTeamByName("Global", "Teams_" + companyId).orElse(null);
		Map<String, Object> customersTeam = entryRepository.findTeamByName("Customers", "Teams_" + companyId)
				.orElse(null);

		customer = entryRepository
				.findUserByEmailAddressIncludingDeleted(message.getEmailAddress().toLowerCase(), "Users_" + companyId)
				.orElse(null);

		Role customerRole = rolesRepository.findRoleName("Customers", "roles_" + companyId).orElse(null);

		if (customer == null) {
			if (globalTeam != null) {
				Map<String, Object> account = getAccountPayload(message.getEmailAddress().toLowerCase(),
						globalTeam.get("_id").toString());
				String accountId = createOrGetAccountId(companyId, message.getEmailAddress().toLowerCase(), account);
				if (customerRole != null) {
					Map<String, Object> newCustomer = getUserPayload(message.getEmailAddress().toLowerCase(),
							globalTeam.get("_id").toString(), customerRole.getId());
					Module usersModule = modulesRepository.findModuleByName("Users", "modules_" + companyId)
							.orElse(null);
					if (usersModule != null) {
						customer = postData(companyId, usersModule.getModuleId(), "Users", newCustomer);
						String customerId = customer.get("_id").toString();

						Map<String, Object> newContact = getContactPayload(message.getFirstName(),
								message.getLastName(), company.getPhone(), accountId, globalTeam.get("_id").toString(),
								customerId);
						Module contactsModule = modulesRepository.findModuleByName("Contacts", "modules_" + companyId)
								.orElse(null);
						if (contactsModule != null) {
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

							if (customersTeam != null) {
								List<String> customerTeamUsers = (List<String>) customersTeam.get("USERS");
								customerTeamUsers.add(customerId);
								customersTeam.put("USERS", customerTeamUsers);
								putData(companyId, teamsModule.getModuleId(), "Teams", customersTeam,
										customersTeam.get("_id").toString());
							}

							Map<String, Object> customerTeam = entryRepository
									.findTeamByName("Customers", "Teams_" + companyId).orElse(null);
							Map<String, Object> updatedUserWithTeams = entryRepository
									.findById(customerId, "Users_" + companyId).orElse(null);
							List<String> teams = (List<String>) updatedUserWithTeams.get("TEAMS");
							if (customerTeam != null) {
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

		Module accountModule = modulesRepository.findModuleByName("Accounts", "modules_" + companyId).orElse(null);
		if (accountModule != null) {
			Map<String, Object> existingAccount = entryRepository
					.findAccountByName(accountName, "Accounts_" + companyId).orElse(null);

			// IF ACCOUNT DOCUMENT NULL, CREATE NEW ONE
			if (existingAccount == null) {
				Map<String, Object> accountToReturn = postData(companyId, accountModule.getModuleId(), "Accounts",
						account);
				accountId = accountToReturn.get("_id").toString();
			} else {
				accountId = existingAccount.get("_id").toString();
			}
		}

		return accountId;

	}

	public Map<String, Object> postData(String companyId, String moduleId, String moduleName,
			Map<String, Object> body) {
		RestHighLevelClient elasticClient = null;
		try {

			String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
			elasticClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(env.getProperty("elastic.host"), 9200, "http")));

			Map<String, Object> entry = body;
			entry.put("DATE_CREATED", new Date());
			entry.put("DATE_UPDATED", new Date());
			entry.put("EFFECTIVE_FROM", new Date());
			entry = entryRepository.save(entry, collectionName);

			elasticService.postIntoElastic(moduleId, companyId, entry);

			return entry;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		throw new InternalErrorException("INTERNAL_ERROR");
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

	public Map<String, Object> putData(String companyId, String moduleId, String moduleName, Map<String, Object> body,
			String dataId) {
		RestHighLevelClient elasticClient = null;
		try {

			String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;

			elasticClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(env.getProperty("elastic.host"), 9200, "http")));

			if (moduleName.equals("Users")) {
				Map<String, Object> existingEntry = entryRepository.findById(dataId, "Users_" + companyId).orElse(null);

				// CHECK IF ROLE CHANGED
				if (!body.get("ROLE").toString().equals(existingEntry.get("ROLE").toString())) {
					// IF CHANGED UPDATE DEFAULT TEAMS

					Role existingRole = rolesRepository
							.findById(existingEntry.get("_id").toString(), "roles_" + companyId).orElse(null);
					Role newRole = rolesRepository.findById(body.get("_id").toString(), "roles_" + companyId)
							.orElse(null);
					if (existingRole != null) {
						String existingRoleName = existingRole.getName();

						if (newRole != null) {
							String newRoleName = newRole.getName();
							Map<String, Object> oldTeam = entryRepository
									.findTeamByName(existingRoleName, "Teams_" + companyId).orElse(null);

							Map<String, Object> newTeam = entryRepository
									.findTeamByName(newRoleName, "Teams_" + companyId).orElse(null);

							String oldTeamId = oldTeam.get("_id").toString();
							String newTeamId = newTeam.get("_id").toString();
							List<String> teams = (List<String>) body.get("TEAMS");
							teams.remove(oldTeamId);
							teams.add(newTeamId);
							newTeam.put("TEAMS", teams);
							entryRepository.updateTeamUser(dataId, existingRoleName, "Teams_" + companyId);
							entryRepository.removeTeamUser(dataId, newRoleName, "Teams_" + companyId);

						}

					}

				}
			}
			// TEMPORAL DATA LOGIC

			Map<String, Object> existingEntry = entryRepository.findById(dataId, collectionName).orElse(null);
			if (existingEntry != null) {
				existingEntry.put("DATA_ID", existingEntry.remove("_id").toString());
				if (!moduleName.equalsIgnoreCase("teams")) {
					existingEntry.put("EFFECTIVE_TO", new Date());
					entryRepository.save(existingEntry, collectionName);
				}
				body.put("_id", new ObjectId(dataId));
				entryRepository.findAndReplace(dataId, body, collectionName);
				String id = body.get("_id").toString();
				elasticService.postIntoElastic(moduleId, companyId, body);

			}

			return body;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

}
