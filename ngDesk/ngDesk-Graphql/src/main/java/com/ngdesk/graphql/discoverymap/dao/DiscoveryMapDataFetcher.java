
package com.ngdesk.graphql.discoverymap.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.discoverymap.DiscoveryMapRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class DiscoveryMapDataFetcher implements DataFetcher<DiscoveryMap> {

	@Autowired
	AuthManager authManager;

	@Autowired
	DiscoveryMapRepository discoveryMapRepository;

	@Override
	public DiscoveryMap get(DataFetchingEnvironment environment) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String id = environment.getArgument("id");

		Optional<DiscoveryMap> optionalDiscoveryMap = discoveryMapRepository.findByCompanyIdAndId(companyId, id,
				"sam_discovery_map");
		if (optionalDiscoveryMap.isPresent()) {
			return optionalDiscoveryMap.get();
		}

		return null;
	}

}