package com.ngdesk;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.concurrent.ListenableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.accounts.Account;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;

@Component
public class Global {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Account account;

	@Value("${email.host}")
	private String host;

	@Value("${elastic.host}")
	private String elasticHost;

	@Value("${manager.host}")
	private String managerHost;

	@Autowired
	ResourceLoader resourceLoader;

	private final Logger log = LoggerFactory.getLogger(Global.class);

	static SecureRandom rnd = new SecureRandom();
	public static List<String> timezones;
	public static List<String> locale;
	public static List<String> times;
	public static Map<String, JSONObject> errors = new HashMap<>();
	public static HttpHeaders postHeaders;
	public static List<String> primaryColors;
	public static List<String> secondaryColors;
	public static List<String> displayDataTypes;
	public static List<String> backendDataTypes;
	public static List<String> reportingOperators;
	public static List<String> operators;
	public static List<String> actions;
	public static String emailAdressregex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
	public static List<String> chatTriggerTypes;
	public static List<String> chatTriggerStringOperators;
	public static List<String> chatTriggerIntegerOperators;
	public static List<String> chatTriggerConditions;
	public static List<String> chatTriggerActions;
	public static List<String> nodeTypes;
	public static List<String> chatVariables;
	public static List<String> emailVariables;
	public static List<String> intervalVariables;
	public static List<String> userNotifications;
	public static List<String> restrictedSubdomains;
	public static List<String> restrictedFieldNames;
	public static List<String> restrictedModuleNames;
	public static List<String> industries;
	public static List<String> departments;
	public static List<String> companySizes;
	public static Map<Integer, String> daysMap;
	public static List<String> defaultModules;
	public static List<String> languages;
	public static List<String> walkthroughApiKeys;
	public static List<String> slaOperators;
	public static List<String> slaViolationOperators;
	public static Map<String, List<String>> validReportingOperators;
	public static List<String> validTextOperators;
	public static List<String> validNumericOperators;
	public static List<String> validDateOperators;
	public static List<String> validRelationOperators;
	public static Map<String, String> countriesMap;
	public static List<String> deletionFeedback;
	public static List<String> validChatPromptOperators;
	public static List<String> validChatPromptConditions;
	public static List<String> validChatPromptTriggers;
	public static List<String> validInstallerPlatforms;

