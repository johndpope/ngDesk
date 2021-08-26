package com.ngdesk.modules;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.wrapper.Wrapper;

@Component
@RestController
public class AutocompleteService {

	@Autowired
	Wrapper wrapper;

	@Autowired
	Authentication auth;

	@Autowired
	Global global;

	private final Logger log = LoggerFactory.getLogger(AutocompleteService.class);

	@GetMapping(value = "/autocomplete")
	public ResponseEntity<Object> getDataListByModuleId(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "module_id", required = true) String moduleId,
			@RequestParam(value = "q", required = true) String q) {

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");

			List<String> responses = wrapper.autocomplete(companyId, q, userId, moduleId);

			JSONObject result = new JSONObject();
			result.put("SUGGESTIONS", responses);
			result.put("TOTAL_COUNT", responses.size());

			return new ResponseEntity<>(result.toString(), global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
