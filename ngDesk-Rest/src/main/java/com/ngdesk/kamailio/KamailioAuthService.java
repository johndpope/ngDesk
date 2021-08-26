package com.ngdesk.kamailio;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.UnauthorizedException;

@Component
@RestController
public class KamailioAuthService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication authentication;

	@Autowired
	private Global global;

	private Logger log = LoggerFactory.getLogger(KamailioAuthService.class);

	@GetMapping("/kamailio/authenticate")
	public ResponseEntity<Object> kamailioAuth(@RequestParam("f_uri") String fUri) {
		log.trace("Enter KamailioAuthService.kamailioAuth");

		try {
			fUri = java.net.URLDecoder.decode(fUri, "UTF-8");

			Pattern pattern = Pattern.compile("sip:(.*)@(.*)");
			Matcher matcher = pattern.matcher(fUri);

			if (matcher.find()) {
				String email = matcher.group(1);
				String domain = matcher.group(2);
				String subdomain = domain.split("\\.")[0];

				log.trace("Email Address: " + email);
				log.trace("Domain: " + domain);
				log.trace("SubDomain: " + subdomain);

				MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
				Document company = collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();

				if (company != null) {

					String companyId = company.getObjectId("_id").toString();

					String collectionName = "Users_" + companyId;
					MongoCollection<Document> usersCollection = mongoTemplate.getCollection(collectionName);

					Document userDocument = usersCollection.find(Filters.eq("EMAIL_ADDRESS", email)).first();

					if (userDocument != null) {
						String password = userDocument.getString("PASSWORD");
						log.trace("Exit KamailioAuthService.kamailioAuth at: " + new Timestamp(new Date().getTime()));
						return new ResponseEntity<>(password, HttpStatus.OK);
					} else {
						throw new BadRequestException("USER_DOES_NOT_EXIST");
					}

				} else {
					throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
				}

			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		throw new UnauthorizedException("INVALID_CREDENTIALS");
	}

//	@GetMapping("/kamailio/authenticate/auth_token")
//	public ResponseEntity<Object> kamailioAuth2(@RequestParam("f_uri") String fUri) {
//		log.trace("Enter KamailioAuthService.kamailioAuth2");
//
//		try {
//			fUri = java.net.URLDecoder.decode(fUri, "UTF-8");
//
//			Pattern pattern = Pattern.compile("sip:(.*)@(.*)");
//			Matcher matcher = pattern.matcher(fUri);
//
//			if (matcher.find()) {
//				String email = matcher.group(1);
//				String domain = matcher.group(2);
//				String subdomain = domain.split("\\.")[0];
//
//				log.trace("Email Address: " + email);
//				log.trace("Domain: " + domain);
//				log.trace("SubDomain: " + subdomain);
//
//				MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
//				Document company = collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
//
//				if (company != null) {
//
//					String companyId = company.getObjectId("_id").toString();
//
//					String collectionName = "Users_" + companyId;
//					MongoCollection<Document> usersCollection = mongoTemplate.getCollection(collectionName);
//
//					Document userDocument = usersCollection.find(Filters.eq("EMAIL_ADDRESS", email)).first();
//
//					if (userDocument != null) {
//
//						email = email.replaceAll("@", "*");
//						String userId = userDocument.getObjectId("_id").toString();
//						String key = userId + "_" + subdomain;
//
//						String authToken = authentication.getAuthTokenFromKey(key);
//
//						if (authToken != null) {
//
//							String passwordToHash = email + "*" + subdomain + ":" + subdomain + ".ngdesk.com:"
//									+ authToken;
//							String hashedPassword = global.passwordHash(passwordToHash);
//							log.trace("Exit KamailioAuthService.kamailioAuth2");
//							return new ResponseEntity<>(hashedPassword, HttpStatus.OK);
//						} else {
//							throw new UnauthorizedException("INVALID_CREDENTIALS");
//						}
//					} else {
//						throw new BadRequestException("USER_DOES_NOT_EXIST");
//					}
//
//				} else {
//					throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
//				}
//			}
//
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
//
//		throw new UnauthorizedException("INVALID_CREDENTIALS");
//	}
}
