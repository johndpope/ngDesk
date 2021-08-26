package com.ngdesk.data.dao;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.joda.time.LocalDateTime;
import org.redisson.api.RMap;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.data.sla.dao.SLA;
import com.ngdesk.data.sla.dao.SLAInstance;
import com.ngdesk.data.sla.dao.SLARedisPayload;
import com.ngdesk.data.sla.dao.SLARelationship;
import com.ngdesk.data.sla.dao.Violation;

import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.sla.SLAInstanceRepository;
import com.ngdesk.repositories.sla.SLARepository;

@Component
public class SlaService {

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ModuleService moduleService;

	@Autowired
	RedissonClient client;

	@Autowired
	SLAInstanceRepository slaInstanceRepository;

	@Autowired
	SLARepository slaRepository;

	public List<String> generateSlaFieldNames(String moduleId, String companyId) {
		List<String> slaFieldNames = new ArrayList<String>();

		List<SLA> slas = slaRepository.findAllSlaByModuleId(moduleId, companyId);
		if (!slas.isEmpty()) {
			for (SLA sla : slas) {
				String name = sla.getName();
				name = name.trim();
				name = name.toUpperCase();
				name = name.replaceAll("\\s+", "_");
				slaFieldNames.add(name);
			}
		}
		return slaFieldNames;
	}

	public Map<String, Object> postSlaCheckViolationAndAddToReddis(SLA sla, Module module, Map<String, Object> entry) {

		Violation violation = sla.getViolation();
		if (violation == null) {
			return entry;
		}
		Map<String, String> fieldNames = new HashMap<String, String>();

		module.getFields().forEach(field -> {
			fieldNames.put(field.getFieldId(), field.getName());
		});

		Set<String> entryKeys = entry.keySet();

		int expiryMinutes = sla.getSlaExpiry();

		Timestamp currentTimestamp = new Timestamp(new Date().getTime());
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentTimestamp);
		cal.add(Calendar.MINUTE, expiryMinutes);
		Date date = cal.getTime();

		String slaFieldName = getSlaFieldName(sla.getName());

		String fieldId = violation.getCondition();
		String operator = violation.getOperator();
		String value = violation.getConditionValue();

		String fieldName = fieldNames.get(fieldId);

		if (!entryKeys.contains(fieldName)) {
			return entry;
		}

		if (operator.equalsIgnoreCase("HAS_BEEN")) {
			if (value.equals(entry.get(fieldName).toString())) {
				entry.put(slaFieldName, date);

				SLARedisPayload redisPayload = new SLARedisPayload(sla.getSlaId(), entry.get("_id").toString());

				addToRedis(redisPayload, expiryMinutes);
				addSlaInstance(sla, entry.get("_id").toString(), date);
			}
		} else if (operator.equalsIgnoreCase("IS_PAST_BY")) {
			entry = evaluateIsPastBy(module, fieldName, entry, expiryMinutes, sla);
		} else if (operator.equalsIgnoreCase("IS_WITHIN")) {
			entry = evaluateIsWithin(module, fieldName, entry, expiryMinutes, sla);
		} else if (!(operator.equalsIgnoreCase("HAS_NOT_BEEN_REPLIED_BY") && value.equalsIgnoreCase("REQUESTOR"))) {
			entry.put(slaFieldName, date);

			SLARedisPayload redisPayload = new SLARedisPayload(sla.getSlaId(), entry.get("_id").toString());

			addToRedis(redisPayload, expiryMinutes);
			addSlaInstance(sla, entry.get("_id").toString(), date);
		}

