package com.ngdesk.repositories.discoverymap;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.discoverymap.dao.DiscoveryMap;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface DiscoveryMapRepository
		extends CustomDiscoveryMapRepository, CustomNgdeskRepository<DiscoveryMap, String> {

}