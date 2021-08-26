package com.ngdesk.sam.enterprisesearch.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.repositories.EnterpriseSearchRepository;

@Component
public class EnterpriseSearchService {
	@Autowired
	private EnterpriseSearchRepository enterpriseSearchRepository;

	public void duplicateEnterpriseSearchRepositoryNameCheck(String enterpriseSearchName) {
		Optional<EnterpriseSearch> optional = enterpriseSearchRepository.findEnterpriseSearchByName(enterpriseSearchName,
				"Enterprise_Search");

		if (optional.isPresent()) {
			throw new BadRequestException("ENTERPRISE_SEARCH_NAME_ALREADY_EXISTS", null);
		}
	}

}
