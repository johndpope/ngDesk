package com.ngdesk.modules.slas;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.redisson.api.RMap;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.SendMail;
import com.ngdesk.data.dao.DataService;
import com.ngdesk.data.dao.PublishDiscussionMessage;
import com.ngdesk.discussion.DiscussionController;
import com.ngdesk.discussion.DiscussionMessage;
import com.ngdesk.nodes.ParentNode;

import net.logstash.logback.encoder.org.apache.commons.lang.exception.ExceptionUtils;

//@Component
public class SlaJob {
	@Autowired
	private Environment env;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@Autowired
	ParentNode parent;

	@Autowired
	SendMail sendMail;

	@Autowired
	RedissonClient redisson;

	@Autowired
	DiscussionController discussionController;

	@Autowired
	DataService dataService;

	private final Logger log = LoggerFactory.getLogger(SlaJob.class);

	@Scheduled(fixedRate = 60000)
	public void executeSlaJob() {
		try {
			log.trace("Enter SlaJob.executeJob()");
			// CONNECT
			Document slaDocument = null;
			String epochDate = "01/01/1970";
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
			Timestamp epoch = new Timestamp(date.getTime());
			Timestamp today = new Timestamp(new Date().getTime());
			long currentTimeDiff = today.getTime() - epoch.getTime();
			log.trace("currentTimeDiff: " + currentTimeDiff);
			RSortedSet<Long> slaTimes = redisson.getSortedSet("slaTimes");
			RMap<Long, String> slaData = redisson.getMap("slaInfo");

			List<Long> timesToRemove = new ArrayList<Long>();
			for (Long timeDiff : slaTimes) {
				log.trace("timeDiff: " + timeDiff);
				if (currentTimeDiff >= timeDiff) {
					JSONObject slaInfo = new JSONObject(slaData.get(timeDiff));
					log.trace("SLA_INFO: " + slaInfo);
					String companyId = slaInfo.getString("COMPANY_ID");
					String slaId = slaInfo.getString("SLA_ID");
					String dataId = slaInfo.getString("DATA_ID");
					String moduleName = slaInfo.getString("MODULE_NAME");
					String moduleId = slaInfo.getString("MODULE_ID");
					String userUuid = slaInfo.getString("USER_UUID");

					MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
					Document company = companiesCollection.find(Filters.eq("_id", new ObjectId(companyId))).first();

					if (company == null) {
						timesToRemove.add(timeDiff);
						continue;
					}

					String companyUuid = company.getString("COMPANY_UUID");
					int slaCount;
					if (slaInfo.has("SLA_COUNT")) {
						slaCount = slaInfo.getInt("SLA_COUNT");
					} else {
						slaCount = 0;
					}

					boolean isRecurring = false;
					int maxReccurence = 0;
					int intervalTime = 0;
					String timestamp = null;
					String fieldName = null;
					Map<String, Object> inputMessage = new HashMap<String, Object>();

					// REMOVE ARRAY FOR REDDISON
					timesToRemove.add(timeDiff);

					MongoCollection<Document> entriesCollection = mongoTemplate
							.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);

					// TODO: CHECK ENTRY IS NOT DELETED
					Document entry = entriesCollection
							.find(Filters.and(Filters.eq("_id", new ObjectId(dataId)), Filters.eq("DELETED", false)))
							.first();
					// GET ENTRY
					if (entry != null) {
						entry.remove("_id");
						entry.put("DATA_ID", dataId);
						entry.put("COMPANY_UUID", companyUuid);
						entry.put("USER_UUID", userUuid);
						entry.put("MODULE", moduleId);
						entry.put("OLD_DATA", new JSONObject().toString());
						entry.put("TYPE", "MODULE");
						// PREPARING INPUT MESSAGE
						inputMessage = new ObjectMapper().readValue(entry.toJson(), Map.class);

						// GET MODULES
						MongoCollection<Document> modulesCollection = mongoTemplate
								.getCollection("modules_" + companyId);
						Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
						Document workflow = null;
						List<Document> violations = null;
						List<Document> fields = (List<Document>) module.get("FIELDS");
						String discussionFieldName = null;
						String discussionFieldId = null;
						for (Document field : fields) {
							Document dataType = (Document) field.get("DATA_TYPE");
							if (dataType.getString("DISPLAY").equalsIgnoreCase("Discussion")) {
								discussionFieldName = field.getString("NAME");
								discussionFieldId = field.getString("FIELD_ID");
								break;
							}
						}

						// GET WORKFLOW FROM SLA
						String slaName = null;
						if (module != null && module.containsKey("SLAS")) {
							List<Document> slas = (List<Document>) module.get("SLAS");
							for (Document sla : slas) {
								if (sla.getString("SLA_ID").equals(slaId) && !sla.getBoolean("DELETED")) {
									// CONSTRUCT FIELD NAME
									slaName = sla.getString("NAME");
									fieldName = slaName;
									fieldName = fieldName.toUpperCase();
									fieldName = fieldName.trim();
									fieldName = fieldName.replaceAll("\\s+", "_");
									slaDocument = sla;
									workflow = (Document) sla.get("WORKFLOW");
									violations = (List<Document>) sla.get("VIOLATIONS");
									isRecurring = (boolean) sla.get("IS_RECURRING");
									maxReccurence = (int) sla.get("MAX_RECCURENCE");
									intervalTime = (int) sla.get("INTERVAL_TIME");

									if (entry.containsKey(fieldName) && entry.getDate(fieldName) != null) {
										TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

										timestamp = global
												.getFormattedDate(new Timestamp(entry.getDate(fieldName).getTime()));

									}
									break;
								}
							}
						}
						log.trace("TIMESTAMP: " + timestamp);
						// EXECUTE WORKFLOW ONLY IF TIMETSAMP IS NOT NULL AND HASN'T REACHED MAX NUMBER
						// OF RECCURENCE
						if (timestamp != null) {
							if (slaInfo.has("TIMESTAMP") && slaInfo.get("TIMESTAMP").equals(timestamp)) {
								// EXECUTE WORKFLOW
								ArrayList<Document> arrayList = (ArrayList<Document>) workflow.get("NODES");
								ArrayList<Document> nodeDocuments = arrayList;
								Document node = nodeDocuments.get(0);
								Map<String, Object> entryMap = new ObjectMapper().readValue(entry.toJson(), Map.class);
								entryMap.put("ENTRY_ID", dataId);
								entryMap.put("MODULE_ID", moduleId);
								List<Map<String, Object>> entriesList = new ArrayList<Map<String, Object>>();
								entriesList.add(entryMap);
								inputMessage.put("UPDATED_ENTRIES", entriesList);
								boolean isTriggerWorkFlow = true;
								if (slaDocument != null) {
									isTriggerWorkFlow = validateBusinessRulesForSla(slaDocument, companyId);
									if (isTriggerWorkFlow) {
										// ADDING META_DATA TO DISCUSSION FIELD
										if (discussionFieldName != null) {

											addMetaDataToDiscussion(slaName, companyId, dataId, discussionFieldId,
													moduleName, moduleId, userUuid, companyUuid);
										}
										parent.executeWorkflow(node, nodeDocuments, inputMessage);
									}
								}
							}
						}
					}

					// REMOVING FROM COLLECTION IF TIMESTAMP IS NULL (IF NOT RECURRING)
					for (Long time : timesToRemove) {
						slaTimes.remove(time);
						slaData.remove(time);
						if (fieldName != null) {
							entriesCollection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
									Updates.unset(fieldName));
						}
					}
					timesToRemove.clear();
					log.trace("Cleard SLA job from redis");

					// IF RECURRING, ADDING JOB BACK TO COLLECTION AND TO REDIS
					if (isRecurring && timestamp != null && entry != null && slaCount < maxReccurence) {
						int expiryMinutes = intervalTime;
						Timestamp currentTimestamp = new Timestamp(new Date().getTime());
						Calendar cal = Calendar.getInstance();
						cal.setTime(currentTimestamp);
						cal.add(Calendar.MINUTE, expiryMinutes);
						currentTimestamp.setTime(cal.getTime().getTime());

						entriesCollection.findOneAndUpdate(Filters.eq("_id", new ObjectId(dataId)),
								Updates.set(fieldName, new Date(currentTimestamp.getTime())));

						slaCount++;
						addToRedis(companyId, slaId, dataId, moduleId, moduleName, expiryMinutes, currentTimestamp,
								companyUuid, userUuid, maxReccurence, slaCount, isRecurring);
						log.trace("Recurring: Added new job to redis");
					}
				}
			}

