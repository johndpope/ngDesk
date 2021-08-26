package com.ngdesk.repositories.sam.file.rule;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import com.ngdesk.graphql.sam.file.rule.dao.SamFileRule;

public interface CustomSamFileRuleRepository {

	public List<SamFileRule> findAllRulesWithPagination(String companyId, Pageable pageable,
			String collectionName);

	public Integer count(String companyId, String collectionName);
	
	public Optional<SamFileRule> findByCompanyIdAndId(String companyId, String id, String collectionName);

}
