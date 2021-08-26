package com.ngdesk.login;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

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
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.api.impl.GoogleTemplate;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.msgraph.api.Microsoft;
import com.microsoft.azure.msgraph.api.impl.MicrosoftTemplate;
import com.microsoft.azure.msgraph.connect.MicrosoftConnectionFactory;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.exceptions.UnauthorizedException;
import com.ngdesk.users.InviteUser;
import com.ngdesk.users.InviteUserService;
import com.ngdesk.users.UserDAO;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

@Component
@RestController
public class LoginService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private Global global;

	@Autowired
	private UserDAO userDao;

	@Value("${spring.social.facebook.app-id}")
	private String facebookAppId;

	@Value("${spring.social.facebook.app-secret}")
	private String facebookSecret;

	@Value("${spring.social.google.app-id}")
	private String googleAppId;

	@Value("${spring.social.google.app-secret}")
	private String googleSecret;

	@Value("${spring.social.twitter.appId}")
	private String twitterAppId;

	@Value("${spring.social.twitter.appSecret}")
	private String twitterSecret;

	@Value("${spring.social.twitter.access.token}")
	private String accessToken;

	@Value("${spring.social.twitter.access.token.secret}")
	private String accessTokenSecret;

	@Value("${spring.social.microsoft.app-id}")
	private String microsoftAppId;

	@Value("${spring.social.microsoft.app-secret}")
	private String microsoftSecret;

	@Value("${spring.social.facebook.url}")
	private String facebookbaseUrl;

	@Value("${spring.social.google.url}")
	private String googlebaseUrl;

	@Value("${spring.social.microsoft.url}")
	private String microsoftbaseUrl;

	@Autowired
	InviteUserService inviteUserService;

	private final Logger log = LoggerFactory.getLogger(LoginService.class);

	@PostMapping("/users/login")
	public ResponseEntity<Object> login(HttpServletRequest request,
			@RequestParam(value = "sam", required = false) String sam, @Valid @RequestBody Login login, Errors error) {
		try {
			log.trace("Enter LoginService.login()");
			// ACCESS MONGO

			String collectionName = "companies";
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			// GET LOGIN DETAILS
			String username = login.getEmailAddress().toLowerCase();
			String subdomain = login.getCompanySubdomain();

			if (login.getCompanySubdomain() == null) {
				subdomain = request.getAttribute("SUBDOMAIN").toString();
			}

			Document companyDocument = collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();

			String companyId = null;
			String dbPassword = null;
			String inputPassword = null;
			String uuid = null;

			// CHECK TYPE OF LOGIN
			if (companyDocument != null) {

				subdomain = companyDocument.getString("COMPANY_SUBDOMAIN");
				login.setCompanySubdomain(companyDocument.getString("COMPANY_SUBDOMAIN"));

				companyId = companyDocument.getObjectId("_id").toString();
				String securitycollectionName = "companies_security";
				MongoCollection<Document> securitycollection = mongoTemplate.getCollection(securitycollectionName);
				Document securityDocument = securitycollection.find(Filters.eq("COMPANY_ID", companyId)).first();
				int maxLoginRetries = securityDocument.getInteger("MAX_LOGIN_RETRIES");
				Document userDocument = userDao.getUserByEmail(username, companyId);
				Document document = null;

				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);

				if (userDocument != null) {
					inputPassword = login.getEmailAddress().toLowerCase().replaceAll("@", "*") + "*" + subdomain + ":"
							+ subdomain + ".ngdesk.com:" + login.getPassword();
					uuid = userDocument.getString("USER_UUID");
					document = userDocument;
				} else {
					throw new UnauthorizedException("INVALID_LOGIN_CREDENTIALS");
				}
				if (document.getString("PASSWORD") == null || document.getString("PASSWORD").equals("")) {
					throw new BadRequestException("CREATE_PASSWORD");
				}
				int noOfAttempts = document.getInteger("LOGIN_ATTEMPTS");
				if (noOfAttempts == maxLoginRetries) {
					throw new UnauthorizedException("MAX_ATTEMPTS_EXCEEDED");
				}
				// AUTHENTICATE THE CREDENTIALS

				boolean authorized = false;
				dbPassword = userDocument.getString("PASSWORD");
				// MIGRATED USER
				if (dbPassword.contains("V1_PASSWORD")) {
					String oldDbPassword = dbPassword.split("\\|V1_PASSWORD")[0];
					if (auth.oldMD5Authenticate(login.getPassword(), oldDbPassword)) {
						String email = login.getEmailAddress().toLowerCase().replaceAll("@", "*") + "*" + subdomain;
						String ha1Password = email + ":" + subdomain + ".ngdesk.com:" + login.getPassword();
						String hashedPassword = global.passwordHash(ha1Password);
						usersCollection.updateOne(Filters.eq("EMAIL_ADDRESS", login.getEmailAddress()),
								Updates.set("PASSWORD", hashedPassword));
						authorized = true;
					}
				} else {
					if (global.MD5Authenticate(inputPassword, dbPassword)) {
						authorized = true;
					}
					if (subdomain.equalsIgnoreCase("subscribeit") && !authorized) {
						inputPassword = login.getEmailAddress().toLowerCase().replaceAll("@", "*") + "*" + "bluemsp-new"
								+ ":" + "bluemsp-new" + ".ngdesk.com:" + login.getPassword();

						if (global.MD5Authenticate(inputPassword, dbPassword)) {
							authorized = true;
						}
					}
				}

				if (authorized) {
					String userId = document.getObjectId("_id").toString();
					document.remove("PASSWORD");
					document.remove("_id");
					document.put("DATA_ID", userId);
					document.remove("META_DATA");

					MongoCollection<Document> contactsCollection = mongoTemplate.getCollection("Contacts_" + companyId);
					Document contact = contactsCollection
							.find(Filters.eq("_id", new ObjectId(document.getString("CONTACT")))).first();

					document.put("FIRST_NAME", contact.getString("FIRST_NAME"));
					document.put("LAST_NAME", contact.getString("LAST_NAME"));

					String jwtToken = null;
					if (sam != null && sam.equals("a396bd3f-162d-4bc1-b9dd-039aa9d390f2")) {

						MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
						Document role = rolesCollection
								.find(Filters.eq("_id", new ObjectId(document.getString("ROLE")))).first();

						if (!role.getString("NAME").equals("SystemAdmin")) {
							throw new ForbiddenException("FORBIDDEN");
						}

						jwtToken = auth.generateInfiniteJwtToken(username, document.toJson(), subdomain,
								companyDocument.getString("COMPANY_UUID"), companyId);
					} else {
						jwtToken = auth.generateJwtToken(username, document.toJson(), subdomain,
								companyDocument.getString("COMPANY_UUID"), companyId);
					}
					JSONObject result = new JSONObject();
					result.put("AUTHENTICATION_TOKEN", jwtToken);
					usersCollection.updateOne(Filters.eq("EMAIL_ADDRESS", username),
							Updates.combine(Updates.set("LOGIN_ATTEMPTS", 0), Updates.set("LAST_SEEN", new Date())));
					log.trace("Exit LoginService.login()");
					return new ResponseEntity<>(result.toString(), Global.postHeaders, HttpStatus.OK);

				} else {
					noOfAttempts = noOfAttempts + 1;
					updateNoOfAttempts(companyId, document, noOfAttempts);
					throw new UnauthorizedException("INVALID_LOGIN_CREDENTIALS");
				}
			} else {
				throw new ForbiddenException("COMPANY_DOES_NOT_EXIST");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@GetMapping("/users/sso")
	public ResponseEntity<Object> haloocomLogin(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("access_key") String accessKey, @RequestParam("email") String emailAddress) {
		try {
			String subdomain = request.getAttribute("SUBDOMAIN").toString();

			if (!subdomain.equalsIgnoreCase("haloocom")) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!accessKey.equals("1e0c51cb-046c-4400-8f75-02ba66236930")) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
			Document companyDoc = collection.find(Filters.eq("COMPANY_SUBDOMAIN", "haloocom")).first();

			if (companyDoc == null) {
				throw new ForbiddenException("UNAUTHORIZED");
			}

			String companyId = companyDoc.getObjectId("_id").toString();
			Document userDocument = userDao.getUserByEmail(emailAddress, companyId);
			if (userDocument == null) {
				throw new UnauthorizedException("UNAUTHORIZED");
			}

			String userId = userDocument.getObjectId("_id").toString();
			userDocument.remove("PASSWORD");
			userDocument.remove("_id");
			userDocument.put("DATA_ID", userId);
			userDocument.remove("META_DATA");

			String jwtToken = auth.generateJwtToken(emailAddress, userDocument.toJson(), subdomain,
					companyDoc.getString("COMPANY_UUID"), companyId);

			response.sendRedirect("https://haloocom.ngdesk.com/login?authentication_token=" + jwtToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/lead")
	public ResponseEntity<Object> haloocomLeadGenerate(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("customer_number") String phoneNumber, @RequestParam("email") String emailAddress) {
		try {
			String subdomain = request.getAttribute("SUBDOMAIN").toString();
			if (!subdomain.equalsIgnoreCase("haloocom")) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("companies");
			Document companyDoc = collection.find(Filters.eq("COMPANY_SUBDOMAIN", "haloocom")).first();

			if (companyDoc == null) {
				throw new ForbiddenException("UNAUTHORIZED");
			}

			String companyId = companyDoc.getObjectId("_id").toString();

			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
			Document userDocument = usersCollection.find(Filters.eq("EMAIL_ADDRESS", emailAddress)).first();
			if (userDocument == null) {
				throw new UnauthorizedException("UNAUTHORIZED");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document userModule = modulesCollection.find(Filters.eq("NAME", "Users")).first();
			String userModuleId = userModule.getObjectId("_id").toString();

			Document ticketModule = modulesCollection.find(Filters.eq("NAME", "Tickets")).first();
			String ticketModuleId = ticketModule.getObjectId("_id").toString();

			String userId = userDocument.getObjectId("_id").toString();
			userDocument.remove("PASSWORD");
			userDocument.remove("_id");
			userDocument.put("DATA_ID", userId);
			userDocument.remove("META_DATA");

			String jwtToken = auth.generateJwtToken(emailAddress, userDocument.toJson(), subdomain,
					companyDoc.getString("COMPANY_UUID"), companyId);

			Document existingUser = usersCollection.find(Filters.eq("PHONE_NUMBER.PHONE_NUMBER", phoneNumber)).first();
			if (existingUser != null) {
				response.sendRedirect("https://haloocom.ngdesk.com/render/" + userModuleId + "/edit/"
						+ existingUser.getObjectId("_id").toString() + "?authentication_token=" + jwtToken);
			} else {
				response.sendRedirect("https://haloocom.ngdesk.com/render/" + ticketModuleId
						+ "/create/new?phone_number=" + phoneNumber + "&authentication_token=" + jwtToken);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("users/login/facebook/redirect")
	public ResponseEntity<Object> facebookRedirect(@RequestParam(value = "state", required = false) String subdomain,
			@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "error_description", required = false) String errorDescription,
			@RequestParam(value = "error_reason", required = false) String errorReason, HttpServletResponse response) {

		log.trace("Enter LoginService.facebookRedirect(): ");

		try {
			if (error != null) {
				throw new BadRequestException("FACEBOOK_LOGIN_FAILED");
			} else {
				FacebookConnectionFactory connectionFactory = new FacebookConnectionFactory(facebookAppId,
						facebookSecret);
				AccessGrant accessGrant = connectionFactory.getOAuthOperations().exchangeForAccess(code,
						facebookbaseUrl + "/users/login/facebook/redirect", null);
				String accessToken = accessGrant.getAccessToken();
				Facebook facebook = new FacebookTemplate(accessToken);

				String[] fields = { "email", "first_name", "last_name" };
				User profile = facebook.fetchObject("me", User.class, fields);

				String emailAddress = profile.getEmail();
				String firstName = profile.getFirstName();
				String lastName = profile.getLastName();

				MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
				Document company = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();

				if (company == null) {
					throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
				}

				String companyId = company.getObjectId("_id").toString();
				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);

				Document userDocument = usersCollection.find(Filters.eq("EMAIL_ADDRESS", emailAddress))
						.projection(Projections.exclude("PASSWORD")).first();
				if (userDocument == null) {

					// CREATE USER
					MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
					Document customerRole = rolesCollection.find(Filters.eq("NAME", "Customers")).first();
					String customerRoleId = customerRole.getObjectId("_id").toString();

					InviteUser user = new InviteUser();
					user.setEmailAddress(emailAddress);
					user.setFirstName(firstName);
					user.setLastName(lastName);
					user.setRole(customerRoleId);

					Document systemUser = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "system@ngdesk.com"))
							.first();

					userDocument = inviteUserService.createUsers(user, companyId,
							company.getString("COMPANY_SUBDOMAIN"), systemUser.getObjectId("_id").toString());

					if (userDocument == null) {
						throw new BadRequestException("USER_CREATION_FAILED");
					}

				} else {
					if (userDocument.getBoolean("DELETED")) {
						usersCollection.updateOne(Filters.eq("EMAIL_ADDRESS", emailAddress),
								Updates.set("DELETED", false));
					}
				}

				usersCollection.updateOne(Filters.eq("EMAIL_ADDRESS", emailAddress),
						Updates.combine(Updates.set("LOGIN_ATTEMPTS", 0), Updates.set("LAST_SEEN", new Date())));

				String userId = userDocument.getObjectId("_id").toString();
				userDocument.remove("_id");
				userDocument.put("DATA_ID", userId);
				userDocument.remove("META_DATA");
				userDocument.remove("PASSWORD");

				MongoCollection<Document> contactsCollection = mongoTemplate.getCollection("Contacts_" + companyId);
				Document contact = contactsCollection
						.find(Filters.eq("_id", new ObjectId(userDocument.getString("CONTACT").toString()))).first();
				userDocument.put("FIRST_NAME", contact.getString("FIRST_NAME"));
				userDocument.put("LAST_NAME", contact.getString("LAST_NAME"));

				String jwtToken = auth.generateJwtToken(emailAddress, userDocument.toJson(), subdomain,
						company.getString("COMPANY_UUID"), companyId);

				response.sendRedirect("https://" + subdomain + ".ngdesk.com/login?authentication_token=" + jwtToken);
				return new ResponseEntity<>(HttpStatus.FOUND);

			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/users/login/facebook")
	public RedirectView loginWithFacebook(HttpServletRequest request) {
		try {
			log.trace("Enter LoginService.loginWithFacebook() ");

			RedirectView redirectView = new RedirectView();

			String subdomain = request.getAttribute("SUBDOMAIN").toString();

			FacebookConnectionFactory connectionFactory = new FacebookConnectionFactory(facebookAppId, facebookSecret);
			OAuth2Operations oauthOperations = connectionFactory.getOAuthOperations();

			OAuth2Parameters params = new OAuth2Parameters();
			params.setRedirectUri(facebookbaseUrl + "/users/login/facebook/redirect");
			params.setScope("public_profile,email");
			params.setState(subdomain);

			redirectView.setUrl(oauthOperations.buildAuthorizeUrl(params));
			log.trace("Exit LoginService.loginWithFacebook()");

			return redirectView;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/users/login/google/redirect")
	public ResponseEntity<Object> googleRedirect(@RequestParam(value = "state", required = false) String subdomain,
			@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "error_description", required = false) String errorDescription,
			@RequestParam(value = "error_reason", required = false) String errorReason,
			@RequestParam(value = "scope", required = false) String scope,
			@RequestParam(value = "authuser", required = false) String authUser,
			@RequestParam(value = "hd", required = false) String hd,
			@RequestParam(value = "session_state", required = false) String sessionState,
			@RequestParam(value = "prompt", required = false) String prompt, HttpServletResponse response) {

		log.trace("Enter LoginService.googleRedirect(): ");

		try {
			if (error != null) {
				throw new BadRequestException("GOOGLE_LOGIN_FAILED");
			} else {
				GoogleConnectionFactory connectionFactory = new GoogleConnectionFactory(googleAppId, googleSecret);

				AccessGrant accessGrant = connectionFactory.getOAuthOperations().exchangeForAccess(code,
						googlebaseUrl + "/users/login/google/redirect", null);
				String accessToken = accessGrant.getAccessToken();
				Google google = new GoogleTemplate(accessToken);

				String emailAddress = google.userOperations().getUserInfo().getEmail();
				String firstName = google.userOperations().getUserInfo().getFirstName();
				String lastName = google.userOperations().getUserInfo().getLastName();

				MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
				Document company = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();

				if (company == null) {
					throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
				}

				String companyId = company.getObjectId("_id").toString();
				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);

				Document userDocument = usersCollection.find(Filters.eq("EMAIL_ADDRESS", emailAddress))
						.projection(Projections.exclude("PASSWORD", "META_DATA")).first();
				if (userDocument == null) {

					// CREATE USER
					MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
					Document customerRole = rolesCollection.find(Filters.eq("NAME", "Customers")).first();
					String customerRoleId = customerRole.getObjectId("_id").toString();

					InviteUser user = new InviteUser();
					user.setEmailAddress(emailAddress);
					user.setFirstName(firstName);
					user.setLastName(lastName);
					user.setRole(customerRoleId);

					Document systemUser = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "system@ngdesk.com"))
							.first();

					userDocument = inviteUserService.createUsers(user, companyId,
							company.getString("COMPANY_SUBDOMAIN"), systemUser.getObjectId("_id").toString());

					if (userDocument == null) {
						throw new BadRequestException("USER_CREATION_FAILED");
					}

				} else {
					if (userDocument.getBoolean("DELETED")) {
						usersCollection.updateOne(Filters.eq("EMAIL_ADDRESS", emailAddress),
								Updates.set("DELETED", false));
					}
				}

				usersCollection.updateOne(Filters.eq("EMAIL_ADDRESS", emailAddress),
						Updates.combine(Updates.set("LOGIN_ATTEMPTS", 0), Updates.set("LAST_SEEN", new Date())));

				String userId = userDocument.getObjectId("_id").toString();
				userDocument.remove("_id");
				userDocument.put("DATA_ID", userId);
				userDocument.remove("META_DATA");

				MongoCollection<Document> contactsCollection = mongoTemplate.getCollection("Contacts_" + companyId);
				Document contact = contactsCollection
						.find(Filters.eq("_id", new ObjectId(userDocument.getString("CONTACT").toString()))).first();

				userDocument.put("FIRST_NAME", contact.getString("FIRST_NAME"));
				userDocument.put("LAST_NAME", contact.getString("LAST_NAME"));

				String jwtToken = auth.generateJwtToken(emailAddress, userDocument.toJson(), subdomain,
						company.getString("COMPANY_UUID"), companyId);

				response.sendRedirect("https://" + subdomain + ".ngdesk.com/login?authentication_token=" + jwtToken);
				return new ResponseEntity<>(HttpStatus.FOUND);

			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/users/login/google")
	public RedirectView loginWithGoogle(HttpServletRequest request) {
		try {
			log.trace("Enter LoginService.loginWithGoogle() ");

			RedirectView redirectView = new RedirectView();

			String subdomain = request.getAttribute("SUBDOMAIN").toString();

			GoogleConnectionFactory connectionFactory = new GoogleConnectionFactory(googleAppId, googleSecret);
			OAuth2Operations oauthOperations = connectionFactory.getOAuthOperations();

			OAuth2Parameters params = new OAuth2Parameters();
			params.setRedirectUri(googlebaseUrl + "/users/login/google/redirect");
			params.setScope("profile email");
			params.setState(subdomain);
			redirectView.setUrl(oauthOperations.buildAuthorizeUrl(params));
			log.trace("Exit LoginService.loginWithGoogle()");

			return redirectView;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/users/login/twitter/redirect")
	public ResponseEntity<Object> twitterRedirect(HttpServletRequest request,
			@RequestParam(value = "oauth_token", required = false) String oauthToken,
			@RequestParam(value = "oauth_verifier", required = false) String oauthVerifier,
			@RequestParam(value = "state", required = false) String state, HttpServletResponse response) {

		log.trace("Enter LoginService.twitterRedirect(): ");

		try {

			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true).setOAuthConsumerKey(twitterAppId).setOAuthConsumerSecret(twitterSecret)
					.setOAuthAccessToken(accessToken).setOAuthAccessTokenSecret(accessTokenSecret);

			TwitterFactory tf = new TwitterFactory(cb.build());
			Twitter twitter = tf.getInstance();

//			twitter.verifyCredentials().getEmail(); //TODO: for later
			String emailAddress = twitter.getId() + "@" + state + ".ngdesk.com";
			String firstName = twitter.getScreenName();
			String lastName = "";

			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			Document company = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", state)).first();

			if (company == null) {
				throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
			}

			String companyId = company.getObjectId("_id").toString();
			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);

			Document userDocument = usersCollection.find(Filters.eq("EMAIL_ADDRESS", emailAddress))
					.projection(Projections.exclude("PASSWORD", "META_DATA")).first();
			if (userDocument == null) {

				// CREATE USER
				MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
				Document customerRole = rolesCollection.find(Filters.eq("NAME", "Customers")).first();
				String customerRoleId = customerRole.getObjectId("_id").toString();

				InviteUser user = new InviteUser();
				user.setEmailAddress(emailAddress);
				user.setFirstName(firstName);
				user.setLastName(lastName);
				user.setRole(customerRoleId);

				Document systemUser = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "system@ngdesk.com")).first();

				userDocument = inviteUserService.createUsers(user, companyId, company.getString("COMPANY_SUBDOMAIN"),
						systemUser.getObjectId("_id").toString());

				if (userDocument == null) {
					throw new BadRequestException("USER_CREATION_FAILED");
				}

			} else {
				if (userDocument.getBoolean("DELETED")) {
					usersCollection.updateOne(Filters.eq("EMAIL_ADDRESS", emailAddress), Updates.set("DELETED", false));
				}
			}

			usersCollection.updateOne(Filters.eq("EMAIL_ADDRESS", emailAddress),
					Updates.combine(Updates.set("LOGIN_ATTEMPTS", 0), Updates.set("LAST_SEEN", new Date())));

			String userId = userDocument.getObjectId("_id").toString();
			userDocument.remove("_id");
			userDocument.put("DATA_ID", userId);
			userDocument.remove("META_DATA");

			MongoCollection<Document> contactsCollection = mongoTemplate.getCollection("Contacts_" + companyId);
			Document contact = contactsCollection
					.find(Filters.eq("_id", new ObjectId(userDocument.getString("CONTACT").toString()))).first();

			userDocument.put("FIRST_NAME", contact.getString("FIRST_NAME"));
			userDocument.put("LAST_NAME", contact.getString("LAST_NAME"));
			
			String jwtToken = auth.generateJwtToken(emailAddress, userDocument.toJson(), state,
					company.getString("COMPANY_UUID"), companyId);

			response.sendRedirect("https://" + state + ".ngdesk.com/login?authentication_token=" + jwtToken);
			return new ResponseEntity<>(HttpStatus.FOUND);

		} catch (Exception e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/users/login/twitter")
	public RedirectView loginWithTwitter(HttpServletRequest request) {
		try {
			log.trace("Enter LoginService.loginWithTwitter() ");

			RedirectView redirectView = new RedirectView();
			String subdomain = request.getAttribute("SUBDOMAIN").toString();
			String callbackURL = "https://prod.ngdesk.com/ngdesk-rest/ngdesk/users/login/twitter/redirect?state="
					+ subdomain;

			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true).setOAuthConsumerKey(twitterAppId).setOAuthConsumerSecret(twitterSecret)
					.setOAuthAccessToken(null).setOAuthAccessTokenSecret(null);

			TwitterFactory tf = new TwitterFactory(cb.build());
			Twitter twitter = tf.getInstance();

			request.getSession().setAttribute("twitter", twitter);

			RequestToken requestToken = twitter.getOAuthRequestToken(callbackURL);

			request.getSession().setAttribute("requestToken", requestToken);
			redirectView.setUrl(requestToken.getAuthenticationURL());
			log.trace("Exit LoginService.loginWithTwitter()");

			return redirectView;
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("users/login/microsoft/redirect")
	public ResponseEntity<Object> microsoftRedirect(@RequestParam(value = "state", required = false) String subdomain,
			@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "error_description", required = false) String errorDescription,
			@RequestParam(value = "error_reason", required = false) String errorReason, HttpServletResponse response) {

		log.trace("Enter LoginService.microsoftRedirect(): ");

		try {
			if (error != null) {
				throw new BadRequestException("MICROSOFT_LOGIN_FAILED");
			} else {
				MicrosoftConnectionFactory connectionFactory = new MicrosoftConnectionFactory(microsoftAppId,
						microsoftSecret);
				AccessGrant accessGrant = connectionFactory.getOAuthOperations().exchangeForAccess(code,
						microsoftbaseUrl + "/users/login/microsoft/redirect", null);
				String accessToken = accessGrant.getAccessToken();
				Microsoft micorsoft = new MicrosoftTemplate(accessToken);

				String emailAddress = micorsoft.userOperations().getUserProfile().getMail();

				if (emailAddress == null) {

					emailAddress = micorsoft.userOperations().getUserProfile().getId() + "@" + subdomain
							+ ".ngdesk.com";
				}
				String firstName = micorsoft.userOperations().getUserProfile().getGivenName();
				String lastName = micorsoft.userOperations().getUserProfile().getSurname();

				MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
				Document company = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();

				if (company == null) {
					throw new BadRequestException("COMPANY_DOES_NOT_EXIST");
				}

				String companyId = company.getObjectId("_id").toString();
				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);

				Document userDocument = usersCollection.find(Filters.eq("EMAIL_ADDRESS", emailAddress))
						.projection(Projections.exclude("PASSWORD", "META_DATA")).first();
				if (userDocument == null) {

					// CREATE USER
					MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
					Document customerRole = rolesCollection.find(Filters.eq("NAME", "Customers")).first();
					String customerRoleId = customerRole.getObjectId("_id").toString();

					InviteUser user = new InviteUser();
					user.setEmailAddress(emailAddress);
					user.setFirstName(firstName);
					user.setLastName(lastName);
					user.setRole(customerRoleId);

					Document systemUser = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "system@ngdesk.com"))
							.first();

					userDocument = inviteUserService.createUsers(user, companyId,
							company.getString("COMPANY_SUBDOMAIN"), systemUser.getObjectId("_id").toString());

					if (userDocument == null) {
						throw new BadRequestException("USER_CREATION_FAILED");
					}

				} else {
					if (userDocument.getBoolean("DELETED")) {
						usersCollection.updateOne(Filters.eq("EMAIL_ADDRESS", emailAddress),
								Updates.set("DELETED", false));
					}
				}

				usersCollection.updateOne(Filters.eq("EMAIL_ADDRESS", emailAddress),
						Updates.combine(Updates.set("LOGIN_ATTEMPTS", 0), Updates.set("LAST_SEEN", new Date())));

				String userId = userDocument.getObjectId("_id").toString();
				userDocument.remove("_id");
				userDocument.put("DATA_ID", userId);
				String jwtToken = auth.generateJwtToken(emailAddress, userDocument.toJson(), subdomain,
						company.getString("COMPANY_UUID"), companyId);

				response.sendRedirect("https://" + subdomain + ".ngdesk.com/login?authentication_token=" + jwtToken);
				return new ResponseEntity<>(HttpStatus.FOUND);

			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/users/login/microsoft")
	public RedirectView loginWithMicrosoft(HttpServletRequest request) {
		try {
			log.trace("Enter LoginService.loginWithMicrosoft() ");

			RedirectView redirectView = new RedirectView();

			String subdomain = request.getAttribute("SUBDOMAIN").toString();

			MicrosoftConnectionFactory connectionFactory = new MicrosoftConnectionFactory(microsoftAppId,
					microsoftSecret);
			OAuth2Operations oauthOperations = connectionFactory.getOAuthOperations();

			OAuth2Parameters params = new OAuth2Parameters();
			params.setRedirectUri(microsoftbaseUrl + "/users/login/microsoft/redirect");
			params.setScope("openid profile user.read email");
			params.setState(subdomain);

			redirectView.setUrl(oauthOperations.buildAuthorizeUrl(params));
			log.trace("Exit LoginService.loginWithMicrosoft()");

			return redirectView;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public void updateNoOfAttempts(String companyId, Document document, int noOfAttempts) {
		log.trace("Enter LoginService.updateNoOfAttempts() companyId: " + companyId + "," + ", noOfAttempts: "
				+ noOfAttempts);
		String userCollectionName = "Users_" + companyId;
		MongoCollection<Document> usercollection = mongoTemplate.getCollection(userCollectionName);
		document.put("LOGIN_ATTEMPTS", noOfAttempts);
		String userId = document.getObjectId("_id").toString();
		usercollection.findOneAndReplace(Filters.eq("_id", new ObjectId(userId)), document);
		log.trace("Exit LoginService.updateNoOfAttempts() companyId: " + companyId + ", noOfAttempts: " + noOfAttempts);
	}
}
