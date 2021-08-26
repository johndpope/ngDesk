package com.ngdesk.commons.managers;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.ngdesk.commons.models.User;

@FeignClient(name = "ngdesk-auth-v1")
public interface AuthProxy {
	
	@GetMapping("/user/details")
	User getUserDetails(@RequestHeader(value = "authentication_token") String authToken);
	
	@GetMapping("/user/details/internal")
	User getUserDetailsForInternalCalls(@RequestParam("user_uuid") String userUuid, @RequestParam("company_id") String companyId);
}
