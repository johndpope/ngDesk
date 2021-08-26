package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.sam.discoverymap.dao.DiscoveryMap;

public interface CustomDiscoveryMapRepository {

	public Optional<DiscoveryMap> findByCompanyIdAndId(String id, String companyId, String collectionName);

	public Optional<DiscoveryMap> findDiscoveryMapByName(String name, String collectionName);

	public Optional<DiscoveryMap> findOtherDiscoveryMapsWithDuplicateName(String name, String discoveryMapId,
			String collectionName);

}
