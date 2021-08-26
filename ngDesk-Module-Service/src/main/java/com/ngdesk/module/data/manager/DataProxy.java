package com.ngdesk.module.data.manager;

import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.ngdesk.module.dao.CommonRequest;

@FeignClient(name = "ngdesk-data-service-v1")
public interface DataProxy {

	@PostMapping("/default/entries")
	public void postDefaultModuleEntry(@RequestHeader("internal_call") String internalCall,
			@RequestBody CommonRequest request);

	@PostMapping("/modules/{module_id}/data")
	public Map<String, Object> postModuleEntry(@RequestBody Map<String, Object> entry,
			@PathVariable("module_id") String moduleId,
			@RequestParam(value = "is_trigger", required = false) boolean isTrigger,
			@RequestParam(value = "company_id", required = false) String companyId,
			@RequestParam(value = "user_uuid", required = false) String userUuid);
}
