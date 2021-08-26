package com.ngdesk.jobs;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.campaigns.Address;
import com.ngdesk.campaigns.Campaigns;
import com.ngdesk.campaigns.Column;
import com.ngdesk.campaigns.ColumnSettings;
import com.ngdesk.campaigns.Footer;
import com.ngdesk.campaigns.Row;
import com.ngdesk.companies.EmailList;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.fields.Field;
import com.ngdesk.modules.rules.Condition;
import com.ngdesk.roles.RoleService;

@Component
public class SendCampaignJob {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	@Autowired
	RedissonClient redisson;
	
	@Value("${email.host}")
	private String host;
	

	private final Logger log = LoggerFactory.getLogger(SendCampaignJob.class);

	// RUNS FOR EVERY ONE MINUTE
	@Scheduled(fixedRate = 10000)
	public void sendCampaign() {

		try {

			log.trace("Enter SendCampaignJob.sendCampaign()");
			String userCompanyId = null;
			String epochDate = "01/01/1970";
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
			Timestamp epoch = new Timestamp(date.getTime());

			Timestamp today = new Timestamp(new Date().getTime());
			long currentTimeDiff = today.getTime() - epoch.getTime();

			RSortedSet<Long> campaignScheduledTimes = redisson.getSortedSet("campaignScheduledTimes");
			SortedSet<Long> campaignsScheduledTimesSet = new TreeSet<Long>(campaignScheduledTimes);

			RMap<Long, String> campaigns = redisson.getMap("campaigns");
			Map<Long, String> campaignsList = new HashMap<Long, String>(campaigns);

			for (Long timeDiff : campaignsScheduledTimesSet) {

				if (currentTimeDiff >= timeDiff) {

					JSONObject campaignObj = new JSONObject(campaignsList.get(timeDiff));

					campaignScheduledTimes.remove(timeDiff);
					campaigns.remove(timeDiff);

					userCompanyId = campaignObj.getString("COMPANY_ID");
					String campaignId = campaignObj.getString("CAMPAIGN_ID");
					String uuid = campaignObj.getString("UUID");
					JSONObject user = auth.getUserDetails(uuid);

					MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
					Document companyDoc = companiesCollection.find(Filters.eq("_id", new ObjectId(userCompanyId)))
							.first();

					MongoCollection<Document> campaignsCollection = mongoTemplate
							.getCollection("campaigns_" + userCompanyId);
					Document campaignDoc = campaignsCollection.find(Filters.eq("_id", new ObjectId(campaignId)))
							.first();
					campaignDoc.remove("_id");
					campaignDoc.put("CAMPAIGN_ID", campaignId);
					Campaigns campaign = new ObjectMapper().readValue(campaignDoc.toJson(), Campaigns.class);

					String body = createCampaignBody(campaign, userCompanyId);
					String subject = campaign.getSubject();
					String reg = "\\{\\{(?i)(inputMessage[_a-zA-Z\\.\\-]+)\\}\\}";
					Pattern r = Pattern.compile(reg);

					String companySubdomain = companyDoc.getString("COMPANY_SUBDOMAIN");
					List<String> usersSent = new ArrayList<String>();
					String fromEmail = "no-reply@" + companySubdomain + ".ngdesk.com";
					String redirectPage = "https://" + companySubdomain + ".ngdesk.com/redirect/redirect.html";

					List<String> usersToSend = new ArrayList<String>(campaign.getRecipientUsers());

					List<Document> companies = companiesCollection.find(Filters.eq("VERSION", "v2"))
							.into(new ArrayList<Document>());

					MongoCollection<Document> modulesCollection = mongoTemplate
							.getCollection("modules_" + userCompanyId);
					String usersModuleId = modulesCollection.find(Filters.eq("NAME", "Users")).first()
							.getObjectId("_id").toString();
					MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + userCompanyId);

					MongoCollection<Document> emailListsCollection = mongoTemplate
							.getCollection("email_lists_" + userCompanyId);
					List<String> emailLists = campaign.getRecipientLists();

					if (usersToSend.isEmpty() && emailLists.isEmpty()) {
						throw new BadRequestException("EMAIL_LISTS_AND_RECIPIENTS_MISSING");
					}

					for (String emailListId : emailLists) {
						Document emailListDoc = emailListsCollection.find(Filters.eq("_id", new ObjectId(emailListId)))
								.first();
						String id = emailListDoc.remove("_id").toString();
						emailListDoc.put("EMAIL_LIST_ID", id);
						EmailList emailList = new ObjectMapper().readValue(emailListDoc.toJson(), EmailList.class);
						List<Condition> listConditions = emailList.getConditions();

						for (Document company : companies) {
							String companyId = company.getObjectId("_id").toString();
							MongoCollection<Document> companyUsersModCollection = mongoTemplate
									.getCollection("modules_" + companyId);
							Document companyUsersModuleDoc = companyUsersModCollection.find(Filters.eq("NAME", "Users"))
									.first();
							String companyUsersModuleId = companyUsersModuleDoc.getObjectId("_id").toString();
							MongoCollection<Document> companyUsersCollection = mongoTemplate
									.getCollection("Users_" + companyId);
							JSONObject usersObj = null;
							if (!companySubdomain.equals("support")) {
								usersObj = getFilteredData(usersModuleId, userCompanyId, user, listConditions);
								JSONArray usersData = usersObj.getJSONArray("DATA");
								for (int i = 0; i < usersData.length(); i++) {
									JSONObject userJSON = usersData.getJSONObject(i);
									String dataId = userJSON.getString("DATA_ID");

									Document entry = usersCollection.find(Filters.eq("_id", new ObjectId(dataId)))
											.first();
									String userEmail = entry.getString("EMAIL_ADDRESS");
									String userUUID = entry.getString("USER_UUID");

									String unsubscribeLink = "https://" + companySubdomain
											+ ".ngdesk.com/unsubscribe-to-marketing-email?uuid=" + userUUID + "&email="
											+ userEmail;
									String emailBlastHTML = global.getFile("emails_blasts_with_unsubscribe.html");
									if (campaign.getCampaignType().equals("Welcome")) {
										emailBlastHTML = global.getFile("email_welcome_template.html");
									} else if (campaign.getCampaignType().equals("Simple")) {
										emailBlastHTML = global.getFile("email_simple_template.html");
									}
									String newBody = body;
									String newSubject = subject;
									if (r != null) {
										Matcher matcher1 = r.matcher(newBody);
										while (matcher1.find()) {
											String path = matcher1.group(1).split("(?i)inputMessage\\.")[1];
											String firstField = path;
											if (path.contains(".")) {
												firstField = path.split("\\.")[0];
											}
											if (entry.containsKey(firstField)) {
												String value = global.getValue(path, entry, userCompanyId,
														usersModuleId, dataId, false);
												newBody = newBody.replace("{{inputMessage." + path + "}}", value);
												matcher1 = r.matcher(newBody);
											} else {
												newBody = newBody.replace("{{inputMessage." + path + "}}", "");
												matcher1 = r.matcher(newBody);
											}

										}

										Matcher matcher2 = r.matcher(newSubject);
										while (matcher2.find()) {
											String path = matcher2.group(1).split("(?i)inputMessage\\.")[1];
											String firstField = path;
											if (path.contains(".")) {
												firstField = path.split("\\.")[0];
											}
											if (entry.containsKey(firstField)) {
												String value = global.getValue(path, entry, userCompanyId,
														usersModuleId, dataId, false);
												newSubject = newSubject.replace("{{inputMessage." + path + "}}", value);
												matcher2 = r.matcher(newSubject);
											} else {
												newSubject = newSubject.replace("{{inputMessage." + path + "}}", "");
												matcher2 = r.matcher(newSubject);
											}
										}
									}

									if (newBody.contains("TRACKING_IMAGE_REPLACE")) {
										Pattern trackingImagePattern = Pattern
												.compile("TRACKING_IMAGE_REPLACE_(.*?)\"");
										Matcher trackingImageIdMatcher = trackingImagePattern.matcher(newBody);
										while (trackingImageIdMatcher.find()) {
											String imageUrl = "https://" + companySubdomain
													+ ".ngdesk.com/ngdesk-rest/ngdesk/companies/gallery/image/"
													+ trackingImageIdMatcher.group(1) + "?email_address=" + userEmail
													+ "&company_id=" + userCompanyId + "&user_company_id="
													+ userCompanyId + "&campaign_id=" + campaignId;
											trackingImageIdMatcher.replaceFirst(imageUrl);
											newBody = newBody.replace(
													"TRACKING_IMAGE_REPLACE_" + trackingImageIdMatcher.group(1),
													imageUrl);
											trackingImageIdMatcher = trackingImagePattern.matcher(newBody);
										}
									}

									if (newBody.contains("ADDRESS_REPLACE")) {
										Pattern footerPattern = Pattern.compile("ADDRESS_REPLACE(.*?)\"");
										Matcher footerMatcher = footerPattern.matcher(newBody);
										while (footerMatcher.find()) {
											Address footer = campaign.getFooter().getAddress();
											String address = footer.getCompanyName() + "<br>" + footer.getAddress1()
													+ ", " + footer.getAddress2() + "<br>" + footer.getCity() + ", "
													+ footer.getState() + ", " + footer.getCountry() + "-"
													+ footer.getZipCode() + "<br>" + footer.getPhone();
											footerMatcher.replaceFirst(address);
											newBody = newBody.replace("ADDRESS_REPLACE", address);
											footerMatcher = footerPattern.matcher(newBody);
										}
									}

									if (newBody.contains("IMAGE_REPLACE")) {
										Pattern imagePattern = Pattern.compile("IMAGE_REPLACE_(.*?)\"");
										Matcher imageIdMatcher = imagePattern.matcher(newBody);
										while (imageIdMatcher.find()) {
											String imageUrl = "https://" + companySubdomain
													+ ".ngdesk.com/ngdesk-rest/ngdesk/companies/gallery/image/"
													+ imageIdMatcher.group(1) + "?email_address=" + userEmail
													+ "&company_id=" + userCompanyId + "&user_company_id="
													+ userCompanyId;
											imageIdMatcher.replaceFirst(imageUrl);
											newBody = newBody.replace("IMAGE_REPLACE_" + imageIdMatcher.group(1),
													imageUrl);
											imageIdMatcher = imagePattern.matcher(newBody);
										}
									}

									if (newBody.contains("REDIRECT_URL_REPLACE")) {
										redirectPage = redirectPage + "?email_address=" + userEmail + "&company_id="
												+ userCompanyId + "&user_company_id=" + userCompanyId + "&campaign_id="
												+ campaignId + "&subdomain=" + companySubdomain;
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
									
									SendEmail sendCampaign = new SendEmail(userEmail, fromEmail, newSubject,
											emailBlastHTML, host);
									sendCampaign.sendEmail();
									usersSent.add(dataId);
								}
							} else {
								usersObj = getFilteredData(companyUsersModuleId, companyId, user, listConditions);
								JSONArray usersData = usersObj.getJSONArray("DATA");
								for (int i = 0; i < usersData.length(); i++) {
									JSONObject userJSON = usersData.getJSONObject(i);
									String dataId = userJSON.getString("DATA_ID");

									Document entry = companyUsersCollection
											.find(Filters.eq("_id", new ObjectId(dataId))).first();
									String userEmail = entry.getString("EMAIL_ADDRESS");
									String userUUID = entry.getString("USER_UUID");

									String unsubscribeLink = "https://" + company.getString("COMPANY_SUBDOMAIN")
											+ ".ngdesk.com/unsubscribe-to-marketing-email?uuid=" + userUUID + "&email="
											+ userEmail;
									String emailBlastHTML = global.getFile("emails_blasts_with_unsubscribe.html");
									if (campaign.getCampaignType().equals("Welcome")) {
										emailBlastHTML = global.getFile("email_welcome_template.html");
									} else if (campaign.getCampaignType().equals("Simple")) {
										emailBlastHTML = global.getFile("email_simple_template.html");
									}

									String newBody = body;
									String newSubject = subject;
									if (r != null) {
										Matcher matcher1 = r.matcher(newBody);
										while (matcher1.find()) {
											String path = matcher1.group(1).split("(?i)inputMessage\\.")[1];
											String firstField = path;
											if (path.contains(".")) {
												firstField = path.split("\\.")[0];
											}
											if (entry.containsKey(firstField)) {
												String value = global.getValue(path, entry, companyId,
														companyUsersModuleId, dataId, false);
												newBody = newBody.replace("{{inputMessage." + path + "}}", value);
												matcher1 = r.matcher(newBody);
											} else {
												newBody = newBody.replace("{{inputMessage." + path + "}}", "");
												matcher1 = r.matcher(newBody);
											}
										}

										Matcher matcher2 = r.matcher(newSubject);
										while (matcher2.find()) {
											String path = matcher2.group(1).split("(?i)inputMessage\\.")[1];
											String firstField = path;
											if (path.contains(".")) {
												firstField = path.split("\\.")[0];
											}
											if (entry.containsKey(firstField)) {
												String value = global.getValue(path, entry, companyId,
														companyUsersModuleId, dataId, false);
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
											String imageUrl = "https://" + company.getString("COMPANY_SUBDOMAIN")
													+ ".ngdesk.com/ngdesk-rest/ngdesk/companies/gallery/image/"
													+ trackingImageIdMatcher.group(1) + "?email_address=" + userEmail
													+ "&company_id=" + userCompanyId + "&user_company_id=" + companyId
													+ "&campaign_id=" + campaignId;
											trackingImageIdMatcher.replaceFirst(imageUrl);
											newBody = newBody.replace(
													"TRACKING_IMAGE_REPLACE_" + trackingImageIdMatcher.group(1),
													imageUrl);
											trackingImageIdMatcher = trackingImagePattern.matcher(newBody);
										}
									}
									if (newBody.contains("ADDRESS_REPLACE")) {
										Pattern footerPattern = Pattern.compile("ADDRESS_REPLACE(.*?)\"");
										Matcher footerMatcher = footerPattern.matcher(newBody);
										while (footerMatcher.find()) {
											Address footer = campaign.getFooter().getAddress();
											String address = footer.getCompanyName() + "<br>" + footer.getAddress1()
													+ ", " + footer.getAddress2() + "<br>" + footer.getCity() + ", "
													+ footer.getState() + ", " + footer.getCountry() + "-"
													+ footer.getZipCode() + "<br>" + footer.getPhone();
											footerMatcher.replaceFirst(address);
											newBody = newBody.replace("ADDRESS_REPLACE", address);
											footerMatcher = footerPattern.matcher(newBody);
										}
									}

									if (newBody.contains("IMAGE_REPLACE")) {
										Pattern imagePattern = Pattern.compile("IMAGE_REPLACE_(.*?)\"");
										Matcher imageIdMatcher = imagePattern.matcher(newBody);
										while (imageIdMatcher.find()) {
											String imageUrl = "https://" + company.getString("COMPANY_SUBDOMAIN")
													+ ".ngdesk.com/ngdesk-rest/ngdesk/companies/gallery/image/"
													+ imageIdMatcher.group(1) + "?email_address=" + userEmail
													+ "&company_id=" + userCompanyId + "&user_company_id=" + companyId;
											imageIdMatcher.replaceFirst(imageUrl);
											newBody = newBody.replace("IMAGE_REPLACE_" + imageIdMatcher.group(1),
													imageUrl);
											imageIdMatcher = imagePattern.matcher(newBody);
										}
									}

									if (newBody.contains("REDIRECT_URL_REPLACE")) {
										redirectPage = redirectPage + "?email_address=" + userEmail + "&company_id="
												+ userCompanyId + "&user_company_id=" + companyId + "&campaign_id="
												+ campaignId + "&subdomain=" + company.getString("COMPANY_SUBDOMAIN");
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
									
									SendEmail sendCampaign = new SendEmail(userEmail, fromEmail, newSubject,
											emailBlastHTML, host);
									sendCampaign.sendEmail();
									usersSent.add(dataId);
								}
							}

							if (!companySubdomain.equals("support")) {
								break;
							}
						}
					}

					usersToSend.removeAll(usersSent);
					for (String userIdToSend : usersToSend) {
						Document userToSendDoc = usersCollection
								.find(Filters.and(Filters.eq("SUBSCRIPTION_ON_MARKETING_EMAIL", true),
										Filters.eq("_id", new ObjectId(userIdToSend))))
								.first();
						if (userToSendDoc != null) {
							String userEmail = userToSendDoc.getString("EMAIL_ADDRESS");
							String userUUID = userToSendDoc.getString("USER_UUID");

							String unsubscribeLink = "https://" + companySubdomain
									+ ".ngdesk.com/unsubscribe-to-marketing-email?uuid=" + userUUID + "&email="
									+ userEmail;
							String emailBlastHTML = global.getFile("emails_blasts_with_unsubscribe.html");
							if (campaign.getCampaignType().equals("Welcome")) {
								emailBlastHTML = global.getFile("email_welcome_template.html");
							} else if (campaign.getCampaignType().equals("Simple")) {
								emailBlastHTML = global.getFile("email_simple_template.html");
							}

							String newBody = body;
							String newSubject = subject;
							if (r != null) {
								Matcher matcher = r.matcher(newBody);
								while (matcher.find()) {
									String path = matcher.group(1).split("(?i)inputMessage\\.")[1];
									String firstField = path;
									if (path.contains(".")) {
										firstField = path.split("\\.")[0];
									}
									if (userToSendDoc.containsKey(firstField)) {
										String value = global.getValue(path, userToSendDoc, userCompanyId,
												usersModuleId, userIdToSend, false);
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
										String value = global.getValue(path, userToSendDoc, userCompanyId,
												usersModuleId, userIdToSend, false);
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
											+ trackingImageIdMatcher.group(1) + "?email_address=" + userEmail
											+ "&company_id=" + userCompanyId + "&user_company_id=" + userCompanyId
											+ "&campaign_id=" + campaignId;
									trackingImageIdMatcher.replaceFirst(imageUrl);
									newBody = newBody.replace(
											"TRACKING_IMAGE_REPLACE_" + trackingImageIdMatcher.group(1), imageUrl);
									trackingImageIdMatcher = trackingImagePattern.matcher(newBody);
								}
							}

							if (newBody.contains("ADDRESS_REPLACE")) {
								Pattern footerPattern = Pattern.compile("ADDRESS_REPLACE(.*?)\"");
								Matcher footerMatcher = footerPattern.matcher(newBody);
								while (footerMatcher.find()) {
									Address footer = campaign.getFooter().getAddress();
									String address = footer.getCompanyName() + "<br>" + footer.getAddress1() + ", "
											+ footer.getAddress2() + "<br>" + footer.getCity() + ", "
											+ footer.getState() + ", " + footer.getCountry() + "-" + footer.getZipCode()
											+ "<br>" + footer.getPhone();
									footerMatcher.replaceFirst(address);
									newBody = newBody.replace("ADDRESS_REPLACE", address);
									footerMatcher = footerPattern.matcher(newBody);
								}
							}

							if (newBody.contains("IMAGE_REPLACE")) {
								Pattern imagePattern = Pattern.compile("IMAGE_REPLACE_(.*?)\"");
								Matcher imageIdMatcher = imagePattern.matcher(newBody);
								while (imageIdMatcher.find()) {
									String imageUrl = "https://" + companySubdomain
											+ ".ngdesk.com/ngdesk-rest/ngdesk/companies/gallery/image/"
											+ imageIdMatcher.group(1) + "?email_address=" + userEmail + "&company_id="
											+ userCompanyId + "&user_company_id=" + userCompanyId;
									imageIdMatcher.replaceFirst(imageUrl);
									newBody = newBody.replace("IMAGE_REPLACE_" + imageIdMatcher.group(1), imageUrl);
									imageIdMatcher = imagePattern.matcher(newBody);
								}
							}

							if (newBody.contains("REDIRECT_URL_REPLACE")) {
								redirectPage = redirectPage + "?email_address=" + userEmail + "&company_id="
										+ userCompanyId + "&user_company_id=" + userCompanyId + "&campaign_id="
										+ campaignId + "&subdomain=" + companySubdomain;
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
							
							SendEmail sendCampaign = new SendEmail(userEmail, fromEmail, newSubject, emailBlastHTML,
									host);
							sendCampaign.sendEmail();
							usersSent.add(userIdToSend);
						} else {
							// user not found
						}
					}

					// Set campaign status to Sent
					campaign.setStatus("Sent");
					campaign.setDateUpdated(new Timestamp(new Date().getTime()));

					String json = new ObjectMapper().writeValueAsString(campaign);
					Document updateCampaign = Document.parse(json);
					campaignsCollection.findOneAndReplace(Filters.eq("_id", new ObjectId(campaignId)), updateCampaign);

				}

			}

			log.trace("Exit SendCampaignJob.sendCampaign()");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String createCampaignBody(Campaigns campaign, String userCompanyId) {
		List<Row> rows = campaign.getRows();
		String campaignTemplate = getCampaignTemplate(campaign.getCampaignType());
		StringBuilder body = new StringBuilder();
		for (Row row : rows) {
			StringBuilder rowHTML = new StringBuilder();
			for (Column column: row.getColumns()) {
				StringBuilder columnStr = new StringBuilder(); 
				ColumnSettings columnSettings = column.getSettings();
				switch (column.getType()) {
					case ("TEXT"): {
						columnStr.append("<p style=\"margin: 0;\">" + column.getSettings().getText() + "</p>");
						break;
					}
					case ("BUTTON"): {
						String buttonStyle = "font-weight: " + columnSettings.getFontWeight() + "; padding: 0 16px; line-height: " +
								(columnSettings.getFontSize()-14)+36 + "px; cursor: pointer; text-shadow: none;color: " + 
								columnSettings.getTextColor() + ";border-radius: " + columnSettings.getCornerRadius() + "px; background-color: " + 
								columnSettings.getBackgroundColor() + "; font-size: " + columnSettings.getFontSize() + "px; font-style: " + 
								columnSettings.getFontItalics() + "; text-decoration: " + columnSettings.getFontUnderline() + "; font-family: " + 
								(columnSettings.getFontFamily().equals("Roboto") ? "Roboto, Helvetica Neue, sans-serif" : columnSettings.getFontFamily()) +
								"; min-width: inherit;";
						String redirectString = "";
						String redirectStyle = "";
						String divStyle = "margin:0;";
						if (columnSettings.getHasBorder()) {
							buttonStyle += "border:" + columnSettings.getBorderWidth() + "px solid " + columnSettings.getBorderColor() + ";";
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
						String buttonString = "<button id=\"button_"+ columnSettings.getId() +"\" style=\"" + buttonStyle + "\">"+ columnSettings.getText() +"</button>";
						String contentString = buttonString;
						if (columnSettings.getLinkTo().equals("URL") && !columnSettings.getLinkValue().equals("")) {
							buttonStyle = "font-weight: inherit; padding: 0 16px; line-height: inherit; cursor: pointer; text-shadow: none;color: inherit;border-radius: " + 
									columnSettings.getCornerRadius() + "px; background-color: " + columnSettings.getBackgroundColor() + ";";
							redirectStyle += "display: inline-block; color: " + columnSettings.getTextColor() + ";line-height: " + ((columnSettings.getFontSize()-14)+36) + 
										"px; font-family: " + (columnSettings.getFontFamily().equals("Roboto") ? "Roboto, Helvetica Neue, sans-serif" : columnSettings.getFontFamily()) + 
										"; font-weight: " + columnSettings.getFontWeight() + ";font-size: " + columnSettings.getFontSize() + "px; font-style: " + 
										columnSettings.getFontItalics() + "; text-decoration: " + columnSettings.getFontUnderline() + ";";
							redirectString = "<a href=\"REDIRECT_URL_REPLACE&button=" + columnSettings.getId() + "&redirect_url=" + columnSettings.getLinkValue() + 
									"\" target=\"_blank\" style=\"" + redirectStyle + "\">" + buttonString + "</a>";
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
							columnStr.append("<p style=\"margin: 0;\"><div style=\"height: " + columnSettings.getHeight() + "px; text-align: " + imageAlignment + 
									"\"><a href=\"REDIRECT_URL_REPLACE&redirect_url=" + columnSettings.getLinkValue() + "\" target=\"_blank\"><img src=\"TRACKING_IMAGE_REPLACE_" + 
									columnSettings.getId() + "\" alt=\"" + columnSettings.getAlternateText() + "\" style=\"width: " + columnSettings.getWidth() + 
									"px; object-fit: contain; overflow: hidden;\"></a></div></p>");
						} else {
							columnStr.append("<p style=\"margin: 0\"><div style=\"height: " + columnSettings.getHeight() + "px; text-align: " + imageAlignment + 
									"\"><img src=\"IMAGE_REPLACE_" + columnSettings.getId() + "\" alt=\"" + columnSettings.getAlternateText() + "\" style=\"width: " + 
									columnSettings.getWidth() + "px; object-fit: contain; overflow: hidden;\"></div></p>");
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
		Document defaultImage = galleryCollection.find(Filters.and(Filters.eq("COMPANY_ID", userCompanyId), Filters.eq("LOGO.FILENAME", "ngdesk.png"))).first();
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

	public JSONObject getFilteredData(String moduleId, String companyId, JSONObject user,
			List<Condition> emailListConditions) {

		JSONArray dataList = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();
		List<Document> documents = new ArrayList<Document>();
		String role = null;
		try {

			String userId = user.getString("USER_ID");
			role = user.getString("ROLE");
			String userCompanyId = user.getString("COMPANY_ID");
			boolean isSystemAdmin = false;

			if (companyId != null && companyId.length() > 0) {
				String moduleCollectionName = "modules_" + companyId;
				MongoCollection<Document> moduleCollection = mongoTemplate.getCollection(moduleCollectionName);
				if (!ObjectId.isValid(moduleId)) {
					throw new BadRequestException("INVALID_MODULE_ID");
				}
				Document module = moduleCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				if (module != null) {

					List<Document> fields = (List<Document>) module.get("FIELDS");
					Map<String, String> fieldsMap = new HashMap<String, String>();
					Map<String, Document> relationFields = new HashMap<String, Document>();

					for (Document field : fields) {
						String fieldId = field.getString("FIELD_ID");
						String fieldName = field.getString("NAME");
						fieldsMap.put(fieldId, fieldName);

						Document dataType = (Document) field.get("DATA_TYPE");
						String displayDataType = dataType.getString("DISPLAY");

						if (displayDataType.equals("Relationship")) {
							if (field.getString("RELATIONSHIP_TYPE").equals("One to One")
									|| field.getString("RELATIONSHIP_TYPE").equals("Many to One")) {
								relationFields.put(fieldName, field);
							}
						}
					}

					if (role != null) {
						if (!roleService.isSystemAdmin(role, userCompanyId)) {
							if (!roleService.isAuthorizedForRecord(role, "GET", moduleId, companyId)) {
								throw new ForbiddenException("FORBIDDEN");
							}
						} else {
							isSystemAdmin = true;
						}
					}

					String moduleName = module.getString("NAME");
					String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
					MongoCollection<Document> dataCollection = mongoTemplate.getCollection(collectionName);

					int lowerLimit = 0;
					int pgSize = (int) dataCollection.countDocuments();
					int pg = 1;
					int skip = 0;

					boolean isMobileLayout = false;

					List<String> showFieldNames = new ArrayList<String>();
					List<Bson> allFilters = new ArrayList<Bson>();
					List<Bson> anyFilters = new ArrayList<Bson>();

					allFilters = generateAllFilter(emailListConditions, moduleId, allFilters, companyId, user);
					anyFilters = generateAnyFilter(emailListConditions, moduleId, anyFilters, companyId, user);
					allFilters.add(Filters.eq("DELETED", false));
					allFilters.add(Filters.eq("SUBSCRIPTION_ON_MARKETING_EMAIL", true));
					if (moduleName.equals("Users")) {
						List<String> emails = new ArrayList<String>();
						emails.add("ghost@ngdesk.com");
						emails.add("system@ngdesk.com");
						allFilters.add(Filters.nin("EMAIL_ADDRESS", emails));
					}
					Bson sortFilter = null;
					List<String> userIds = new ArrayList<String>();
					userIds.add(userId);

					HashSet<String> teamIds = new HashSet<String>();

					String teamCollectionName = "Teams_" + companyId;
					MongoCollection<Document> teamsCollection = mongoTemplate.getCollection(teamCollectionName);
					List<Document> teamDocuments = teamsCollection.find(Filters.in("USERS", userIds))
							.into(new ArrayList<Document>());
					for (Document teamDoc : teamDocuments) {
						String id = teamDoc.getObjectId("_id").toString();
						teamIds.add(id);
					}

					if (allFilters.size() != 0 && anyFilters.size() != 0) {
						if (isSystemAdmin || moduleName.equals("Teams")) {
							totalSize = (int) dataCollection
									.countDocuments(Filters.and(Filters.and(allFilters), Filters.or(anyFilters)));
							if (!isMobileLayout) {
								documents = dataCollection
										.find(Filters.and(Filters.and(allFilters), Filters.or(anyFilters)))
										.sort(sortFilter).skip(skip).limit(pgSize)
										.projection(Projections.include(showFieldNames))
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							} else {
								documents = dataCollection
										.find(Filters.and(Filters.and(allFilters), Filters.or(anyFilters)))
										.sort(sortFilter).skip(skip).limit(pgSize)
										.projection(Projections.include(showFieldNames))
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							}
						} else {
							totalSize = (int) dataCollection.countDocuments(Filters.and(Filters.and(allFilters),
									Filters.or(anyFilters), Filters.in("TEAMS", teamIds)));

							if (!isMobileLayout) {
								documents = dataCollection
										.find(Filters.and(Filters.and(allFilters), Filters.or(anyFilters),
												Filters.in("TEAMS", teamIds)))
										.sort(sortFilter).skip(skip).limit(pgSize)
										.projection(Projections.include(showFieldNames))
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							} else {
								documents = dataCollection
										.find(Filters.and(Filters.and(allFilters), Filters.or(anyFilters),
												Filters.in("TEAMS", teamIds)))
										.sort(sortFilter).skip(skip).limit(pgSize)
										.projection(Projections.include(showFieldNames))
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							}

						}

					} else if (allFilters.size() == 0 && anyFilters.size() != 0) {
						if (isSystemAdmin || moduleName.equals("Teams")) {
							totalSize = (int) dataCollection.countDocuments(Filters.or(anyFilters));
							if (!isMobileLayout) {
								documents = dataCollection.find(Filters.or(anyFilters)).sort(sortFilter).skip(skip)
										.limit(pgSize).projection(Projections.include(showFieldNames))
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							} else {
								documents = dataCollection.find(Filters.or(anyFilters)).sort(sortFilter).skip(skip)
										.limit(pgSize).projection(Projections.include(showFieldNames))
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							}

						} else {
							totalSize = (int) dataCollection
									.countDocuments(Filters.and(Filters.or(anyFilters), Filters.in("TEAMS", teamIds)));
							if (!isMobileLayout) {
								documents = dataCollection
										.find(Filters.and(Filters.or(anyFilters), Filters.in("TEAMS", teamIds)))
										.skip(skip).limit(pgSize).sort(sortFilter)
										.projection(Projections.include(showFieldNames))
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							} else {
								documents = dataCollection
										.find(Filters.and(Filters.or(anyFilters), Filters.in("TEAMS", teamIds)))
										.sort(sortFilter).skip(skip).limit(pgSize)
										.projection(Projections.include(showFieldNames))
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							}

						}
					} else if (anyFilters.size() == 0 && allFilters.size() != 0) {

						if (isSystemAdmin) {
							totalSize = (int) dataCollection.countDocuments(Filters.and(allFilters));
							if (!isMobileLayout) {
								documents = dataCollection.find(Filters.and(allFilters)).sort(sortFilter).skip(skip)
										.limit(pgSize).projection(Projections.include(showFieldNames))
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							} else {

								documents = dataCollection.find(Filters.and(allFilters)).sort(sortFilter).skip(skip)
										.limit(pgSize).projection(Projections.include(showFieldNames))
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							}

						} else {
							totalSize = (int) dataCollection
									.countDocuments(Filters.and(Filters.and(allFilters), Filters.in("TEAMS", teamIds)));
							if (!isMobileLayout) {
								documents = dataCollection
										.find(Filters.and(Filters.and(allFilters), Filters.in("TEAMS", teamIds)))
										.skip(skip).limit(pgSize).sort(sortFilter)
										.projection(Projections.include(showFieldNames))
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							} else {
								documents = dataCollection
										.find(Filters.and(Filters.and(allFilters), Filters.in("TEAMS", teamIds)))
										.skip(skip).limit(pgSize).sort(sortFilter)
										.projection(Projections.include(showFieldNames))
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							}

						}
					} else if (anyFilters.size() == 0 && allFilters.size() == 0) {

						if (isSystemAdmin || moduleName.equals("Teams")) {

							totalSize = (int) dataCollection.countDocuments();

							if (!isMobileLayout) {

								documents = dataCollection.find().sort(sortFilter).skip(skip).limit(pgSize)
										.projection(Projections.include(showFieldNames))
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							} else {
								documents = dataCollection.find().sort(sortFilter).skip(skip).limit(pgSize)
										.projection(Projections.include(showFieldNames))
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							}

						} else {
							totalSize = (int) dataCollection.countDocuments(Filters.in("TEAMS", teamIds));
							if (!isMobileLayout) {
								documents = dataCollection.find(Filters.in("TEAMS", teamIds)).sort(sortFilter)
										.skip(skip).limit(pgSize).projection(Projections.include(showFieldNames))
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							} else {
								documents = dataCollection.find(Filters.in("TEAMS", teamIds)).sort(sortFilter)
										.skip(skip).limit(pgSize).projection(Projections.include(showFieldNames))
										.projection(Projections.exclude("META_DATA")).into(new ArrayList<Document>());
							}

						}
					}
					if (documents != null) {

						for (Document document : documents) {
							for (String fieldName : showFieldNames) {
								if (relationFields.containsKey(fieldName) && document.containsKey(fieldName)) {
									String value = document.getString(fieldName);
									Document fieldDoc = relationFields.get(fieldName);
									String primaryDisplayField = fieldDoc.getString("PRIMARY_DISPLAY_FIELD");
									Document relationModule = moduleCollection
											.find(Filters.eq("_id", new ObjectId(fieldDoc.getString("MODULE"))))
											.first();
									List<Document> relationModuleFields = (List<Document>) relationModule.get("FIELDS");
									String relationModuleName = relationModule.getString("NAME");

									MongoCollection<Document> relationEntries = mongoTemplate
											.getCollection(relationModuleName + "_" + companyId);

									if (new ObjectId().isValid(value)) {
										Document entryDoc = relationEntries.find(Filters.eq("_id", new ObjectId(value)))
												.first();

										for (Document relationField : relationModuleFields) {
											if (relationField.getString("FIELD_ID").equals(primaryDisplayField)) {
												document.put(fieldName,
														entryDoc.getString(relationField.getString("NAME")));
												break;
											}
										}
									}
								}
							}

							String dataId = document.getObjectId("_id").toString();
							document.remove("_id");
							JSONObject data = new JSONObject(document.toJson().toString());
							data.put("DATA_ID", dataId);
							if (moduleName.equals("Users")) {
								data.remove("PASSWORD");
							}
							dataList.put(data);
						}
					}
				} else {
					throw new ForbiddenException("MODULE_DOES_NOT_EXIST");
				}
			} else {
				throw new ForbiddenException("COMPANY_INVALID");
			}
			resultObj.put("DATA", dataList);
			resultObj.put("TOTAL_RECORDS", totalSize);
			return resultObj;

		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
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
							filters.add(Filters.expr(Document
									.parse(" { $gt: [ { '$strLenCP': '$" + fieldName + "' } , " + value + "]} ")));
						}
					} else if (operator.equals("LESS_THAN")) {
						if (isInteger) {
							filters.add(Filters.lt(fieldName, Integer.parseInt(value)));
						} else {
							filters.add(Filters.lt(fieldName, value));
						}
					} else if (operator.equals("LENGTH_IS_LESS_THAN")) {
						if (isString) {
							filters.add(Filters.expr(Document
									.parse(" { $lt: [ { '$strLenCP': '$" + fieldName + "' } , " + value + "]} ")));
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
							filters.add(Filters.expr(Document
									.parse(" { $gt: [ { '$strLenCP': '$" + fieldName + "' } , " + value + "]} ")));
						}
					} else if (operator.equals("LESS_THAN")) {
						if (isInteger) {
							filters.add(Filters.lt(fieldName, Integer.parseInt(value)));
						} else {
							filters.add(Filters.lt(fieldName, value));
						}
					} else if (operator.equals("LENGTH_IS_LESS_THAN")) {
						if (isString) {
							filters.add(Filters.expr(Document
									.parse(" { $lt: [ { '$strLenCP': '$" + fieldName + "' } , " + value + "]} ")));
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
						field = new ObjectMapper().readValue(fieldDoc.toJson(), Field.class);
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