		return entry;
	}

	public Map<String, Object> putSlaCheckViolationAndAddToReddis(SLA sla, Module module, Map<String, Object> entry,
			Map<String, Object> existingEntry) {
		try {
			ObjectMapper mapper = new ObjectMapper();

			Violation violation = sla.getViolation();
			if (violation == null) {
				return entry;
			}

			List<ModuleField> fields = module.getFields();
			Map<String, String> fieldNames = new HashMap<String, String>();
			Set<String> entryKeys = entry.keySet();

			fields.forEach(field -> fieldNames.put(field.getFieldId(), field.getName()));

			String slaFieldName = getSlaFieldName(sla.getName());

			int expiryMinutes = sla.getSlaExpiry();
			Timestamp currentTimestamp = new Timestamp(new Date().getTime());
			Calendar cal = Calendar.getInstance();
			cal.setTime(currentTimestamp);
			cal.add(Calendar.MINUTE, expiryMinutes);
			Date date = cal.getTime();

			String fieldId = violation.getCondition();
			String operator = violation.getOperator();
			String value = violation.getConditionValue();
			String fieldName = fieldNames.get(fieldId);

			if (!entryKeys.contains(fieldName)) {
				return entry;
			}
			if (operator.equalsIgnoreCase("HAS_BEEN")) {
				if (value.equals(entry.get(fieldName).toString())) {

					if (!existingEntry.containsKey(slaFieldName) && existingEntry.get(slaFieldName) == null) {
						entry.put(slaFieldName, date);

						SLARedisPayload payload = new SLARedisPayload(sla.getSlaId(), entry.get("_id").toString());
						addToRedis(payload, expiryMinutes);
						addSlaInstance(sla, entry.get("_id").toString(), date);
					} else {
						entry.put(slaFieldName, existingEntry.get(slaFieldName));
					}
				} else {
					entry.put(slaFieldName, null);
				}
			} else if (operator.equalsIgnoreCase("HAS_NOT_CHANGED")) {
				if (entry.containsKey(fieldName) && entry.get(fieldName) != null) {
					if (existingEntry.containsKey(slaFieldName) && existingEntry.get(slaFieldName) != null) {

						entry.put(slaFieldName, existingEntry.get(slaFieldName));
					} else if (!existingEntry.containsKey(slaFieldName) || existingEntry.get(slaFieldName) == null) {

						entry.put(slaFieldName, date);
						SLARedisPayload payload = new SLARedisPayload(sla.getSlaId(), entry.get("_id").toString());

						addToRedis(payload, expiryMinutes);
						addSlaInstance(sla, entry.get("_id").toString(), date);
					}
				} else {
					entry.remove(slaFieldName);
				}
			} else if (operator.equalsIgnoreCase("HAS_NOT_BEEN_REPLIED_BY")) {

				List<SLARelationship> values = mapper.readValue(value,
						mapper.getTypeFactory().constructCollectionType(List.class, SLARelationship.class));
				List<String> dataIds = new ArrayList<String>();

				values.forEach(relationShipValue -> {
					dataIds.add(relationShipValue.getDataId());
				});

				List<DiscussionMessage> newMessages = mapper.readValue(mapper.writeValueAsString(entry.get(fieldName)),
						mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class));
				List<DiscussionMessage> oldMessages = mapper.readValue(
						mapper.writeValueAsString(existingEntry.get(fieldName)),
						mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class));

				List<DiscussionMessage> filteredNewMessages = newMessages.stream()
						.filter(newMessage -> !newMessage.getMessageType().equalsIgnoreCase("META_DATA"))
						.collect(Collectors.toList());
				List<DiscussionMessage> filteredOldMessages = oldMessages.stream()
						.filter(oldMessage -> !oldMessage.getMessageType().equalsIgnoreCase("META_DATA"))
						.collect(Collectors.toList());

				DiscussionMessage lastMessage = null;
				if (filteredOldMessages.size() != filteredNewMessages.size()) {

					lastMessage = filteredNewMessages.get(filteredNewMessages.size() - 1);

					Sender sender = lastMessage.getSender();
					String requestor = entry.get("REQUESTOR").toString();
					String userUuid = sender.getUserUuid();

					Optional<Map<String, Object>> optionalUser = moduleEntryRepository.findEntryByFieldName("USER_UUID",
							userUuid, "Users_" + authManager.getUserDetails().getCompanyId());

					if (!optionalUser.isEmpty()) {
						Map<String, Object> user = optionalUser.get();
						List<String> teams = (List<String>) user.get("TEAMS");

						for (String dataId : dataIds) {
							if (teams != null && teams.size() > 0 && teams.contains(dataId)) {
								// UPDATE FIELD WITH null
								entry.remove(slaFieldName);

							} else if (value.equalsIgnoreCase("REQUESTOR")
									&& requestor.equals(user.get("_id").toString())) {
								// UPDATE FIELD WITH null
								entry.remove(slaFieldName);
							} else if (existingEntry.containsKey(slaFieldName)
									&& existingEntry.get(slaFieldName) != null) {
								// UPDATE FIELD WITH OLD TIME

								entry.put(slaFieldName, existingEntry.get(slaFieldName));

							} else {
								// ADD NEW TIME TO FIELD AND ADD NEW JOB TO REDDIS

								entry.put(slaFieldName, date);

								SLARedisPayload payload = new SLARedisPayload(sla.getSlaId(),
										entry.get("_id").toString());
								addToRedis(payload, expiryMinutes);
								addSlaInstance(sla, entry.get("_id").toString(), date);

							}
						}

					}
				} else {
					if (existingEntry.containsKey(slaFieldName) && existingEntry.get(slaFieldName) != null) {

						entry.put(slaFieldName, existingEntry.get(slaFieldName));

					} else {
						// ADD NEW TIME TO FIELD AND ADD NEW JOB TO REDDIS
						entry.put(slaFieldName, date);

						SLARedisPayload payload = new SLARedisPayload(sla.getSlaId(), entry.get("_id").toString());

						addToRedis(payload, expiryMinutes);
						addSlaInstance(sla, entry.get("_id").toString(), date);

					}
				}
			} else if (operator.equalsIgnoreCase("IS_PAST_BY")) {
				evaluateIsPastBy(module, fieldName, entry, expiryMinutes, sla);

			} else if (operator.equalsIgnoreCase("IS_WITHIN")) {
				evaluateIsWithin(module, fieldName, existingEntry, expiryMinutes, sla);
			}

		} catch (JsonMappingException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		return entry;
	}

	public Map<String, Object> unsetSlaKeyIfExists(SLA sla, Map<String, Object> payload) {
		String slaFieldName = getSlaFieldName(sla.getName());
		payload.remove(slaFieldName);
		return payload;

	}

	private String getSlaFieldName(String slaName) {
		slaName = slaName.toUpperCase();
		slaName = slaName.trim();
		slaName = slaName.replaceAll("\\s+", "_");
		return slaName;
	}

	private Map<String, Object> evaluateIsPastBy(Module module, String fieldName, Map<String, Object> entry,
			int expiryMinutes, SLA sla) {
		String slaFieldName = getSlaFieldName(sla.getName());
		if (entry.containsKey(fieldName) && entry.get(fieldName) != null) {
			String dateInString = entry.get(fieldName).toString();

			Date entryDate = (Date) entry.get(fieldName);

			long entryTimeInMilliSecond = entryDate.getTime() + (expiryMinutes * 60000);
			Timestamp entrySLATimestamp = new Timestamp(entryTimeInMilliSecond);

			// FETCH TIMEZONE
			Calendar calender = Calendar.getInstance();
			calender.setTime(entryDate);

			// CURRENT TIME FOR FETCHED TIMEZONE
			Date current = LocalDateTime.now().toDate(calender.getTimeZone());
			long currentMilliSecondInEntryTimezone = current.getTime();
			if (currentMilliSecondInEntryTimezone < entryTimeInMilliSecond) {

				entry.put(slaFieldName, entrySLATimestamp);

				// ONLY FOR IS WITHIN AND IS PAST
				expiryMinutes = Math.round((entryTimeInMilliSecond - currentMilliSecondInEntryTimezone) / 60000);

				SLARedisPayload payload = new SLARedisPayload(sla.getSlaId(), entry.get("_id").toString());
				addToRedis(payload, expiryMinutes);
				addSlaInstance(sla, entry.get("_id").toString(), entrySLATimestamp);
			} else {
				entry.remove(slaFieldName);
			}

		}
		return entry;

	}

	private Map<String, Object> evaluateIsWithin(Module module, String fieldName, Map<String, Object> entry,
			int expiryMinutes, SLA sla) {
		try {
			String slaFieldName = getSlaFieldName(sla.getName());
			if (entry.get(fieldName) != null && !entry.get(fieldName).toString().isBlank()) {

				String dateString = entry.get(fieldName).toString();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSX");
				Date entryDate = df.parse(dateString);

				long entryTimeInMilliSecond = entryDate.getTime() - (expiryMinutes * 60000);

				Timestamp entrySLATimestamp = new Timestamp(entryTimeInMilliSecond);

				// FETCH TIMEZONE
				Calendar calender = Calendar.getInstance();
				calender.setTime(entryDate);

				// CURRENT TIME FOR FETCHED TIMEZONE
				Date current = LocalDateTime.now().toDate(calender.getTimeZone());
				long currentMilliSecondInEntryTimezone = current.getTime();

				if (currentMilliSecondInEntryTimezone < entryTimeInMilliSecond) {
					entry.put(slaFieldName, entrySLATimestamp);

					// ONLY FOR IS WITHIN AND IS PAST
					expiryMinutes = Math.round((entryTimeInMilliSecond - currentMilliSecondInEntryTimezone) / 60000);

					SLARedisPayload payload = new SLARedisPayload(sla.getSlaId(), entry.get("_id").toString());
					addToRedis(payload, expiryMinutes);
					addSlaInstance(sla, entry.get("_id").toString(), entrySLATimestamp);
				} else {
					entry.remove(slaFieldName);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return entry;
	}

	private boolean addToRedis(SLARedisPayload payload, int expiryMinutes) {
		try {

			String epochDate = "01/01/1970";
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
			Timestamp epoch = new Timestamp(date.getTime());

			Timestamp today = new Timestamp(new Date().getTime());

			long millisec = TimeUnit.MINUTES.toMillis(expiryMinutes);
			long currentTimeDiff = today.getTime() + millisec - epoch.getTime();

			RSortedSet<Long> slaTimes = client.getSortedSet("slaTimes");

			RMap<Long, String> slaInfo = client.getMap("slaInfo");

			while (slaTimes.contains(currentTimeDiff)) {
				currentTimeDiff += 1;
			}

			slaTimes.add(currentTimeDiff);
			slaInfo.put(currentTimeDiff, new ObjectMapper().writeValueAsString(payload));
		} catch (ParseException e) {
			e.printStackTrace();

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void addSlaInstance(SLA sla, String dataId, Date date) {
		try {
			SLAInstance slaInstance = new SLAInstance();
			slaInstance.setCompanyId(sla.getCompanyId());
			slaInstance.setDataId(dataId);
			slaInstance.setModuleId(sla.getModuleId());
			slaInstance.setSlaId(sla.getSlaId());
			slaInstance.setSlaTimeInfo(date);
			slaInstance.setNumberOfExecutions(0);
			Optional<SLAInstance> optionalSlaInstance = slaInstanceRepository.findBySlaIdAndDataId(sla.getSlaId(),
					dataId, sla.getModuleId(), sla.getCompanyId());
			if (!optionalSlaInstance.isPresent()) {
				slaInstanceRepository.save(slaInstance, "sla_in_execution");
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
	}
}
