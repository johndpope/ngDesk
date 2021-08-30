package com.ngdesk.sam.discoveryrules.dao;

import java.util.Date;
import java.util.Optional;

import javax.validation.Valid;

import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.DiscoveryRuleRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RefreshScope
public class DiscoveryRuleAPI {

	@Autowired
	private DiscoveryRuleRepository discoveryRuleRepository;

	@Autowired
	private AuthManager authManager;

	@GetMapping("/rules")
	@Operation(summary = "Get all", description = "Gets all the discovery rules  with pagination and search")
	@PageableAsQueryParam
	public Page<DiscoveryRule> getDiscoveryRules(
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable) {
		return discoveryRuleRepository.findAllByCompanyId(pageable, "sam_discovery_rules",
				authManager.getUserDetails().getCompanyId());
	}

	@GetMapping("/rule/{id}")
	@Operation(summary = "Get by ID", description = "Gets the discovery rules based on ID")
	public DiscoveryRule getDiscoveryRuleById(
			@Parameter(description = "Discovery rule ID", required = true) @PathVariable("id") String id) {
		Optional<DiscoveryRule> optional = discoveryRuleRepository.findByIdAndCompanyId(id,
				authManager.getUserDetails().getCompanyId(), "sam_discovery_rules");
		if (optional.isEmpty()) {
			String vars[] = { "Discovery rule" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		return optional.get();
	}

	@PostMapping("/rule")
	@Operation(summary = "Post Discovery rule", description = "Post a single discovery rule")
	public DiscoveryRule postDiscoveryRule(@Valid @RequestBody DiscoveryRule discoveryRule) {

		discoveryRule.setDateCreated(new Date());
		discoveryRule.setDateUpdated(new Date());
		discoveryRule.setLastUpdated(authManager.getUserDetails().getUserId());
		discoveryRule.setCreatedBy(authManager.getUserDetails().getUserId());
		discoveryRule.setCompanyID(authManager.getUserDetails().getCompanyId());

		discoveryRule = discoveryRuleRepository.save(discoveryRule, "sam_discovery_rules");
		return discoveryRule;
	}

	@PutMapping("/rule")
	@Operation(summary = "Put Discovery rule", description = "Update a discovery rule")
	public DiscoveryRule putDiscoveryRule(@Valid @RequestBody DiscoveryRule discoveryRule) {

		Optional<DiscoveryRule> optional = discoveryRuleRepository.findByIdAndCompanyId(discoveryRule.getId(),
				authManager.getUserDetails().getCompanyId(), "sam_discovery_rules");

		if (optional.isEmpty()) {
			String vars[] = { "Discovery rule" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}

		DiscoveryRule existingDiscoveryRule = optional.get();

		discoveryRule.setDateCreated(existingDiscoveryRule.getDateCreated());
		discoveryRule.setCreatedBy(existingDiscoveryRule.getCreatedBy());
		discoveryRule.setDateUpdated(new Date());
		discoveryRule.setLastUpdated(authManager.getUserDetails().getUserId());
		discoveryRule.setCompanyID(existingDiscoveryRule.getCompanyID());

		discoveryRule = discoveryRuleRepository.save(discoveryRule, "sam_discovery_rules");

		return discoveryRule;
	}

	@DeleteMapping("/rule/{id}")
	@Operation(summary = "Delete discovery rule", description = "Delete a discovery rule by ID")
	public void deleteDiscoveryRule(
			@Parameter(description = "Discovery rule ID", required = true) @PathVariable("id") String id) {
		Optional<DiscoveryRule> optional = discoveryRuleRepository.findByIdAndCompanyId(id,
				authManager.getUserDetails().getCompanyId(), "sam_discovery_rules");

		if (optional.isEmpty()) {
			String vars[] = { "Discovery rule" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}

		discoveryRuleRepository.deleteById(id, "sam_discovery_rules");
	}

}
