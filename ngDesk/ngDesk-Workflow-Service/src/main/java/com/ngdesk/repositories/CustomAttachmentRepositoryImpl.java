package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.data.dao.Attachment;

@Repository
public class CustomAttachmentRepositoryImpl implements CustomAttachmentRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Attachment> findAttachmentByHash(String hash, String collectionName) {
		Assert.notNull(hash, "The given hash should not be null");
		return Optional.ofNullable(mongoOperations.findOne(new Query(Criteria.where("HASH").is(hash)),
				Attachment.class, collectionName));
	}

}
