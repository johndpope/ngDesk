package com.ngdesk.companies;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@RestController
public class SocialSignInService {
	@Autowired
	private Authentication auth;

	@Autowired
	RoleService role;

	private final Logger log = LoggerFactory.getLogger(SocialSignInService.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@GetMapping("/companies/security/social_sign_in")
	public SocialSignIn getSocialSignInSettings(HttpServletRequest request) {
		try {
			log.trace("Enter SocialSignInService.getSocialSignInSettings()");

			String subdomain = request.getAttribute("SUBDOMAIN").toString();

			SocialSignIn settings = new SocialSignIn();
			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
			Document company = collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
			if (company != null) {
				Document socialSignIn = (Document) company.get("SOCIAL_SIGN_IN");
				settings.setEnableFacebook(socialSignIn.getBoolean("ENABLE_FACEBOOK"));
				settings.setEnableGoogle(socialSignIn.getBoolean("ENABLE_GOOGLE"));
				settings.setEnableMicrosoft(socialSignIn.getBoolean("ENABLE_MICROSOFT"));
				settings.setEnableTwitter(socialSignIn.getBoolean("ENABLE_TWITTER"));

			} else {
				throw new ForbiddenException("COMPANY_DOES_NOT_EXIST");
			}
			log.trace("Exit SocialSignInService.getSocialSignInSettings()");
			return settings;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/companies/security/social_sign_in")
	public ResponseEntity<Object> putSocialSignInSettings(HttpServletRequest request,@RequestParam(value="authentication_token",required=false) String uuid,
			@RequestBody SocialSignIn settings) {
		
		try {
			
			log.trace("Enter SocialSignInService.putSocialSignInSettings()");
			
			
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
		
			JSONObject user = auth.getUserDetails(uuid);
			
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
			
			String json = new ObjectMapper().writeValueAsString(settings);
			collection.findOneAndUpdate(Filters.eq("_id", new ObjectId(companyId)),
					Updates.set("SOCIAL_SIGN_IN", Document.parse(json)));

			log.trace("Exit SocialSignInService.getSocialSignInSettings()");
			return new ResponseEntity<Object>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");

	}
}
