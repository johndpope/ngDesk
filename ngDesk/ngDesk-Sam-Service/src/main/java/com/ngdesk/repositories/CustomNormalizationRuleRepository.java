package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.DeleteResult;
import com.ngdesk.sam.normalizationrules.dao.NormalizationRule;

@Repository
public interface CustomNormalizationRuleRepository {

	public Optional<NormalizationRule> findByRuleIdAndCompanyId(String ruleId, String companyId, String collectionName);

	public Optional<List<NormalizationRule>> findAllNormalizationRulesInCompany(Pageable pageable, String companyId,
			String collectionName);

	public Optional<DeleteResult> findByCompanyIdAndRuleIdAndRemove(String companyId, String ruleId,
			String collectionName);

	public int normalizationRulesCount(String companyId, String collectionName);

}
