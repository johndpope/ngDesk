package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.workflow.executor.dao.EmailChannel;

@Repository
public interface EmailChannelRepository extends CustomNgdeskRepository<EmailChannel, String>, CustomEmailChannelRepository {

}
