package com.ngdesk.websocket.channels.chat.dao;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.ngdesk.repositories.ChatChannelRepository;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.websocket.SessionService;
import com.ngdesk.websocket.UserSessions;
import com.ngdesk.websocket.companies.dao.ChatBusinessRules;
import com.ngdesk.websocket.companies.dao.ChatRestrictions;
import com.ngdesk.websocket.companies.dao.Company;

@Component
public class AgentAvailabilityService {

	@Autowired
	SessionService sessionService;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	CompaniesRepository companiesRepository;

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	RedisTemplate<String, AgentAvailability> redisTemplateForAgentAvailability;

	@Autowired
	RedisTemplate<String, ChatChannelMessage> redisTemplate;

	public void agentAvailability(AgentAvailability agentAvailability) {

		Optional<Company> optionalCompany = companiesRepository
				.findCompanyBySubdomain(agentAvailability.getCompanySubdomain());
		if (optionalCompany.isPresent()) {
			Company company = optionalCompany.get();
			String companyId = company.getId();
			ConcurrentHashMap<String, UserSessions> sessionMap = sessionService.sessions
					.get(company.getCompanySubdomain());
			String userId = null;
			Boolean isAgentAvailable = false;
			Boolean isBusinessHoursActive = null;
			ChatBusinessRules businessRules = company.getChatSettings().getChatBusinessRules();
			Boolean hasRestriction = company.getChatSettings().getHasRestrictions();
			String message = null;
			for (String key : sessionMap.keySet()) {
				userId = key;
				Optional<Map<String, Object>> optionalUserEntry = entryRepository.findById(userId,
						"Users_" + companyId);
				if (optionalUserEntry.isPresent()) {
					Integer chatEntries = entryRepository.findByAgentAndCollectionName(userId.toString(),
							"Chats_" + company.getId());
					UserSessions userSessions = sessionMap.get(key);
					String chatStatus = userSessions.getChatStatus();
					if (hasRestriction == null || !hasRestriction) {
						if (chatEntries < company.getChatSettings().getMaxChatPerAgent() && chatStatus != null
								&& chatStatus.equalsIgnoreCase("available")) {
							isAgentAvailable = true;
						}
					} else if (hasRestriction) {
						isBusinessHoursActive = validateBusinessRulesForAgentAssign(company, businessRules);
						if (chatEntries < company.getChatSettings().getMaxChatPerAgent() && chatStatus != null
								&& chatStatus.equalsIgnoreCase("available") && isBusinessHoursActive) {
							isAgentAvailable = true;
						}
					}
				}
			}

			if (isBusinessHoursActive != null && !isBusinessHoursActive) {
				message = "Inactive";
			}
			if (!isAgentAvailable) {
				AgentAvailability agentAvailable = new AgentAvailability(agentAvailability.getSessionUUID(),
						company.getCompanySubdomain(), "AgentAvailability", agentAvailability.getChannelName(), false,
						hasRestriction, message);
				addToAgentAvailabilityQueue(agentAvailable);
			} else {
				AgentAvailability agentAvailable = new AgentAvailability(agentAvailability.getSessionUUID(),
						company.getCompanySubdomain(), "AgentAvailability", agentAvailability.getChannelName(), true,
						hasRestriction, message);
				addToAgentAvailabilityQueue(agentAvailable);
			}

			Optional<ChatChannel> optionalChatChannel = chatChannelRepository
					.findChannelByName(agentAvailability.getChannelName(), "channels_chat_" + companyId);
			if (optionalChatChannel.isPresent()) {
				ChatChannelMessage chatChannelMessage = new ChatChannelMessage(companyId,
						agentAvailability.getSessionUUID(), optionalChatChannel.get(), "CHAT_CHANNEL");
				addToChatChannelQueue(chatChannelMessage);

			}

		}

	}

	public void addToAgentAvailabilityQueue(AgentAvailability message) {
		redisTemplateForAgentAvailability.convertAndSend("agentAvailability_notification", message);
	}

	public void addToChatChannelQueue(ChatChannelMessage chatChannelMessage) {
		redisTemplate.convertAndSend("chat_channel", chatChannelMessage);
	}

	public boolean validateBusinessRulesForAgentAssign(Company company, ChatBusinessRules chatBuisnessRules) {

		String timeZone = "UTC";
		if (company.getTimezone() != null) {
			timeZone = company.getTimezone();
		}

		// GET CURRENT HOURS AND MINUTES
		ZonedDateTime now = ZonedDateTime.now();
		now = now.toInstant().atZone(ZoneId.of(timeZone));
		int currentHour = now.getHour();
		int currentMinutes = now.getMinute();

		// GET CURRENT DAY OF THE WEEK
		Calendar calendar = Calendar.getInstance();
		int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
		String restrictionType = chatBuisnessRules.getRestrictionType();
		for (ChatRestrictions restriction : chatBuisnessRules.getChatRestrictions()) {
			return isValidBusinessHour(restriction, currentHour, currentMinutes, currentDay, restrictionType);
		}
		return false;
	}

	private boolean isValidBusinessHour(ChatRestrictions restriction, int currentHour, int currentMinutes,
			int currentDay, String restrictionType) {
		try {
			String startTime = restriction.getStartTime();
			String endTime = restriction.getEndTime();

			Calendar cal = Calendar.getInstance();
			DateFormat dateFormat = new SimpleDateFormat("HH:mm");

			cal.setTime(dateFormat.parse(startTime));
			int startHour = cal.get(Calendar.HOUR_OF_DAY);
			int startMinute = cal.get(Calendar.MINUTE);
			cal.setTime(dateFormat.parse(endTime));

			int endHour = cal.get(Calendar.HOUR_OF_DAY);
			int endMinute = cal.get(Calendar.MINUTE);
			switch (restrictionType) {

			case "Day":
				if (currentHour >= startHour && currentHour <= endHour) {
					if ((currentHour == endHour) && (currentMinutes > endMinute)) {
						return false;
					} else if ((currentHour == startHour) && (currentMinutes < startMinute)) {
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
				break;

			case "Week":
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
				break;
			default:
				return false;
			}
		} catch (ParseException e) {
			e.printStackTrace();

		}
		return false;

	}

	private int getDay(String day) {
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
