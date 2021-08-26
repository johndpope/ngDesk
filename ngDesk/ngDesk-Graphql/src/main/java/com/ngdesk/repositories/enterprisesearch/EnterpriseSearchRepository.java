package com.ngdesk.repositories.enterprisesearch;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.enterprisesearch.dao.EnterpriseSearch;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface EnterpriseSearchRepository extends CustomEnterpriseSearchRepository,
		CustomNgdeskRepository<EnterpriseSearch, String> {

}
