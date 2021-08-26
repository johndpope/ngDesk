package com.ngdesk.schedule.dao;

//import java.sql.Time;
//import java.sql.Timestamp;
//import java.text.DateFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.time.Instant;
//import java.time.ZoneId;
//import java.time.ZoneOffset;
//import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Date;
//import java.util.TimeZone;
//
//import org.bson.Document;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.stereotype.Component;
//
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.model.Filters;

// @Component
public class OnCallUser {
//	private final Logger log = LoggerFactory.getLogger(OnCallUser.class);
//
//	@Autowired
//	private MongoTemplate mongoTemplate;
//
//	@SuppressWarnings("deprecation")
//	public String CallUser(String name, String companyId) {
//		String userId = null;
//
//		try {
//			log.trace("Enter OnCallUser.CallUser() companyId: " + companyId + ", name:" + name);
//			String collectionName = "schedules_" + companyId;
//			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
//			Document scheduleDocument = collection.find(Filters.eq("NAME", name)).first();
//
//			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//			JSONObject schedule = new JSONObject(scheduleDocument.toJson().toString());
//			JSONArray layers = schedule.getJSONArray("LAYERS");
//
//			String timeZone = schedule.getString("TIMEZONE");
//			Instant instant = Instant.now();
//			ZoneId z = ZoneId.of(timeZone);
//			ZoneOffset currentOffsetForMyZone = z.getRules().getOffset(instant);
//			int secondsOffset = currentOffsetForMyZone.getTotalSeconds();
//
//			for (int i = layers.length() - 1; i >= 0; i--) {
//
//				JSONObject layer = layers.getJSONObject(i);
//				String rotationType = layer.getString("ROTATION_TYPE");
//				JSONArray users = layer.getJSONArray("USERS");
//
//				String startDate = layer.getString("START_DATE");
//				Date parsedDate = df.parse(startDate);
//				Timestamp startTimestamp = new Timestamp(parsedDate.getTime());
//
//				Timestamp UTCtimestamp = getUTC(timeZone);
//
//				long ms = UTCtimestamp.getTime() - startTimestamp.getTime();
//				double hours = (double) ms / (60 * 60 * 1000);
//
//				if (rotationType.equals("Weekly")) {
//					int noOfRotations = (int) (Math.ceil(hours / 168));
//					userId = users.get((noOfRotations - 1) % users.length()).toString();
//				} else if (rotationType.equals("Daily")) {
//					int noOfRotations = (int) (Math.ceil(hours / 24));
//					userId = users.get((noOfRotations - 1) % users.length()).toString();
//				}
//
//				// Restrictions
//				boolean hasRestrictions = layer.getBoolean("HAS_RESTRICTIONS");
//				if (hasRestrictions) {
//					JSONArray layerRestrictions = layer.getJSONArray("LAYER_RESTRICTIONS");
//					for (int j = 0; j < layerRestrictions.length(); j++) {
//						JSONObject restriction = layerRestrictions.getJSONObject(j);
//						String restrictionType = layer.getString("RESTRICTION_TYPE");
//						String startTime = restriction.getString("START_TIME");
//						String endTime = restriction.getString("END_TIME");
//						DateFormat formatter = new SimpleDateFormat("HH:mm");
//						Time start_Time = new Time(formatter.parse(startTime).getTime() - secondsOffset * 1000);
//						Time end_Time = new Time(formatter.parse(endTime).getTime() - secondsOffset * 1000);
//						Timestamp startTimeStamp = getstartTime(timeZone, startTime);
//						Timestamp endTimeStamp = getEndTime(timeZone, endTime);
//
//						if (restrictionType.equals("Day")) {
//							if (start_Time.before(end_Time)) {
//								if (UTCtimestamp.after(startTimeStamp) && UTCtimestamp.before(endTimeStamp)) {
//									return userId;
//								}
//								if (UTCtimestamp.equals(startTimeStamp) || UTCtimestamp.equals(endTimeStamp)) {
//									return userId;
//								}
//							} else {
//								endTimeStamp.setDate(endTimeStamp.getDate() + 1);
//								if (UTCtimestamp.after(startTimeStamp) && UTCtimestamp.before(endTimeStamp)) {
//									return userId;
//								}
//								if (UTCtimestamp.equals(startTimeStamp) || UTCtimestamp.equals(endTimeStamp)) {
//									return userId;
//								}
//							}
//						} else if (restrictionType.equals("Week")) {
//							String startDay = restriction.getString("START_DAY");
//							String endDay = restriction.getString("END_DAY");
//							int day_UTC = UTCtimestamp.getDay();
//							int start = getDay(startDay);
//							int end = getDay(endDay);
//
//							Timestamp currentTimestamp = getTimestampAtTimezone(timeZone);
//
//							Double currTime = (currentTimestamp.getHours()
//									+ ((double) currentTimestamp.getMinutes() / 60));
//							Double stTime = Integer.parseInt(startTime.split(":")[0])
//									+ ((double) Integer.parseInt(startTime.split(":")[1]) / 60);
//							Double etTime = Integer.parseInt(endTime.split(":")[0])
//									+ ((double) Integer.parseInt(endTime.split(":")[1]) / 60);
//
//							if (start > end || (start == end && stTime > etTime)) {
//								if (day_UTC <= end) {
//									day_UTC = day_UTC + 7;
//								}
//								end = end + 7;
//							}
//
//							if (day_UTC == start && day_UTC == end) {
//								if (stTime <= currTime && currTime < etTime) {
//									return userId;
//								}
//							} else if (day_UTC >= start && day_UTC <= end) {
//								if (day_UTC >= 7 && day_UTC == end && start + 7 == end
//										&& (currTime < etTime || currTime >= stTime)) {
//									return userId;
//								} else if (day_UTC == start) {
//									if (currTime >= stTime) {
//										return userId;
//									}
//								} else if (day_UTC == end) {
//									if (currTime < etTime) {
//										return userId;
//									}
//								} else {
//									return userId;
//								}
//							}
//						}
//					}
//				} else {
//					return userId;
//				}
//			}
//		} catch (ParseException e) {
//			e.printStackTrace();
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		log.trace("Exit OnCallUser.CallUser() companyId: " + companyId + ", name:" + name);
//		return "";
//	}
//
//	private Timestamp getUTC(String timeZone) {
//
//		log.trace("Enter OnCallUser.getUTC() timeZone: " + timeZone);
//		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//		Timestamp currentTimezoneTimestamp = null;
//		try {
//			Timestamp currentTimestamp = new Timestamp(new Date().getTime());
//
//			ZonedDateTime z = currentTimestamp.toInstant().atZone(ZoneId.of(timeZone));
//			String currentTime = z.format(fmt);
//
//			Date currentparsedDate = dateFormat.parse(currentTime);
//			currentTimezoneTimestamp = new Timestamp(currentparsedDate.getTime());
//
//			TimeZone tz = TimeZone.getTimeZone(timeZone);
//			int offset = tz.getOffset(new Date().getTime());
//			currentTimezoneTimestamp.setTime(currentparsedDate.getTime() - offset);
//
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//		log.trace("Exit OnCallUser.getUTC() timeZone: " + timeZone);
//		return currentTimezoneTimestamp;
//	}
//
//	private Timestamp getTimestampAtTimezone(String timeZone) {
//		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//		Timestamp currentTimezoneTimestamp = null;
//
//		try {
//			Timestamp currentTimestamp = new Timestamp(new Date().getTime());
//
//			ZonedDateTime z = currentTimestamp.toInstant().atZone(ZoneId.of("America/Chicago"));
//			String currentTime = z.format(fmt);
//
//			Date currentparsedDate = dateFormat.parse(currentTime);
//			currentTimezoneTimestamp = new Timestamp(currentparsedDate.getTime());
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//
//		return currentTimezoneTimestamp;
//	}
//
//	private Timestamp getstartTime(String timeZone, String startTime) throws ParseException {
//		log.trace("Enter OnCallUser.getstartTime() timeZone: " + timeZone + ", startTime: " + startTime);
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
//		SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
//
//		Timestamp currentTimestamp = new Timestamp(new Date().getTime());
//		ZonedDateTime z = currentTimestamp.toInstant().atZone(ZoneId.of(timeZone));
//		String currentTime = z.format(fmt);
//		Date currentparsedDate = dateFormat.parse(currentTime);
//		Timestamp startTimestamp = new Timestamp(currentparsedDate.getTime());
//
//		Date startparsedDate = hourFormat.parse(startTime);
//
//		startTimestamp.setHours(startparsedDate.getHours());
//		startTimestamp.setMinutes(startparsedDate.getMinutes());
//		startTimestamp.setSeconds(startparsedDate.getSeconds());
//		Date parsedDate = dateFormat.parse(startTimestamp.toString());
//
//		TimeZone tz = TimeZone.getTimeZone(timeZone);
//		int offset = tz.getOffset(new Date().getTime());
//		startTimestamp.setTime(parsedDate.getTime() - offset);
//		log.trace("Exit OnCallUser.getstartTime() timeZone: " + timeZone + ", startTime: " + startTime);
//		return startTimestamp;
//	}
//
//	private Timestamp getEndTime(String timeZone, String endTime) throws ParseException {
//		log.trace("Enter OnCallUser.getstartTime() timeZone: " + timeZone + ", endTime: " + endTime);
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
//		SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
//
//		Timestamp currentTimestamp = new Timestamp(new Date().getTime());
//		ZonedDateTime z = currentTimestamp.toInstant().atZone(ZoneId.of(timeZone));
//		String currentTime = z.format(fmt);
//		Date currentparsedDate = dateFormat.parse(currentTime);
//		Timestamp endTimestamp = new Timestamp(currentparsedDate.getTime());
//
//		Date endparsedDate = hourFormat.parse(endTime);
//		endTimestamp.setHours(endparsedDate.getHours());
//		endTimestamp.setMinutes(endparsedDate.getMinutes());
//		endTimestamp.setSeconds(endparsedDate.getSeconds());
//		Date parsedDate = dateFormat.parse(endTimestamp.toString());
//		TimeZone tz = TimeZone.getTimeZone(timeZone);
//		int offset = tz.getOffset(new Date().getTime());
//		endTimestamp.setTime(parsedDate.getTime() - offset);
//		log.trace("Exit OnCallUser.getstartTime() timeZone: " + timeZone + ", endTime: " + endTime);
//		return endTimestamp;
//	}
//
//	private Timestamp getcurrentDate(String timeZone) throws ParseException {
//		log.trace("Enter OnCallUser.getcurrentDate() timeZone: " + timeZone);
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
//		Timestamp currentTimestamp = new Timestamp(new Date().getTime());
//
//		ZonedDateTime z = currentTimestamp.toInstant().atZone(ZoneId.of(timeZone));
//		String currentTime = z.format(fmt);
//		Date currentparsedDate = dateFormat.parse(currentTime);
//		Timestamp currentTimezoneTimestamp = new Timestamp(currentparsedDate.getTime());
//		log.trace("Exit OnCallUser.getcurrentDate() timeZone: " + timeZone);
//		return currentTimezoneTimestamp;
//	}
//
//	public int getDay(String day) {
//		if (day.equals("Sun")) {
//			return 0;
//		}
//		if (day.equals("Mon")) {
//			return 1;
//		}
//		if (day.equals("Tue")) {
//			return 2;
//		}
//		if (day.equals("Wed")) {
//			return 3;
//		}
//		if (day.equals("Thu")) {
//			return 4;
//		}
//		if (day.equals("Fri")) {
//			return 5;
//		}
//		if (day.equals("Sat")) {
//			return 6;
//		}
//		return -1;
//	}
//
}
