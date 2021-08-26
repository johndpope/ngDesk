package com.ngdesk.accounts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.wrapper.Wrapper;

@Component
public class Account {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Wrapper wrapper;

	@Autowired
	private Global global;

	private final Logger log = LoggerFactory.getLogger(Account.class);

	public Document createAccount(String accountName, String companyId, String globalTeamId) {
		Document accountDocument = null;
		try {
			log.trace("Enter Account.createAccount() accountName: " + accountName + ", companyId: " + companyId);

			Document account = new Document();

			List<String> teams = new ArrayList<String>();

			if (globalTeamId != null) {
				teams.add(globalTeamId);
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("modules_" + companyId);
			Document accountDoc = collection.find(Filters.eq("NAME", "Accounts")).first();

			account.put("ACCOUNT_NAME", accountName);
			account.put("DATE_CREATED", new Date());
			account.put("TEAMS", teams);
			account.put("DELETED", false);

			accountDocument = Document.parse(wrapper.postData(companyId, accountDoc.getObjectId("_id").toString(),
					"Accounts", new ObjectMapper().writeValueAsString(account)));

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit Account.createAccount() accountName: " + accountName + ", companyId: " + companyId);
		return accountDocument;
	}

	public boolean accountExists(String accountName, String companyId) {
		boolean flag = false;
		try {
			log.trace("Enter Account.accountExists() accountName: " + accountName + ", companyId: " + companyId);
			String collectionName = "Accounts_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document existingAccountDocument = collection.find(Filters.eq("ACCOUNT_NAME", accountName)).first();
			flag = (existingAccountDocument == null ? false : true);

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit Account.accountExists() accountName: " + accountName + ", companyId: " + companyId);
		return flag;

	}

	public Document getAccountByName(String accountName, String companyId) {

		Document account = null;
		try {
			log.trace("Enter Account.getAccountByName() accountName: " + accountName + ", companyId: " + companyId);
			String collectionName = "Accounts_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			account = collection.find(Filters.eq("ACCOUNT_NAME", accountName)).first();
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit Account.getAccountByName() accountName: " + accountName + ", companyId: " + companyId);
		return account;
	}

	public Document getAccountById(String accountId, String companyId) {

		Document account = null;
		try {
			log.trace("Enter Account.getAccountById() accountId: " + accountId + ", companyId: " + companyId);
			String collectionName = "Accounts_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!new ObjectId().isValid(accountId)) {
				throw new BadRequestException("INVALID_ACCOUNT_ID");
			}
			account = collection.find(Filters.eq("_id", new ObjectId(accountId))).first();
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit Account.getAccountById() accountId: " + accountId + ", companyId: " + companyId);
		return account;
	}

}
