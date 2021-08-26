package com.ngdesk.repositories.attachments;

import org.springframework.stereotype.Repository;

import com.ngdesk.data.dao.Attachment;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface AttachmentsRepository extends  CustomAttachmentsRepository, CustomNgdeskRepository<Attachment, String> {

}
