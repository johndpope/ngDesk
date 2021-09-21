package com.ngdesk.websocket.channels.chat.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ngdesk-data-service-v1")
public interface DataProxy {

	@PostMapping("/modules/{module_id}/data")
	public Map<String, Object> postModuleEntry(@RequestBody HashMap<String, Object> entry,
			@PathVariable("module_id") String moduleId,
			@RequestParam(value = "is_trigger", required = false) boolean isTrigger,
			@RequestParam(value = "company_id", required = false) String companyId,
			@RequestParam(value = "user_uuid", required = false) String userUuid);	
	
}
