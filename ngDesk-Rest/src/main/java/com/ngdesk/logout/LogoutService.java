package com.ngdesk.logout;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.Authentication;
import com.ngdesk.exceptions.BadRequestException;

@Component
@RestController
public class LogoutService {

	@Autowired
	private Authentication auth;

	private final Logger log = LoggerFactory.getLogger(LogoutService.class);

	@PostMapping("/users/logout")
	public ResponseEntity<Object> postLogout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		if (request.getHeader("authentication_token") != null) {
			uuid = request.getHeader("authentication_token").toString();
		}
		log.trace("Enter LogoutService.postLogout()");
		JSONObject result = new JSONObject();
		if (!auth.isValidToken(uuid, request.getAttribute("SUBDOMAIN").toString(), false)) {
			throw new BadRequestException("USER_NOT_LOGGED_IN");
		}
		log.trace("Exit LogoutService.postLogout()");
		result.put("MESSAGE", "Logout Success");
		return new ResponseEntity<Object>(result.toString(), HttpStatus.OK);
	}
}
