package com.ngdesk.logging;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.Authentication;

@Component
@RestController
public class LoggingService {

	@Autowired
	private Authentication auth;

	private final Logger log = LoggerFactory.getLogger(LoggingService.class);

	@PostMapping("/log")
	public ResponseEntity<Object> log(HttpServletRequest request, @RequestBody FrontEndLog frontEndLog,
			@RequestParam(value = "authentication_token", required = false) String uuid) {

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject logMessageJson = new JSONObject();
			if (uuid != null && auth.isValidUser(uuid)) {
				JSONObject user = auth.getUserDetails(uuid);
				logMessageJson.put("companyId", user.getString("COMPANY_ID"));
				logMessageJson.put("userId", user.getString("USER_ID"));
				logMessageJson.put("username", user.getString("USERNAME"));
				logMessageJson.put("userUuid", user.getString("USER_UUID"));
				logMessageJson.put("companySubdomain", user.getString("COMPANY_SUBDOMAIN"));
				logMessageJson.put("role", user.getString("ROLE"));
			}
			logMessageJson.put("logMessage", frontEndLog.getMessage());
			String logMessage = logMessageJson.toString();

			if (frontEndLog.getLevel() == 6) {
				log.error(logMessage);
			} else if (frontEndLog.getLevel() == 5) {
				log.error(logMessage);
			} else if (frontEndLog.getLevel() == 4) {
				log.warn(logMessage);
			} else if (frontEndLog.getLevel() == 3) {
				log.info(logMessage);
			} else if (frontEndLog.getLevel() == 2) {
				log.info(logMessage);
			} else if (frontEndLog.getLevel() == 1) {
				log.debug(logMessage);
			} else if (frontEndLog.getLevel() == 0) {
				log.trace(logMessage);
			}
			// add if statment based on level

			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

	}

}
