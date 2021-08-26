package com.ngdesk.repositories.attachments;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.mongodb.MongoNamespace;
import com.ngdesk.data.dao.Attachment;

@Repository
public class CustomAttachmentsRepositoryImpl implements CustomAttachmentsRepository {
	
	@Autowired
	private MongoOperations mongoOperations;
	
	@Override
	public Optional<Attachment> findAttachmentByHash(String hash, String collectionName) {
		Assert.notNull(hash, "The given hash must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		return Optional.ofNullable(mongoOperations.findOne( new Query(Criteria.where("HASH").is(hash)), Attachment.class, collectionName));
	}

	@Override
	public Optional<Attachment> findAttachmentByUUID(String attachmentUuid, String collectionName) {
		Assert.notNull(attachmentUuid, "The given attachmentUuid must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");
		return Optional.ofNullable(mongoOperations.findOne( new Query(Criteria.where("ATTACHMENT_UUID").is(attachmentUuid)), Attachment.class, collectionName));
	}

}
