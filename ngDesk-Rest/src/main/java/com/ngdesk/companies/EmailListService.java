package com.ngdesk.companies;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.redisson.api.RMap;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.campaigns.Address;
import com.ngdesk.campaigns.Campaigns;
import com.ngdesk.campaigns.Column;
import com.ngdesk.campaigns.ColumnSettings;
import com.ngdesk.campaigns.Footer;
import com.ngdesk.campaigns.Row;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.fields.Field;
import com.ngdesk.modules.rules.Condition;
import com.ngdesk.roles.RoleService;

@RestController
public class EmailListService {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(EmailListService.class);

	@Autowired
	RedissonClient redisson;

	@Value("${email.host}")
	private String host;

	@PostMapping("/companies/email_list")
	public EmailList postEmailList(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestBody @Valid EmailList emailLists) {
		try {
			log.trace("Enter EmailListService.postEmailList()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			String name = emailLists.getName();

			emailLists.setCreatedBy(userId);
			emailLists.setLastUpdatedBy(userId);

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (emailLists != null) {
				// FETCHING USER MODULE AS EMAIL LISTS IS RELATED TO USERS MODULE
				MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
				Document moduleDoc = modulesCollection.find(Filters.eq("NAME", "Users")).first();

				// FETCHING LIST OF TEAMS IN THE WHOLE SYSTEM IN ORDER TO GET ALL TEAM IDS.
				List<String> teamLists = new ArrayList<String>();
				MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
				List<Document> teamDocuments = teamsCollection.find().into(new ArrayList<Document>());
				for (Document teamDoc : teamDocuments) {
					teamLists.add(teamDoc.getObjectId("_id").toString());
				}

				// LOOPING THROUGH THE CONDITIONS IF IT CONTAINS TEAMS FIELD AS CONDITION
				for (Condition condition : emailLists.getConditions()) {
					String fieldId = condition.getCondition();
					String conditionValue = condition.getConditionValue();
					String moduleId = moduleDoc.getObjectId("_id").toString();
					Field field = getField(fieldId, companyId, moduleId);
					// VALIDATE THE CONDITIONS IF USER HAS ENTERED CORRECT TEAM OR NOT
					if (field != null) {
						if (field.getName().equalsIgnoreCase("TEAMS") && conditionValue != null) {
							if (!teamLists.contains(conditionValue)) {
								throw new BadRequestException("INVALID_TEAM");
							}
						}
					}
				}
			}

			MongoCollection<Document> emailListsCollection = mongoTemplate.getCollection("email_lists_" + companyId);
			String collectionName = "email_lists_" + companyId;

			if (global.isExists("NAME", name, collectionName)) {
				throw new BadRequestException("EMAIL_LIST_ALREADY_EXISTS");
			}
			String json = new ObjectMapper().writeValueAsString(emailLists);
			Document emailList = Document.parse(json);
			emailList.remove("EMAIL_LIST_ID");
			emailList.put("DATE_CREATED", new Date());
			emailList.put("DATE_UPDATED", new Date());
			emailListsCollection.insertOne(emailList);
			emailLists.setEmailListId(emailList.getObjectId("_id").toString());

			log.trace("Exit EmailListService.postEmailList()");
			return emailLists;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/companies/email_list/{id}")
	public EmailList putEmailList(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid, @PathVariable("id") String id,
			@RequestBody @Valid EmailList emailLists) {
		try {
			log.trace("Enter EmailListService.putEmailList()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (!ObjectId.isValid(id)) {
				throw new BadRequestException("EMAIL_LIST_NOT_FOUND");
			}

			// FETCHING USER MODULE AS EMAIL LISTS IS RELATED TO USERS MODULE
			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			Document moduleDoc = modulesCollection.find(Filters.eq("NAME", "Users")).first();

			// FETCHING LIST OF TEAMS IN THE WHOLE SYSTEM IN ORDER TO GET ALL TEAM IDS.
			List<String> teamLists = new ArrayList<String>();
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			List<Document> teamDocuments = teamsCollection.find().into(new ArrayList<Document>());
			for (Document teamDoc : teamDocuments) {
				teamLists.add(teamDoc.getObjectId("_id").toString());
			}

			// LOOPING THROUGH THE CONDITIONS IF IT CONTAINS TEAMS FIELD AS CONDITION
			for (Condition condition : emailLists.getConditions()) {
				String fieldId = condition.getCondition();
				String conditionValue = condition.getConditionValue();
				String moduleId = moduleDoc.getObjectId("_id").toString();
				Field field = getField(fieldId, companyId, moduleId);
				// VALIDATE THE CONDITIONS IF USER HAS ENTERED CORRECT TEAM OR NOT
				if (field.getName().equalsIgnoreCase("TEAMS") && conditionValue != null) {
					if (!teamLists.contains(conditionValue)) {
						throw new BadRequestException("INVALID_TEAM");
					}
				}

			}

			emailLists.setLastUpdatedBy(userId);

			MongoCollection<Document> emailListsCollection = mongoTemplate.getCollection("email_lists_" + companyId);
			Document existingEmailList = emailListsCollection.find(Filters.eq("_id", new ObjectId(id))).first();
			if (existingEmailList == null) {
				throw new BadRequestException("EMAIL_LIST_NOT_FOUND");
			}

			String json = new ObjectMapper().writeValueAsString(emailLists);
			Document updateEmailList = Document.parse(json);
			updateEmailList.remove("EMAIL_LIST_ID");
			updateEmailList.append("DATE_UPDATED", new Date());
			emailListsCollection.findOneAndReplace(Filters.eq("_id", new ObjectId(id)), updateEmailList);
			log.trace("Exit EmailListService.putEmailList()");

			return emailLists;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");

	}

	@DeleteMapping("/companies/email_list/{id}")
	public ResponseEntity<Object> deleteEmailList(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("id") String id) {
		log.trace("Enter EmailListService.deleteEmailList()");

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(id)) {
				throw new BadRequestException("EMAIL_LIST_NOT_FOUND");
			}
			MongoCollection<Document> emailListCollection = mongoTemplate.getCollection("email_lists_" + companyId);
			Document removed = emailListCollection.findOneAndDelete(Filters.eq("_id", new ObjectId(id)));
			if (removed == null) {
				throw new BadRequestException("EMAIL_LIST_NOT_FOUND");
			}
			log.trace("Exit EmailListService.deleteEmailList()");
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/companies/campaigns/send")
	public ResponseEntity<Object> sendCampaign(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "campaign_id", required = true) String campaignId) {
		try {
			log.trace("Enter EmailListService.sendCampaign()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String userCompanyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(userRole, userCompanyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String epochDate = "01/01/1970";
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
			Timestamp epoch = new Timestamp(date.getTime());
			Timestamp today = new Timestamp(new Date().getTime());

			MongoCollection<Document> campaignsCollection = mongoTemplate.getCollection("campaigns_" + userCompanyId);
			Document campaignDoc = campaignsCollection.find(Filters.eq("_id", new ObjectId(campaignId))).first();

			ArrayList<String> recipientUsers = (ArrayList<String>) campaignDoc.get("RECIPIENT_USERS");
			ArrayList<String> recipientLists = (ArrayList<String>) campaignDoc.get("RECIPIENT_LISTS");
			if (recipientUsers.isEmpty() && recipientLists.isEmpty()) {
				throw new BadRequestException("CAMPAIGN_RECIPIENTS_REQUIRED");
			}

			RSortedSet<Long> campaignScheduledTimes = redisson.getSortedSet("campaignScheduledTimes");
			RMap<Long, String> campaigns = redisson.getMap("campaigns");

			Map<Long, String> campaignsList = new HashMap<Long, String>(campaigns);
			for (Map.Entry campaign : campaignsList.entrySet()) {
				Long campaignTime = (Long) campaign.getKey();
				String campaignJson = (String) campaign.getValue();
				JSONObject campaignObj = new JSONObject(campaignJson);
				if (campaignObj.get("CAMPAIGN_ID").equals(campaignId)) {
					campaigns.remove(campaignTime);
					campaignScheduledTimes.remove(campaignTime);
				}
			}

			JSONObject updateCampaignObj = new JSONObject(campaignDoc.toJson());
			long currentTimeDiff = today.getTime() - epoch.getTime();
			updateCampaignObj.put("STATUS", "Processing");
			if (campaignDoc.getString("SEND_OPTION").equals("Send later")
					&& (!campaignDoc.getString("SEND_DATE").equals("")
							|| !campaignDoc.getString("SEND_DATE").equals(null))) {
				String sendDateString = campaignDoc.getString("SEND_DATE");
				Date sendDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(sendDateString);
				currentTimeDiff = sendDate.getTime() - epoch.getTime();
				updateCampaignObj.put("STATUS", "Scheduled");
			}

			while (campaignScheduledTimes.contains(currentTimeDiff)) {
				currentTimeDiff += 1;
			}

			campaignScheduledTimes.add(currentTimeDiff);

			JSONObject campaign = new JSONObject(campaignDoc.toJson().toString());
			campaign.put("COMPANY_ID", userCompanyId);
			campaign.put("CAMPAIGN_ID", campaignId);
			campaign.put("UUID", uuid);

			campaigns.put(currentTimeDiff, campaign.toString());

			updateCampaignObj.remove("_id");
			updateCampaignObj.put("CAMPAIGN_ID", campaignId);

			Document updateCampaign = Document.parse(updateCampaignObj.toString());
			campaignsCollection.findOneAndReplace(Filters.eq("_id", new ObjectId(campaignId)), updateCampaign);

			log.trace("Exit EmailListService.sendCampaign()");
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/companies/campaigns/send/test")
	public ResponseEntity<Object> sendTestCampaign(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestBody @Valid Campaigns campaign) {
		try {
			log.trace("Enter EmailListService.sendCampaign()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String userCompanyId = user.getString("COMPANY_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");
			String fromEmail = "no-reply@" + companySubdomain + ".ngdesk.com";
			String redirectPage = "https://" + companySubdomain + ".ngdesk.com/redirect/redirect.html";

			if (!roleService.isSystemAdmin(userRole, userCompanyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + userCompanyId);
			String usersModuleId = modulesCollection.find(Filters.eq("NAME", "Users")).first().getObjectId("_id")
					.toString();
			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + userCompanyId);

			String body = createCampaignBody(campaign, userCompanyId);
			String subject = campaign.getSubject();
			String reg = "\\{\\{(?i)(inputMessage[_a-zA-Z\\.\\-]+)\\}\\}";
			Pattern r = Pattern.compile(reg);

			List<String> usersToSend = new ArrayList<String>();
			usersToSend = campaign.getRecipientUsers();

			for (String userIdToSend : usersToSend) {

				Document previewUserDoc = null;
				String newBody = body;
				String newSubject = subject;
				List<Bson> sendFilters = new ArrayList<Bson>();
				sendFilters.add(Filters.eq("_id", new ObjectId(userIdToSend)));
				newSubject = "Preview - " + subject;
				if (campaign.getPreviewUser() != null) {
					String previewUserId = campaign.getPreviewUser();
					previewUserDoc = usersCollection.find(Filters.eq("_id", new ObjectId(previewUserId))).first();
				}

				Document userToSendDoc = usersCollection.find(Filters.and(sendFilters)).first();

				if (userToSendDoc != null) {
					String userEmail = userToSendDoc.getString("EMAIL_ADDRESS");
					String userUUID = userToSendDoc.getString("USER_UUID");

					String unsubscribeLink = "https://" + companySubdomain
							+ ".ngdesk.com/unsubscribe-to-marketing-email?uuid=" + userUUID + "&email=" + userEmail;
					String emailBlastHTML = global.getFile("emails_blasts_with_unsubscribe.html");
					if (campaign.getCampaignType().equals("Welcome")) {
						emailBlastHTML = global.getFile("email_welcome_template.html");
					} else if (campaign.getCampaignType().equals("Simple")) {
						emailBlastHTML = global.getFile("email_simple_template.html");
					}

					if (r != null) {
						Matcher matcher = r.matcher(newBody);
						while (matcher.find()) {
							String path = matcher.group(1).split("(?i)inputMessage\\.")[1];
							String firstField = path;
							if (path.contains(".")) {
								firstField = path.split("\\.")[0];
							}
							if (userToSendDoc.containsKey(firstField)) {
								String value = global.getValue(path, userToSendDoc, userCompanyId, usersModuleId,
										userIdToSend, false);
								if (previewUserDoc != null) {
									value = global.getValue(path, previewUserDoc, userCompanyId, usersModuleId,
											userIdToSend, false);
								}
								newBody = newBody.replace("{{inputMessage." + path + "}}", value);
								matcher = r.matcher(newBody);
							} else {
								newBody = newBody.replace("{{inputMessage." + path + "}}", "");
								matcher = r.matcher(newBody);
							}

						}

						Matcher matcher2 = r.matcher(newSubject);
						while (matcher2.find()) {
							String path = matcher2.group(1).split("(?i)inputMessage\\.")[1];
							String firstField = path;
							if (path.contains(".")) {
								firstField = path.split("\\.")[0];
							}
							if (userToSendDoc.containsKey(firstField)) {
								String value = global.getValue(path, userToSendDoc, userCompanyId, usersModuleId,
										userIdToSend, false);
								if (previewUserDoc != null) {
									value = global.getValue(path, previewUserDoc, userCompanyId, usersModuleId,
											userIdToSend, false);
								}
								newSubject = newSubject.replace("{{inputMessage." + path + "}}", value);
								matcher2 = r.matcher(newSubject);
							} else {
								newSubject = newSubject.replace("{{inputMessage." + path + "}}", "");
								matcher2 = r.matcher(newSubject);
							}
						}
					}
					if (newBody.contains("TRACKING_IMAGE_REPLACE")) {
						Pattern trackingImagePattern = Pattern.compile("TRACKING_IMAGE_REPLACE_(.*?)\"");
						Matcher trackingImageIdMatcher = trackingImagePattern.matcher(newBody);
						while (trackingImageIdMatcher.find()) {
							String imageUrl = "https://" + companySubdomain
									+ ".ngdesk.com/ngdesk-rest/ngdesk/companies/gallery/image/"
									+ trackingImageIdMatcher.group(1) + "?email_address=" + userEmail + "&company_id="
									+ userCompanyId + "&user_company_id=" + userCompanyId;
							trackingImageIdMatcher.replaceFirst(imageUrl);
							newBody = newBody.replace("TRACKING_IMAGE_REPLACE_" + trackingImageIdMatcher.group(1),
									imageUrl);
							trackingImageIdMatcher = trackingImagePattern.matcher(newBody);
						}
					}

					if (newBody.contains("IMAGE_REPLACE")) {
						Pattern imagePattern = Pattern.compile("IMAGE_REPLACE_(.*?)\"");
						Matcher imageIdMatcher = imagePattern.matcher(newBody);
						while (imageIdMatcher.find()) {
							String imageUrl = "https://" + companySubdomain
									+ ".ngdesk.com/ngdesk-rest/ngdesk/companies/gallery/image/"
									+ imageIdMatcher.group(1) + "?email_address=" + userEmail + "&company_id="
									+ userCompanyId + "&user_company_id=" + userCompanyId + "&campaign_id="
									+ campaign.getCampaignId();
							imageIdMatcher.replaceFirst(imageUrl);
							newBody = newBody.replace("IMAGE_REPLACE_" + imageIdMatcher.group(1), imageUrl);
							imageIdMatcher = imagePattern.matcher(newBody);
						}
					}
					if (newBody.contains("ADDRESS_REPLACE")) {
						Pattern footerPattern = Pattern.compile("ADDRESS_REPLACE(.*?)\"");
						Matcher footerMatcher = footerPattern.matcher(newBody);
						while (footerMatcher.find()) {
							Address footer = (campaign.getFooter().getAddress());
							String address = footer.getCompanyName() + "<br>" + footer.getAddress1() + ", "
									+ footer.getAddress2() + "<br>" + footer.getCity() + ", " + footer.getState() + ", "
									+ footer.getCountry() + "-" + footer.getZipCode() + "<br>" + footer.getPhone();
							footerMatcher.replaceFirst(address);
							newBody = newBody.replace("ADDRESS_REPLACE", address);
							footerMatcher = footerPattern.matcher(newBody);
						}
					}

					if (newBody.contains("REDIRECT_URL_REPLACE")) {
						redirectPage = redirectPage + "?company_id=" + userCompanyId + "&user_company_id="
								+ userCompanyId + "&subdomain=" + companySubdomain;
						Pattern redirectPattern = Pattern.compile("REDIRECT_URL_REPLACE");
						Matcher redirectVarMatcher = redirectPattern.matcher(newBody);
						while (redirectVarMatcher.find()) {
							redirectVarMatcher.replaceFirst(redirectPage);
							newBody = newBody.replace("REDIRECT_URL_REPLACE", redirectPage);
							redirectVarMatcher = redirectPattern.matcher(newBody);
						}
					}

					emailBlastHTML = emailBlastHTML.replaceAll("BODY", newBody);
					emailBlastHTML = emailBlastHTML.replaceAll("UNSUBSCRIBE_LINK", unsubscribeLink);

					SendEmail sendCampaign = new SendEmail(userEmail, fromEmail, newSubject, emailBlastHTML, host);
					sendCampaign.sendEmail();
				} else {
					// user not found
				}
			}

			log.trace("Exit EmailListService.sendCampaign()");
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public String createCampaignBody(Campaigns campaign, String userCompanyId) {
		List<Row> rows = campaign.getRows();
		String campaignTemplate = getCampaignTemplate(campaign.getCampaignType());
		StringBuilder body = new StringBuilder();
		for (Row row : rows) {
			StringBuilder rowHTML = new StringBuilder();
			for (Column column : row.getColumns()) {
				StringBuilder columnStr = new StringBuilder();
				ColumnSettings columnSettings = column.getSettings();
				switch (column.getType()) {
				case ("TEXT"): {
					columnStr.append("<p style=\"margin: 0;\">" + column.getSettings().getText() + "</p>");
					break;
				}
				case ("BUTTON"): {
					String buttonStyle = "font-weight: " + columnSettings.getFontWeight()
							+ "; padding: 0 16px; line-height: " + (columnSettings.getFontSize() - 14) + 36
							+ "px; cursor: pointer; text-shadow: none;color: " + columnSettings.getTextColor()
							+ ";border-radius: " + columnSettings.getCornerRadius() + "px; background-color: "
							+ columnSettings.getBackgroundColor() + "; font-size: " + columnSettings.getFontSize()
							+ "px; font-style: " + columnSettings.getFontItalics() + "; text-decoration: "
							+ columnSettings.getFontUnderline() + "; font-family: "
							+ (columnSettings.getFontFamily().equals("Roboto") ? "Roboto, Helvetica Neue, sans-serif"
									: columnSettings.getFontFamily())
							+ "; min-width: inherit;";
					String redirectString = "";
					String redirectStyle = "";
					String divStyle = "margin:0;";
					if (columnSettings.getHasBorder()) {
						buttonStyle += "border:" + columnSettings.getBorderWidth() + "px solid "
								+ columnSettings.getBorderColor() + ";";
					}
					if (columnSettings.getHasFullWidth()) {
						redirectStyle += "min-width:100%;";
					} else {
						String buttonAlignment = columnSettings.getAlignment();
						if (buttonAlignment.equals("flex-start")) {
							buttonAlignment = "start";
						} else if (buttonAlignment.equals("flex-end")) {
							buttonAlignment = "end";
						}
						divStyle += "text-align: " + buttonAlignment + ";";
					}
					String buttonString = "<button id=\"button_" + columnSettings.getId() + "\" style=\"" + buttonStyle
							+ "\">" + columnSettings.getText() + "</button>";
					String contentString = buttonString;
					if (columnSettings.getLinkTo().equals("URL") && !columnSettings.getLinkValue().equals("")) {
						buttonStyle = "font-weight: inherit; padding: 0 16px; line-height: inherit; cursor: pointer; text-shadow: none;color: inherit;border-radius: "
								+ columnSettings.getCornerRadius() + "px; background-color: "
								+ columnSettings.getBackgroundColor() + ";";
						redirectStyle += "display: inline-block; color: " + columnSettings.getTextColor()
								+ ";line-height: " + ((columnSettings.getFontSize() - 14) + 36) + "px; font-family: "
								+ (columnSettings.getFontFamily().equals("Roboto")
										? "Roboto, Helvetica Neue, sans-serif"
										: columnSettings.getFontFamily())
								+ "; font-weight: " + columnSettings.getFontWeight() + ";font-size: "
								+ columnSettings.getFontSize() + "px; font-style: " + columnSettings.getFontItalics()
								+ "; text-decoration: " + columnSettings.getFontUnderline() + ";";
						redirectString = "<a href=\"REDIRECT_URL_REPLACE&button=" + columnSettings.getId()
								+ "&redirect_url=" + columnSettings.getLinkValue() + "\" target=\"_blank\" style=\""
								+ redirectStyle + "\">" + buttonString + "</a>";
						contentString = redirectString;
					}
					columnStr.append("<p style=\"" + divStyle + "\">" + contentString + "</p>");
					break;
				}
				case ("IMAGE"): {
					String imageAlignment = columnSettings.getAlignment();
					if (columnSettings.getAlignment().equals("flex-start")) {
						imageAlignment = "start";
					} else if (columnSettings.getAlignment().equals("flex-end")) {
						imageAlignment = "end";
					}
					if (!columnSettings.getLinkValue().equals("") && !columnSettings.getLinkValue().equals(null)) {
						columnStr.append("<p style=\"margin: 0;\"><div style=\"height: " + columnSettings.getHeight()
								+ "px; text-align: " + imageAlignment
								+ "\"><a href=\"REDIRECT_URL_REPLACE&redirect_url=" + columnSettings.getLinkValue()
								+ "\" target=\"_blank\"><img src=\"TRACKING_IMAGE_REPLACE_" + columnSettings.getId()
								+ "\" alt=\"" + columnSettings.getAlternateText() + "\" style=\"width: "
								+ columnSettings.getWidth()
								+ "px; object-fit: contain; overflow: hidden;\"></a></div></p>");
					} else {
						columnStr.append("<p style=\"margin: 0\"><div style=\"height: " + columnSettings.getHeight()
								+ "px; text-align: " + imageAlignment + "\"><img src=\"IMAGE_REPLACE_"
								+ columnSettings.getId() + "\" alt=\"" + columnSettings.getAlternateText()
								+ "\" style=\"width: " + columnSettings.getWidth()
								+ "px; object-fit: contain; overflow: hidden;\"></div></p>");
					}

					break;
				}
				}
				String columnHTML = getColumnHTML(column.getWidth());
				columnHTML = columnHTML.replace("$content", columnStr);
				rowHTML.append(columnHTML);
			}
			body.append("<div style=\"display: flex; flex-direction: row\">" + rowHTML + "</div>");
		}

		Footer footer = campaign.getFooter();
		String footerAlignment = footer.getAlignment();
		if (footer.getAlignment().equals("flex-start")) {
			footerAlignment = "start";
		} else if (footer.getAlignment().equals("flex-end")) {
			footerAlignment = "end";
		}

		MongoCollection<Document> galleryCollection = mongoTemplate.getCollection("image_gallery");
		Document defaultImage = galleryCollection
				.find(Filters.and(Filters.eq("COMPANY_ID", userCompanyId), Filters.eq("LOGO.FILENAME", "ngdesk.png")))
				.first();
		String trackingImageId = defaultImage.remove("_id").toString();

		campaignTemplate = campaignTemplate.replace("$body", body);
		campaignTemplate = campaignTemplate.replace("$imageId", trackingImageId);
		campaignTemplate = campaignTemplate.replace("$footerAlignment", footerAlignment);

		return campaignTemplate;
	}

	private String getCampaignTemplate(String type) {
		String campaignTemplate = "";
		switch (type) {
		case ("Welcome"): {
			campaignTemplate = "<div style=\"background: white;padding: 10px 20px;\"><div style=\"flex-direction: row; display: flex; place-content: stretch center; align-items: stretch;\"><div style=\"flex: 1 1 100%;width: 100%;\">$body</div></div></div><div id=\"footer\" style=\"text-align: $footerAlignment;padding: 10px 20px;font-family: "
					+ "Arial;margin: 0 10px;\"><label>ADDRESS_REPLACE</label><br><label><a target=\"_blank\" href=\"UNSUBSCRIBE_LINK\">Unsubscribe</a></label><div><div style=\"display: inline-block; "
					+ "vertical-align: middle;width: 25px; height: 25px;margin-right: 10px;\"><a href=\"https://www.ngdesk.com\" target=\"_blank\"><img src=\"TRACKING_IMAGE_REPLACE_$imageId\" "
					+ "alt=\"logo\" style=\"width: 100%; height: 100%; object-fit: contain; overflow: hidden;\"></a></div><div style=\"display: inline-block; vertical-align: middle;\">"
					+ "<label><a href=\"https://www.ngdesk.com\" target=\"_blank\">Powered by ngDesk</a> &#169;</label></div></div></div>";
			break;
		}
		case ("Simple"): {
			campaignTemplate = "<div style=\"padding: 10px 20px;\"><div style=\"flex-direction: row; display: flex; place-content: stretch center; align-items: stretch;\"><div style=\"flex: 1 1 100%;width: 100%;\">$body</div></div></div><div id=\"footer\" style=\"text-align: $footerAlignment;padding: 10px 20px;font-family: Arial;margin: 0 10px;\">"
					+ "<label>ADDRESS_REPLACE</label><br><label><a target=\"_blank\" href=\"UNSUBSCRIBE_LINK\">Unsubscribe</a></label><div><div style=\"display: inline-block; vertical-align: middle;"
					+ "width: 25px; height: 25px;margin-right: 10px;\"><a href=\"https://www.ngdesk.com\" target=\"_blank\"><img src=\"TRACKING_IMAGE_REPLACE_$imageId\" alt=\"logo\" "
					+ "style=\"width: 100%; height: 100%; object-fit: contain; overflow: hidden;\"></a></div><div style=\"display: inline-block; vertical-align: middle;\"><label><a href=\"https://www.ngdesk.com\" target=\"_blank\">Powered by ngDesk</a> &#169;</label>"
					+ "</div></div></div>";
			break;
		}
		default: {
			campaignTemplate = "<div>$body<br></div><div id=\"footer\" style=\"text-align: $footerAlignment;font-family: Arial;margin: 0 10px;\"><label>ADDRESS_REPLACE</label><br><label><a target=\"_blank\""
					+ " href=\"UNSUBSCRIBE_LINK\">Unsubscribe</a></label><div><div style=\"display: inline-block; vertical-align: middle;width: 25px; height: 25px;margin-right: 10px;\">"
					+ "<a href=\"https://www.ngdesk.com\" target=\"_blank\"><img src=\"TRACKING_IMAGE_REPLACE_$imageId\" alt=\"logo\" style=\"width: 100%; height: 100%; object-fit: contain;"
					+ " overflow: hidden;\"></a></div><div style=\"display: inline-block; vertical-align: middle;\"><label><a href=\"https://www.ngdesk.com\" target=\"_blank\">Powered by ngDesk</a> &#169;</label></div></div></div>";
		}
		}
		return campaignTemplate;
	}

	private String getColumnHTML(Double columnWidth) {
		String columnHTML = "";
		if (columnWidth.equals(1.0)) {
			columnHTML = ("<div style=\"width: 33.333%; margin: 10px;\">$content</div>");
		} else if (columnWidth.equals(1.5)) {
			columnHTML = ("<div style=\"width: 50%; margin: 10px;\">$content</div>");
		} else if (columnWidth.equals(3.0)) {
			columnHTML = ("<div style=\"width: 100%; margin: 10px;\">$content</div>");
		} else if (columnWidth.equals(0.3333)) {
			columnHTML = ("<div style=\"width: 33.333%; margin: 10px;\">$content</div>");
		} else if (columnWidth.equals(0.6667)) {
			columnHTML = ("<div style=\"width: 67.667%; margin: 10px;\">$content</div>");
		}
		return columnHTML;
	}

	public List<Bson> generateAllFilter(List<Condition> layoutConditions, String moduleId, List<Bson> filters,
			String companyId, JSONObject user) {
		try {
			log.trace("Enter DataService.generateAllFilter()");
			String userId = user.getString("USER_ID");
			boolean isInteger = false;
			boolean isBoolean = false;
			boolean isString = false;
			String userCompanyId = user.getString("COMPANY_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");
			for (Condition condition : layoutConditions) {
				String requirementType = condition.getRequirementType();

				if (requirementType.equalsIgnoreCase("All")) {
					String fieldId = condition.getCondition();
					String operator = condition.getOpearator();
					String value = condition.getConditionValue();

					String reg = "\\{\\{(.*)\\}\\}";
					Pattern r1 = Pattern.compile(reg);
					Matcher m1 = r1.matcher(value);
					Field field = getField(fieldId, companyId, moduleId);
					String fieldName = field.getName();
					if (m1.find()) {
						value = userId;
					}
					String displayDatatype = field.getDatatypes().getDisplay();
					String backendDatatype = field.getDatatypes().getBackend();

					if (displayDatatype.equals("Number") || displayDatatype.equals("Auto Number")
							|| displayDatatype.equals("Chronometer")) {
						isInteger = true;
					}
					if (backendDatatype.equalsIgnoreCase("Boolean")) {
						isBoolean = true;
					}
					if (backendDatatype.equalsIgnoreCase("String") || displayDatatype.equals("String")) {
						isString = true;
					}

					if (operator.equals("EQUALS_TO")) {
						if (isInteger) {
							filters.add(Filters.eq(fieldName, Integer.parseInt(value)));
						} else if (isBoolean) {
							filters.add(Filters.eq(fieldName, Boolean.parseBoolean(value)));
						} else if (companySubdomain.equals("support") && fieldName.equals("ROLE")) {
							MongoCollection<Document> rolesCollection = mongoTemplate
									.getCollection("roles_" + userCompanyId);
							Document selectedRoleDoc = rolesCollection.find(Filters.eq("_id", new ObjectId(value)))
									.first();
							String roleName = selectedRoleDoc.getString("NAME");

							MongoCollection<Document> companyRolesCollection = mongoTemplate
									.getCollection("roles_" + companyId);
							String companyRoleId = companyRolesCollection.find(Filters.eq("NAME", roleName)).first()
									.getObjectId("_id").toString();
							filters.add(Filters.eq(fieldName, companyRoleId));
						} else {
							filters.add(Filters.eq(fieldName, value));
						}
					} else if (operator.equals("NOT_EQUALS_TO")) {
						if (isInteger) {
							filters.add(Filters.ne(fieldName, Integer.parseInt(value)));
						} else if (isBoolean) {
							filters.add(Filters.ne(fieldName, Boolean.parseBoolean(value)));
						} else if (companySubdomain.equals("support") && fieldName.equals("ROLE")) {
							MongoCollection<Document> rolesCollection = mongoTemplate
									.getCollection("roles_" + userCompanyId);
							Document selectedRoleDoc = rolesCollection.find(Filters.eq("_id", new ObjectId(value)))
									.first();
							String roleName = selectedRoleDoc.getString("NAME");

							MongoCollection<Document> companyRolesCollection = mongoTemplate
									.getCollection("roles_" + companyId);
							String companyRoleId = companyRolesCollection.find(Filters.eq("NAME", roleName)).first()
									.getObjectId("_id").toString();
							filters.add(Filters.ne(fieldName, companyRoleId));
						} else {
							filters.add(Filters.ne(fieldName, value));
						}
					} else if (operator.equals("GREATER_THAN")) {
						if (isInteger) {
							filters.add(Filters.gt(fieldName, Integer.parseInt(value)));
						} else {
							filters.add(Filters.gt(fieldName, value));
						}
					} else if (operator.equals("LENGTH_IS_GREATER_THAN")) {
						if (isString) {
							filters.add(Filters.expr(Document.parse(
									" $ifNull:{ $gt: [ { '$strLenCP': '$" + fieldName + "' } , " + value + "]} ")));
						}
					} else if (operator.equals("LESS_THAN")) {
						if (isInteger) {
							filters.add(Filters.lt(fieldName, Integer.parseInt(value)));
						} else {
							filters.add(Filters.lt(fieldName, value));
						}
					} else if (operator.equals("LENGTH_IS_LESS_THAN")) {
						if (isString) {
							filters.add(Filters.expr(Document.parse(
									" $ifNull:{ $lt: [ { '$strLenCP': '$" + fieldName + "' } , " + value + "]} ")));
						}
					} else if (operator.equals("CONTAINS")) {
						if (isInteger) {
							filters.add(Filters.regex(fieldName, ".*" + Integer.parseInt(value) + ".*"));
						} else {
							filters.add(Filters.regex(fieldName, ".*" + value + ".*"));
						}

					} else if (operator.equals("DOES_NOT_CONTAIN")) {
						if (isInteger) {
							filters.add(Filters.not(Filters.regex(fieldName, ".*" + Integer.parseInt(value) + ".*")));
						} else {
							filters.add(Filters.not(Filters.regex(fieldName, ".*" + value + ".*")));
						}
					} else if (operator.equals("REGEX")) {
						filters.add(Filters.regex(fieldName, value));
					} else if (operator.equals("EXISTS")) {
						filters.add(Filters.exists(fieldName));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.trace("Enter DataService.generateAllFilter()");
		return filters;
	}

	public List<Bson> generateAnyFilter(List<Condition> layoutConditions, String moduleId, List<Bson> filters,
			String companyId, JSONObject user) {
		try {
			log.trace("Enter DataService.generateAnyFilter()");
			boolean isInteger = false;
			boolean isBoolean = false;
			boolean isString = false;
			String userId = user.getString("USER_ID");
			String userCompanyId = user.getString("COMPANY_ID");
			String companySubdomain = user.getString("COMPANY_SUBDOMAIN");
			for (Condition condition : layoutConditions) {
				String requirementType = condition.getRequirementType();
				if (requirementType.equalsIgnoreCase("Any")) {
					String fieldId = condition.getCondition();
					String operator = condition.getOpearator();
					String value = condition.getConditionValue();
					String reg = "\\{\\{(.*)\\}\\}";
					Pattern r2 = Pattern.compile(reg);
					Matcher m2 = r2.matcher(value);
					if (m2.find()) {
						value = userId;
					}
					Field field = getField(fieldId, companyId, moduleId);
					String fieldName = field.getName();
					String displayDatatype = field.getDatatypes().getDisplay();
					String backendDatatype = field.getDatatypes().getBackend();

					if (displayDatatype.equals("Number") || displayDatatype.equals("Auto Number")
							|| displayDatatype.equals("Chronometer")) {
						isInteger = true;
					}

					if (backendDatatype.equalsIgnoreCase("Boolean")) {
						isBoolean = true;
					}

					if (backendDatatype.equalsIgnoreCase("String") || displayDatatype.equals("String")) {
						isString = true;
					}

					if (operator.equals("EQUALS_TO")) {
						if (isInteger) {
							filters.add(Filters.eq(fieldName, Integer.parseInt(value)));
						} else if (isBoolean) {
							filters.add(Filters.eq(fieldName, Boolean.parseBoolean(value)));
						} else if (companySubdomain.equals("support") && fieldName.equals("ROLE")) {
							MongoCollection<Document> rolesCollection = mongoTemplate
									.getCollection("roles_" + userCompanyId);
							Document selectedRoleDoc = rolesCollection.find(Filters.eq("_id", new ObjectId(value)))
									.first();
							String roleName = selectedRoleDoc.getString("NAME");

							MongoCollection<Document> companyRolesCollection = mongoTemplate
									.getCollection("roles_" + companyId);
							String companyRoleId = companyRolesCollection.find(Filters.eq("NAME", roleName)).first()
									.getObjectId("_id").toString();
							filters.add(Filters.eq(fieldName, companyRoleId));
						} else {
							filters.add(Filters.eq(fieldName, value));
						}
					} else if (operator.equals("NOT_EQUALS_TO")) {
						if (isInteger) {
							filters.add(Filters.ne(fieldName, Integer.parseInt(value)));
						} else if (isBoolean) {
							filters.add(Filters.ne(fieldName, Boolean.parseBoolean(value)));
						} else if (companySubdomain.equals("support") && fieldName.equals("ROLE")) {
							MongoCollection<Document> rolesCollection = mongoTemplate
									.getCollection("roles_" + userCompanyId);
							Document selectedRoleDoc = rolesCollection.find(Filters.eq("_id", new ObjectId(value)))
									.first();
							String roleName = selectedRoleDoc.getString("NAME");

							MongoCollection<Document> companyRolesCollection = mongoTemplate
									.getCollection("roles_" + companyId);
							String companyRoleId = companyRolesCollection.find(Filters.eq("NAME", roleName)).first()
									.getObjectId("_id").toString();
							filters.add(Filters.ne(fieldName, companyRoleId));
						} else {
							filters.add(Filters.ne(fieldName, value));
						}
					} else if (operator.equals("GREATER_THAN")) {
						if (isInteger) {
							filters.add(Filters.gt(fieldName, Integer.parseInt(value)));
						} else {
							filters.add(Filters.gt(fieldName, value));
						}
					} else if (operator.equals("LENGTH_IS_GREATER_THAN")) {
						if (isString) {
							filters.add(Filters.expr(Document.parse(
									" $ifNull:{ $gt: [ { '$strLenCP': '$" + fieldName + "' } , " + value + "]} ")));
						}
					} else if (operator.equals("LESS_THAN")) {
						if (isInteger) {
							filters.add(Filters.lt(fieldName, Integer.parseInt(value)));
						} else {
							filters.add(Filters.lt(fieldName, value));
						}
					} else if (operator.equals("LENGTH_IS_LESS_THAN")) {
						if (isString) {
							filters.add(Filters.expr(Document.parse(
									" $ifNull:{ $lt: [ { '$strLenCP': '$" + fieldName + "' } , " + value + "]} ")));
						}
					} else if (operator.equals("CONTAINS")) {
						if (isInteger) {
							filters.add(Filters.regex(fieldName, ".*" + Integer.parseInt(value) + ".*"));
						} else {
							filters.add(Filters.regex(fieldName, ".*" + value + ".*"));
						}

					} else if (operator.equals("DOES_NOT_CONTAIN")) {
						if (isInteger) {
							filters.add(Filters.not(Filters.regex(fieldName, ".*" + Integer.parseInt(value) + ".*")));
						} else {
							filters.add(Filters.not(Filters.regex(fieldName, ".*" + value + ".*")));
						}
					} else if (operator.equals("REGEX")) {
						filters.add(Filters.regex(fieldName, value));
					} else if (operator.equals("EXISTS")) {
						filters.add(Filters.exists(fieldName));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.trace("Exit DataService.generateAnyFilter()");
		return filters;
	}

	public Field getField(String fieldId, String companyId, String moduleId) {
		Field field = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			String collectionName = "modules_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module != null && module.get("FIELDS") != null) {
				ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");
				for (Document fieldDoc : fieldDocuments) {
					if (fieldDoc.getString("FIELD_ID").equals(fieldId)) {
						String fieldDocument = mapper.writeValueAsString(fieldDoc);
						field = mapper.readValue(fieldDocument, Field.class);
					}

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return field;
	}

}
