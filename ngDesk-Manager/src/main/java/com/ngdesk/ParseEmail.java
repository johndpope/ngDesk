package com.ngdesk;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.createuser.CreateUserController;
import com.ngdesk.flowmanager.Attachment;
import com.ngdesk.flowmanager.InputMessage;

@RestController
@Component
public class ParseEmail {

	private final Logger log = LoggerFactory.getLogger(ParseEmail.class);

	@Autowired
	private Global global;

	@Autowired
	private Environment env;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private SimpMessagingTemplate template;

	@Autowired
	private CreateUserController createUserController;

	@Autowired
	SendMail sendMail;

	@Autowired
	RedissonClient redisson;

	final int maxLengthInBytes = 104857600;

	@PostMapping("/emails/parse/new")
	public ResponseEntity<Object> newParseEmails(@RequestBody IncomingEmail email) {

		try {
			createUserAndStartWorkflowNew(email);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@PostMapping(value = "emails/parse")
	public ResponseEntity<Object> parseEmails(@RequestParam Map<String, String> inputMap, HttpServletRequest request)
			throws FileUploadException, IOException {

		log.trace("Enter ParseEmail.parseEmails()");

		try {
			JSONObject email = new JSONObject();
			JSONArray ccEmailsArr = new JSONArray();
			JSONArray attachments = new JSONArray();

			String emailPattern = "[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+";
			Pattern pattern = Pattern.compile(emailPattern);

			if (!ServletFileUpload.isMultipartContent(request)) {
				log.trace("Email has no attachments");

				for (String key : inputMap.keySet()) {
					if (key.equalsIgnoreCase("To") || key.equalsIgnoreCase("Delivered-To")
							|| key.equalsIgnoreCase("recipient")) {
						String[] emailArr = inputMap.get(key).split(",");
						boolean firstEmail = true;
						for (String toEmail : emailArr) {
							log.trace("to email:" + toEmail);

							Matcher matcher = pattern.matcher(toEmail);
							if (matcher.find()) {
								log.trace("matched email: " + matcher.group());
								firstEmail = false;
								JSONObject ccEmailObj = new JSONObject();
								String emailAddress = decodeUTF8(matcher.group().getBytes("ISO-8859-1")).toLowerCase();
								ccEmailObj.put("CC_EMAIL", emailAddress);
								ccEmailObj.put("IS_CC", false);

								boolean addToCc = true;
								for (int i = 0; i < ccEmailsArr.length(); i++) {
									JSONObject ccEmail = ccEmailsArr.getJSONObject(i);
									if (ccEmail.getString("CC_EMAIL").equalsIgnoreCase(emailAddress)) {
										addToCc = false;
										break;
									}
								}

								if (addToCc) {
									ccEmailsArr.put(ccEmailObj);
								}
							}

						}
					} else if (key.equalsIgnoreCase("From")) {
						Matcher matcher = pattern.matcher(inputMap.get(key));
						if (matcher.find()) {

							Pattern p = Pattern.compile("(.+) \\<");
							Matcher m = p.matcher(inputMap.get(key));
							if (m.find()) {
								String fullName = m.group(1);
								fullName = fullName.replaceAll("\"", "");
								if (fullName.contains(",")) {

									if (fullName.split(",").length > 1) {
										email.put("FIRST_NAME", fullName.split(",")[1].trim());
										email.put("LAST_NAME", fullName.split(",")[0].trim());
									} else {
										email.put("FIRST_NAME", fullName);
										email.put("LAST_NAME", "");
									}

								} else if (fullName.trim().contains(" ")) {
									int beginIndex = fullName.indexOf(" ");
									email.put("FIRST_NAME", fullName.split("\\s")[0].trim());
									email.put("LAST_NAME", fullName.substring(beginIndex + 1));
								} else {
									email.put("FIRST_NAME", fullName);
								}
							}
							String from = decodeUTF8(matcher.group().getBytes("ISO-8859-1"));
							from = from.split("@")[0].replaceAll("\\+(.*)", "") + "@" + from.split("@")[1];
							email.put("FROM", decodeUTF8(matcher.group().getBytes("ISO-8859-1")));
						}
					} else if (key.equalsIgnoreCase("sender")) {
						Matcher matcher = pattern.matcher(inputMap.get(key));
						if (matcher.find()) {
							String sender = decodeUTF8(matcher.group().getBytes("ISO-8859-1"));
							sender = sender.split("@")[0].replaceAll("\\+(.*)", "") + "@" + sender.split("@")[1];
							email.put("SENDER", sender);
						}
					} else if (key.equalsIgnoreCase("subject") || key.equalsIgnoreCase("Subject")) {
						if (!email.has("SUBJECT")) {
							email.put("SUBJECT", inputMap.get(key));
						}
					} else if (key.equalsIgnoreCase("Cc")) {
						String[] ccEmailsDetail = inputMap.get("Cc").split(",");
						for (String ccEmail : ccEmailsDetail) {
							Matcher matcher = pattern.matcher(ccEmail);
							if (matcher.find()) {
								JSONObject ccEmailObj = new JSONObject();
								String emailAddress = decodeUTF8(matcher.group().getBytes("ISO-8859-1")).toLowerCase();
								ccEmailObj.put("CC_EMAIL", emailAddress);
								ccEmailObj.put("IS_CC", true);

								// CHECK TO IGNORE DUPLICATE ENTRIES
								boolean addToCc = true;
								for (int i = 0; i < ccEmailsArr.length(); i++) {
									JSONObject ccEmailObject = ccEmailsArr.getJSONObject(i);
									if (ccEmailObject.getString("CC_EMAIL").equalsIgnoreCase(emailAddress)) {
										addToCc = false;
										break;
									}
								}
								if (addToCc) {
									ccEmailsArr.put(ccEmailObj);
								}
							}
						}
					} else if (key.equals("body-plain")) {
						String plainBody = inputMap.get(key).toString();
						plainBody = plainBody.replaceAll("\r\n", "<br>");
						email.put("BODY", plainBody);
					} else if (key.equals("body-html")) {
						String htmlContent = inputMap.get(key);
						email.put("BODY_HTML", htmlContent);
					}
				}
				email.put("CC_EMAILS", ccEmailsArr);

			} else {

				DiskFileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);

				List<FileItem> formItems = null;
				formItems = upload.parseRequest(request);

				if (formItems != null && formItems.size() > 0) {
					for (FileItem item : formItems) {
						if (item.isFormField()) {
							if (item.getFieldName().equalsIgnoreCase("To")
									|| item.getFieldName().equalsIgnoreCase("Delivered-To")
									|| item.getFieldName().equals("recipient")) {
								String[] emailArr = item.getString().split(",");

								boolean firstEmail = true;
								for (String toEmail : emailArr) {
									Matcher matcher = pattern.matcher(toEmail);
									if (matcher.find()) {
										JSONObject ccEmailObj = new JSONObject();

										String emailAddress = decodeUTF8(matcher.group().getBytes("ISO-8859-1"))
												.toLowerCase();
										ccEmailObj.put("CC_EMAIL", emailAddress);
										ccEmailObj.put("IS_CC", false);

										boolean addToCc = true;
										for (int i = 0; i < ccEmailsArr.length(); i++) {
											JSONObject ccEmail = ccEmailsArr.getJSONObject(i);
											if (ccEmail.getString("CC_EMAIL").equalsIgnoreCase(emailAddress)) {
												addToCc = false;
												break;
											}
										}

										if (addToCc) {
											ccEmailsArr.put(ccEmailObj);
										}

									}
								}

							} else if (item.getFieldName().equalsIgnoreCase("From")) {
								Matcher matcher = pattern.matcher(decodeUTF8(item.getString().getBytes("ISO-8859-1")));
								if (matcher.find()) {

									Pattern p = Pattern.compile("(.+) \\<");
									Matcher m = p.matcher(decodeUTF8(item.getString().getBytes("ISO-8859-1")));
									if (m.find()) {
										String fullName = m.group(1);
										fullName = fullName.replaceAll("\"", "");
										if (fullName.contains(",")) {
											email.put("FIRST_NAME", fullName.split(",")[1].trim());
											email.put("LAST_NAME", fullName.split(",")[0].trim());
										} else if (fullName.trim().contains(" ")) {
											int beginIndex = fullName.indexOf(" ");
											email.put("FIRST_NAME", fullName.split("\\s")[0].trim());
											email.put("LAST_NAME", fullName.substring(beginIndex + 1));
										} else {
											email.put("FIRST_NAME", fullName);
										}
									}
									String from = decodeUTF8(matcher.group().getBytes("ISO-8859-1"));
									from = from.split("@")[0].replaceAll("\\+(.*)", "") + "@" + from.split("@")[1];
									email.put("FROM", from);
								}
							} else if (item.getFieldName().equalsIgnoreCase("sender")) {
								Matcher matcher = pattern.matcher(decodeUTF8(item.getString().getBytes("ISO-8859-1")));
								if (matcher.find()) {
									String sender = decodeUTF8(matcher.group().getBytes("ISO-8859-1"));
									sender = sender.split("@")[0].replaceAll("\\+(.*)", "") + "@"
											+ sender.split("@")[1];
									email.put("SENDER", sender);
								}
							} else if (item.getFieldName().equalsIgnoreCase("subject")
									|| item.getFieldName().equalsIgnoreCase("Subject")) {
								if (!email.has("SUBJECT")) {
									email.put("SUBJECT", decodeUTF8(item.getString().getBytes("ISO-8859-1")));
								}
							} else if (item.getFieldName().equalsIgnoreCase("Cc")) {
								if (item.getString() != null) {
									String[] ccEmailsDetail = item.getString().split(",");
									for (String ccEmail : ccEmailsDetail) {
										Matcher matcher = pattern.matcher(ccEmail);
										if (matcher.find()) {
											JSONObject ccEmailObj = new JSONObject();
											String emailAddress = decodeUTF8(matcher.group().getBytes("ISO-8859-1"))
													.toLowerCase();
											ccEmailObj.put("CC_EMAIL", emailAddress);
											ccEmailObj.put("IS_CC", true);

											// CHECK TO IGNORE DUPLICATE ENTRIES
											boolean addToCc = true;
											for (int i = 0; i < ccEmailsArr.length(); i++) {
												JSONObject ccEmailObject = ccEmailsArr.getJSONObject(i);
												if (ccEmailObject.getString("CC_EMAIL")
														.equalsIgnoreCase(emailAddress)) {
													addToCc = false;
													break;
												}
											}
											if (addToCc) {
												ccEmailsArr.put(ccEmailObj);
											}
										}
									}
								}
							} else if (item.getFieldName().equalsIgnoreCase("body-plain")
									|| item.getFieldName().equalsIgnoreCase("body")
									|| item.getFieldName().equalsIgnoreCase("stripped-text")) {
								email.put("BODY", decodeUTF8(item.getString().getBytes("ISO-8859-1")));
							} else if (item.getFieldName().equalsIgnoreCase("body-html")) {
								String htmlContent = decodeUTF8(item.getString().getBytes("ISO-8859-1"));
								email.put("BODY_HTML", htmlContent);
							}

						} else {
							JSONObject attachment = new JSONObject();

							String fileName = new File(item.getName()).getName();
							attachment.put("FILE_NAME", fileName);
							if (fileName.contains(".")) {
								int idx = fileName.lastIndexOf('.');
								attachment.put("FILE_EXTENSION", fileName.substring(idx, fileName.length()));
							}
							byte[] bytes = IOUtils.toByteArray(item.getInputStream());
							String encoded = java.util.Base64.getEncoder().encodeToString(bytes);
							if (encoded.length() < 104000000) {
								attachment.put("FILE", encoded);
								attachments.put(attachment);
							}

							email.put("ATTACHMENTS", attachments);
						}
					}
					email.put("CC_EMAILS", ccEmailsArr);
				}
			}

			if (email.getString("FROM").toLowerCase().contains("spencer")
					|| email.getString("FROM").toLowerCase().contains("spenny")
					|| email.getString("FROM").toLowerCase().contains("rob")) {

				log.debug("Email: " + email.toString());
			}

			if (request.getContentLength() > maxLengthInBytes) {

				sendMail.send(email.getString("FROM"), "support@ngdesk.com",
						"RE: Failed to create ticket. " + email.getString("SUBJECT"),
						"Hello,<br/><br/>We are unable to process your email as its size has exceeded our limit of 10MB.<br/><br/>"
								+ "Thank you,<br/>" + "ngDesk Support Team<br/>support@ngdesk.com");

			} else {
				// SEND TO CREATE USER
				createUserAndStartWorkflow(inputMap, email);
			}

		} catch (Exception e) {
			global.notifySpencerAndShashank("Ticket Dropped");
			e.printStackTrace();

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();
			String environment = env.getProperty("env");
			if (environment.equals("prd")) {
				sendMail.send("spencer@allbluesolutions.com", "support@ngdesk.com",
						"RE: Failed to create ticket. " + "Ticket Dropped", sStackTrace);
			}
			e.printStackTrace();
			return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.trace("Exit ParseEmail.parseEmails()");
		return new ResponseEntity<Object>(HttpStatus.OK);
	}

	public void createUserAndStartWorkflowNew(IncomingEmail email) throws JsonProcessingException {
		log.trace("Enter ParseEmail.createUserSend()");

		try {
			InputMessage message = new InputMessage();

			String subdomain = null;
			String companyId = null;
			String channelEmail = null;
			Document company = null;
			boolean channelFound = false;

			MongoCollection<Document> globalCollection = mongoTemplate.getCollection("external_emails");
			List<String> ccsToRemove = new ArrayList<String>();

			List<EmailAddress> ccEmails = email.getEmailAddresses();

			// FETCH EXTERNAL CHANNEL EMAIL IF ANY
			for (int i = 0; i < ccEmails.size(); i++) {
				EmailAddress ccEmailObj = ccEmails.get(i);
				String ccEmail = ccEmailObj.getEmail();
				boolean isCc = false;

				if (ccEmailObj.getIsCc() != null) {
					isCc = ccEmailObj.getIsCc();
				} else {
					String environment = env.getProperty("env");
					if (environment.equals("prd")) {
						sendMail.send("spencer@allbluesolutions.com", "support@ngdesk.com",
								"RE: Failed to create ticket. " + "Ticket Dropped ", email.toString());
						sendMail.send("shashank@allbluesolutions.com", "support@ngdesk.com",
								"RE: Failed to create ticket. " + "Ticket Dropped ", email.toString());
						global.notifySpencerAndShashank("Ticket Dropped");
					}

				}

				// CHECK IF EMAIL IS NOT COMING FROM CC
				if (!isCc) {

					Document externalEmail = globalCollection.find(Filters.eq("EMAIL_ADDRESS", ccEmail)).first();
					if (externalEmail != null) {
						subdomain = externalEmail.getString("COMPANY_SUBDOMAIN");
						company = global.getCompanyFromSubdomain(subdomain);
						if (company != null) {

							companyId = company.getObjectId("_id").toString();
							channelFound = true;
							ccsToRemove.add(ccEmail);
							channelEmail = ccEmail;
							break;
						} else {
							// IF COMPANY NOT PRESENT FOR EXTERNAL EMAIL FOUND
							ccsToRemove.add(ccEmail);
						}
					}
				}
			}

			if (!channelFound) {
				// CHECK FOR INTERNAL CHANNEL
				for (int i = 0; i < ccEmails.size(); i++) {
					EmailAddress ccEmailObj = ccEmails.get(i);
					String ccEmail = ccEmailObj.getEmail();
					log.debug("cc email: " + ccEmail);
					boolean isCc = ccEmailObj.getIsCc();
					log.debug("is cc: " + isCc);

					// CHECK IF EMAIL IS NOT COMING FROM CC
					if (!isCc) {
						if (ccEmail.endsWith(".ngdesk.com")) {
							String companySubdomain = ccEmail.split("@")[1].split("\\.")[0];
							log.trace("Subdomain: " + companySubdomain);

							// HARDCODED FIX FOR BLUEMSP-NEW
							// TALK TO SHASHANK BEFORE MODIFYING
							if (companySubdomain.equalsIgnoreCase("bluemsp-new")) {
								companySubdomain = "subscribeit";
							}
							company = global.getCompanyFromSubdomain(companySubdomain);
							if (company != null) {
								companyId = company.getObjectId("_id").toString();
								MongoCollection<Document> channelsCollection = mongoTemplate
										.getCollection("channels_email_" + companyId);
								Document internalChannel = channelsCollection.find(Filters.eq("EMAIL_ADDRESS", ccEmail))
										.first();

								if (internalChannel != null) {
									if (!channelFound) {
										subdomain = companySubdomain;
										channelFound = true;
										channelEmail = ccEmail;
										ccsToRemove.add(ccEmail);
										break;
									}
								}
							} else {
								// IF COMPANY NOT PRESENT FOR INTERNAL EMAIL FOUND
								ccsToRemove.add(ccEmail);
							}
						}
					}
				}
			}

			// IF CHANNEL IS FOUND
			if (channelFound) {
				MongoCollection<Document> channelsCollection = mongoTemplate
						.getCollection("channels_email_" + companyId);

				for (int i = 0; i < ccEmails.size(); i++) {
					EmailAddress ccEmailObj = ccEmails.get(i);
					String ccEmail = ccEmailObj.getEmail();

					// ADD TO CCS_TO_REMOVE ARRAY IF ANY EXTERNAL OR INTERNAL EMAIL
					Document externalChannel = channelsCollection.find(Filters.eq("EMAIL_ADDRESS", ccEmail)).first();
					Document internalChannel = channelsCollection.find(Filters.eq("EMAIL_ADDRESS", ccEmail)).first();
					if (externalChannel != null || internalChannel != null) {
						ccsToRemove.add(ccEmail);
					}
				}
			}
			if (subdomain != null) {
				company = global.getCompanyFromSubdomain(subdomain);
			}
			log.debug("Subdomain: " + subdomain);
			log.debug("ChannelFound: " + channelFound);
			log.debug("ChannelEmail: " + channelEmail);

			log.debug("Checking If: " + (channelFound && channelEmail != null & company != null
					&& company.getString("VERSION").equals("v2")));

			if (channelFound && channelEmail != null & company != null && company.getString("VERSION").equals("v2")) {
				for (int i = ccEmails.size() - 1; i >= 0; i--) {
					EmailAddress ccEmailObj = ccEmails.get(i);
					String ccEmail = ccEmailObj.getEmail();
					if (ccsToRemove.contains(ccEmail)) {
						ccEmails.remove(i);
					}
				}
				message.setTo(channelEmail);
				log.trace("TO: " + channelEmail);

				companyId = company.getObjectId("_id").toString();
				String companyUUID = company.getString("COMPANY_UUID");
				message.setCompanyUUID(companyUUID);
				message.setType("email");
				Document channel = getEmailChannelFromAddress(channelEmail, companyId);

				if (channel != null) {

					String widgetId = channel.getObjectId("_id").toString();
					String channelName = channel.getString("NAME").toString();

					message.setWidgetId(widgetId);
					message.setChannelName(channelName);

					String from = email.getFrom().toLowerCase();

					String domain = from.split("@")[1];
					domain = domain.trim().toLowerCase();

					MongoCollection<Document> blackListWhiteListCollection = mongoTemplate
							.getCollection("blacklisted_whitelisted_emails_" + companyId);

					// WHITELISTED & BLACKLISTED EMAIL & DOMAIN CHECK
					Document whitelistedDomain = blackListWhiteListCollection
							.find(Filters.and(Filters.eq("TYPE", "INCOMING"), Filters.eq("EMAIL_ADDRESS", domain),
									Filters.eq("IS_DOMAIN", true), Filters.eq("STATUS", "WHITELIST")))
							.first();

					Document whitelistedEmail = blackListWhiteListCollection
							.find(Filters.and(Filters.eq("TYPE", "INCOMING"), Filters.eq("EMAIL_ADDRESS", from),
									Filters.eq("STATUS", "WHITELIST"), Filters.eq("IS_DOMAIN", false)))
							.first();

					Document blacklistedEmail = blackListWhiteListCollection
							.find(Filters.and(Filters.eq("TYPE", "INCOMING"), Filters.eq("EMAIL_ADDRESS", from),
									Filters.eq("STATUS", "BLACKLIST"), Filters.eq("IS_DOMAIN", false)))
							.first();

					Document blacklistedDomain = blackListWhiteListCollection
							.find(Filters.and(Filters.eq("TYPE", "INCOMING"), Filters.eq("EMAIL_ADDRESS", domain),
									Filters.eq("IS_DOMAIN", true), Filters.eq("STATUS", "BLACKLIST")))
							.first();

					boolean addToJob = true;
					boolean startWorkflow = true;

					// WHITELISTED DO NOT ADD TO LIST
					if (whitelistedDomain != null || whitelistedEmail != null) {
						addToJob = false;
					} else if (blacklistedEmail != null || blacklistedDomain != null) {
						startWorkflow = false;
					}

					if (addToJob) {
						// ADD TIMESTAMP TO MAP
						Timestamp currentTimestamp = new Timestamp(new Date().getTime());

						RMap<String, Map<String, List<Timestamp>>> incomingMails = redisson.getMap("incomingMails");

						if (!incomingMails.containsKey(companyId)) {
							incomingMails.put(companyId, new HashMap<String, List<Timestamp>>());
						}

						if (incomingMails.get(companyId).containsKey(from)) {

							Map<String, List<Timestamp>> map = incomingMails.get(companyId);
							List<Timestamp> timestamps = map.get(from);
							timestamps.add(currentTimestamp);

							map.put(from, timestamps);
							incomingMails.put(companyId, map);

						} else {
							Map<String, List<Timestamp>> map = incomingMails.get(companyId);

							List<Timestamp> timestamps = new ArrayList<Timestamp>();
							timestamps.add(currentTimestamp);
							map.put(from, timestamps);

							incomingMails.put(companyId, map);
						}
					}

					if (startWorkflow) {
						if (email.getFirstName() != null && !email.getFirstName().isBlank()) {
							message.setFirstName(email.getFirstName());
						}
						if (email.getLastName() != null && !email.getLastName().isBlank()) {
							message.setLastName(email.getLastName());
						} else {
							message.setLastName("");
						}

						if (email.getFirstName() == null || email.getFirstName().isBlank()) {
							String name = from.split("@")[0];
							message.setFirstName(name);
						}

						message.setFrom(from.toLowerCase());
						message.setEmailAddress(from.toLowerCase());
						message.setSubject(email.getSubject());
						if (email.getBodyHtml() != null) {
							message.setBody(email.getBodyHtml());
						} else {
							message.setBody(email.getBody());
						}

						List<String> emails = new ArrayList<String>();
						if (email.getEmailAddresses() != null) {
							for (int i = 0; i < ccEmails.size(); i++) {
								EmailAddress ccEmailObj = ccEmails.get(i);
								emails.add(ccEmailObj.getEmail().toLowerCase());
							}
							message.setCc(emails);

						} else {
							message.setCc(new ArrayList<String>());
						}

						if (email.getAttachments() != null) {
							message.setAttachments(email.getAttachments());
						} else {
							message.setAttachments(new ArrayList<Attachment>());
						}
						createUserController.doSubmit(message);
					}
				}
			}

			// TODO: Handle case
//			String to = email.getString("TO").toLowerCase();
//			// Support for ngdesk request to go to bluemsp support
//			if (to.equalsIgnoreCase("support@ngdesk.com")) {
//				to = "support@support.ngdesk.com";
//			}

		} catch (Exception e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();
			String environment = env.getProperty("env");
			if (environment.equals("prd")) {
				sendMail.send("spencer@allbluesolutions.com", "support@ngdesk.com",
						"RE: Failed to create ticket. " + "Ticket Dropped ", sStackTrace);
				sendMail.send("shashank@allbluesolutions.com", "support@ngdesk.com",
						"RE: Failed to create ticket. " + "Ticket Dropped ", sStackTrace);
				global.notifySpencerAndShashank("Ticket Dropped");
			}

		}
		log.trace("Exit ParseEmail.createUserSend()");
	}

	public void createUserAndStartWorkflow(Map<String, String> inputMap, JSONObject email)
			throws JsonProcessingException {
		log.trace("Enter ParseEmail.createUserSend()");

		try {
			InputMessage message = new InputMessage();

			String subdomain = null;
			String companyId = null;
			String channelEmail = null;
			Document company = null;
			boolean channelFound = false;

			MongoCollection<Document> globalCollection = mongoTemplate.getCollection("external_emails");

			List<String> ccsToRemove = new ArrayList<String>();
			JSONArray ccEmails = email.getJSONArray("CC_EMAILS");

			// FETCH EXTERNAL CHANNEL EMAIL IF ANY
			for (int i = 0; i < ccEmails.length(); i++) {
				JSONObject ccEmailObj = ccEmails.getJSONObject(i);
				String ccEmail = ccEmailObj.getString("CC_EMAIL");
				boolean isCc = false;

				if (ccEmailObj.has("IS_CC") && !ccEmailObj.isNull("IS_CC")) {
					isCc = ccEmailObj.getBoolean("IS_CC");
				} else {
					String environment = env.getProperty("env");
					if (environment.equals("prd")) {
						sendMail.send("spencer@allbluesolutions.com", "support@ngdesk.com",
								"RE: Failed to create ticket. " + "Ticket Dropped ", email.toString());
						sendMail.send("shashank@allbluesolutions.com", "support@ngdesk.com",
								"RE: Failed to create ticket. " + "Ticket Dropped ", email.toString());
						global.notifySpencerAndShashank("Ticket Dropped");
					}

				}

				// CHECK IF EMAIL IS NOT COMING FROM CC
				if (!isCc) {
					Document externalEmail = globalCollection.find(Filters.eq("EMAIL_ADDRESS", ccEmail)).first();
					if (externalEmail != null) {
						subdomain = externalEmail.getString("COMPANY_SUBDOMAIN");
						company = global.getCompanyFromSubdomain(subdomain);
						if (company != null) {
							companyId = company.getObjectId("_id").toString();
							channelFound = true;
							ccsToRemove.add(ccEmail);
							channelEmail = ccEmail;
							break;
						} else {
							// IF COMPANY NOT PRESENT FOR EXTERNAL EMAIL FOUND
							ccsToRemove.add(ccEmail);
						}
					}
				}
			}

			if (!channelFound) {
				// CHECK FOR INTERNAL CHANNEL
				for (int i = 0; i < ccEmails.length(); i++) {
					JSONObject ccEmailObj = ccEmails.getJSONObject(i);
					String ccEmail = ccEmailObj.getString("CC_EMAIL");
					log.debug("cc email: " + ccEmail);
					boolean isCc = ccEmailObj.getBoolean("IS_CC");
					log.debug("is cc: " + isCc);

					// CHECK IF EMAIL IS NOT COMING FROM CC
					if (!isCc) {
						if (ccEmail.endsWith(".ngdesk.com")) {
							String companySubdomain = ccEmail.split("@")[1].split("\\.")[0];
							log.trace("Subdomain: " + companySubdomain);
							company = global.getCompanyFromSubdomain(companySubdomain);

							if (company != null) {
								companyId = company.getObjectId("_id").toString();
								MongoCollection<Document> channelsCollection = mongoTemplate
										.getCollection("channels_email_" + companyId);
								Document internalChannel = channelsCollection.find(Filters.eq("EMAIL_ADDRESS", ccEmail))
										.first();

								if (internalChannel != null) {
									if (!channelFound) {
										subdomain = companySubdomain;
										channelFound = true;
										channelEmail = ccEmail;
										ccsToRemove.add(ccEmail);
										break;
									}
								}
							} else {
								// IF COMPANY NOT PRESENT FOR INTERNAL EMAIL FOUND
								ccsToRemove.add(ccEmail);
							}
						}
					}
				}
			}

			// IF CHANNEL IS FOUND
			if (channelFound) {
				MongoCollection<Document> channelsCollection = mongoTemplate
						.getCollection("channels_email_" + companyId);

				for (int i = 0; i < ccEmails.length(); i++) {
					JSONObject ccEmailObj = ccEmails.getJSONObject(i);
					String ccEmail = ccEmailObj.getString("CC_EMAIL");

					// ADD TO CCS_TO_REMOVE ARRAY IF ANY EXTERNAL OR INTERNAL EMAIL
					Document externalChannel = channelsCollection.find(Filters.eq("EMAIL_ADDRESS", ccEmail)).first();
					Document internalChannel = channelsCollection.find(Filters.eq("EMAIL_ADDRESS", ccEmail)).first();
					if (externalChannel != null || internalChannel != null) {
						ccsToRemove.add(ccEmail);
					}
				}
			}
			if (subdomain != null) {
				company = global.getCompanyFromSubdomain(subdomain);
			}
			log.debug("Subdomain: " + subdomain);
			log.debug("ChannelFound: " + channelFound);
			log.debug("ChannelEmail: " + channelEmail);

			log.debug("Checking If: " + (channelFound && channelEmail != null & company != null
					&& company.getString("VERSION").equals("v2")));

			if (channelFound && channelEmail != null & company != null && company.getString("VERSION").equals("v2")) {
				for (int i = ccEmails.length() - 1; i >= 0; i--) {
					JSONObject ccEmailObj = ccEmails.getJSONObject(i);
					String ccEmail = ccEmailObj.getString("CC_EMAIL");
					if (ccsToRemove.contains(ccEmail)) {
						ccEmails.remove(i);
					}
				}
				message.setTo(channelEmail);

				log.trace("TO: " + channelEmail);

				companyId = company.getObjectId("_id").toString();
				String companyUUID = company.getString("COMPANY_UUID");
				message.setCompanyUUID(companyUUID);
				message.setType("email");
				Document channel = getEmailChannelFromAddress(channelEmail, companyId);

				if (channel == null && email.has("SENDER") && email.get("SENDER") != null) {
					String sender = email.getString("SENDER");
					channel = getEmailChannelFromAddress(sender, companyId);
					message.setTo(sender);
				}

				if (channel != null) {

					String widgetId = channel.getObjectId("_id").toString();
					String channelName = channel.getString("NAME").toString();

					message.setWidgetId(widgetId);
					message.setChannelName(channelName);

					if (!email.has("FROM")) {
						email.put("FROM", email.getString("SENDER"));
					}

					String from = email.getString("FROM").toLowerCase();

					String domain = from.split("@")[1];
					domain = domain.trim().toLowerCase();

					MongoCollection<Document> blackListWhiteListCollection = mongoTemplate
							.getCollection("blacklisted_whitelisted_emails_" + companyId);

					// WHITELISTED & BLACKLISTED EMAIL & DOMAIN CHECK
					Document whitelistedDomain = blackListWhiteListCollection
							.find(Filters.and(Filters.eq("TYPE", "INCOMING"), Filters.eq("EMAIL_ADDRESS", domain),
									Filters.eq("IS_DOMAIN", true), Filters.eq("STATUS", "WHITELIST")))
							.first();

					Document whitelistedEmail = blackListWhiteListCollection
							.find(Filters.and(Filters.eq("TYPE", "INCOMING"), Filters.eq("EMAIL_ADDRESS", from),
									Filters.eq("STATUS", "WHITELIST"), Filters.eq("IS_DOMAIN", false)))
							.first();

					Document blacklistedEmail = blackListWhiteListCollection
							.find(Filters.and(Filters.eq("TYPE", "INCOMING"), Filters.eq("EMAIL_ADDRESS", from),
									Filters.eq("STATUS", "BLACKLIST"), Filters.eq("IS_DOMAIN", false)))
							.first();

					Document blacklistedDomain = blackListWhiteListCollection
							.find(Filters.and(Filters.eq("TYPE", "INCOMING"), Filters.eq("EMAIL_ADDRESS", domain),
									Filters.eq("IS_DOMAIN", true), Filters.eq("STATUS", "BLACKLIST")))
							.first();

					boolean addToJob = true;
					boolean startWorkflow = true;

					// WHITELISTED DO NOT ADD TO LIST
					if (whitelistedDomain != null || whitelistedEmail != null) {
						addToJob = false;
					} else if (blacklistedEmail != null || blacklistedDomain != null) {
						startWorkflow = false;
					}

					if (addToJob) {
						// ADD TIMESTAMP TO MAP
						Timestamp currentTimestamp = new Timestamp(new Date().getTime());

						RMap<String, Map<String, List<Timestamp>>> incomingMails = redisson.getMap("incomingMails");

						if (!incomingMails.containsKey(companyId)) {
							incomingMails.put(companyId, new HashMap<String, List<Timestamp>>());
						}

						if (incomingMails.get(companyId).containsKey(from)) {
							incomingMails.get(companyId).get(from).add(currentTimestamp);
						} else {
							List<Timestamp> timestamps = new ArrayList<Timestamp>();
							timestamps.add(currentTimestamp);
							incomingMails.get(companyId).put(from, timestamps);
						}
					}

					if (startWorkflow) {
						if (email.has("FIRST_NAME")) {
							message.setFirstName(email.getString("FIRST_NAME"));
						}
						if (email.has("LAST_NAME")) {
							message.setLastName(email.getString("LAST_NAME"));
						} else {
							message.setLastName("");
						}

						if (!email.has("FIRST_NAME")) {
							String name = from.split("@")[0];
							message.setFirstName(name);
						}

						message.setFrom(from.toLowerCase());
						message.setEmailAddress(from.toLowerCase());
						message.setSubject(email.getString("SUBJECT"));
						if (email.has("BODY_HTML")) {
							message.setBody(email.getString("BODY_HTML"));
						} else {
							message.setBody(email.getString("BODY"));
						}

						List<String> emails = new ArrayList<String>();
						if (email.has("CC_EMAILS")) {
							for (int i = 0; i < ccEmails.length(); i++) {
								JSONObject ccEmail = ccEmails.getJSONObject(i);
								emails.add(ccEmail.getString("CC_EMAIL").toLowerCase());
							}
							message.setCc(emails);

						} else {
							message.setCc(new ArrayList<String>());
						}

						if (email.has("ATTACHMENTS")) {
							JSONArray attachments = email.getJSONArray("ATTACHMENTS");
							List<Attachment> attachmentsList = new ArrayList<Attachment>();

							for (int i = 0; i < attachments.length(); i++) {
								JSONObject attachment = attachments.getJSONObject(i);
								Attachment attachmentObj = new Attachment();
								if (attachment.has("FILE_EXTENSION")) {
									String fileExtension = attachment.getString("FILE_EXTENSION");
									attachmentObj.setFileExtension(fileExtension);
								}
								String file = attachment.getString("FILE");
								String fileName = attachment.getString("FILE_NAME");

								attachmentObj.setFileName(fileName);
								attachmentObj.setFile(file);
								attachmentsList.add(attachmentObj);

							}
							message.setAttachments(attachmentsList);

						} else {
							message.setAttachments(new ArrayList<Attachment>());
						}
						createUserController.doSubmit(message);
					}
				}
			}

			// TODO: Handle case
//			String to = email.getString("TO").toLowerCase();
//			// Support for ngdesk request to go to bluemsp support
//			if (to.equalsIgnoreCase("support@ngdesk.com")) {
//				to = "support@support.ngdesk.com";
//			}

		} catch (Exception e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();
			String environment = env.getProperty("env");
			if (environment.equals("prd")) {
				sendMail.send("spencer@allbluesolutions.com", "support@ngdesk.com",
						"RE: Failed to create ticket. " + "Ticket Dropped ", sStackTrace);
				sendMail.send("shashank@allbluesolutions.com", "support@ngdesk.com",
						"RE: Failed to create ticket. " + "Ticket Dropped ", sStackTrace);
				global.notifySpencerAndShashank("Ticket Dropped");
			}

		}
		log.trace("Exit ParseEmail.createUserSend()");
	}

	private Document getEmailChannelFromAddress(String to, String companyId) {
		try {
			log.trace("Enter ParseEmail.getEmailChannelFromAddress() to: " + to + ", companyId: " + companyId);
			String collectionName = "channels_email_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document channel = collection.find(Filters.eq("EMAIL_ADDRESS", to)).first();
			log.trace("Exit ParseEmail.getEmailChannelFromAddress() to: " + to + ", companyId: " + companyId);
			return channel;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String decodeUTF8(byte[] bytes) {
		return new String(bytes, Charset.forName("UTF-8"));
	}

}