	public String errorMsg(String language, String msgKey) {
		try {
			log.trace("Enter Global.errorMsg() language: " + language + ", msgKey: " + msgKey);
			log.trace("Exit Global.errorMsg() language: " + language + ", msgKey: " + msgKey);

			if (!errors.containsKey(language)) {
				language = "en";
			}

			return errors.get(language).getString(msgKey);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Date verifyDate(String input) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		log.trace("Enter Global.verifyDate() input: " + input);
		if (input != null) {
			try {
				java.util.Date ret = sdf.parse(input.trim());
				if (sdf.format(ret).equals(input.trim())) {
					log.trace("Exit Global.verifyDate() input: " + input);
					return ret;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		log.trace("Exit Global.verifyDate() input: " + input);
		return null;
	}

	public Date verifyDateTime(String input) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		log.trace("Enter Global.verifyDateTime() input: " + input);
		if (input != null) {
			try {
				java.util.Date ret = sdf.parse(input.trim());
				if (sdf.format(ret).equals(input.trim())) {
					log.trace("Exit Global.verifyDateTime() input: " + input);
					return ret;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		log.trace("Exit Global.verifyDateTime() input: " + input);
		return null;
	}

	/**
	 * 
	 * @param input   - the unhashed user input
	 * @param dbValue - a hash & salted value to compare against
	 * 
	 * @return
	 */
	public Boolean MD5Authenticate(String input, String dbValue) {
		try {
			log.trace("Enter Global.MD5Authenticate() input: " + input + ", dbValue: " + dbValue);
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(input.getBytes());
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			String hashtext = bigInt.toString(16);

			if (hashtext.toString().equals(dbValue)) {
				log.trace("Exit Global.MD5Authenticate() input: " + input + ", dbValue: " + dbValue);
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit Global.MD5Authenticate() input: " + input + ", dbValue: " + dbValue);
		return false;
	}

	public void sendVerificationEmail(String emailAddress, String subdomain, String firstName, String lastName,
			String userUUID) {
		try {
			log.trace("Enter Global.sendVerificationEmail() emailAddress: " + emailAddress + ", subdomain: " + subdomain
					+ ",firstName:  " + firstName + ",lastName:  " + lastName);
			String from = "support@" + subdomain + ".ngdesk.com";
			String messageVerification = getFile("EmailVerification.txt");
			messageVerification = messageVerification.replaceAll("FIRST_NAME", firstName)
					.replaceAll("LAST_NAME", lastName).replaceAll("SUBDOMAIN", subdomain)
					.replaceAll("EMAIL", emailAddress).replaceAll("UUID", userUUID).replaceAll("TYPE", "user");
			String subject = "Verify your email address";
			SendEmail sendemail = new SendEmail(emailAddress, from, subject, messageVerification, host);
			log.trace("Exit Global.sendVerificationEmail() emailAddress: " + emailAddress + ", subdomain: " + subdomain
					+ ",firstName:  " + firstName + ",lastName:  " + lastName);
			sendemail.sendEmail();

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

	public void sendSurveyEmail(String emailAddress, String subdomain, String firstName, String lastName,
			String userUUID) {
		try {
			log.trace("Enter Global.sendSurveyEmail() emailAddress: " + emailAddress + ", subdomain: " + subdomain
					+ ",firstName:  " + firstName + ",lastName:  " + lastName);
			String from = "support@" + subdomain + ".ngdesk.com";

			String messageSurvey = getFile("SurveyEmail.html");
			messageSurvey = messageSurvey.replaceAll("FIRST_NAME", firstName).replaceAll("LAST_NAME", lastName)
					.replaceAll("SUBDOMAIN", subdomain).replaceAll("EMAIL", emailAddress).replaceAll("UUID", userUUID)
					.replaceAll("TYPE", "user");
			String subjectSurvey = "We would love to hear from you!";
			SendEmail sendSurveyEmail = new SendEmail(emailAddress, from, subjectSurvey, messageSurvey, host);
			log.trace("Exit Global.sendSurveyEmail() emailAddress: " + emailAddress + ", subdomain: " + subdomain
					+ ",firstName:  " + firstName + ",lastName:  " + lastName);
			sendSurveyEmail.sendEmail();
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

	public int timeDurationCalculator(Timestamp t1, Timestamp t2) {
		log.trace("Enter Global.timeDurationCalculator() Timestamp: " + t1 + ", Timestamp: " + t2);
		// calculating the difference in milliseconds between the two timestamps
		long milliseconds = t2.getTime() - t1.getTime();

		// converting milliseconds to seconds
		int seconds = (int) milliseconds / 1000;

		int hours = seconds / 3600;
		int minutes = (seconds % 3600) / 60;
		seconds = (seconds % 3600) % 60;

		log.trace("Exit Global.timeDurationCalculator() Timestamp: " + t1 + ", Timestamp: " + t2);
		return hours;
	}

	public String passwordHash(String pwd) {
		log.trace("Enter Global.passwordHash()");
		String hashedPassword = "";

		if (pwd == "") {
			return "";
		}

		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(pwd.getBytes());
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			hashedPassword = bigInt.toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit Global.passwordHash()");
		return hashedPassword;
	}

	public String request(String url, String body, String type, Map<String, String> headerMap) {
		int responsecode = -1;
		StringBuffer response = new StringBuffer();
		try {

			log.trace("Enter Global.request() url: " + url + ", body: " + body + ",type: " + type);

			// OPEN CONNECTION
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// ADD REQUEST HEADER
			con.setRequestMethod(type.toUpperCase());
			con.setRequestProperty("Content-Type", "application/json");

			// SET PROPERTIES OF REQUEST
			if (headerMap != null) {
				for (Map.Entry<String, String> header : headerMap.entrySet()) {
					con.setRequestProperty(header.getKey(), header.getValue());
				}
			}

			// SEND POST REQUEST
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());

//			wr.writeBytes(body);
			wr.write(body.getBytes("UTF-8"));
			wr.flush();
			wr.close();

			responsecode = con.getResponseCode();
			if (responsecode < 200 || responsecode >= 300) {
				return null;
			}

			// WRITE RESULTS
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}

		log.trace("Exit Global.request() url: " + url + ", body: " + body + ",type: " + type);
		return response.toString();
	}

	public String get(String url, Map<String, String> headerMap) {
		log.trace("Enter HttpRequestNode.get() url: " + url);
		StringBuffer result = new StringBuffer();
		HttpResponse response;

		try {

			// CREATE HTTP CLIENT
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);

			// SET HEADERS
			if (headerMap != null) {
				for (Map.Entry<String, String> header : headerMap.entrySet()) {
					request.setHeader(header.getKey(), header.getValue());
				}
			}

			response = client.execute(request);

			// WRITE RESULT
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		log.trace("Exit HttpRequestNode.get() url: " + url);
		return result.toString();
	}

	public String getFile(String fileName) {
		try {
			log.trace("Enter Global.getFile() fileName: " + fileName);
			Resource resource = resourceLoader.getResource("classpath:" + fileName);
			InputStream inputStream = resource.getInputStream();
			byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
			String data = new String(bdata, StandardCharsets.UTF_8);
			log.trace("Exit Global.getFile() fileName: " + fileName);
			return data;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new InternalErrorException("FILE_NOT_FOUND");
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

	public InputStream getActualFile(String fileName) {
		StringBuilder result = new StringBuilder("");
		try {
			log.trace("Enter Global.getFile() fileName: " + fileName);
			String operatingSystem = System.getProperty("os.name");

			if (operatingSystem.equalsIgnoreCase("Windows 10")) {
				return new ClassPathResource(fileName).getInputStream();
			} else {
				Resource res = new ClassPathResource("classpath:" + fileName);
				return res.getInputStream();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new InternalErrorException("FILE_NOT_FOUND");
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

	public boolean isExists(String variable, String value, String collectionName) {
		try {

			log.trace("Enter Global.isExists() variable: " + variable + ", value: " + value + ", collectionName: "
					+ collectionName);
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document document = collection.find(Filters.eq(variable, value)).first();
			if (document != null) {

				log.trace("Exit Global.isExists() variable: " + variable + ", value: " + value + ", collectionName: "
						+ collectionName);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}

		log.trace("Exit Global.isExists() variable: " + variable + ", value: " + value + ", collectionName: "
				+ collectionName);
		return false;
	}

	public boolean isExistsIgnoreCase(String variable, String value, String collectionName) {
		try {

			log.trace("Enter Global.isExistsIgnoreCase() variable: " + variable + ", value: " + value
					+ ", collectionName: " + collectionName);
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document document = collection.find(Filters.eq(variable, value)).first();
			List<Document> documentsList = (List<Document>) collection.find().into(new ArrayList<Document>());
			for (Document doc : documentsList) {
				if (doc.getString(variable).equalsIgnoreCase(value)) {
					document = doc;
				}
			}
			if (document != null) {

				log.trace("Exit Global.isExistsIgnoreCase() variable: " + variable + ", value: " + value
						+ ", collectionName: " + collectionName);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}

		log.trace("Exit Global.isExistsIgnoreCase() variable: " + variable + ", value: " + value + ", collectionName: "
				+ collectionName);
		return false;
	}

	public boolean isDocumentIdExists(String id, String collectionName) {
		try {
			log.trace("Enter Global.isDocumentIdExists() id: " + id + ", collectionName: " + collectionName);
			if (!ObjectId.isValid(id)) {
				return false;
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document document = collection.find(Filters.eq("_id", new ObjectId(id))).first();

			log.trace("Exit Global.isDocumentIdExists() id: " + id + ", collectionName: " + collectionName);
			return document != null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}

	}

	public String getFormattedDate(Timestamp date) {
		String formattedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(date);
		return formattedDate;
	}

	public Date getStringToDateFormatted(String dateInString) {
		Date entryDate = null;
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			entryDate = format.parse(dateInString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return entryDate;
	}

	public Document getCompanyFromSubdomain(String subdomain) {
		Document company = null;
		try {
			log.trace("Enter Global.getCompanyFromSubdomain() subdomain: " + subdomain);
			String collectionName = "companies";
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			company = collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
			if (company == null) {
				throw new ForbiddenException("COMPANY_DOES_NOT_EXIST");
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit Global.getCompanyFromSubdomain() subdomain: " + subdomain);
		return company;
	}

	public String createOrGetAccountId(String emailAddress, String companyId, String globalTeamId) {

		String accountId = null;
		try {

			log.trace(
					"Enter Global.createOrGetAccountId() emailAddress: " + emailAddress + ", companyId: " + companyId);
			// CHECK IF ACCOUNT EXISTS AND GET ACCOUNT IF EXISTS
			String accountName = emailAddress.split("@")[1];
			String accountsCollectionName = "Accounts_" + companyId;
			MongoCollection<Document> accountsCollection = mongoTemplate.getCollection(accountsCollectionName);
			Document existingAccountDocument = accountsCollection
					.find(Filters.and(Filters.eq("ACCOUNT_NAME", accountName.toLowerCase()),
							Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
					.first();

			// IF ACCOUNT DOCUMENT NULL, CREATE NEW ONE
			if (existingAccountDocument == null) {
				Document accountDocument = account.createAccount(accountName, companyId, globalTeamId);
				accountId = accountDocument.getObjectId("_id").toString();
			} else {
				accountId = existingAccountDocument.getObjectId("_id").toString();
			}
			if (accountId == null) {
				throw new ForbiddenException("ACCOUNT_ID_NULL");
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}

		log.trace("Exit Global.createOrGetAccountId() emailAddress: " + emailAddress + ", companyId: " + companyId);
		return accountId;
	}

	public boolean isValidSourceType(String sourceType) {
		try {

			log.trace("Enter Global.isValidSourceType() sourceType: " + sourceType);
			String[] validTypes = { "chat", "email", "interval" };
			if (Arrays.asList(validTypes).contains(sourceType)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

	public boolean validatePrimaryColor(String color) {
		try {
			log.trace("Enter Global.validatePrimaryColor() color: " + color);
			if (primaryColors.contains(color)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

	public boolean validateSecondaryColor(String color) {
		try {
			log.trace("Exit Global.validateSecondaryColor() color: " + color);
			if (secondaryColors.contains(color)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

	public boolean isExistsInModule(String variable, String value, String collectionName, String moduleName) {
		try {
			log.trace("Enter Global.isExists() variable: " + variable + ", value: " + value + ", collectionName: "
					+ collectionName + ", moduleName: " + moduleName);
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			// Get Document
			Document module = collection.find(Filters.eq("NAME", moduleName)).first();
			ArrayList<Document> documents = (ArrayList) module.get(variable);

			// GET SPECIFIC FIELD
			for (Document document : documents) {
				if (document.getString("NAME").equals(value)) {

					log.trace("Exit Global.isExists() variable: " + variable + ", value: " + value
							+ ", collectionName: " + collectionName + ", moduleName: " + moduleName);
					return true;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit Global.isExists() variable: " + variable + ", value: " + value + ", collectionName: "
				+ collectionName + ", moduleName: " + moduleName);
		return false;
	}

	public boolean isExistsInModuleLayoutId(String variable, String value, String collectionName, String moduleName) {
		try {
			log.trace("Enter Global.isExistsInModuleLayoutId() variable: " + variable + ", value: " + value
					+ ", collectionName: " + collectionName + ", moduleName: " + moduleName);
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			// Get Document
			Document module = collection.find(Filters.eq("NAME", moduleName)).first();
			ArrayList<Document> documents = (ArrayList) module.get(variable);
			// GET SPECIFIC FIELD
			for (Document document : documents) {
				if (document.getString("LAYOUT_ID").equals(value)) {
					log.trace("Exit Global.isExistsInModuleLayoutId() variable: " + variable + ", value: " + value
							+ ", collectionName: " + collectionName + ", moduleName: " + moduleName);
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace("Exit Global.isExistsInModuleLayoutId() variable: " + variable + ", value: " + value
				+ ", collectionName: " + collectionName + ", moduleName: " + moduleName);
		return false;
	}

	public void setDefaultContextInElastic(String companyId) {
		RestHighLevelClient elasticClient = null;
		try {
			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			String json = getFile("elasticSearchContext.json");
			JSONObject contextObject = new JSONObject(json);
			JSONObject propertiesObject = contextObject.getJSONObject("properties");
			JSONObject suggestObject = propertiesObject.getJSONObject("suggest");
			JSONArray contextArray = suggestObject.getJSONArray("contexts");

			JSONObject roleContext = new JSONObject();
			roleContext.put("name", "ROLES");
			roleContext.put("path", "ROLES");
			roleContext.put("type", "category");

			JSONObject teamContext = new JSONObject();
			teamContext.put("name", "TEAMS");
			teamContext.put("path", "TEAMS");
			teamContext.put("type", "category");

			JSONObject fieldNameContext = new JSONObject();
			fieldNameContext.put("name", "FIELD_NAME");
			fieldNameContext.put("path", "FIELD_NAME");
			fieldNameContext.put("type", "category");

			JSONObject moduleTeamsContext = new JSONObject();
			moduleTeamsContext.put("name", "MODULE_TEAMS");
			moduleTeamsContext.put("path", "MODULE_TEAMS");
			moduleTeamsContext.put("type", "category");

			contextArray.put(roleContext);
			contextArray.put(teamContext);
			contextArray.put(fieldNameContext);
			contextArray.put(moduleTeamsContext);

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			List<Document> modules = modulesCollection.find().into(new ArrayList<Document>());
			for (Document module : modules) {
				List<Document> fields = (List<Document>) module.get("FIELDS");
				for (Document field : fields) {

					Document datatype = (Document) field.get("DATA_TYPE");
					String displayDatatype = datatype.getString("DISPLAY");

					if (displayDatatype.equals("Text") || displayDatatype.equals("Text Area")
							|| displayDatatype.equals("Text Area Long") || displayDatatype.equals("Text Area Rich")
							|| displayDatatype.equals("Discussion") || displayDatatype.equals("Picklist")) {
						String fieldName = field.getString("NAME");
						JSONObject context = new JSONObject();
						context.put("name", fieldName);
						context.put("type", "category");
						context.put("path", fieldName);
						contextArray.put(context);
					}
				}

			}

			// CHECK IF INDEX EXISTS
			GetIndexRequest getIndexRequest = new GetIndexRequest("autocomplete_" + companyId);
			boolean exists = elasticClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);

			if (exists) {
				DeleteIndexRequest deleteRequest = new DeleteIndexRequest("autocomplete_" + companyId);
				elasticClient.indices().delete(deleteRequest, RequestOptions.DEFAULT);
			}

			CreateIndexRequest request = new CreateIndexRequest("autocomplete_" + companyId);
			elasticClient.indices().create(request, RequestOptions.DEFAULT);

			PutMappingRequest putRequest = new PutMappingRequest("autocomplete_" + companyId);
			putRequest.source(contextObject.toString(), XContentType.JSON);
			elasticClient.indices().putMapping(putRequest, RequestOptions.DEFAULT);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadBackModuleDataInElastic(String companyId) {

		RestHighLevelClient elasticClient = null;
		try {
			log.trace("Enter Global.loadBackModuleDataInElastic()");

			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			List<Document> modules = modulesCollection.find().into(new ArrayList<Document>());

			MongoCollection<Document> rolesColletion = mongoTemplate.getCollection("roles_" + companyId);
			List<Document> roleDocuments = rolesColletion.find().into(new ArrayList<Document>());

			List<String> roles = new ArrayList<String>();
			for (Document role : roleDocuments) {
				roles.add(role.getObjectId("_id").toString());
			}

			BulkRequest request = new BulkRequest();
			int count = 0;

			for (Document module : modules) {
				String moduleName = module.getString("NAME");

				MongoCollection<Document> moduleNameCollection = mongoTemplate
						.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);
				List<Document> moduleEntries = moduleNameCollection.find(Filters.eq("DELETED", false))
						.into(new ArrayList<Document>());

				List<Document> fields = (List<Document>) module.get("FIELDS");
				String moduleId = module.getObjectId("_id").toString();

				for (Document moduleEntry : moduleEntries) {

					String entryId = moduleEntry.getObjectId("_id").toString();
					moduleEntry.remove("_id");
					JSONObject body = new JSONObject(moduleEntry);
					body.put("DATA_ID", entryId);

					JSONArray teamsArray = new JSONArray();
					if (body.has("TEAMS")) {
						teamsArray = body.getJSONArray("TEAMS");
					}

					JSONArray moduleTeamsHash = new JSONArray();
					for (int i = 0; i < teamsArray.length(); i++) {

						String teamId = teamsArray.getString(i);
						String textToHash = moduleId + teamId;

						MessageDigest md = MessageDigest.getInstance("MD5");
						byte[] messageDigest = md.digest(textToHash.toString().getBytes());
						BigInteger number = new BigInteger(1, messageDigest);
						String hashText = number.toString(16);

						moduleTeamsHash.put(hashText);
					}

					JSONObject contextsObj = new JSONObject();
					contextsObj.put("ROLES", roles);
					contextsObj.put("TEAMS", teamsArray);
					contextsObj.put("MODULE_TEAMS", moduleTeamsHash);

					String discussionField = "";
					for (Document field : fields) {

						String fieldName = field.getString("NAME");
						Document datatype = (Document) field.get("DATA_TYPE");

						if (body.has(fieldName) && body.get(fieldName) != null) {
							if (datatype.getString("DISPLAY").equals("Discussion")) {
								JSONArray discussionMessages = body.getJSONArray(fieldName);

								for (int i = 0; i < discussionMessages.length(); i++) {
									JSONObject discussionMessage = discussionMessages.getJSONObject(i);
									String message = discussionMessage.getString("MESSAGE");

									discussionField += Jsoup.parse(message).text();
								}

								StringReader reader = new StringReader(discussionField);
								StandardTokenizer source = new StandardTokenizer();
								source.setReader(reader);
								ShingleFilter sf = new ShingleFilter(source);
								sf.setOutputUnigrams(true);
								sf.setMinShingleSize(2);
								sf.setMaxShingleSize(5);
								CharTermAttribute charTermAttribute = sf.addAttribute(CharTermAttribute.class);
								sf.reset();

								List<String> discussionShingles = new ArrayList<String>();
								while (sf.incrementToken()) {
									String shingle = charTermAttribute.toString().toLowerCase();
									discussionShingles.add(shingle);
								}
								sf.end();
								sf.close();

								contextsObj.put(fieldName, discussionShingles);

							} else if (datatype.getString("DISPLAY").equals("Picklist")) {
								contextsObj.put(fieldName, body.getString(fieldName));
							}
						}

					}

					for (Document field : fields) {
						String fieldName = field.getString("NAME");
						Document datatype = (Document) field.get("DATA_TYPE");
						String displayDatatype = datatype.getString("DISPLAY");

						if (displayDatatype.equals("Text") || displayDatatype.equals("Text Area")
								|| displayDatatype.equals("Text Area Long") || displayDatatype.equals("Text Area Rich")
								|| displayDatatype.equals("Discussion")) {
							if (body.has(fieldName) && body.get(fieldName) != null) {
								StringReader reader = null;
								if (displayDatatype.equals("Discussion")) {
									reader = new StringReader(discussionField);
								} else {
									reader = new StringReader(body.getString(fieldName));
								}
								StandardTokenizer source = new StandardTokenizer();
								source.setReader(reader);
								ShingleFilter sf = new ShingleFilter(source);
								sf.setOutputUnigrams(true);
								sf.setMinShingleSize(2);
								sf.setMaxShingleSize(5);
								CharTermAttribute charTermAttribute = sf.addAttribute(CharTermAttribute.class);
								sf.reset();

								while (sf.incrementToken()) {

									JSONObject suggestObj = new JSONObject();

									JSONObject objIn = new JSONObject();
									objIn.put("input", charTermAttribute.toString().toLowerCase());
									contextsObj.remove(fieldName);
									contextsObj.put("FIELD_NAME", fieldName);
									objIn.put("contexts", contextsObj);
									suggestObj.put("suggest", objIn);

									IndexRequest requestIn = new IndexRequest("autocomplete_" + companyId);
									MessageDigest md = MessageDigest.getInstance("MD5");
									byte[] messageDigest = md.digest(charTermAttribute.toString().getBytes());
									BigInteger number = new BigInteger(1, messageDigest);
									String hashtext = number.toString(16);

									requestIn.id(hashtext);
									requestIn.source(suggestObj.toString(), XContentType.JSON);
									request.add(requestIn);
									count++;

									if (count % 100 == 0) {
										elasticClient.bulk(request, RequestOptions.DEFAULT);
										request = new BulkRequest();
									}
								}
								sf.end();
								sf.close();
							}

						}
					}
					if (count % 100 != 0) {
						elasticClient.bulk(request, RequestOptions.DEFAULT);
					}
				}
			}
			log.trace("Exit Global.loadBackModuleDataInElastic()");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isValidDate(String input) {
		log.trace("Enter Global.isValidDate() input: " + input);
		if (input != null) {
			try {
				Instant instant = Instant.parse(input);
				Date dateValue = (Date) Date.from(instant);
				return true;
			} catch (DateTimeException e) {
				log.trace("Exit Global.isValidDate() input: " + input);
				return false;
			}
		}
		log.trace("Exit Global.isValidDate() input: " + input);
		return false;
	}

	public String chronometerFormatTransform(int value, String formattedTime) {
		// Conversion rates 1d = 8h, 1w = 5d, 1mo = 20d(4w)
		try {
			if (value >= 9600) {
				// 1 Month = 9600 minutes
				int remainder = value % 9600;
				if (remainder == 0) {
					return value / 9600 + "mo";
				} else {
					formattedTime = value / 9600 + "mo";
					return chronometerFormatTransform(remainder, formattedTime);
				}
			} else if (value >= 2400) {
				// 1 Week = 2400 minutes
				int remainder = value % 2400;
				if (remainder == 0) {
					if (formattedTime.length() > 0) {
						return formattedTime + " " + value / 2400 + "w";
					} else {
						return value / 2400 + "w";
					}
				} else {
					if (formattedTime.length() > 0) {
						formattedTime = formattedTime + ' ' + (value / 2400) + 'w';
					} else {
						formattedTime = (value / 2400) + "w";
					}
					return this.chronometerFormatTransform(remainder, formattedTime);
				}
			} else if (value >= 480) {
				// 1 Day = 480 minutes
				int remainder = value % 480;
				if (remainder == 0) {
					if (formattedTime.length() > 0) {
						return formattedTime + " " + value / 480 + "d";
					} else {
						return value / 480 + "d";
					}
				} else {
					if (formattedTime.length() > 0) {
						formattedTime = formattedTime + ' ' + (value / 480) + 'd';
					} else {
						formattedTime = (value / 480) + "d";
					}
					return this.chronometerFormatTransform(remainder, formattedTime);
				}
			} else if (value >= 60) {
				// 1 Hour = 60 minutes
				int remainder = value % 60;
				if (remainder == 0) {
					if (formattedTime.length() > 0) {
						return formattedTime + " " + value / 60 + "h";
					} else {
						return value / 60 + "h";
					}
				} else {
					if (formattedTime.length() > 0) {
						formattedTime = formattedTime + ' ' + (value / 60) + "h";
					} else {
						formattedTime = (value / 60) + "h";
					}
					return this.chronometerFormatTransform(remainder, formattedTime);
				}
			} else {
				if (formattedTime.length() > 0) {
					return formattedTime + " " + value + "m";
				} else {
					return formattedTime + value + "m";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public long chronometerValueConversionInSeconds(String input) {
		try {
			Pattern periodPattern = Pattern.compile("(\\d+)(mo|w|d|m|h)");
			Matcher matcher = periodPattern.matcher(input);
			long chronometerValueInSecond = 0;
			while (matcher.find()) {
				int num = Integer.parseInt(matcher.group(1));
				String typ = matcher.group(2);
				switch (typ) {
				case "mo":
					chronometerValueInSecond = num * 9600;
					break;
				case "w":
					chronometerValueInSecond = chronometerValueInSecond + (num * 2400);
					break;
				case "d":
					chronometerValueInSecond = chronometerValueInSecond + (num * 480);
					break;
				case "h":
					chronometerValueInSecond = chronometerValueInSecond + (num * 60);
					break;
				case "m":
					chronometerValueInSecond = chronometerValueInSecond + num;
					break;

				}
			}
			return chronometerValueInSecond;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public String getValue(String path, Map<String, Object> inputMessage, String companyId, String moduleId,
			String dataId, boolean isPremadeResonse) {
		try {

			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			Document company = companiesCollection.find(Filters.eq("_id", new ObjectId(companyId))).first();
			String subdomain = company.getString("COMPANY_SUBDOMAIN");
			String companyName = company.getString("COMPANY_NAME");

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			List<Document> fields = (List<Document>) moduleDocument.get("FIELDS");
			Map<String, Document> relationFieldsMap = new HashMap<String, Document>();
			String discussionField = null;
			List<String> phoneDatatypes = new ArrayList<String>();
			List<String> dateTimeDatatypes = new ArrayList<String>();
			List<String> dateDatatypes = new ArrayList<String>();
			List<String> timeDatatypes = new ArrayList<String>();

			String fieldLabel = null;
			for (Document field : fields) {
				String fieldName = field.getString("NAME");
				Document dataType = (Document) field.get("DATA_TYPE");
				if (dataType.getString("DISPLAY").equals("Relationship")
						&& (field.getString("RELATIONSHIP_TYPE").equals("One to One")
								|| field.getString("RELATIONSHIP_TYPE").equals("Many to One"))) {
					relationFieldsMap.put(field.getString("NAME"), field);
				} else if (dataType.getString("DISPLAY").equals("Discussion")) {
					discussionField = field.getString("NAME");
				} else if (dataType.getString("DISPLAY").equals("Phone")) {
					phoneDatatypes.add(fieldName);
				} else if (dataType.getString("DISPLAY").equals("Date/Time")) {
					dateTimeDatatypes.add(fieldName);
				} else if (dataType.getString("DISPLAY").equals("Date")) {
					dateDatatypes.add(fieldName);
				} else if (dataType.getString("DISPLAY").equals("Time")) {
					timeDatatypes.add(fieldName);
				}
			}

			String section = path.split("\\.")[0];

			for (Document field : fields) {

				if (field.getString("NAME").equals(section)) {

					fieldLabel = field.getString("DISPLAY_LABEL");
				}
			}
			if (relationFieldsMap.containsKey(section)) {
				Document field = relationFieldsMap.get(section);

				String relationModuleId = field.getString("MODULE");
				Document relationModule = modulesCollection.find(Filters.eq("_id", new ObjectId(relationModuleId)))
						.first();

				if (relationModule != null && inputMessage.get(section) != null) {
					String value = inputMessage.get(section).toString();
					String primaryDisplayField = field.getString("PRIMARY_DISPLAY_FIELD");

					String id = relationModule.getObjectId("_id").toString();

					String primaryDisplayFieldName = null;
					List<Document> relationFields = (List<Document>) relationModule.get("FIELDS");

					for (Document relationField : relationFields) {
						if (relationField.getString("FIELD_ID").equals(primaryDisplayField)) {
							primaryDisplayFieldName = relationField.getString("NAME");
							break;
						}
					}

					String relationModuleName = relationModule.getString("NAME");
					String entriesCollectionName = relationModuleName + "_" + companyId;
					MongoCollection<Document> entriesCollection = mongoTemplate.getCollection(entriesCollectionName);
					Document entry = entriesCollection.find(Filters.eq("_id", new ObjectId(value))).first();
					String entryId = entry.getObjectId("_id").toString();
					entry.remove("_id");
					Map<String, Object> newMap = new ObjectMapper().readValue(entry.toJson(), Map.class);

					if (path.split("\\.").length > 1) {
						return getValue(path.split(section + "\\.")[1], newMap, companyId, id, entryId, false);
					} else {
						return new ObjectMapper().writeValueAsString(newMap);
					}
				}
			} else if (discussionField != null && discussionField.equals(section) && path.split("\\.").length == 1) {
				if (inputMessage.containsKey(section) && inputMessage.get(section) != null) {
					String body = "";
					if (!isPremadeResonse) {
						body = "<p style=\"color:#b5b5b5; font-family: Lucida Grande, Verdana, Arial, sans-serif, serif, EmojiFont; font-size: 12px;\">##-- Please type your reply above this line --##</p>";
					}
					List<Map<String, Object>> messages = (List<Map<String, Object>>) inputMessage.get(section);

					MongoCollection<Document> attachmentsCollection = mongoTemplate
							.getCollection("attachments_" + companyId);
					for (int i = messages.size() - 1; i >= 0; i--) {
						Map<String, Object> message = messages.get(i);

						// IGNORING META_DATA FROM BEING SENT
						if (!message.get("MESSAGE_TYPE").toString().equalsIgnoreCase("META_DATA")) {

							String messageBody = message.get("MESSAGE").toString();

							Pattern pattern = Pattern.compile("<body>(.*?)<\\/body>");
							Matcher matcher = pattern.matcher(messageBody);

							if (matcher.find()) {
								String match = matcher.group(1);
								match = match.replaceAll("\n", "<br/>");

								messageBody = messageBody.replaceAll("<body>(.*?)<\\/body>",
										"<body>" + match + "</body>");
							}

							String messageId = message.get("MESSAGE_ID").toString();
							Map<String, Object> senderMap = (Map<String, Object>) message.get("SENDER");
							String senderFirstName = senderMap.get("FIRST_NAME").toString();

							String senderLastName = null;
							if (senderMap.containsKey("LAST_NAME")) {
								senderLastName = senderMap.get("LAST_NAME").toString();
							}

//							Date parsedDate = new Date();
//							parsedDate = (Date) message.get("DATE_CREATED");
							
							// AS WE ARE PARSING THE PAYLOAD DATE IS CONVERTED TO TIMESTAMP HENCE CASTING DIRECTLY
							Timestamp dateCreated = new Timestamp((long) message.get("DATE_CREATED"));

							String formattedDateCreated = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
									.format(dateCreated);

							String messageHTML = getFile("email_message.html");

							messageHTML = messageHTML.replaceAll("FIRST_NAME", senderFirstName);
							messageHTML = messageHTML.replaceAll("LAST_NAME", senderLastName);
							messageHTML = messageHTML.replaceAll("COMPANY_NAME", companyName);
							messageHTML = messageHTML.replaceAll("DATE_AND_TIME", formattedDateCreated);
							messageHTML = messageHTML.replaceAll("MESSAGE_REPLACE", messageBody);

							body += messageHTML;
							List<Document> allAttachments = new ArrayList<Document>();

							if (message.containsKey("ATTACHMENTS") && message.get("ATTACHMENTS") != null) {
								List<Map<String, Object>> attachments = (List<Map<String, Object>>) message
										.get("ATTACHMENTS");
								for (Map<String, Object> attachment : attachments) {
									Document actualAttachment = attachmentsCollection
											.find(Filters.eq("HASH", attachment.get("HASH").toString())).first();
									if (actualAttachment != null) {
										actualAttachment.put("FILE_NAME", attachment.get("FILE_NAME").toString());
										allAttachments.add(actualAttachment);
									}
								}
							}

							if (allAttachments.size() > 0) {
								body += "<br/>";
								body += "Attachments: <br/>";
							}

							for (Document attachment : allAttachments) {
								String uuid = attachment.get("ATTACHMENT_UUID").toString();
								String filename = attachment.get("FILE_NAME").toString();
								body += "<a target=\"_blank\" href=\"https://" + subdomain
										+ ".ngdesk.com/ngdesk-rest/ngdesk/attachments?attachment_uuid=" + uuid
										+ "&message_id=" + messageId + "&entry_id=" + dataId + "&module_id=" + moduleId
										+ "\">" + filename + "</a><br/>";

							}

							body += "<br/><hr/>";
							if (i == 0) {
								String ticketLink = "<a href=\"https://" + company.getString("COMPANY_SUBDOMAIN")
										+ ".ngdesk.com/render/" + moduleId + "/detail/" + dataId
										+ "\"> View it on ngdesk </a>";
								body += ticketLink;
								body += "<br/><br/>";
								// TODO: NOT NEEDED
								if (!isPremadeResonse) {
									body += "*****************************************************************";
									body += "<br/>" + "Do not modify the following content: <br/>" + "ENTRY_ID: "
											+ dataId + "<br/>" + "MODULE_ID: " + moduleId + "<br/>"
											+ "COMPANY_SUBDOMAIN: " + subdomain;
								}
								log.trace("Successfully added body");
							}
						}
					}
					return body;
				}
			} else {
				if (path.split("\\.").length > 1) {
					Map<String, Object> newMap = (Map<String, Object>) inputMessage.get(section);
					return getValue(path.split(section + "\\.")[1], newMap, companyId, moduleId, dataId, false);
				} else {
					if (inputMessage.get(section) == null) {
						return fieldLabel + " Not Set";
					} else {
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

						if (phoneDatatypes.contains(section) && inputMessage.get(section) != null) {
							Map<String, Object> phoneMap = (Map<String, Object>) inputMessage.get(section);
							String number = "";
							if (phoneMap.get("DIAL_CODE") != null && phoneMap.get("PHONE_NUMBER") != null) {
								number = phoneMap.get("DIAL_CODE").toString() + phoneMap.get("PHONE_NUMBER").toString();
							}
							return number;
						} else if (dateTimeDatatypes.contains(section) && inputMessage.get(section) != null) {
							// CHANGE DATE FORMAT IF FIELD IS DATE/TIME

							String date = dateFormat.format(inputMessage.get(section));

							Date parsedDate = dateFormat.parse(date.toString());
							Timestamp currentValue = new Timestamp(parsedDate.getTime());
							String formattedValue = new SimpleDateFormat("MMMM dd, yyyy HH:mm a").format(currentValue);

							return formattedValue;
						} else if (dateDatatypes.contains(section) && inputMessage.get(section) != null) {
							// CHANGE DATE FORMAT IF FIELD IS DATE

							Date parsedDate = dateFormat.parse(inputMessage.get(section).toString());
							Timestamp currentValue = new Timestamp(parsedDate.getTime());
							String formattedValue = new SimpleDateFormat("MMMM dd, yyyy").format(currentValue);
							return formattedValue;
						} else if (timeDatatypes.contains(section) && inputMessage.get(section) != null) {
							// CHANGE DATE FORMAT IF FIELD IS TIME

							Date parsedDate = dateFormat.parse(inputMessage.get(section).toString());
							Timestamp currentValue = new Timestamp(parsedDate.getTime());
							String formattedValue = new SimpleDateFormat("HH:mm a").format(currentValue);
							return formattedValue;
						} else {
							return inputMessage.get(section).toString();
						}

					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// METHOD FOR ADDING THE METADATA WHEN VALUE IN THE TEXT AND PICKLIST FIELD IS
	// CHANGED

	public void addMetadataAfterFieldUpdate(JSONObject entry, Document existingEntry, Document company, String moduleId,
			String userUUID, String dataId) {
		try {
			log.trace("Entered addMetadataAfterFieldUpdate()");

			String companyId = company.getObjectId("_id").toString();

			JSONObject payloadWithMetadata = new JSONObject();

			MongoCollection<Document> userCollection = mongoTemplate.getCollection("Users_" + companyId);
			Document userDoc = userCollection.find(Filters.eq("USER_UUID", userUUID)).first();
			String userEmail = userDoc.getString("EMAIL_ADDRESS");

			Document systemUserDoc = userCollection.find(Filters.eq("EMAIL_ADDRESS", "system@ngdesk.com")).first();

			String userId = userDoc.get("_id").toString();
			String metadataList = "";
			String metadataHtml = getFile("metadata_field_value_updated.html");

			MongoCollection<Document> moduleCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document moduledoc = moduleCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			String discussionFieldName = "";
			JSONObject discussionWithMetadata = new JSONObject();

			List<Document> fields = (List<Document>) moduledoc.get("FIELDS");
			List<String> fieldNames = new ArrayList<String>();
			Map<String, String> fieldDisplayNames = new HashMap<String, String>();
			for (Document fieldName : fields) {
				Document dataType = (Document) fieldName.get("DATA_TYPE");
				if (dataType.getString("DISPLAY").equalsIgnoreCase("Picklist")
						|| dataType.getString("DISPLAY").equalsIgnoreCase("Text")
						|| dataType.getString("DISPLAY").equalsIgnoreCase("Auto Number")
						|| dataType.getString("DISPLAY").equalsIgnoreCase("Text Area Rich")
						|| dataType.getString("DISPLAY").equalsIgnoreCase("Text Area")
						|| dataType.getString("DISPLAY").equalsIgnoreCase("Text Area Long")
						|| dataType.getString("DISPLAY").equalsIgnoreCase("Email")
						|| dataType.getString("DISPLAY").equalsIgnoreCase("Checkbox")
						|| dataType.getString("DISPLAY").equalsIgnoreCase("Date")
						|| dataType.getString("DISPLAY").equalsIgnoreCase("Street 1")
						|| dataType.getString("DISPLAY").equalsIgnoreCase("Street 2")
						|| dataType.getString("DISPLAY").equalsIgnoreCase("City")
						|| dataType.getString("DISPLAY").equalsIgnoreCase("Country")
						|| dataType.getString("DISPLAY").equalsIgnoreCase("Zipcode")) {
					fieldNames.add(fieldName.getString("NAME"));
					fieldDisplayNames.put(fieldName.getString("NAME"), fieldName.getString("DISPLAY_LABEL"));
				}

				if (dataType.getString("DISPLAY").equals("Discussion")) {
					discussionFieldName = fieldName.getString("NAME");
				}

			}
			for (String field : fieldNames) {
				String oldFieldValue = "";
				String latestFieldValue = "";
				if (existingEntry.containsKey(field) || entry.has(field)) {

					if (existingEntry.get(field) != null) {
						oldFieldValue = existingEntry.get(field).toString();
					}
					if (!entry.isNull(field)) {
						latestFieldValue = entry.get(field).toString();
					}
					if (!oldFieldValue.equalsIgnoreCase(latestFieldValue)) {
						if (!oldFieldValue.isEmpty() && !latestFieldValue.isEmpty()) {
							Pattern pattern = Pattern.compile(
									"<div class='oldvalue'>(.*)?<\\/div>(.*?)<div class='emptyOldValue'",
									Pattern.DOTALL);
							Matcher matcher = pattern.matcher(metadataHtml);
							if (matcher.find()) {
								String metadataDiv = matcher.group(1);
								metadataDiv = metadataDiv.replace("FIELD_NAME_REPLACE", fieldDisplayNames.get(field));
								metadataDiv = metadataDiv.replace("OLD_FIELD_VALUE_REPLACE", oldFieldValue);
								metadataDiv = metadataDiv.replace("NEW_FIELD_VALUE_REPLACE", latestFieldValue);
								metadataDiv = metadataDiv.replace("EMAIL_IDS_REPLACE", userEmail);
								metadataList = metadataList + metadataDiv;
							}
						} else if (latestFieldValue.isEmpty() && !oldFieldValue.isEmpty()) {
							Pattern pattern = Pattern.compile("<div class='emptyIncomingValue'>(.*)?<\\/div>",
									Pattern.DOTALL);
							Matcher matcher = pattern.matcher(metadataHtml);
							if (matcher.find()) {
								String metadataDiv = matcher.group(1);
								metadataDiv = metadataDiv.replace("FIELD_NAME_REPLACE", fieldDisplayNames.get(field));
								metadataDiv = metadataDiv.replace("OLD_FIELD_VALUE_REPLACE", oldFieldValue);
								metadataDiv = metadataDiv.replace("EMAIL_IDS_REPLACE", userEmail);
								metadataList = metadataList + metadataDiv;
							}
						} else if (!latestFieldValue.isEmpty() && oldFieldValue.isEmpty()) {
							Pattern pattern = Pattern.compile(
									"<div class='emptyOldValue'>(.*)?<\\/div>(.*?)<div class=", Pattern.DOTALL);
							Matcher matcher = pattern.matcher(metadataHtml);
							if (matcher.find()) {
								String metadataDiv = matcher.group(1);
								metadataDiv = metadataDiv.replace("FIELD_NAME_REPLACE", fieldDisplayNames.get(field));
								metadataDiv = metadataDiv.replace("NEW_FIELD_VALUE_REPLACE", latestFieldValue);
								metadataDiv = metadataDiv.replace("EMAIL_IDS_REPLACE", userEmail);
								metadataList = metadataList + metadataDiv;
							}
						}

					}
				}
			}
			if (!metadataList.isEmpty()) {

				Pattern p = Pattern.compile("<div class='header'>(.*)?<\\/div>(.*?)<div class='oldvalue",
						Pattern.DOTALL);
				Matcher match = p.matcher(metadataHtml);
				if (match.find()) {
					String metadataHeader = match.group(1);
					metadataHeader = metadataHeader.replace("MODULE_NAME_REPLACE",
							moduledoc.getString("SINGULAR_NAME"));
					metadataHeader = metadataHeader.replace("EMAIL_IDS_REPLACE", userEmail);
					metadataList = metadataHeader + metadataList;
				}
				JSONObject sender = new JSONObject();
				if (systemUserDoc != null) {
					sender.put("FIRST_NAME", systemUserDoc.getString("FIRST_NAME"));
					sender.put("LAST_NAME", systemUserDoc.getString("LAST_NAME"));
					sender.put("ROLE", systemUserDoc.getString("ROLE"));
					sender.put("USER_UUID", systemUserDoc.getString("USER_UUID"));
				}

				payloadWithMetadata.put("MESSAGE", metadataList);
				payloadWithMetadata.put("SENDER", sender);
				payloadWithMetadata.put("MESSAGE_TYPE", "META_DATA");
				payloadWithMetadata.put("MESSAGE_ID", UUID.randomUUID().toString());
				payloadWithMetadata.put("COMPANY_SUBDOMAIN", company.get("COMPANY_SUBDOMAIN"));
				payloadWithMetadata.put("ENTRY_ID", dataId);
				payloadWithMetadata.put("MODULE", moduleId);

				// CONNECT TO THE WEBSOCKET AND POST THE DISCUSSION MESSAGE

				String payload = payloadWithMetadata.toString();

				String url = "ws://" + managerHost + ":9081/ngdesk/ngdesk-websocket";
				ListenableFuture<StompSession> managerWebSocketSession = new ManagerWebSocket().connect(url);

				log.debug("Before getting stompSession");
				StompSession stompSession = managerWebSocketSession.get(1, TimeUnit.SECONDS);
				log.debug("After getting stompSession");
				stompSession.send("ngdesk/discussion", payload.getBytes());
				log.debug("After publish");
				stompSession.disconnect();

				log.trace("Exit addMetadataAfterFieldUpdate()");

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
