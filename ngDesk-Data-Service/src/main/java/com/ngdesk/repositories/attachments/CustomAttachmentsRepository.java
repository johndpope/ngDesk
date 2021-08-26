package com.ngdesk.repositories.attachments;

import java.util.Optional;

import com.ngdesk.data.dao.Attachment;

public interface CustomAttachmentsRepository {
	
	public Optional<Attachment> findAttachmentByHash(String hash, String collectionName);
	public Optional<Attachment> findAttachmentByUUID(String attachmentUuid, String collectionName);
}
