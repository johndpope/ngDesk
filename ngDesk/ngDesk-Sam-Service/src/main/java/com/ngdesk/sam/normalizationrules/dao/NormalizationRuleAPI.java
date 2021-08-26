package com.ngdesk.sam.normalizationrules.dao;

import java.util.Date;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.result.DeleteResult;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.NormalizationRuleRepository;
import com.ngdesk.repositories.RolesRepository;

@RestController
public class NormalizationRuleAPI {

	@Autowired
	NormalizationRuleRepository normalizationRuleRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	RolesRepository rolesRepository;

	@PostMapping("/normalization_rules")
	public NormalizationRule postNormalizationRule(@Valid @RequestBody NormalizationRule normalizationRule) {
		normalizationRule.setCompanyId(authManager.getUserDetails().getCompanyId());
		if ((authManager.getUserDetails().getCompanySubdomain()).equals("bluemsp-new")) {
			normalizationRule.setStatus("Approved");
		} else {
			normalizationRule.setStatus("Unapproved");
		}
		normalizationRule.setDateCreated(new Date());
		normalizationRule = normalizationRuleRepository.save(normalizationRule, "normalization_rules");
		return normalizationRule;
	}

	@PostMapping("/normalization_rules/approve")
	public void postApprovalNormalizationRule(@RequestParam("rule_id") String ruleId) {
		Optional<NormalizationRule> optionalNormalizationRule = normalizationRuleRepository
				.findByRuleIdAndCompanyId(ruleId, authManager.getUserDetails().getCompanyId(), "normalization_rules");

		if (optionalNormalizationRule.isEmpty()) {
			throw new NotFoundException("NORMALIZATION_RULE_NOT_FOUND", null);

		} else {
			NormalizationRule normalizationRule = optionalNormalizationRule.get();
			if ((authManager.getUserDetails().getCompanySubdomain()).equals("bluemsp-new")) {
				String systemAdminRoleId = rolesRepository
						.findRoleName("SystemAdmin", "roles_" + authManager.getUserDetails().getCompanyId()).get()
						.getId();
				String userRoleId = authManager.getUserDetails().getRole();
				if (systemAdminRoleId.equals(userRoleId)) {
					normalizationRule.setStatus("Approved");
					normalizationRuleRepository.save(normalizationRule, "normalization_rules");
				} else {
					throw new ForbiddenException("FORBIDDEN");
				}
			} else {
				normalizationRule.setStatus("Unapproved");
				normalizationRuleRepository.save(normalizationRule, "normalization_rules");
				throw new ForbiddenException("FORBIDDEN");
			}

		}
	}

	@PutMapping("/normalization_rules")
	public NormalizationRule putNormalizationRule(@Valid @RequestBody NormalizationRule normalizationRule) {
		Optional<NormalizationRule> optional = normalizationRuleRepository
				.findById(normalizationRule.getNormalizationRuleId(), "normalization_rules");

		if (optional.isEmpty()) {
			throw new NotFoundException("NORMALIZATION_RULE_NOT_FOUND", null);
		}

		normalizationRule.setCompanyId(authManager.getUserDetails().getCompanyId());
		normalizationRule.setDateCreated(new Date());
		normalizationRule = normalizationRuleRepository.save(normalizationRule, "normalization_rules");
		return normalizationRule;
	}

	@DeleteMapping("/normalization_rules")
	public DeleteResult deleteNormalizationRule(@RequestParam("rule_id") String ruleId) {
		Optional<DeleteResult> optionalDeleteResult = normalizationRuleRepository.findByCompanyIdAndRuleIdAndRemove(
				authManager.getUserDetails().getCompanyId(), ruleId, "normalization_rules");
		DeleteResult deleteResult = optionalDeleteResult.get();
		return deleteResult;
	}

}
