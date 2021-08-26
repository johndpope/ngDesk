package com.ngdesk;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.exceptions.ForbiddenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import redis.clients.jedis.Jedis;

@Component
public class Authentication {

	private final Logger log = LoggerFactory.getLogger(Authentication.class);

	
	@Value("${jwt.secret}")
    private String jwtSecretProp;
	
//	@Value("${redis.host}")
//    private String redisHostProp;
	
//	@Value("${redis.password}")
//    private String redisPasswordProp;

	@Autowired
	MongoTemplate mongoTemplate;

	public boolean isValidUser(String token) {
		log.trace("Enter Authentication.isValidUser() token: " + token);
		try {
			Claims claims = Jwts.parser().setSigningKey(jwtSecretProp).parseClaimsJws(token)
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
		return false;
	}

	public static Boolean oldMD5Authenticate(String input, String dbValue) {
		try {
			String hash = dbValue.split("\\|")[0];
			String salt = dbValue.split("\\|")[1];
			int saltLength = 16;

			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update((input + salt).getBytes(), 0, (input + salt).length());
			String userHash = new BigInteger(1, m.digest()).toString(saltLength);

			if (userHash.toString().equals(hash)) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}


//	public String getAuthTokenFromKey(String key) {
//		Jedis jedis = null;
//
//		try {
//			log.trace("Enter Authentication.getAuthTokenFromKey() key: " + key);
//
//			jedis = new Jedis(redisHostProp);
//			jedis.connect();
//			jedis.auth(redisPasswordProp);
//
//			String authToken = jedis.get(key);
//
//			log.trace("Exit Authentication.getAuthTokenFromKey() key: " + key);
//			return authToken;
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			jedis.close();
//		}
//		log.trace("Exit Authentication.getAuthTokenFromKey() key: " + key);
//		return null;
//	}

	public JSONObject getUserDetails(String uuid) {

		JSONObject userDetails = new JSONObject();
		try {
			log.trace("Enter Authentication.getUserDetails() uuid: " + uuid);

			Claims body = Jwts.parser().setSigningKey(jwtSecretProp).parseClaimsJws(uuid)
					.getBody();

			JSONObject claims = new JSONObject(body);
			JSONObject user = new JSONObject(claims.getString("USER"));
			
			userDetails.put("COMPANY_ID", claims.getString("COMPANY_ID"));
			userDetails.put("USER_ID", user.getString("DATA_ID"));
			userDetails.put("USERNAME", user.getString("EMAIL_ADDRESS"));
			userDetails.put("USER_UUID", user.getString("USER_UUID"));
			userDetails.put("COMPANY_SUBDOMAIN", claims.getString("SUBDOMAIN"));
			userDetails.put("LANGUAGE", user.getString("LANGUAGE"));
			userDetails.put("COMPANY_UUID", claims.getString("COMPANY_UUID"));
			userDetails.put("ROLE", user.getString("ROLE"));
			userDetails.put("CONTACT", user.getString("CONTACT"));

		} catch (Exception e) {
			e.printStackTrace();
			throw new ForbiddenException("FORBIDDEN");
		}
		log.trace("Exit Authentication.getUserDetails() uuid: " + uuid);
		return userDetails;
	}

	public String generateJwtToken(String username, String user, String companySubdomain, String companyUUID,
			String companyId) {
		try {
			log.trace("Enter Authentication.generateJwtToken()");

			Claims claims = Jwts.claims().setSubject(username);
			claims.setIssuedAt(new Timestamp(new Date().getTime()));

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.DAY_OF_WEEK, 30);
			claims.setExpiration(new Timestamp(calendar.getTimeInMillis()));

			claims.put("USER", user);
			claims.put("COMPANY_UUID", companyUUID);
			claims.put("SUBDOMAIN", companySubdomain);
			claims.put("COMPANY_ID", companyId);
			log.trace("Exit Authentication.generateJwtToken()");

			return Jwts.builder().setClaims(claims)
					.signWith(SignatureAlgorithm.HS512, jwtSecretProp).compact();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public String generateInfiniteJwtToken(String username, String user, String companySubdomain, String companyUUID,
			String companyId) {
		try {
			log.trace("Enter Authentication.generateJwtToken()");

			Claims claims = Jwts.claims().setSubject(username);
			claims.setIssuedAt(new Timestamp(new Date().getTime()));

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.YEAR, 1000);
			claims.setExpiration(new Timestamp(calendar.getTimeInMillis()));

			claims.put("USER", user);
			claims.put("COMPANY_UUID", companyUUID);
			claims.put("SUBDOMAIN", companySubdomain);
			claims.put("COMPANY_ID", companyId);
			log.trace("Exit Authentication.generateJwtToken()");

			return Jwts.builder().setClaims(claims)
					.signWith(SignatureAlgorithm.HS512, jwtSecretProp).compact();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean isValidToken(String token, String companySubdomain, boolean internal) {
		try {

			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			Document company = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", companySubdomain)).first();

			if (company != null) {

				String companyId = company.getObjectId("_id").toString();

				MongoCollection<Document> apiKeysCollection = mongoTemplate.getCollection("api_keys");
				Document key = apiKeysCollection
						.find(Filters.and(Filters.eq("COMPANY_ID", companyId), Filters.eq("TOKEN", token))).first();

				if (key != null) {
					if (key.getBoolean("REVOKED")) {
						return false;
					}
					if (key.getBoolean("INTERNAL") && !internal) {
						return false;
					}
				}

			} else {
				return false;
			}

			Claims claims = Jwts.parser().setSigningKey(jwtSecretProp).parseClaimsJws(token)
					.getBody();

			Date expirationDate = claims.getExpiration();
			Timestamp expirationTimestamp = new Timestamp(expirationDate.getTime());
			Timestamp currentTimestamp = new Timestamp(new Date().getTime());

			if (currentTimestamp.after(expirationTimestamp)) {
				return false;
			}

			return true;
		} catch (Exception e) {
//			e.printStackTrace();

		}
		return false;
	}

}
