package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.workflow.executor.dao.EmailChannel;

public interface CustomEmailChannelRepository {
	
	public Optional<EmailChannel> findChannelByEmailAddressAndModuleId(String emailAddress, String moduleId, String companyId);
	public Optional<EmailChannel> findChannelByEmailAddress(String emailAddress, String companyId);
}
