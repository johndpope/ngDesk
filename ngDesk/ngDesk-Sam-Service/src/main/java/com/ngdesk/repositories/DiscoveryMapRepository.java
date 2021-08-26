package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.sam.discoverymap.dao.DiscoveryMap;

@Repository
public interface DiscoveryMapRepository
		extends CustomDiscoveryMapRepository, CustomNgdeskRepository<DiscoveryMap, String> {

}