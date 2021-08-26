package com.ngdesk.modules.monitors;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.json.JSONObject;
import org.redisson.api.RMap;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ngdesk.email.SendEmail;

@Component
public class MonitorJob {

	@Autowired
	private RedissonClient redisson;

	@Value("${email.host}")
	private String host;
	
	@Value("${env}")
	private String environment;	
	
	private final Logger log = LoggerFactory.getLogger(MonitorJob.class);

	// TODO: Is Unique operator could be added back, look up checkforUniqueness
	// function

//	@Scheduled(fixedRate = 60000)
//	public void executeMonitorJob() {
//
//		log.trace("Enter MonitorJob.executeMonitorJob()");
//		RedissonClient redisson = null;
//		try {
//			Config config = new Config();
//			config.useSingleServer().setAddress("redis://" + env.getProperty("redis.host") + ":6379")
//					.setPassword(env.getProperty("redis.password"));
//			redisson = Redisson.create(config);
//
//			String epochDate = "01/01/1970";
//			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
//			Timestamp epoch = new Timestamp(date.getTime());
//
//			Timestamp today = new Timestamp(new Date().getTime());
//			long currentTimeDiff = today.getTime() - epoch.getTime();
//
//			RSet<Long> intervalTimes = redisson.getSet("intervalTimes");
//			RMap<Long, String> monitorMap = redisson.getMap("monitorMap");
//
//			List<Long> times = new ArrayList<Long>();
//
//			for (Long timeDiff : intervalTimes) {
//
//				if (currentTimeDiff >= timeDiff) {
//
//					JSONObject monitorJson = new JSONObject(monitorMap.get(timeDiff));
//					String monitorId = monitorJson.getString("MONITOR_ID");
//					String moduleId = monitorJson.getString("MODULE_ID");
//					String companyId = monitorJson.getString("COMPANY_ID");
//
//					boolean exists = checkMonitorExists(monitorId, companyId, moduleId, timeDiff);
//
//					if (exists) {
//						executeMonitor(monitorJson);
//						int interval = getInterval(companyId, moduleId, monitorId);
//						times.add(timeDiff);
//						long newTimeDiff = today.getTime() + TimeUnit.MINUTES.toMillis(interval) - epoch.getTime();
//						intervalTimes.add(newTimeDiff);
//						monitorMap.put(newTimeDiff, monitorJson.toString());
//					} else {
//						times.add(timeDiff);
//					}
//
//				} else {
//					break;
//				}
//			}
//			for (Long time : times) {
//				monitorMap.remove(time);
//				intervalTimes.remove(time);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			redisson.shutdown();
//		}
//		log.trace("Exit MonitorJob.executeMonitorJob()");
//	}

//	public void executeMonitor(JSONObject monitorObj) {
//
//		log.trace("Enter MonitorJob.executeMonitor() at: " + new Timestamp(new Date().getTime()));
//		try {
//
//			String monitorId = monitorObj.getString("MONITOR_ID");
//
//			String moduleId = monitorObj.getString("MODULE_ID");
//			String companyId = monitorObj.getString("COMPANY_ID");
//
//			boolean valid = false;
//
//			ModuleMonitor monitor = getMonitor(companyId, moduleId, monitorId);
//			List<Condition> monitorConditions = getMonitorConditions(companyId, moduleId, monitorId);
//			List<Bson> allFilters = new ArrayList<Bson>();
//			List<Bson> anyFilters = new ArrayList<Bson>();
//			List<Document> documents = new ArrayList<Document>();
//
//			for (Condition condition : monitorConditions) {
//				generateFilter(condition, companyId, moduleId, allFilters, anyFilters);
//			}
//
//			if (allFilters.size() > 0 || anyFilters.size() > 0) {
//				documents = getDocuments(companyId, moduleId, allFilters, anyFilters);
//				if (documents.size() > 0) {
//					valid = true;
//				}
//			}
//
//			if (valid) {
//				List<MonitorAction> actions = monitor.getMonitoractions();
//				for (MonitorAction actionObj : actions) {
//
//					if (actionObj.getAction().equals("Send Email")) {
//						List<Value> values = actionObj.getValues();
//						String to = null;
//						String from = null;
//						String subject = null;
//						String body = null;
//						for (Value value : values) {
//							if (value.getOrder() == 1) {
//								from = value.getValue().toString();
//							}
//							if (value.getOrder() == 2) {
//								to = value.getValue().toString();
//							}
//							if (value.getOrder() == 3) {
//								subject = value.getValue().toString();
//							}
//							if (value.getOrder() == 4) {
//								body = value.getValue().toString();
//							}
//						}
//						String host = env.getProperty("email.host");
//						SendEmail email = new SendEmail(to, from, subject, body, host);
//						email.sendEmail();
//
//					} else if (actionObj.getAction().equals("Start Escalation")) {
//						List<Value> values = actionObj.getValues();
//						String escalationId = values.get(0).getValue();
//						String subject = values.get(1).getValue();
//						String body = values.get(2).getValue();
//
//						entryEscalation(escalationId, companyId, documents, monitorId, moduleId, subject, body);
//
//					} else if (actionObj.getAction().equals("Stop Escalation")) {
//						deleteEscalationsFromRedis(companyId, documents);
//					}
//				}
//
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		log.trace("Exit MonitorJob.executeMonitor() at: " + new Timestamp(new Date().getTime()));
//	}

//	public void generateFilter(Condition condition, String companyId, String moduleId, List<Bson> allFilters,
//			List<Bson> anyFilters) {
//		String fieldId = condition.getCondition();
//		String operator = condition.getOpearator();
//		String value = condition.getConditionValue();
//		String requirementType = condition.getRequirementType();
//		String fieldName = getFieldName(fieldId, companyId, moduleId);
//
//		if (operator.equals("equals to")) {
//			if (requirementType.equalsIgnoreCase("All")) {
//				allFilters.add(Filters.eq(fieldName, value));
//			}
//			if (requirementType.equalsIgnoreCase("Any")) {
//				anyFilters.add(Filters.eq(fieldName, value));
//			}
//		} else if (operator.equals("not equals")) {
//			if (requirementType.equalsIgnoreCase("All")) {
//				allFilters.add(Filters.ne(fieldName, value));
//			}
//			if (requirementType.equalsIgnoreCase("Any")) {
//				anyFilters.add(Filters.ne(fieldName, value));
//			}
//		} else if (operator.equals("greater than")) {
//			if (requirementType.equalsIgnoreCase("All")) {
//				allFilters.add(Filters.gt(fieldName, value));
//			}
//			if (requirementType.equalsIgnoreCase("Any")) {
//				anyFilters.add(Filters.gt(fieldName, value));
//			}
//		} else if (operator.equals("less than")) {
//			if (requirementType.equalsIgnoreCase("All")) {
//				allFilters.add(Filters.lt(fieldName, value));
//			}
//			if (requirementType.equalsIgnoreCase("Any")) {
//				anyFilters.add(Filters.lt(fieldName, value));
//			}
//		} else if (operator.equalsIgnoreCase("contain")) {
//			if (requirementType.equalsIgnoreCase("All")) {
//				allFilters.add(Filters.regex(fieldName, ".*" + value + ".*"));
//			}
//			if (requirementType.equalsIgnoreCase("Any")) {
//				anyFilters.add(Filters.regex(fieldName, ".*" + value + ".*"));
//			}
//		} else if (operator.equalsIgnoreCase("does not contain")) {
//			if (requirementType.equalsIgnoreCase("All")) {
//				allFilters.add(Filters.not(Filters.regex(fieldName, ".*" + value + ".*")));
//			}
//			if (requirementType.equalsIgnoreCase("Any")) {
//				anyFilters.add(Filters.not(Filters.regex(fieldName, ".*" + value + ".*")));
//			}
//		} else if (operator.equalsIgnoreCase("regex")) {
//			if (requirementType.equalsIgnoreCase("All")) {
//				allFilters.add(Filters.regex(fieldName, ".*" + value + ".*"));
//			}
//			if (requirementType.equalsIgnoreCase("Any")) {
//				anyFilters.add(Filters.regex(fieldName, ".*" + value + ".*"));
//			}
//		}
//	}

//	public List<Document> getDocuments(String companyId, String moduleId, List<Bson> allFilters,
//			List<Bson> anyFilters) {
//
//		List<Document> entryDocuments = new ArrayList<Document>();
//		try {
//			log.trace("Enter MonitorJob.evaluateConditions() at: " + new Timestamp(new Date().getTime()));
//			String CollectionName = "modules_" + companyId;
//			MongoCollection<Document> collection = mongoTemplate.getCollection(CollectionName);
//			if (!ObjectId.isValid(moduleId)) {
//				throw new BadRequestException("INVALID_MODULE_ID");
//			}
//			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
//			String modulename = module.getString("NAME");
//			String modulecollectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
//			MongoCollection<Document> modulecollection = mongoTemplate.getCollection(modulecollectionName);
//
//			if (allFilters.size() != 0 && anyFilters.size() != 0) {
//				entryDocuments = modulecollection.find(Filters.and(Filters.and(allFilters), Filters.or(anyFilters)))
//						.into(new ArrayList<Document>());
//			} else if (allFilters.size() == 0) {
//				entryDocuments = modulecollection.find(Filters.or(anyFilters)).into(new ArrayList<Document>());
//			} else if (anyFilters.size() == 0) {
//				entryDocuments = modulecollection.find(Filters.and(allFilters)).into(new ArrayList<Document>());
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		log.trace("Exit MonitorJob.evaluateConditions() at: " + new Timestamp(new Date().getTime()));
//		return entryDocuments;
//	}

//	public String getFieldName(String fieldId, String companyId, String moduleId) {
//		String fieldName = null;
//		String CollectionName = "modules_" + companyId;
//		MongoCollection<Document> collection = mongoTemplate.getCollection(CollectionName);
//		if (!ObjectId.isValid(moduleId)) {
//			throw new BadRequestException("INVALID_MODULE_ID");
//		}
//		Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
//		ArrayList<Document> fieldDocuments = (ArrayList) module.get("FIELDS");
//
//		for (Document fieldDoc : fieldDocuments) {
//			if (fieldDoc.getString("FIELD_ID").equals(fieldId)) {
//				fieldName = fieldDoc.getString("NAME");
//			}
//		}
//		return fieldName;
//	}
//
	public void addEscalationToRedis(String escalationId, Document ruleDocument, String companyId, String entryId) {
		log.trace("Enter MonitorJob.addEscalationToRedis()");
		try {

			String epochDate = "01/01/1970";
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
			Timestamp epoch = new Timestamp(date.getTime());

			Timestamp today = new Timestamp(new Date().getTime());

			int minutesAfter = ruleDocument.getInteger("MINS_AFTER");
			long millisec = TimeUnit.MINUTES.toMillis(minutesAfter);
			long currentTimeDiff = today.getTime() + millisec - epoch.getTime();

			RSortedSet<Long> escalationTimes = redisson.getSortedSet("escalationTimes");
			RMap<Long, String> escalationRules = redisson.getMap("escalationRules");

			while (escalationTimes.contains(currentTimeDiff)) {
				currentTimeDiff += 1;
			}

			escalationTimes.add(currentTimeDiff);
			JSONObject ruleJson = new JSONObject(ruleDocument.toJson().toString());
			ruleJson.put("COMPANY_ID", companyId);
			ruleJson.put("ESCALATION_ID", escalationId);
			ruleJson.put("ENTRY_ID", entryId);
			escalationRules.put(currentTimeDiff, ruleJson.toString());

			log.trace("ADDED NEW ESCALATION");

		} catch (Exception e) {
			e.printStackTrace();

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();

			if (environment.equals("prd")) {
				SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com", "support@ngdesk.com",
						"Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToSpencer.sendEmail();

				SendEmail sendEmailToShashank = new SendEmail("shashank.shankaranand@allbluesolutions.com",
						"support@ngdesk.com", "Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToShashank.sendEmail();
			}
		}
		log.trace("Exit MonitorJob.addEscalationToRedis()");
	}
//
//	public void deleteEscalationsFromRedis(String companyId, List<Document> documents) {
//		RedissonClient redisson = null;
//		try {
//			log.trace("Enter MonitorJob.deleteEscalationFromRedis()");
//			String collectionName = "escalated_entries_" + companyId;
//			MongoCollection<Document> escalationEntriescollection = mongoTemplate.getCollection(collectionName);
//
//			for (Document entryDoc : documents) {
//				String entryId = entryDoc.getObjectId("_id").toString();
//				escalationEntriescollection.findOneAndDelete(Filters.eq("ENTRY_ID", entryId));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		log.trace("Exit MonitorJob.deleteEscalationFromRedis()");
//	}
//
//	public int getInterval(String companyId, String moduleId, String monitorId) {
//		int interval = 0;
//		try {
//			log.trace("Enter MonitorJob.getInterval()");
//			String moduleCollectionName = "modules_" + companyId;
//			MongoCollection<Document> collection = mongoTemplate.getCollection(moduleCollectionName);
//			if (!ObjectId.isValid(moduleId)) {
//				throw new BadRequestException("INVALID_MODULE_ID");
//			}
//			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
//			ArrayList<Document> monitorDocuments = (ArrayList) module.get("MONITORS");
//			for (Document monitorDoc : monitorDocuments) {
//				if (monitorDoc.getString("MONITOR_ID").equals(monitorId)) {
//
//					ModuleMonitor monitor = new ObjectMapper().readValue(monitorDoc.toJson(), ModuleMonitor.class);
//
//					interval = monitor.getInterval();
//				}
//
//			}
//
//		} catch (JsonParseException e) {
//			e.printStackTrace();
//		} catch (JsonMappingException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		log.trace("Exit MonitorJob.getInterval()");
//		return interval;
//	}
//
//	public List<Condition> getMonitorConditions(String companyId, String moduleId, String monitorId) {
//		List<Condition> conditions = new ArrayList<Condition>();
//		try {
//			log.trace("Enter MonitorJob.getMonitorConditions() companyId: " + companyId + ", monitorId: " + monitorId
//					+ ", moduleId: " + moduleId);
//			String moduleCollectionName = "modules_" + companyId;
//			MongoCollection<Document> collection = mongoTemplate.getCollection(moduleCollectionName);
//			if (!ObjectId.isValid(moduleId)) {
//				throw new BadRequestException("INVALID_MODULE_ID");
//			}
//			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
//			ArrayList<Document> monitorDocuments = (ArrayList) module.get("MONITORS");
//			for (Document monitorDoc : monitorDocuments) {
//				if (monitorDoc.getString("MONITOR_ID").equals(monitorId)) {
//					ModuleMonitor monitor;
//					monitor = new ObjectMapper().readValue(monitorDoc.toJson(), ModuleMonitor.class);
//
//					conditions = monitor.getConditions();
//				}
//			}
//		} catch (JsonParseException e) {
//			e.printStackTrace();
//		} catch (JsonMappingException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		log.trace("Exit MonitorJob.getMonitorConditions() companyId: " + companyId + ", monitorId: " + monitorId
//				+ ", moduleId: " + moduleId);
//		return conditions;
//	}
//
//	public ModuleMonitor getMonitor(String companyId, String moduleId, String monitorId) {
//
//		try {
//			log.trace("Enter MonitorJob.getMonitor() companyId: " + companyId + ", monitorId: " + monitorId
//					+ ", moduleId: " + moduleId);
//			String moduleCollectionName = "modules_" + companyId;
//			MongoCollection<Document> collection = mongoTemplate.getCollection(moduleCollectionName);
//			if (!ObjectId.isValid(moduleId)) {
//				throw new BadRequestException("INVALID_MODULE_ID");
//			}
//			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
//			ArrayList<Document> monitorDocuments = (ArrayList) module.get("MONITORS");
//			for (Document monitorDoc : monitorDocuments) {
//				if (monitorDoc.getString("MONITOR_ID").equals(monitorId)) {
//					ModuleMonitor monitor = new ObjectMapper().readValue(monitorDoc.toJson(), ModuleMonitor.class);
//					return monitor;
//				}
//			}
//		} catch (JsonParseException e) {
//			e.printStackTrace();
//		} catch (JsonMappingException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		log.trace("Exit MonitorJob.getMonitor() companyId: " + companyId + ", monitorId: " + monitorId + ", moduleId: "
//				+ moduleId);
//		return null;
//	}
//
//	public void entryEscalation(String escalationId, String companyId, List<Document> documents, String monitorId,
//			String moduleId, String subject, String body) {
//		log.trace("Enter MonitorJob.entryEscalation() escalationId: " + escalationId + ", companyId: " + companyId);
//		try {
//
//			String collectionName = "escalated_entries_" + companyId;
//			MongoCollection<Document> escalationEntriescollection = mongoTemplate.getCollection(collectionName);
//			String escalationCollectionName = "escalations_" + companyId;
//			MongoCollection<Document> escalationcollection = mongoTemplate.getCollection(escalationCollectionName);
//
//			for (Document entryDoc : documents) {
//				String entryId = entryDoc.getObjectId("_id").toString();
//				Document document = escalationEntriescollection
//						.find(Filters.and(Filters.eq("ENTRY_ID", entryId), Filters.eq("ESCALATION_ID", escalationId)))
//						.first();
//				if (document == null) {
//					JSONObject entryObj = new JSONObject();
//
//					entryObj.put("ENTRY_ID", entryId);
//					entryObj.put("MODULE_ID", moduleId);
//					entryObj.put("ESCALATION_ID", escalationId);
//					entryObj.put("SUBJECT", subject);
//					entryObj.put("BODY", body);
//
//					Document escalatedEntryDoc = Document.parse(entryObj.toString());
//					escalationEntriescollection.insertOne(escalatedEntryDoc);
//					Document escalationDocument = escalationcollection
//							.find(Filters.eq("_id", new ObjectId(escalationId))).first();
//
//					// Add First Rule to Redis
//					ArrayList<Document> ruleDocuments = (ArrayList) escalationDocument.get("RULES");
//					addEscalationToRedis(escalationId, ruleDocuments.get(0), companyId);
//				}
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		log.trace("Exit MonitorJob.entryEscalation() escalationId: " + escalationId + ", companyId: " + companyId);
//	}
//
//	public boolean checkMonitorExists(String monitorId, String companyId, String moduleId, long timeDiff) {
//		RedissonClient redisson = null;
//		try {
//			log.trace("Enter MonitorJob.checkMonitorExists() monitorId: " + monitorId + ", moduleId: " + moduleId);
//			Config config = new Config();
//			config.useSingleServer().setAddress("redis://" + env.getProperty("redis.host") + ":6379")
//					.setPassword(env.getProperty("redis.password"));
//			redisson = Redisson.create(config);
//			RSet<Long> intervalTimes = redisson.getSet("intervalTimes");
//			RMap<Long, String> monitorMap = redisson.getMap("monitorMap");
//
//			String moduleCollectionName = "modules_" + companyId;
//			MongoCollection<Document> collection = mongoTemplate.getCollection(moduleCollectionName);
//			if (!ObjectId.isValid(moduleId)) {
//				throw new BadRequestException("INVALID_MODULE_ID");
//			}
//			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
//			if (module != null) {
//				if (module.get("MONITORS") != null) {
//					ArrayList<Document> monitorDocuments = (ArrayList) module.get("MONITORS");
//					for (Document monitorDoc : monitorDocuments) {
//						if (monitorDoc.get("MONITOR_ID") != null) {
//							if (monitorDoc.getString("MONITOR_ID").equals(monitorId)) {
//								log.trace("Exit MonitorJob.checkMonitorExists() monitorId: " + monitorId
//										+ ", moduleId: " + moduleId);
//								return true;
//							}
//						}
//					}
//				}
//			}
//			intervalTimes.remove(timeDiff);
//			monitorMap.remove(timeDiff);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			redisson.shutdown();
//		}
//		log.trace("Exit MonitorJob.checkMonitorExists() monitorId: " + monitorId + ", moduleId: " + moduleId);
//		return false;
//	}

}
