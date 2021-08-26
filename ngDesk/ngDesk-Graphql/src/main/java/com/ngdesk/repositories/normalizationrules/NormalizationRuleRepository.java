package com.ngdesk.repositories.normalizationrules;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.normalizationrules.dao.NormalizationRule;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface NormalizationRuleRepository
		extends CustomNormalizationRuleRepository, CustomNgdeskRepository<NormalizationRule, String> {

}
