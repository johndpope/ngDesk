package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.workflow.executor.dao.BlackListWhiteList;

@Repository
public class CustomBlackListWhiteListRepositoryImpl implements CustomBlackListWhiteListRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<BlackListWhiteList> findWhiteListedRecordByEmailAddressAndType(String emailAddress,
			String companyId, String type) {
		Assert.notNull(emailAddress, "The given email address should not be null");
		Assert.notNull(companyId, "The given companyId should not be null");
		Assert.notNull(type, "The given type should not be null");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("EMAIL_ADDRESS").is(emailAddress), Criteria.where("STATUS").is("WHITELIST"),
				Criteria.where("TYPE").is(type));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), BlackListWhiteList.class,
				"blacklisted_whitelisted_emails_" + companyId));
	}

	@Override
	public Optional<BlackListWhiteList> findWhiteListedRecordByDomainAndType(String domain, String companyId,
			String type) {
		Assert.notNull(domain, "The given domain address should not be null");
		Assert.notNull(companyId, "The given companyId should not be null");
		Assert.notNull(type, "The given type should not be null");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("EMAIL_ADDRESS").is(domain), Criteria.where("STATUS").is("WHITELIST"),
				Criteria.where("TYPE").is(type));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), BlackListWhiteList.class,
				"blacklisted_whitelisted_emails_" + companyId));
	}

	@Override
	public Optional<BlackListWhiteList> findBlackListedRecordByEmailAddressAndType(String emailAddress, String companyId,
			String type) {
		Assert.notNull(emailAddress, "The given email address should not be null");
		Assert.notNull(companyId, "The given companyId should not be null");
		Assert.notNull(type, "The given type should not be null");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("EMAIL_ADDRESS").is(emailAddress), Criteria.where("STATUS").is("BLACKLIST"),
				Criteria.where("TYPE").is(type));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), BlackListWhiteList.class,
				"blacklisted_whitelisted_emails_" + companyId));
	}

	@Override
	public Optional<BlackListWhiteList> findBlackListedRecordByDomainAndType(String domain, String companyId,
			String type) {
		Assert.notNull(domain, "The given domain address should not be null");
		Assert.notNull(companyId, "The given companyId should not be null");
		Assert.notNull(type, "The given type should not be null");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("EMAIL_ADDRESS").is(domain), Criteria.where("STATUS").is("BLACKLIST"),
				Criteria.where("TYPE").is(type));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), BlackListWhiteList.class,
				"blacklisted_whitelisted_emails_" + companyId));
	}

}
