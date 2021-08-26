package com.ngdesk.repositories.sam.file.rule;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.sam.file.rule.dao.SamFileRule;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface SamFileRuleRepository extends CustomSamFileRuleRepository, CustomNgdeskRepository<SamFileRule, String> {

}
