package com.ngdesk.referrals;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.validator.routines.EmailValidator;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.channels.chat.ChatService;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class ReferralService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;
	
	@Value("${email.host}")
	private String host;

	private final Logger log = LoggerFactory.getLogger(ChatService.class);

	@GetMapping("/companies/referrals")
	public ResponseEntity<Object> getReferrals(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {

		try {
			JSONObject result = new JSONObject();
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyUuid = user.getString("COMPANY_UUID");
			String userUuid = user.getString("USER_UUID");

			int currentMonth = 0;
			int allTime = 0;

			MongoCollection<Document> companyCollection = mongoTemplate.getCollection("companies");
			List<Document> companyList = companyCollection.find().into(new ArrayList<>());

			Date date = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int month = cal.get(Calendar.MONTH) + 1;

			for (Document company : companyList) {
				String dateCreated = company.getString("DATE_CREATED");
				DateFormat outputFormatter = new SimpleDateFormat("yyyy-MM-dd");
				Date signedupDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateCreated);
				cal.setTime(signedupDate);

				int monthCreated = cal.get(Calendar.MONTH) + 1;
				if (company.get("REFERED_BY") != null) {
					Document referral = (Document) company.get("REFERED_BY");
					if (userUuid.equals(referral.getString("USER_UUID"))
							&& companyUuid.equals(referral.getString("COMPANY_UUID"))) {
						if (month == monthCreated) {
							currentMonth++;
							allTime++;
						} else {
							allTime++;
						}

					}

				}
				result.put("CURRENT_MONTH", currentMonth);
				result.put("ALL_TIME", allTime);
			}
			return new ResponseEntity<Object>(result.toString(), HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("company/refer/email")
	public ResponseEntity<Object> referralEmail(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestBody List<String> emailIds) {
		try {
			log.trace("Enter ChatService.emailToDevelopers(), ChannelName: ");

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			if (emailIds.isEmpty()) {
				throw new BadRequestException("EMAIL_INVALID");
			}

			for (String emailId : emailIds) {
				if (!EmailValidator.getInstance().isValid(emailId)) {
					throw new BadRequestException("EMAIL_INVALID");
				}
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String subdomain = user.getString("COMPANY_SUBDOMAIN");
			String companyUuid = user.getString("COMPANY_UUID");
			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
			Document companyDocument = collection.find(Filters.eq("_id", new ObjectId(companyId))).first();
			String companyName = companyDocument.getString("COMPANY_NAME");
			String userUuid = user.getString("USER_UUID");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String body = global.getFile("CompanyReferralEmail.html");
			body = body.replace("COMPANY_UUID", companyUuid);
			body = body.replace("USER_UUID", userUuid);

			String emailFrom = "support@" + subdomain + ".ngdesk.com";
			String emailSubject = "Get ngDesk to improve your business performance";

			for (String emailTo : emailIds) {

				SendEmail sendEmail = new SendEmail(emailTo, emailFrom, emailSubject, body, host);
				sendEmail.sendEmail();
			}
			log.trace("Exit ChatService.emailToDevelopers(), ChannelName: ");
			return new ResponseEntity<Object>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

}
