package com.ngdesk.sam.discoverymap.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.repositories.DiscoveryMapRepository;
import com.ngdesk.repositories.ModuleRepository;

@Component
public class DiscoverMapService {

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	private DiscoveryMapRepository discoverMapRepository;

	public void validateApplication(List<String> softwareProducts, String collectionName) {
		for (String softwareProduct : softwareProducts) {
			Optional<Map<String, Object>> optional = moduleRepository.findById(softwareProduct, collectionName);
			if (optional.isEmpty()) {
				throw new BadRequestException("SOFTWARE_PRODUCT_ID_INVALID", null);
			}
		}
	}

	public void duplicateDiscoveryMapByNameCheck(String discoveryMapName) {
		Optional<DiscoveryMap> optional = discoverMapRepository.findDiscoveryMapByName(discoveryMapName,
				"sam_discovery_map");
		if (optional.isPresent()) {
			throw new BadRequestException("DISCOVERY_MAP_NAME_ALREADY_EXISTS", null);
		}
	}

	public void duplicatePiiDiscoveryMapNameAndIdCheck(String discoveryMapName, String discoveryMapId) {
		Optional<DiscoveryMap> optional = discoverMapRepository
				.findOtherDiscoveryMapsWithDuplicateName(discoveryMapName, discoveryMapId, "sam_discovery_map");
		if (optional.isPresent()) {
			throw new BadRequestException("DISCOVERY_MAP_NAME_ALREADY_EXISTS", null);
		}
	}

}