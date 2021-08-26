package com.ngdesk.gateway;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WhitelistService {

	public Map<String, List<String>> pathsMap;

	@Autowired
	WhitelistService whitelistService;

	@PostConstruct
	public void init() {

		whitelistService.pathsMap = new HashMap<String, List<String>>();
		whitelistService.pathsMap.put("/company", Arrays.asList("POST"));
		whitelistService.pathsMap.put("/attachments", Arrays.asList("GET"));
		whitelistService.pathsMap.put("/company/onpremise", Arrays.asList("POST"));
		whitelistService.pathsMap.put("/company/onpremise/users", Arrays.asList("POST"));
		whitelistService.pathsMap.put("/zoom/authorized", Arrays.asList("GET"));
		whitelistService.pathsMap.put("/zoom/uninstall", Arrays.asList("POST"));
		whitelistService.pathsMap.put("/amazon/aws", Arrays.asList("POST"));
		whitelistService.pathsMap.put("/conference/xml", Arrays.asList("GET"));
		whitelistService.pathsMap.put("/signature_document", Arrays.asList("GET", "PUT"));
		whitelistService.pathsMap.put("/microsoft_team", Arrays.asList("POST", "GET"));
		whitelistService.pathsMap.put("/microsoft_teams/ticket_status", Arrays.asList("POST"));
		whitelistService.pathsMap.put("/noauth/query", Arrays.asList("POST"));
		whitelistService.pathsMap.put("/reports/download", Arrays.asList("GET"));
		whitelistService.pathsMap.put("/reports/schedules/download", Arrays.asList("GET"));

	}

}
