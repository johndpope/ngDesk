package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.sam.discoveryrules.dao.DiscoveryRule;

public interface CustomDiscoveryRuleRepository {
	
	public Optional<DiscoveryRule> findDiscoveryRuleName(String name, String collection);
}
