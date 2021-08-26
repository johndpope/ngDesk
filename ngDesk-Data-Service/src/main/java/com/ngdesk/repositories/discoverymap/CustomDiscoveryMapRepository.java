package com.ngdesk.repositories.discoverymap;

import java.util.List;
import java.util.Optional;

import com.ngdesk.data.sam.dao.DiscoveryMap;

public interface CustomDiscoveryMapRepository {
	public Optional<DiscoveryMap> findByCompanyIdAndId(String id, String companyId, String collectionName);

	public List<DiscoveryMap> findAllDiscoveryMaps();

}
