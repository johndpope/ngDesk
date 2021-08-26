package com.ngdesk;

import java.sql.Timestamp;
import java.util.Date;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class Authentication {
	private static final Logger logger = LoggerFactory.getLogger(Authentication.class);

	@Autowired
	private Environment env;

	public boolean isValidUser(String uuid) {

		logger.trace("Enter Authentication.isValidUser() uuid: " + uuid);

		try {
			Claims claims = Jwts.parser().setSigningKey(env.getProperty("jwt.secret").toString()).parseClaimsJws(uuid)
					.getBody();
			Date expirationDate = claims.getExpiration();
			Timestamp expirationTimestamp = new Timestamp(expirationDate.getTime());
			Timestamp currentTimestamp = new Timestamp(new Date().getTime());

			if (currentTimestamp.after(expirationTimestamp)) {
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.trace("Exit Authentication.isValidUser() uuid: " + uuid);
		return false;
	}

	public JSONObject getUserDetails(String uuid) {

		JSONObject userDetails = new JSONObject();
		try {
			logger.trace("Enter Authentication.getUserDetails() uuid: " + uuid);

			Claims body = Jwts.parser().setSigningKey(env.getProperty("jwt.secret").toString()).parseClaimsJws(uuid)
					.getBody();

			JSONObject claims = new JSONObject(body);
			JSONObject user = new JSONObject(claims.getString("USER"));

			userDetails.put("COMPANY_ID", claims.getString("COMPANY_ID"));
			userDetails.put("USER_ID", user.getString("DATA_ID"));
			userDetails.put("USERNAME", body.getSubject());
			userDetails.put("USER_UUID", user.getString("USER_UUID"));
			userDetails.put("COMPANY_SUBDOMAIN", claims.getString("SUBDOMAIN"));
			userDetails.put("LANGUAGE", user.getString("LANGUAGE"));
			userDetails.put("COMPANY_UUID", claims.getString("COMPANY_UUID"));
			userDetails.put("ROLE", user.getString("ROLE"));
			logger.trace("Exit Authentication.getUserDetails() uuid: " + uuid);
			return userDetails;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
