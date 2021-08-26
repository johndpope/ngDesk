package com.ngdesk.repositories.discoverymap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.discoverymap.dao.DiscoveryMap;

@Repository
public interface CustomDiscoveryMapRepository {
	public Optional<DiscoveryMap> findByCompanyIdAndId(String companyId, String id, String collectionName);

	public Optional<List<DiscoveryMap>> findAllDiscoveryMapInCompany(Pageable pageable, String companyId,
			String collectionName);

	public Optional<List<DiscoveryMap>> findUnaproverdDiscoveryMap(Pageable pageable, String collectionName);
	
	public List<DiscoveryMap> findDiscoveryMapsWithSearch(List<String> entryIds, Pageable pageable,
			String collectionName);
}