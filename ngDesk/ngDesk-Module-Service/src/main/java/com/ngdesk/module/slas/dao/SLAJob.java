package com.ngdesk.module.slas.dao;

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
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.redisson.api.RMap;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.data.dao.SingleWorkflowPayload;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.field.dao.ModuleField;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.repositories.SLAInstanceRepository;
import com.ngdesk.repositories.SLARepository;

@Component
public class SLAJob {

	@Autowired
	private SLARepository slaRepository;

	@Autowired
	RedissonClient redisson;

	@Autowired
	DataService dataService;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	SLAInstanceRepository slaInstanceRepository;

	private final Logger log = LoggerFactory.getLogger(SLAJob.class);

	@Scheduled(fixedRate = 60000)
	public void executeSlaJob() {
		try {
			log.trace("Enter SlaJob.executeJob()");
			// CONNECT
			SLA slaDocument = new SLA();
			String epochDate = "01/01/1970";
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
			Timestamp epoch = new Timestamp(date.getTime());
			Timestamp today = new Timestamp(new Date().getTime());
			long currentTimeDiff = today.getTime() - epoch.getTime();
			log.trace("currentTimeDiff: " + currentTimeDiff);
			RSortedSet<Long> slaTimes = redisson.getSortedSet("slaTimes");
			RMap<Long, String> slaData = redisson.getMap("slaInfo");
			ObjectMapper mapper = new ObjectMapper();
			List<Long> timesToRemove = new ArrayList<Long>();

			for (Long timeDiff : slaTimes) {

				log.trace("timeDiff: " + timeDiff);

				if (currentTimeDiff >= timeDiff) {

					SLARedisPayload slaInfo = mapper.readValue(slaData.get(timeDiff), SLARedisPayload.class);
					log.trace("SLA_INFO: " + slaInfo);
					String slaId = slaInfo.getSlaId();
					String dataId = slaInfo.getDataId();

					Optional<SLAInstance> instance = slaInstanceRepository.findBySlaInstanceId(slaId, dataId);
					if (instance.isEmpty()) {
						// REMOVE ARRAY FOR REDDISON
						timesToRemove.add(timeDiff);
						continue;
					}
					SLAInstance slaInstance = instance.get();
					String companyId = slaInstance.getCompanyId();
					String moduleId = slaInstance.getModuleId();

					Optional<Map<String, Object>> optionalCompany = slaRepository.findBycompanyId(companyId,
							"companies");
					if (optionalCompany.isEmpty()) {
						// REMOVE ARRAY FOR REDDISON
						timesToRemove.add(timeDiff);
						slaInstanceRepository.deleteBySlaId(slaId, dataId, moduleId, companyId);
						continue;
					}

					int slaCount;
					if (slaInstance.getNumberOfExecutions() != null) {
						slaCount = slaInstance.getNumberOfExecutions();
					} else {
						slaCount = 0;
					}

					boolean isRecurring = false;
					int maxReccurence = 0;
					int intervalTime = 0;
					Date timestamp = null;
					String fieldName = null;

					// REMOVE ARRAY FOR REDDISON
					timesToRemove.add(timeDiff);

					Optional<Module> optionalModule = moduleRepository.findById(moduleId, "modules_" + companyId);
					if (optionalModule.isEmpty()) {
						// REMOVE ARRAY FOR REDDISON
						timesToRemove.add(timeDiff);
						continue;
					}
					Module module = optionalModule.get();
					String moduleName = module.getName();

					// GET ENTRY
					Optional<Map<String, Object>> optionalEntry = entryRepository.findById(dataId,
							moduleName + "_" + companyId);
					String collectionName = moduleName + "_" + companyId;
					Map<String, Object> entry = new HashMap<String, Object>();

					if (optionalEntry.isPresent()) {

						entry = optionalEntry.get();

						// GET DISCUSSION FIELD
						String discussionFieldName = null;
						String discussionFieldId = null;
						ModuleField discussionField = dataService.getDiscussionField(module.getFields());
						if (discussionField != null) {
							discussionFieldName = discussionField.getName();
							discussionFieldId = discussionField.getFieldId();
						}

						// GET SLA
						Optional<SLA> optioanlSla = slaRepository.findSlaBySlaId(slaId, companyId, moduleId);
						String slaName = null;
						if (optioanlSla.isPresent()) {
							SLA sla = optioanlSla.get();
							if (!sla.getDeleted()) {
								// CONSTRUCT FIELD NAME
								slaName = sla.getName();
								fieldName = dataService.getSlaFieldName(slaName);
								slaDocument = sla;
								isRecurring = sla.getIsRecurring();
								maxReccurence = sla.getRecurrence().getMaxRecurrence();
								intervalTime = sla.getRecurrence().getIntervalTime();

								if (entry.containsKey(fieldName) && entry.get(fieldName) != null) {
									timestamp = (Date) entry.get(fieldName);
								}

							}
						}
						log.trace("TIMESTAMP: " + timestamp);

						// EXECUTE WORKFLOW ONLY IF TIMETSAMP IS NOT NULL AND HASN'T REACHED MAX NUMBER
						// OF RECCURENCE
						if (timestamp != null) {
							// EXECUTE WORKFLOW
							if (slaInstance.getSlaTimeInfo() != null
									&& slaInstance.getSlaTimeInfo().equals(timestamp)) {
								boolean isTriggerWorkFlow = true;
								if (slaDocument != null) {
									isTriggerWorkFlow = validateBusinessRulesForSla(slaDocument, optionalCompany.get());
									if (isTriggerWorkFlow) {
										// ADDING META_DATA TO DISCUSSION FIELD
										addMetaDataToDiscussion(slaName, discussionFieldId, moduleName, slaInstance,
												discussionFieldName, entry);
										executeWorkflow(slaDocument, dataId);
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
							entryRepository.findEntryAndUpdateUnset(collectionName, dataId, fieldName);
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
						slaCount++;
						entryRepository.findEntryAndUpdate(collectionName, dataId, fieldName,
								new Date(currentTimestamp.getTime()));
						slaInstanceRepository.findEntryAndUpdate(slaId, dataId, "slaTimeInfo",
								new Date(currentTimestamp.getTime()));
						slaInstanceRepository.findEntryAndUpdate(slaId, dataId, "numberOfExecutions", slaCount);
						addToRedis(slaDocument, dataId, expiryMinutes, currentTimestamp);
						log.trace("Recurring: Added new job to redis");
					}
				}
			}

			log.trace("Exit SlaJob.executeJob()");
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public void addMetaDataToDiscussion(String slaName, String discussionFieldId, String moduleName,
			SLAInstance instance, String discussionFieldName, Map<String, Object> entry) {

		String companyId = instance.getCompanyId();
		String dataId = instance.getDataId();
		String metadata = "<div style='color: #68737D'><div class='mat-caption'><svg xmlns='http://www.w3.org/2000/svg' width=14 height=18 viewBox='0 0 24 24' style='fill: #d32f2f'><path d='M12 5.177l8.631 15.823h-17.262l8.631-15.823zm0-4.177l-12 22h24l-12-22zm-1 9h2v6h-2v-6zm1 9.75c-.689 0-1.25-.56-1.25-1.25s.561-1.25 1.25-1.25 1.25.56 1.25 1.25-.561 1.25-1.25 1.25z' /></svg><div style='color: #68737D; display: inline-block; vertical-align: top; font-weight: 500;'>SLA SLA_NAME_REPLACE violated</div></div></div>";
		metadata = metadata.replace("SLA_NAME_REPLACE", slaName.trim());
		metadata = metadata.replaceAll("[\\n\\t]", " ");
		DiscussionMessage metaDataMessage = dataService.buildMetaDataPayload(metadata, instance);
		if (discussionFieldId != null) {
			entryRepository.addDiscussionMessage(dataId, discussionFieldName, metaDataMessage,
					moduleName + "_" + companyId);
		}
		dataService.addMetaData(metaDataMessage, dataId, moduleName, companyId);

	}

	public void executeWorkflow(SLA sla, String dataId) {

		SingleWorkflowPayload singleWorkflowPayload = new SingleWorkflowPayload(sla.getCompanyId(), sla.getModuleId(),
				dataId, sla.getWorkflow(), sla.getLastUpdatedBy(), sla.getDateCreated());
		dataService.addToWorkflowQueue(singleWorkflowPayload);

	}

	private boolean addToRedis(SLA sla, String dataId, int Expiry, Date timestamp) {

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

			SLARedisPayload slaJsonInfo = new SLARedisPayload();
			slaJsonInfo.setSlaId(sla.getSlaId());
			slaJsonInfo.setDataId(dataId);
			slaInfo.put(currentTimeDiff, new ObjectMapper().writeValueAsString(slaJsonInfo));
			log.trace("Exit SlaJob.addToRedis()");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean validateBusinessRulesForSla(SLA document, Map<String, Object> company) {
		try {
			log.trace("Enter DataService.validateBusinessRulesForSla()");

			if (document.getBusinessRules() == null) {
				return true;
			}
			SLABusinessRules slaBusinessRules = document.getBusinessRules();
			if (document.getIsRestricted() != null && document.getIsRestricted() == true) {
				String timeZone = "UTC";
				if (!company.get("TIMEZONE").toString().isEmpty()) {
					timeZone = company.get("TIMEZONE").toString();
				}

				// GET CURRENT HOURS AND MINUTES
				ZonedDateTime now = ZonedDateTime.now();
				now = now.toInstant().atZone(ZoneId.of(timeZone));
				int currentHour = now.getHour();
				int currentMinutes = now.getMinute();

				// GET CURRENT DAY OF THE WEEK
				Calendar calendar = Calendar.getInstance();
				int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

				String restrictionType = slaBusinessRules.getRestrictionType();
				List<SLARestriction> slaRestrictions = slaBusinessRules.getRestrictions();
				for (int j = 0; j < slaRestrictions.size(); j++) {
					SLARestriction restriction = slaRestrictions.get(j);
					String startTime = restriction.getStartTime();
					String endTime = restriction.getEndTime();

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
						String startDay = restriction.getStartDay();
						String endDay = restriction.getEndDay();
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
