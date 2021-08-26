package com.ngdesk.sam.rules.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.RuleRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class RuleAPI {
	@Autowired
	AuthManager authManager;

	@Autowired
	RuleService ruleService;

	@Autowired
	RuleRepository ruleRepository;

	@PostMapping("/software/probe/rules")
	public SamFileRule postSamFileRule(@Valid @RequestBody SamFileRule samFileRule) {
		ruleService.validateRuleCondition(samFileRule);

		samFileRule.setCompanyId(authManager.getUserDetails().getCompanyId());
		samFileRule.setDateCreated(new Date());
		samFileRule.setDateUpdated(new Date());
		samFileRule.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		samFileRule.setCreatedBy(authManager.getUserDetails().getUserId());
		samFileRule.setHash("");
		ruleRepository.save(samFileRule, "sam_file_rules");
		return samFileRule;
	}

	@PutMapping("/software/probe/rules")
	@Operation(summary = "Put sam file rule", description = "Update a file rule")
	public SamFileRule putSamFileRule(@Valid @RequestBody SamFileRule samFileRule) {
		ruleService.validateRuleCondition(samFileRule);

		Optional<SamFileRule> optional = ruleRepository.findById(samFileRule.getId(), "sam_file_rules");

		if (optional.isEmpty()) {
			throw new NotFoundException("RULE_ID_NOT_FOUND", null);
		}
		SamFileRule existingSamFileRule = optional.get();
		samFileRule.setDateCreated(existingSamFileRule.getDateCreated());
		samFileRule.setCreatedBy(existingSamFileRule.getCreatedBy());
		samFileRule.setCompanyId(existingSamFileRule.getCompanyId());
		samFileRule.setDateUpdated(new Date());
		samFileRule.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		samFileRule = ruleRepository.save(samFileRule, "sam_file_rules");

		return samFileRule;
	}

	@GetMapping("/software/probe/rules")
	@PageableAsQueryParam
	public List<SamFileRule> getAllSamFileRules(
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable) {

		Optional<List<SamFileRule>> optional = ruleRepository.findAllRulesInCompany(pageable, "sam_file_rules");
		List<SamFileRule> listRule = optional.get();
		return listRule;
	}

	@GetMapping("/software/probe/rules/{rule_id}")
	public SamFileRule getSamFileRuleById(
			@Parameter(description = "sam file rule ID", required = true) @PathVariable("rule_id") String ruleId) {
		Optional<SamFileRule> optional = ruleRepository.findById(ruleId, "sam_file_rules");
		if (optional.isEmpty()) {
			throw new NotFoundException("DAO_NOT_FOUND", null);
		}
		return optional.get();
	}
}
