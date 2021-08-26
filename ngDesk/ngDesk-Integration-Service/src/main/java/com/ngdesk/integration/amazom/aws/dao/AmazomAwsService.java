package com.ngdesk.integration.amazom.aws.dao;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.integration.company.dao.Company;
import com.ngdesk.repositories.AmazonAwsRepository;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.DNSRecordRepository;
import com.ngdesk.repositories.ModuleEntryRepository;

@Component
public class AmazomAwsService {
	@Autowired
	AmazonAwsRepository amazonAwsRepository;

	@Autowired
	Global global;

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	DataProxy dataProxy;

	@Autowired
	DNSRecordRepository dnsRecordrepository;

	@Autowired
	CompanyRepository companyRepository;

	public void saveAwsMessage(AwsMessage awsMessage) {

		AwsMessage updatedAwsMessage = awsMessage;

		updatedAwsMessage.setSignature(null);
		updatedAwsMessage.setUnsubscribeURL(null);

		amazonAwsRepository.saveMessage(updatedAwsMessage, "current_aws_notifications");
	}

	public void ifTypeNotification(AwsMessage awsMessage) {
		if (awsMessage.getType().equalsIgnoreCase("Notification")) {

			Message message = parseMessage(awsMessage.getMessage());
			awsMessage.setCompanyId(authManager.getUserDetails().getCompanyId());
			awsMessage.setAlarmName(message.getAlarmName());
			awsMessage.setAwsAccountId(message.getAwsAccountId());

			Optional<Map<String, Object>> optionalAwsNotification = amazonAwsRepository.findByAlarmNameAndAccountId(
					message.getAlarmName(), message.getAwsAccountId(), awsMessage.getCompanyId(),
					"current_aws_notifications");

			if (optionalAwsNotification.isEmpty() && message.getNewStateValue().equalsIgnoreCase("Alarm")) {
				AwsMessage updatedMessage = ifNewStatevalueAlarm(awsMessage, message);
				saveAwsMessage(updatedMessage);
				return;

			} else if (optionalAwsNotification.isEmpty() && message.getNewStateValue().equalsIgnoreCase("OK")) {
				return;
			}

			Map<String, Object> awsNotification = optionalAwsNotification.get();

			if (awsNotification.get("ticketId") == null) {
				return;
			}
			String ticketId = awsNotification.get("ticketId").toString();
			awsMessage.setTicketId(ticketId);

			if (message.getNewStateValue().equalsIgnoreCase("Alarm")) {
				if (checkForDuplicateTicket(awsMessage, message, ticketId)) {
					AwsMessage updatedMessage = ifNewStatevalueAlarm(awsMessage, message);
					awsMessage.setTicketId(updatedMessage.getTicketId());
				}
			} else if (message.getNewStateValue().equalsIgnoreCase("OK")) {
				Optional<Map<String, Object>> optionalTicketEntry = moduleEntryRepository.findEntryByVariable("_id",
						ticketId, "Tickets_" + authManager.getUserDetails().getCompanyId());

				if (optionalTicketEntry.isEmpty()) {
					return;
				}

				Map<String, Object> ticketEntry = optionalTicketEntry.get();

				HashMap<String, Object> updatedTicketEntry = new HashMap<String, Object>();
				updatedTicketEntry.put("DATA_ID", ticketEntry.get("_id").toString());
				updatedTicketEntry.put("STATUS", "Closed");

				dataProxy.putModuleEntry(updatedTicketEntry, generateTicketModuleId(), false,
						authManager.getUserDetails().getCompanyId(), authManager.getUserDetails().getUserUuid(), false);

			}
			saveAwsMessage(awsMessage);
		}
	}

