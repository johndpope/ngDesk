package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.workflow.executor.dao.BlackListWhiteList;

public interface CustomBlackListWhiteListRepository {

	public Optional<BlackListWhiteList> findWhiteListedRecordByEmailAddressAndType(String emailAddress, String companyId, String type);

	public Optional<BlackListWhiteList> findWhiteListedRecordByDomainAndType(String domain, String companyId, String type);

	public Optional<BlackListWhiteList> findBlackListedRecordByEmailAddressAndType(String emailAddress, String companyId, String type);

	public Optional<BlackListWhiteList> findBlackListedRecordByDomainAndType(String domain, String companyId, String type);
}
