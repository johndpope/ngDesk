package com.ngdesk.integration.amazom.aws.dao;

import java.util.HashMap;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@FeignClient(name = "ngdesk-data-service-v1")
public interface DataProxy {

	@PostMapping("/modules/{module_id}/data")
	@Operation(summary = "Post Module Entry", description = "Post a single entry for a module")
	public Map<String, Object> postModuleEntry(@RequestBody HashMap<String, Object> entry,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
			@Parameter(description = "Is Trigger", required = false) @RequestParam(value = "is_trigger", required = false) boolean isTrigger,
			@Parameter(description = "Company ID", required = false) @RequestParam(value = "company_id", required = false) String companyId,
			@Parameter(description = "User UUID", required = false) @RequestParam(value = "user_uuid", required = false) String userUuid);

	@PutMapping("/modules/{module_id}/data")
	@Operation(summary = "Put Module Entry", description = "Edit a single entry of a module")
	public Map<String, Object> putModuleEntry(@RequestBody HashMap<String, Object> entry,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
			@Parameter(description = "Trigger", required = false) @RequestParam(value = "is_trigger", required = false) boolean isTrigger,
			@Parameter(description = "Company ID", required = false) @RequestParam(value = "company_id", required = false) String companyId,
			@Parameter(description = "User UUID", required = false) @RequestParam(value = "user_uuid", required = false) String userUuid,
			@Parameter(description = "Probe", required = false) @RequestParam(value = "is_probe", required = false) boolean isProbe);

}
