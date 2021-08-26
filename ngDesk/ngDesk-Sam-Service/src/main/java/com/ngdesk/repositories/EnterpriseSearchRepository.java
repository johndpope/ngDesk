package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.sam.enterprisesearch.dao.EnterpriseSearch;

@Repository
public interface EnterpriseSearchRepository
		extends CustomEnterpriseSearchRepository,CustomNgdeskRepository<EnterpriseSearch, String> {

}
