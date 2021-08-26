package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.data.dao.Attachment;

public interface CustomAttachmentRepository {
	
	public Optional<Attachment> findAttachmentByHash(String hash, String collectionName);
}
