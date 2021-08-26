package com.ngdesk.sam.discoverymap.dao;

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

import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.DiscoveryMapRepository;
import com.ngdesk.repositories.RolesRepository;

@RestController
public class DiscoveryMapAPI {

	@Autowired
	private DiscoveryMapRepository discoverMapRepository;

	@Autowired
	private RolesRepository rolesRepository;

	@Autowired
	private DiscoverMapService discoveryMapService;

	@Autowired
	private AuthManager authManager;

	@Autowired
	private ElasticSearchForDiscoveryMaps elasticSearch;

	@PostMapping("/discovery_map")
	public DiscoveryMap postDiscoveryMap(@Valid @RequestBody DiscoveryMap discoveryMap) {
		String systemAdminId = rolesRepository
				.findRoleName("SystemAdmin", "roles_" + authManager.getUserDetails().getCompanyId()).get().getId();
		String userId = authManager.getUserDetails().getRole();
		if (!systemAdminId.equals(userId)) {
			throw new ForbiddenException("FORBIDDEN");
		}
		discoveryMapService.duplicateDiscoveryMapByNameCheck(discoveryMap.getName());
		discoveryMapService.validateApplication(discoveryMap.getProducts(),
				"Software_Products_" + authManager.getUserDetails().getCompanyId());
		discoveryMap.setApproved("false");
		discoveryMap.setCompanyId(authManager.getUserDetails().getCompanyId());
		discoveryMap.setDateCreated(new Date());
		discoveryMap.setDateUpdated(new Date());
		discoveryMap.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		discoveryMap.setCreatedBy(authManager.getUserDetails().getUserId());
		discoveryMap = discoverMapRepository.save(discoveryMap, "sam_discovery_map");
		return discoveryMap;

	}

	@PostMapping("/discovery_map/approval")
	public void postDiscoveryMapApproval(@RequestParam("discovery_map_id") String discoveryMapId) {
		Optional<DiscoveryMap> optionalDiscoveryMap = discoverMapRepository.findByCompanyIdAndId(discoveryMapId,
				authManager.getUserDetails().getCompanyId(), "sam_discovery_map");
		if (optionalDiscoveryMap.isEmpty()) {
			throw new NotFoundException("DISCOVERY_MAP_ID_NOT_FOUND", null);
		} else {
			DiscoveryMap discoveryMap = optionalDiscoveryMap.get();
			if (authManager.getUserDetails().getCompanySubdomain().equals("bluemsp-new")) {
				String systemAdminId = rolesRepository
						.findRoleName("SystemAdmin", "roles_" + authManager.getUserDetails().getCompanyId()).get()
						.getId();
				String userId = authManager.getUserDetails().getRole();
				if (systemAdminId.equals(userId)) {
					discoveryMap.setApproved("true");
					discoverMapRepository.save(discoveryMap, "sam_discovery_map");
				} else {
					throw new ForbiddenException("FORBIDDEN");
				}
			} else {
				discoveryMap.setApproved("false");
				discoverMapRepository.save(discoveryMap, "sam_discovery_map");
				throw new ForbiddenException("FORBIDDEN");
			}
		}
	}

	@PutMapping("/discovery_map")
	public DiscoveryMap putDiscoveryMap(@Valid @RequestBody DiscoveryMap discoveryMap) {
		String systemAdminId = rolesRepository
				.findRoleName("SystemAdmin", "roles_" + authManager.getUserDetails().getCompanyId()).get().getId();
		String userId = authManager.getUserDetails().getRole();
		if (!systemAdminId.equals(userId)) {
			throw new ForbiddenException("FORBIDDEN");
		}
		discoveryMapService.duplicatePiiDiscoveryMapNameAndIdCheck(discoveryMap.getName(), discoveryMap.getId());
		discoveryMapService.validateApplication(discoveryMap.getProducts(),
				"Software_Products_" + authManager.getUserDetails().getCompanyId());
		Optional<DiscoveryMap> optional = discoverMapRepository.findById(discoveryMap.getId(), "sam_discovery_map");
		if (optional.isEmpty()) {
			throw new NotFoundException("DISCOVERY_MAP_ID_NOT_FOUND", null);
		}
		DiscoveryMap existingDiscoveryMap = optional.get();

		discoveryMap.setApproved(existingDiscoveryMap.getApproved());
		discoveryMap.setDateCreated(existingDiscoveryMap.getDateCreated());
		discoveryMap.setCreatedBy(existingDiscoveryMap.getCreatedBy());
		discoveryMap.setCompanyId(existingDiscoveryMap.getCompanyId());
		discoveryMap.setDateUpdated(new Date());
		discoveryMap.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		discoveryMap = discoverMapRepository.save(discoveryMap, "sam_discovery_map");

		return discoveryMap;
	}

	@DeleteMapping("/discovery_map")
	public void deleteDiscoveryMap(@RequestParam("discovery_map_id") String discoveryMapId, DiscoveryMap discoveryMap) {
		String systemAdminId = rolesRepository
				.findRoleName("SystemAdmin", "roles_" + authManager.getUserDetails().getCompanyId()).get().getId();
		String userId = authManager.getUserDetails().getRole();
		if (!systemAdminId.equals(userId)) {
			throw new ForbiddenException("FORBIDDEN");
		}
		Optional<DiscoveryMap> optional = discoverMapRepository.findById(discoveryMapId, "sam_discovery_map");

		if (optional.isEmpty()) {
			throw new NotFoundException("DISCOVERY_MAP_ID_NOT_FOUND", null);
		}
		discoverMapRepository.deleteById(discoveryMapId, "sam_discovery_map");
	}
}