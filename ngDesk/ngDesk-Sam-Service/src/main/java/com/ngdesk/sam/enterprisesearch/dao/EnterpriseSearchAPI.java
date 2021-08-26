package com.ngdesk.sam.enterprisesearch.dao;

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

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.EnterpriseSearchRepository;
import com.ngdesk.repositories.RolesRepository;

@RestController
public class EnterpriseSearchAPI {
	@Autowired
	private AuthManager authManager;

	@Autowired
	private EnterpriseSearchRepository enterpriseSearchRepository;

	@Autowired
	private RolesRepository rolesRepository;

	@Autowired
	private EnterpriseSearchService enterpriseSearchService;

	@PostMapping("/enterprise_search")
	public EnterpriseSearch postEnterpriseSearch(@Valid @RequestBody EnterpriseSearch enterpriseSearch) {
		String systemAdminId = rolesRepository
				.findRoleName("SystemAdmin", "roles_" + authManager.getUserDetails().getCompanyId()).get().getId();
		String userId = authManager.getUserDetails().getRole();
		if (!systemAdminId.equals(userId)) {
			throw new ForbiddenException("FORBIDDEN");
		}
		enterpriseSearch.setStatus("Unapproved");
		if ((authManager.getUserDetails().getCompanySubdomain()).equals("bluemsp-new")) {
			enterpriseSearch.setStatus("Approved");
		} else {
			enterpriseSearch.setStatus("Unapproved");
		}
		enterpriseSearchService.duplicateEnterpriseSearchRepositoryNameCheck(enterpriseSearch.getName());
		enterpriseSearch.setCompanyId(authManager.getUserDetails().getCompanyId());
		enterpriseSearch.setDateCreated(new Date());
		enterpriseSearch.setDateUpdated(new Date());
		enterpriseSearch.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		enterpriseSearch.setCreatedBy(authManager.getUserDetails().getUserId());
		enterpriseSearch = enterpriseSearchRepository.save(enterpriseSearch, "Enterprise_Search");
		return enterpriseSearch;
	}

	@PostMapping("/enterprise_search/approval")
	public void postApprovalEnterpriseSearch(@RequestParam("enterprise_search_id") String enterpriseSearchId) {
		Optional<EnterpriseSearch> optionalEnterpriseSearch = enterpriseSearchRepository.findByRuleIdAndCompanyId(
				enterpriseSearchId, authManager.getUserDetails().getCompanyId(), "Enterprise_Search");

		if (optionalEnterpriseSearch.isEmpty()) {
			throw new NotFoundException("ENTERPRISE_SEARCH_NOT_FOUND", null);

		} else {
			EnterpriseSearch enterpriseSearch = optionalEnterpriseSearch.get();
			if ((authManager.getUserDetails().getCompanySubdomain()).equals("bluemsp-new")) {
				String systemAdminRoleId = rolesRepository
						.findRoleName("SystemAdmin", "roles_" + authManager.getUserDetails().getCompanyId()).get()
						.getId();
				String userRoleId = authManager.getUserDetails().getRole();
				if (systemAdminRoleId.equals(userRoleId)) {
					enterpriseSearch.setStatus("Approved");
					enterpriseSearchRepository.save(enterpriseSearch, "Enterprise_Search");
				} else {
					throw new ForbiddenException("FORBIDDEN");
				}
			} else {
				enterpriseSearch.setStatus("Unapproved");
				enterpriseSearchRepository.save(enterpriseSearch, "Enterprise_Search");
				throw new ForbiddenException("FORBIDDEN");
			}

		}
	}

	@PutMapping("/enterprise_search")
	public EnterpriseSearch putEnterpriseSearch(@Valid @RequestBody EnterpriseSearch enterpriseSearch) {

		String systemAdminId = rolesRepository
				.findRoleName("SystemAdmin", "roles_" + authManager.getUserDetails().getCompanyId()).get().getId();
		String userId = authManager.getUserDetails().getRole();
		if (!systemAdminId.equals(userId)) {
			throw new ForbiddenException("FORBIDDEN");
		}
		Optional<EnterpriseSearch> optional = enterpriseSearchRepository.findById(enterpriseSearch.getEnterpriseSearchId(),
				"Enterprise_Search");
		if (optional.isEmpty()) {
			throw new NotFoundException("ENTERPRISE_SEARCH_NOT_FOUND", null);
		}
		EnterpriseSearch existingEnterpriseSearch = optional.get();
		enterpriseSearch.setStatus(existingEnterpriseSearch.getStatus());
		enterpriseSearch.setDateCreated(existingEnterpriseSearch.getDateCreated());
		enterpriseSearch.setCreatedBy(existingEnterpriseSearch.getCreatedBy());
		enterpriseSearch.setCompanyId(existingEnterpriseSearch.getCompanyId());
		enterpriseSearch.setDateUpdated(new Date());
		enterpriseSearch.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		Optional<EnterpriseSearch> optionalDuplicateName = enterpriseSearchRepository.findEnterpriseSearchByIdAndName(
				enterpriseSearch.getEnterpriseSearchId(), enterpriseSearch.getName(), "Enterprise_Search");
		if (optionalDuplicateName.isPresent()) {
			throw new BadRequestException("ENTERPRISE_SEARCH_NAME_ALREADY_EXISTS", null);
		}
		enterpriseSearch = enterpriseSearchRepository.save(enterpriseSearch, "Enterprise_Search");
		return enterpriseSearch;
	}

	@DeleteMapping("/enterprise_search")
	public void deleteEnterpriseSearch(@RequestParam("enterprise_search_id") String enterpriseSearchId,
			EnterpriseSearch personallyIdentifiableInformation) {
		String systemAdminId = rolesRepository
				.findRoleName("SystemAdmin", "roles_" + authManager.getUserDetails().getCompanyId()).get().getId();
		String userId = authManager.getUserDetails().getRole();
		if (!systemAdminId.equals(userId)) {
			throw new ForbiddenException("FORBIDDEN");
		}
		Optional<EnterpriseSearch> optional = enterpriseSearchRepository.findById(enterpriseSearchId, "Enterprise_Search");

		if (optional.isEmpty()) {
			throw new NotFoundException("ENTERPRISE_SEARCH_NOT_FOUND", null);
		}
		enterpriseSearchRepository.deleteById(enterpriseSearchId, "Enterprise_Search");
	}

}
