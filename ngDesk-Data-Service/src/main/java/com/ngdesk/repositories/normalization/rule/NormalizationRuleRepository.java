package com.ngdesk.repositories.normalization.rule;

import org.springframework.stereotype.Repository;

import com.ngdesk.data.sam.dao.NormalizationRule;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface NormalizationRuleRepository
		extends CustomNormalizationRuleRepository, CustomNgdeskRepository<NormalizationRule, String> {

}
