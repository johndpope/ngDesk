package com.ngdesk.services;

import java.util.Map;

import org.json.JSONObject;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.UnauthorizedErrorException;

@RestController
@Component
public class UserService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Environment env;

	@Autowired
	private Authentication auth;
	
	@Autowired
	Global global;
	
	@Autowired
	RedissonClient redisson;

	private final Logger log = LoggerFactory.getLogger(UserService.class);

	@GetMapping("/users")
	public ResponseEntity<Object> getUsers(@RequestParam("authentication_token") String uuid) {

		JSONObject resultObj = new JSONObject();
		try {
			log.trace("Enter UserService.getUsers()");

			JSONObject user = auth.getUserDetails(uuid);

			if (user.has("COMPANY_ID")) {
				String subdomain = user.getString("COMPANY_SUBDOMAIN");
				String userId = user.getString("USER_ID");
				
				RMap<String, Map<String, Map<String, Object>>> companiesMap = redisson.getMap("companiesUsers");
				
				if (companiesMap.containsKey(subdomain)) {
					Map<String, Map<String, Object>> usersMap = companiesMap.get(subdomain);
					
					if (usersMap.containsKey(userId)) {
						Map<String, Object> uMap = usersMap.get(userId);
						resultObj = new JSONObject(new ObjectMapper().writeValueAsString(uMap));
						
						return new ResponseEntity<>(resultObj.toString(), global.postHeaders, HttpStatus.OK);
					}
				}
				
				log.trace("Exit UserService.getUsers() ");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}