			log.trace("Exit SlaJob.executeJob()");
		} catch (Exception e) {
			e.printStackTrace();
			String environment = env.getProperty("env");
			if (environment.equals("prd")) {
				sendMail.send("spencer@allbluesolutions.com", "support@ngdesk.com", "Exception in Sla Job",
						ExceptionUtils.getStackTrace(e));
				sendMail.send("shashank@allbluesolutions.com", "support@ngdesk.com", "Exception in Sla Job",
						ExceptionUtils.getStackTrace(e));
			}

		}
	}

	public void addMetaDataToDiscussion(String slaName, String companyId, String dataId, String discussionFieldId,
			String moduleName, String moduleId, String userUuid, String companyUuid) {

		log.trace("Enter SlaJob.addMetaDataToDiscussion()");

		try {
			String metadataHtml = global.getFile("sla_metadata.html");
			metadataHtml = metadataHtml.replace("SLA_NAME_REPLACE", slaName.trim());
			metadataHtml = metadataHtml.replaceAll("[\\n\\t]", " ");
			Document company = global.getCompanyFromUUID(companyUuid);
			if (discussionFieldId != null) {

				// INSERT META_DATA INTO MESSAGES USING DISCUSSION CONTROLLER

				Map<String, Object> inputMessage1 = new HashMap<String, Object>();
				inputMessage1.put("COMPANY_UUID", companyUuid);
				inputMessage1.put("USER_UUID", global.getSystemUser(companyId));

				List<Map<String, Object>> discussion = global.buildDiscussionPayload(inputMessage1, metadataHtml,
						"META_DATA");
				discussion.get(0).remove("DATE_CREATED");
				DiscussionMessage discussionMessage = new ObjectMapper().readValue(
						new ObjectMapper().writeValueAsString(discussion.get(0)).toString(), DiscussionMessage.class);
				discussionMessage.setSubdomain(company.getString("COMPANY_SUBDOMAIN"));
				discussionMessage.setModuleId(moduleId);
				discussionMessage.setEntryId(dataId);

				// To post discussion message.
				String systemAdminUserId = dataService.generateSystemUserEntry(companyId).getObjectId("_id").toString();
				dataService.addToDiscussionQueue(new PublishDiscussionMessage(discussionMessage,
						company.getString("COMPANY_SUBDOMAIN"), systemAdminUserId, true));
			}

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		log.trace("Exit SlaJob.addMetaDataToDiscussion()");
	}

	private boolean addToRedis(String companyId, String slaId, String dataId, String moduleId, String moduleName,
			int Expiry, Date timestamp, String companyUuid, String userUuid, int maxReccurence, int slaCount,
			boolean isRecurring) {

		log.trace("Enter SlaJob.addToRedis()");

		try {

			String epochDate = "01/01/1970";
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
			Timestamp epoch = new Timestamp(date.getTime());

			Timestamp today = new Timestamp(new Date().getTime());

			int minutesAfter = Expiry;
			long millisec = TimeUnit.MINUTES.toMillis(minutesAfter);
			long currentTimeDiff = today.getTime() + millisec - epoch.getTime();

			RSortedSet<Long> slaTimes = redisson.getSortedSet("slaTimes");
			RMap<Long, String> slaInfo = redisson.getMap("slaInfo");

			while (slaTimes.contains(currentTimeDiff)) {
				currentTimeDiff += 1;
			}
			slaTimes.add(currentTimeDiff);

			// PREPARING THE JSON FOR REDDIS
			JSONObject slaJsonInfo = new JSONObject();
			slaJsonInfo.put("COMPANY_ID", companyId);
			slaJsonInfo.put("SLA_ID", slaId);
			slaJsonInfo.put("MODULE_ID", moduleId);
			slaJsonInfo.put("MODULE_NAME", moduleName);
			slaJsonInfo.put("DATA_ID", dataId);
			slaJsonInfo.put("TIMESTAMP", global.getFormattedDate(new Timestamp(timestamp.getTime())));
			slaJsonInfo.put("COMPANY_UUID", companyUuid);
			slaJsonInfo.put("USER_UUID", userUuid);
			slaJsonInfo.put("SLA_COUNT", slaCount);
			slaInfo.put(currentTimeDiff, slaJsonInfo.toString());
			log.trace("Exit SlaJob.addToRedis()");
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean validateBusinessRulesForSla(Document document, String companyId) {
		try {
			log.trace("Enter DataService.validateBusinessRulesForSla()");
			if (!document.containsKey("BUSINESS_RULES")) {
				return true;
			}
			Document slaBusinessRules = (Document) document.get("BUSINESS_RULES");
			if (slaBusinessRules.containsKey("HAS_RESTRICTIONS") && slaBusinessRules.getBoolean("HAS_RESTRICTIONS")) {
				MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
				Document company = companiesCollection.find(Filters.eq("_id", new ObjectId(companyId))).first();
				String timeZone = "UTC";
				if (!company.getString("TIMEZONE").isEmpty()) {
					timeZone = company.getString("TIMEZONE");
				}

				// GET CURRENT HOURS AND MINUTES
				ZonedDateTime now = ZonedDateTime.now();
				now = now.toInstant().atZone(ZoneId.of(timeZone));
				int currentHour = now.getHour();
				int currentMinutes = now.getMinute();

				// GET CURRENT DAY OF THE WEEK
				Calendar calendar = Calendar.getInstance();
				int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

				String restrictionType = slaBusinessRules.getString("RESTRICTION_TYPE");
				List<Document> slaRestrictions = (ArrayList<Document>) slaBusinessRules.get("SLA_RESTRICTIONS");
				for (int j = 0; j < slaRestrictions.size(); j++) {
					Document restriction = slaRestrictions.get(j);
					String startTime = restriction.getString("START_TIME");
					String endTime = restriction.getString("END_TIME");

					Calendar cal = Calendar.getInstance();
					DateFormat dateFormat = new SimpleDateFormat("HH:mm");
					cal.setTime(dateFormat.parse(startTime));
					int startHour = cal.get(Calendar.HOUR_OF_DAY);

					cal.setTime(dateFormat.parse(endTime));

					int endHour = cal.get(Calendar.HOUR_OF_DAY);
					int endMinute = cal.get(Calendar.MINUTE);

					if (restrictionType.equals("Day")) {
						if (currentHour >= startHour && currentHour <= endHour) {
							if ((currentHour == endHour) && (currentMinutes > endMinute)) {
								return false;
							}
							return true;
						}
						if (endHour <= startHour) {

							if (endHour == startHour) {
								endHour = 24 + endHour;
								int timeWindow = endHour - startHour;
								if (currentHour < timeWindow && currentMinutes < endMinute) {
									return true;
								}
								return false;
							}
							if (currentHour <= startHour && currentHour > endHour) {
								if ((currentHour == endHour) && (currentMinutes > endMinute)) {
									return false;
								}
								return true;
							}
							endHour = 24 + endHour;
							int timeWindow = endHour - startHour;
							if (currentHour < timeWindow) {
								return true;
							}
						}
					} else if (restrictionType.equals("Week")) {
						String startDay = restriction.getString("START_DAY");
						String endDay = restriction.getString("END_DAY");
						int start = getDay(startDay);
						int end = getDay(endDay);
						if (start > end || (start == end && currentHour > endHour)) {
							if (currentDay <= end) {
								currentDay = currentDay + 7;
							}
							end = end + 7;
						}

						if (currentDay == start && currentDay == end) {
							if (startHour <= currentHour && currentHour < endHour) {
								if ((currentHour == endHour) && (currentMinutes > endMinute)) {
									return false;
								}
								return true;
							}
						} else if (currentDay >= start && currentDay <= end) {
							if (currentDay >= 7 && currentDay == end && start + 7 == end
									&& (currentHour < endHour || currentHour >= startHour)) {
								if ((currentHour == endHour) && (currentMinutes > endMinute)) {
									return false;
								}
								return true;
							} else if (currentDay == start) {
								if (currentHour >= startHour) {
									if ((currentHour == endHour) && (currentMinutes > endMinute)) {
										return false;
									}
									return true;
								}
							} else if (currentDay == end) {
								if (currentHour < endHour) {
									return true;
								}
							} else {
								return true;
							}
						}
					}
				}
			} else {
				return true;
			}
			log.trace("Exit DataService.validateBusinessRulesForSla()");
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	public int getDay(String day) {
		if (day.equals("Sun")) {
			return 0;
		}
		if (day.equals("Mon")) {
			return 1;
		}
		if (day.equals("Tue")) {
			return 2;
		}
		if (day.equals("Wed")) {
			return 3;
		}
		if (day.equals("Thu")) {
			return 4;
		}
		if (day.equals("Fri")) {
			return 5;
		}
		if (day.equals("Sat")) {
			return 6;
		}
		return -1;
	}

}
