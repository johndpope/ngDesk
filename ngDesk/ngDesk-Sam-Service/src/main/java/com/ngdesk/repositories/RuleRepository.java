package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.sam.rules.dao.SamFileRule;

@Repository
public interface RuleRepository extends CustomRuleRepository, CustomNgdeskRepository<SamFileRule, String> {

}
