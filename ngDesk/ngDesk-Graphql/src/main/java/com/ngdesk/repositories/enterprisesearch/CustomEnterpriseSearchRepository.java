package com.ngdesk.repositories.enterprisesearch;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.enterprisesearch.dao.EnterpriseSearch;

public interface CustomEnterpriseSearchRepository {

	public Optional<List<EnterpriseSearch>> findAllEnterpriseSearchIdByCompanyId(Pageable pageable, String companyId,
			String collectionName);

	public Optional<EnterpriseSearch> findByCompanyIdAndId(String companyId, String id, String collectionName);

	public int enterpriseSearchCount(String companyId, String collectionName);

	public Optional<List<EnterpriseSearch>> findAllUnapprovedEnterpriseSearch(Pageable pageable, String collectionName);

}