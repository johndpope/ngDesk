package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.sam.normalizationrules.dao.NormalizationRule;

@Repository
public interface NormalizationRuleRepository
		extends CustomNormalizationRuleRepository, CustomNgdeskRepository<NormalizationRule, String> {

}
