package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.sam.rules.dao.SamFileRule;

public interface CustomRuleRepository {

	public Optional<List<SamFileRule>> findAllRulesInCompany(Pageable pageable, String collectionName);

}
