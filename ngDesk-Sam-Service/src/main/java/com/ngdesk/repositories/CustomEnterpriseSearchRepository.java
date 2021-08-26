package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.sam.enterprisesearch.dao.EnterpriseSearch;

public interface CustomEnterpriseSearchRepository {

	public Optional<EnterpriseSearch> findEnterpriseSearchByName(String name, String collectionName);

	public Optional<EnterpriseSearch> findEnterpriseSearchByIdAndName(String id, String name, String collectionName);

	public Optional<EnterpriseSearch> findByRuleIdAndCompanyId(String ruleId, String companyId, String collectionName);

}
