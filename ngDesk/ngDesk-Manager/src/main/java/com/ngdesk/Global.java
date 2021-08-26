package com.ngdesk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.discussion.Sender;
import com.twilio.Twilio;
import com.twilio.http.HttpMethod;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;

@Component
public class Global {
	private static final Logger logger = LoggerFactory.getLogger(Global.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	public static Map<String, JSONObject> errors = new HashMap<>();

	public static Map<String, JSONObject> translation = new HashMap<>();
	public static List<String> languages = new ArrayList<String>();
	public static HttpHeaders postHeaders;

	@Value("${twillo.account.sid}")
	public String ACCOUNT_SID;

	@Value("${twillo.auth.token}")
	public String AUTH_TOKEN;

	@Value("${firebase.authorisation.key}")
	private String firebaseAuthorisationKey;

	@Value("${twillo.from.number}")
	private String fromNumber;

	@Value("${twillo.phonecall.url}")
	private String phoneCallUrl;

	@Value("${env}")
	private String env;

	public String getDialCode(String countryCode) {
		try {
			logger.trace("Enter Global.getDialCode countryCode: " + countryCode);
			String countriesJson = getFile("countries.json");
			JSONObject countries = new JSONObject(countriesJson);
			JSONArray countriesArr = countries.getJSONArray("COUNTRIES");
			for (int i = 0; i < countriesArr.length(); i++) {
				JSONObject country = countriesArr.getJSONObject(i);
				if (countryCode.equalsIgnoreCase(country.getString("COUNTRY_CODE"))) {
					String dialCode = country.get("COUNTRY_DIAL_CODE").toString();
					return "+" + dialCode;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "+1";
	}

	public boolean isValidDate(String input) {
		if (input != null) {
			try {
				Instant instant = Instant.parse(input);
				Date dateValue = (Date) Date.from(instant);
				return true;
			} catch (DateTimeException e) {
				return false;
			}
		}
		return false;
	}

	public String getCountryCode(String dialCode) {
		logger.trace("Enter Global.getDialCode dialCode: " + dialCode);
		try {
			String countriesJson = getFile("countries.json");
			JSONObject countries = new JSONObject(countriesJson);
			JSONArray countriesArr = countries.getJSONArray("COUNTRIES");

			for (int i = 0; i < countriesArr.length(); i++) {
				JSONObject country = countriesArr.getJSONObject(i);
				if (dialCode.equalsIgnoreCase(country.getString("COUNTRY_DIAL_CODE"))) {
					String countryCode = country.get("COUNTRY_CODE").toString();
					return countryCode;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "us";
	}

	public String errorMsg(String language, String msgKey) {
		try {
			return errors.get(language).getString(msgKey);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return msgKey;
	}

	public String getTranslation(String language, String msgKey) {
		logger.trace("Language: " + language + ", MessageKey: " + msgKey);
		try {
			if (languages.contains(language)) {
				return translation.get(language).getString(msgKey);
			} else {
				return translation.get("en").getString(msgKey);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return msgKey;
	}

	public String getFormattedDate(Timestamp date) {
		String formattedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(date);
		return formattedDate;
	}

	public String getValue(String valuePattern, Map<String, Object> inputMessage) {
		try {
			String reg = "\\{\\{(.*?)\\}\\}";
			Pattern r = Pattern.compile(reg);
			Matcher m = r.matcher(valuePattern);

			while (m.find()) {
				String path = m.group(1);

				String sections[] = path.split("\\.");
				if (sections.length > 1 && sections[0].equalsIgnoreCase("INPUTMESSAGE")) {
					Object obj = inputMessage;
					for (int j = 1; j < sections.length; ++j) {
						obj = ((Map<String, Object>) obj).get(sections[j]);
						if (obj == null)
							break;
					}
					if (obj != null) {
						valuePattern = valuePattern.replaceAll("\\{\\{" + path + "\\}\\}", obj.toString());
					}
				}
			}
			return valuePattern;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Document getCompanyFromUUID(String uuid) {
		try {
			logger.trace("Enter Global.getCompanyFromUUID() uuid: " + uuid);
			String collectionName = "companies";
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document company = collection.find(Filters.eq("COMPANY_UUID", uuid)).first();
			logger.trace("Exit Global.getCompanyFromUUID() uuid: " + uuid);
			return company;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void notifySpencerAndShashank(String message) {

		try {
			logger.trace("Enter Global.notifySpencerAndShashank() message: " + message);
			if (env.equalsIgnoreCase("prd")) {
				Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
				Call.creator(new PhoneNumber("+14692009202"), new PhoneNumber(fromNumber),
						new URI(phoneCallUrl + "text=" + URLEncoder.encode(message, "utf-8"))).setMethod(HttpMethod.GET)
						.create();

				Call.creator(new PhoneNumber("+13126784446"), new PhoneNumber(fromNumber),
						new URI(phoneCallUrl + "text=" + URLEncoder.encode(message, "utf-8"))).setMethod(HttpMethod.GET)
						.create();
			}

			logger.trace("Enter Global.notifySpencerAndShashank() message: " + message);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Document getModuleFromId(String moduleId, String companyId) {
		try {
			logger.trace("Enter Global.getModuleFromId() moduleId: " + moduleId + ", companyId: " + companyId);
			String collectionName = "modules_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document company = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			logger.trace("Exit Global.getModuleFromId() moduleId: " + moduleId + ", companyId: " + companyId);
			return company;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String ordinal(int i) {
		String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
		switch (i % 100) {
		case 11:
		case 12:
		case 13:
			return i + "th";
		default:
			return i + sufixes[i % 10];

		}
	}

	public Document getFieldFromId(String fieldId, String moduleName, String companyId) {
		try {

			logger.trace("Enter Global.getFieldFromId() fieldId: " + fieldId + ", moduleName: " + moduleName
					+ ", companyId: " + companyId);

			Document fieldDocument = null;
			String collectionName = "modules_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document module = collection.find(Filters.eq("NAME", moduleName)).first();

			List<Document> fields = (List<Document>) module.get("FIELDS");

			for (Document field : fields) {
				if (fieldId.equals(field.getString("FIELD_ID"))) {
					fieldDocument = field;
					break;
				}
			}
			logger.trace("Exit Global.getFieldFromId() fieldId: " + fieldId + ", moduleName: " + moduleName
					+ ", companyId: " + companyId);
			return fieldDocument;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Document getUserFromUUID(String userUUID, String companyId) {
		try {

			logger.trace("Enter Global.getModuleFromId() userUUID: " + userUUID + ", companyId: " + companyId);
			String collectionName = "Users_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document user = collection
					.find(Filters.and(Filters.eq("USER_UUID", userUUID), Filters.eq("DELETED", false),
							Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
					.first();
			logger.trace("Exit Global.getModuleFromId() userUUID: " + userUUID + ", companyId: " + companyId);
			return user;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Document getCustomerFromUUID(String userUUID, String companyId) {
		try {
			logger.trace("Enter Global.getCustomerFromUUID() userUUID: " + userUUID + ", companyId: " + companyId);
			String collectionName = "Users_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document user = collection
					.find(Filters.and(Filters.eq("USER_UUID", userUUID), Filters.eq("DELETED", false),
							Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false))))
					.first();
			logger.trace("Exit Global.getCustomerFromUUID() userUUID: " + userUUID + ", companyId: " + companyId);
			return user;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Map<String, Object>> buildDiscussionPayload(Map<String, Object> inputMessage, String value,
			String messageType) {
		List<Map<String, Object>> payload = new ArrayList<Map<String, Object>>();
		try {
			logger.trace("Enter Global.buildDiscussionPayload()");

			Map<String, Object> message = new HashMap<String, Object>();

			Document company = getCompanyFromUUID(inputMessage.get("COMPANY_UUID").toString());
			String companyId = company.getObjectId("_id").toString();

			Document user = getCustomerFromUUID(inputMessage.get("USER_UUID").toString(), companyId);

			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			Document customerRole = rolesCollection.find(Filters.eq("NAME", "Customers")).first();
			String custolerRoleId = customerRole.getObjectId("_id").toString();

			Sender sender = new Sender(user.getString("FIRST_NAME"), user.getString("LAST_NAME"),
					user.getString("USER_UUID"), custolerRoleId);

			message.put("MESSAGE", StringEscapeUtils.escapeJava(value));
			message.put("DATE_CREATED", new Date());
			message.put("SENDER", sender);
			message.put("MESSAGE_TYPE", messageType);
			message.put("MESSAGE_ID", UUID.randomUUID().toString());

			payload.add(message);
			logger.trace("Exit Global.buildDiscussionPayload()");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return payload;

	}

	public String getSystemUser(String companyId) {
		String UUID = "";
		try {

			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
			Document systemUser = usersCollection.find(Filters.eq("EMAIL_ADDRESS", "system@ngdesk.com")).first();
			UUID = systemUser.getString("USER_UUID");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return UUID;
	}

	public String getCompanyId(String companyUUID) {
		logger.trace("Enter Global.getCompanyId() companyUUID: " + companyUUID);
		String companyId = null;
		try {
			String collectionName = "companies";
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document company = collection.find(Filters.eq("COMPANY_UUID", companyUUID)).first();
			companyId = company.getObjectId("_id").toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.trace("Exit Global.getCompanyId()companyUUID: " + companyUUID);
		return companyId;
	}

	public Document cleanMessageForPush(Document message) {
		logger.trace("Enter Global.cleanMessageForPush() message: " + message);
		String[] fields = { "MODULE", "COMPANY_UUID" };
		for (String field : fields) {
			message.remove(field);
		}
		logger.trace("Exit Global.cleanMessageForPush() message: " + message);
		return message;
	}

	public Map<String, String> getMessageHeaders() {
		logger.trace("Enter Global.getMessageHeaders()");
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("content-type", "application/json");
		headers.put("authorization", "key=" + firebaseAuthorisationKey);
		logger.trace("Exit Global.getMessageHeaders()");
		return headers;
	}

	public JSONObject getMessageData(String title, String body, String userWebToken, String subdomain, String url) {

		logger.trace("Enter Global.getMessageData() title: " + title + ", body: " + body + "userWebtoken: "
				+ userWebToken + ", subdomain: " + subdomain);
		JSONObject data = new JSONObject();
		JSONObject notification = new JSONObject();

		notification.put("title", title);
		notification.put("body", body);
		notification.put("icon", "https://signup.ngdesk.com/landing-pages/assets/images/ngdesk-logo-text.png");
		// IF THERE IS CUSTOM URL THE URL WILL NOT BE NULL
		if (url != null) {
			notification.put("click_action", url);
		} else {
			notification.put("click_action", "https://" + subdomain + ".ngdesk.com");
		}

		data.put("notification", notification);
		data.put("to", userWebToken);
		logger.trace("Exit Global.getMessageData() title: " + title + ", body: " + body + "userWebtoken: "
				+ userWebToken + ", subdomain: " + subdomain);
		return data;
	}

	public Document getCompanyFromSubdomain(String subdomain) {
		try {
			logger.trace("Enter Global.getCompanyFromSubdomain() subdomain: " + subdomain);
			String collectionName = "companies";
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document company = collection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
			logger.trace("Exit Global.getCompanyFromSubdomain() subdomain: " + subdomain);
			return company;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isValidStatus(String status) {
		if (status.equals("ONLINE") || status.equals("OFFLINE") || status.equals("BUSY") || status.equals("AWAY")) {
			return true;
		} else {
			return false;
		}
	}

	public String getFile(String fileName) {
		StringBuilder result = new StringBuilder("");
		try {
			logger.trace("Enter Global.getFile() fileName: " + fileName);
			String operatingSystem = System.getProperty("os.name");

			if (operatingSystem.equalsIgnoreCase("Windows 10")) {
				File file = new ClassPathResource(fileName).getFile();
				Scanner scanner = new Scanner(file);
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					result.append(line).append("\n");
				}

				scanner.close();
			} else {
				Resource res = new ClassPathResource(fileName);
				// Resource res = new ClassPathResource(fileName);
				// File file = new ClassPathResource(fileName).getFile();
				InputStream is = res.getInputStream();
				Scanner scanner = new Scanner(is);
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					result.append(line).append("\n");
				}
				scanner.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.trace("Exit Global.getFile() fileName: " + fileName);
		return result.toString().trim();
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

}
