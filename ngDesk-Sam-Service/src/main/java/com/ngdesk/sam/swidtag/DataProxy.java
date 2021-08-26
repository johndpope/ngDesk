package com.ngdesk.sam.swidtag;

import java.util.HashMap;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@FeignClient(name = "ngdesk-data-service-v1")
public interface DataProxy {
	@PostMapping("/modules/{module_id}/probes/data")
	@Operation(summary = "Post Module Entry", description = "Post a single entry for a module from probe")
	public void postModuleEntry(@RequestBody HashMap<String, Object> entry,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
			@Parameter(description = "Company ID", required = false) @RequestParam(value = "company_id", required = false) String companyId,
			@Parameter(description = "User UUID", required = false) @RequestParam(value = "user_uuid", required = false) String userUuid);

}
