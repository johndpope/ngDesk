package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.sam.discoveryrules.dao.DiscoveryRule;

@Repository
public interface DiscoveryRuleRepository extends CustomDiscoveryRuleRepository, CustomNgdeskRepository<DiscoveryRule, String> {
	
}