	public Message parseMessage(String stringMessage) {
		Message message = new Message();
		try {
			ObjectMapper mapper = new ObjectMapper();
			message = mapper.readValue(stringMessage, Message.class);
			return message;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}

	public String generateHtml(Message message, AwsMessage awsMessage) {
		String htmlMessage = global.getFile("aws_message.html");

		htmlMessage = htmlMessage.replace("TOPIC_REPLACE", awsMessage.getTopicArn());
		htmlMessage = htmlMessage.replace("ALARM_NAME_REPLACE", message.getAlarmName());
		htmlMessage = htmlMessage.replace("ALARM_DESCRIPTION_REPLACE", message.getAlarmDescription());
		htmlMessage = htmlMessage.replace("AWS_ACCOUNT_ID_REPLACE", message.getAwsAccountId());
		htmlMessage = htmlMessage.replace("NEW_STATE_VALUE_REPLACE", message.getNewStateValue());
		htmlMessage = htmlMessage.replace("NEW_STATE_REASON_REPLACE", message.getNewStateReason());
		htmlMessage = htmlMessage.replace("STATE_CHANGE_TIME_REPLACE", message.getStateChangeTime());
		htmlMessage = htmlMessage.replace("REGION_REPLACE", message.getRegion());
		htmlMessage = htmlMessage.replace("ALARM_ARN_REPLACE", message.getAlarmArn());
		htmlMessage = htmlMessage.replace("OLD_STATE_VALUE_REPLACE", message.getOldStateValue());
		htmlMessage = htmlMessage.replace("METRIC_NAME_REPLACE", message.getTrigger().get("MetricName").toString());

		return htmlMessage;
	}

	public Map<String, Object> postTicket(String subject, List<DiscussionMessage> message, String status) {

		HashMap<String, Object> entry = new HashMap<String, Object>();
		Optional<Map<String, Object>> optionalUser = moduleEntryRepository.findEntryByVariable("EMAIL_ADDRESS",
				"system@ngdesk.com", "Users_" + authManager.getUserDetails().getCompanyId());
		Map<String, Object> user = optionalUser.get();
		String requestor = user.get("CONTACT").toString();
		entry.put("MESSAGES", message);
		entry.put("SUBJECT", subject);
		entry.put("REQUESTOR", requestor);
		entry.put("STATUS", status);
		return dataProxy.postModuleEntry(entry, generateTicketModuleId(), false,
				authManager.getUserDetails().getCompanyId(), authManager.getUserDetails().getUserUuid());

	}

	public AwsMessage ifNewStatevalueAlarm(AwsMessage awsMessage, Message message) {

		DiscussionMessage discussionMessage = new DiscussionMessage();
		discussionMessage.setMessage(generateHtml(message, awsMessage));

		List<DiscussionMessage> discussionMessageList = new ArrayList();
		discussionMessageList.add(discussionMessage);

		Map<String, Object> ticketEntry = postTicket(awsMessage.getSubject(), discussionMessageList, "New");

		String ticketId = ticketEntry.get("DATA_ID").toString();
		awsMessage.setTicketId(ticketId);

		Optional<List<Map<String, Object>>> allAwsEntry = amazonAwsRepository.findAllAwsEntry(awsMessage.getAlarmName(),
				awsMessage.getAwsAccountId(), awsMessage.getCompanyId(), "current_aws_notifications");
		
		for (Map<String, Object> entry : allAwsEntry.get()) {
			entry.put("ticketId", ticketId);
			
			amazonAwsRepository.updateAwsEntry(entry, "current_aws_notifications");
		}

		return awsMessage;
	}

	public boolean checkForDuplicateTicket(AwsMessage awsMessage, Message message, String ticketId) {

		Optional<Map<String, Object>> optionalTicketEntry = moduleEntryRepository.findEntryByVariable("_id", ticketId,
				"Tickets_" + authManager.getUserDetails().getCompanyId());

		if (optionalTicketEntry.isPresent()) {
			Map<String, Object> ticketEntry = optionalTicketEntry.get();
			if (ticketEntry.get("STATUS").toString().equals("Closed")
					|| ticketEntry.get("STATUS").toString().equals("Resolved")) {
				return true;
			} else {
				updateTicketDiscussionMessage(awsMessage, message, ticketEntry);
				return false;
			}
		}
		return true;
	}

	public void updateTicketDiscussionMessage(AwsMessage awsMessage, Message message, Map<String, Object> ticketEntry) {

		try {
			DiscussionMessage discussionMessage = new DiscussionMessage();
			discussionMessage.setMessage(generateHtml(message, awsMessage));
			List<DiscussionMessage> discussionMessageList = new ArrayList();
			discussionMessageList.add(discussionMessage);

			HashMap<String, Object> updatedTicketEntry = new HashMap<String, Object>();

			updatedTicketEntry.put("DATA_ID", ticketEntry.get("_id").toString());
			updatedTicketEntry.put("MESSAGES", discussionMessageList);

			dataProxy.putModuleEntry(updatedTicketEntry, generateTicketModuleId(), false,
					authManager.getUserDetails().getCompanyId(), authManager.getUserDetails().getUserUuid(), false);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String generateTicketModuleId() {
		Optional<Map<String, Object>> optionalTicket = moduleEntryRepository.findOneModule("Tickets",
				"modules_" + authManager.getUserDetails().getCompanyId());
		String moduleId = optionalTicket.get().get("_id").toString();
		return moduleId;
	}

	public String getSubDomain(String requestURL) {
		String subDomain = "";

		if (requestURL.equals("localhost") || requestURL.equals("127.0.0.1")) {
			subDomain = "dev1";
		} else if (!requestURL.endsWith("ngdesk.com")) {

			String cname = requestURL;
			Optional<Map<String, Object>> optionalcname = dnsRecordrepository.findDNSRecordByCname(cname,
					"dns_records");
			if (!optionalcname.isEmpty()) {
				subDomain = (String) optionalcname.get().get("COMPANY_SUBDOMAIN");
			}
		} else {
			subDomain = requestURL.split("\\.ngdesk\\.com")[0];
		}
		return subDomain;
	}

	public String getCompanyIdBySubDomain(String subDomain) {

		Optional<Company> optionalCompany = companyRepository.getCompanyBySubdomain(subDomain);
		return optionalCompany.get().getComapnyId();
	}

	public String getSystemUserUUID(String companyId) {
		Optional<Map<String, Object>> optionalSystemUser = moduleEntryRepository.findEntryByVariable("EMAIL_ADDRESS",
				"system@ngdesk.com", "Users_" + companyId);
		Map<String, Object> systemUser = optionalSystemUser.get();

		return systemUser.get("USER_UUID").toString();
	}

	public AwsMessage buildAwsMessage(InputStream inputStream) {
		AwsMessage awsNotification = new AwsMessage();
		try {
			Scanner scan = new Scanner(inputStream);
			StringBuilder builder = new StringBuilder();
			while (scan.hasNextLine()) {
				builder.append(scan.nextLine());
			}
			scan.close();

			ObjectMapper mapper = new ObjectMapper();
			awsNotification = mapper.readValue(builder.toString(), AwsMessage.class);

			return awsNotification;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return awsNotification;
	}

	public void getTicketId(Message message, AwsMessage awsMessage) {

	}
}
