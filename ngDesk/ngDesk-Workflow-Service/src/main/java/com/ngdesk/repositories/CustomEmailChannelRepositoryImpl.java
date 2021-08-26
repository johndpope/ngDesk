package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.workflow.executor.dao.EmailChannel;

@Repository
public class CustomEmailChannelRepositoryImpl implements CustomEmailChannelRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<EmailChannel> findChannelByEmailAddressAndModuleId(String emailAddress, String moduleId,
			String companyId) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("MODULE").is(moduleId), Criteria.where("EMAIL_ADDRESS").is(emailAddress));
		return Optional.ofNullable(
				mongoOperations.findOne(new Query(criteria), EmailChannel.class, "channels_email_" + companyId));
	}

	@Override
	public Optional<EmailChannel> findChannelByEmailAddress(String emailAddress, String companyId) {
		return Optional.ofNullable(mongoOperations.findOne(new Query(Criteria.where("EMAIL_ADDRESS").is(emailAddress)),
				EmailChannel.class, "channels_email_" + companyId));
	}

}
