package com.ngdesk.websocket.graphql.dao;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.ngdesk.websocket.report.dao.ReportInput;

@FeignClient(name = "ngdesk-graphql-service-v1")
public interface GraphqlProxy {

	@PostMapping("/reports/generate")
	public void reportGenerate(@RequestBody ReportInput reportInput,
			@RequestParam(value = "company_id", required = false) String companyId,
			@RequestParam(value = "user_uuid", required = false) String userUuid);

}
