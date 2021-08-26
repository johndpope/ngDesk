package com.ngdesk;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthService {
	private static final Logger logger = LoggerFactory.getLogger(HealthService.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Environment env;
	
	@Autowired
	RedissonClient redisson;

	@GetMapping("/health")
	public ResponseEntity<Object> healthCheck() {

		try {

			if (!mongoTemplate.getDb().getName().equals("ngdesk")) {
				return new ResponseEntity<>("Mongo not available", HttpStatus.SERVICE_UNAVAILABLE);
			} else if (!redisson.getNodesGroup().pingAll()) {
				return new ResponseEntity<>("Redisson not available", HttpStatus.SERVICE_UNAVAILABLE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return new ResponseEntity<>("I'm healthy, Mongodb Hostname is: " + env.getProperty("spring.data.mongodb.host")
				+ " Redis is connected", HttpStatus.OK);
	}

}
