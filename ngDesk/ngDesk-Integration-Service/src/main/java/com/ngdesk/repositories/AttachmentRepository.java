package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.data.dao.Attachment;

@Repository
public interface AttachmentRepository extends CustomAttachmentRepository, CustomNgdeskRepository<Attachment, String>{

}