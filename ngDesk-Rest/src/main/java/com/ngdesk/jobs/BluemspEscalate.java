package com.ngdesk.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.Notify;
import com.ngdesk.schedules.OnCallUser;

@Component
public class BluemspEscalate {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	private OnCallUser callUser;

	@Autowired
	private Notify notify;

	@Autowired
	private Global global;

	@Scheduled(fixedRate = 5 * 60 * 1000)
	public void run() {

		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.MINUTE, -5);

			Date fiveMinutesAgo = calendar.getTime();

			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
			Document bluemsp = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", "bluemsp-new")).first();

			if (bluemsp != null) {

				String companyId = bluemsp.getObjectId("_id").toString();

				MongoCollection<Document> ticketsCollection = mongoTemplate.getCollection("Tickets_" + companyId);
				MongoCollection<Document> contactsCollection = mongoTemplate.getCollection("Contacts_" + companyId);
				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);

				MongoCollection<Document> workflowsCollection = mongoTemplate.getCollection("module_workflows");
				Document workflow = workflowsCollection
						.find(Filters.and(Filters.eq("COMPANY_ID", companyId), Filters.eq("NAME", "Escalate"))).first();

				List<Document> stages = workflow.getList("STAGES", Document.class);
				Document stage = stages.get(0);

				List<Document> nodes = stage.getList("NODES", Document.class);

				Document javascriptNode = nodes.stream().filter(node -> node.getString("TYPE").equals("Javascript"))
						.findFirst().orElse(null);
 
				String code = javascriptNode.getString("CODE");
				String regex = "'(.*?)'+";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(code);

				List<String> domains = new ArrayList<String>();
				while (matcher.find()) {
					String domain = matcher.group(1);
					if (domain.contains(".")) {
						domains.add(domain);
					}
				}

				List<Document> tickets = ticketsCollection
						.find(Filters.and(Filters.eq("DELETED", false), Filters.eq("EFFECTIVE_TO", null),
								Filters.lte("DATE_UPDATED", fiveMinutesAgo),
								Filters.or(Filters.eq("STATUS", "Open"), Filters.eq("STATUS", "New"))))
						.into(new ArrayList<Document>());

				outer: for (Document ticket : tickets) {
					try {
						String subject = ticket.getString("SUBJECT");
						List<String> filteredSubjects = Arrays.asList("UD has started", "CpuWaitIO",
								"Apm_5c104b62b66cef00062d3262_Offline", "LCAD_STG", "ZabbixDEV", "ZDEV");

						for (String sub : filteredSubjects) {
							if (subject.contains(sub)) {
								continue outer;
							}
						}

						String requestorId = ticket.getString("REQUESTOR");
						Document contact = contactsCollection.find(Filters.eq("_id", new ObjectId(requestorId)))
								.first();

						String userId = contact.getString("USER");
						Document user = usersCollection.find(Filters.eq("_id", new ObjectId(userId))).first();

						String emailAddress = user.getString("EMAIL_ADDRESS");
						String domain = emailAddress.split("@")[1];

						if (!domain.equals("allbluesolutions.com")) {
							continue;
						}

						if (!domains.contains(domain)) {
							continue;
						}

						List<Document> messages = ticket.getList("MESSAGES", Document.class);

						int lastMessageFromRequestor = -1;
						for (int i = messages.size() - 1; i >= 0; i--) {

							Document message = messages.get(i);
							Document sender = (Document) message.get("SENDER");

							if (user.getString("USER_UUID").equals(sender.getString("USER_UUID"))) {
								lastMessageFromRequestor = i;
								break;
							}
						}

						if (lastMessageFromRequestor >= 0) {
							boolean escalationTriggered = false;
							for (int i = messages.size() - 1; i >= lastMessageFromRequestor; i--) {
								Document message = messages.get(i);
								if (message.getString("MESSAGE_TYPE").equals("META_DATA")) {
									String msg = message.getString("MESSAGE");
									if (msg.toLowerCase().contains("escalation")) {
										escalationTriggered = true;
										break;
									}
								}
							}

							if (!escalationTriggered) {
								String onCallUserId = callUser.CallUser("Primary On-Call", companyId);
								Document onCallUser = usersCollection
										.find(Filters.eq("_id", new ObjectId(onCallUserId))).first();

								JSONObject mobileParams = new JSONObject();

								String moduleId = "5c104b63b66cef00062d3267";
								String entryId = ticket.getObjectId("_id").toString();

								mobileParams.put("MODULE_ID", moduleId);
								mobileParams.put("DATA_ID", entryId);

								String htmlToReplace = "";
								String filename = "bell-icon.html";

								if (notify.notifyUser(companyId, userId, "Push", null, subject, "Ticket Escalation",
										mobileParams)) {
									htmlToReplace += global.getFile(filename).replaceAll("EMAIL_ADDRESS",
											onCallUser.getString("EMAIL_ADDRESS")) + "<br/>";
								}

								if (notify.notifyUser(companyId, userId, "Push", null, subject, "Ticket Escalation",
										mobileParams)) {
									htmlToReplace += global.getFile(filename).replaceAll("EMAIL_ADDRESS",
											"rob@allbluesolutions.com") + "<br/>";
								}

								String metadataHtml = global.getFile("escalation_metadata.html");
								metadataHtml = metadataHtml.replace("ESCALATION_RULE_NUMBER", "X");
								metadataHtml = metadataHtml.replaceAll("ESCALATION_NAME_REPLACE", "Manual Escalation");
								metadataHtml = metadataHtml.replaceAll("REPLACE_TO_LIST", htmlToReplace);

								Document systemUser = usersCollection
										.find(Filters.eq("EMAIL_ADDRESS", "system@ngdesk.com")).first();
								JSONObject sender = new JSONObject();

								if (systemUser != null) {
									sender.put("FIRST_NAME", "System");
									sender.put("LAST_NAME", "User");
									sender.put("ROLE", systemUser.getString("ROLE"));
									sender.put("USER_UUID", systemUser.getString("USER_UUID"));
								}

								JSONObject message = new JSONObject();
								message.put("MESSAGE_ID", UUID.randomUUID().toString());
								message.put("MESSAGE_TYPE", "META_DATA");
								message.put("MESSAGE", metadataHtml);
								message.put("SENDER", sender);

								Document discussionMessage = Document.parse(message.toString());
								discussionMessage.put("DATE_CREATED", new Date());

								ticketsCollection.updateOne(Filters.eq("_id", new ObjectId(entryId)),
										Updates.addToSet("MESSAGES", discussionMessage));
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
