package com.ngdesk.sam.discoveryrules.dao;

import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.stereotype.Component;

@Component
public class BeforeSaveListener extends AbstractMongoEventListener<DiscoveryRule> {


}
