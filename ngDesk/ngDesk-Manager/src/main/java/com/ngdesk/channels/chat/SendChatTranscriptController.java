package com.ngdesk.channels.chat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.SendMail;

//@Component
//@RestController
public class SendChatTranscriptController {

	private final Logger log = LoggerFactory.getLogger(SendChatTranscriptController.class);

	@Autowired
	private Environment env;

	@Autowired
	SendMail sendMail;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@GetMapping("/modules/{module_id}/entries/{entry_id}")
	public ResponseEntity<Object> sendChatTranscript(HttpServletRequest request,
			@PathVariable("module_id") String moduleId, @PathVariable("entry_id") String entryId) {
		try {
			String url = request.getRequestURL().toString();
			String subdomain = request.getAttribute("SUBDOMAIN").toString();
			String companyId = "";
			Document company = global.getCompanyFromSubdomain(subdomain);
			companyId = company.getObjectId("_id").toString();

			// BUILDING CHAT TRANSCRIPT
			MongoCollection<Document> chatCollection = mongoTemplate.getCollection("Chat_" + companyId);
			Document chatDoc = chatCollection.find(Filters.eq("_id", new ObjectId(entryId))).first();
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			String chatTranscipt = global.getFile("chat_transcript.html");
			List<Document> chatMessages = (List<Document>) chatDoc.get("CHAT");
			String messageChat = "";
			String companyTimezone = "UTC";
			if (company.getString("TIMEZONE") != null) {
				companyTimezone = company.get("TIMEZONE").toString();
			}
			for (Document chat : chatMessages) {
				String chatWithoutHtml = Jsoup.clean(chat.get("MESSAGE").toString(), Whitelist.simpleText());
				Document sender = (Document) chat.get("SENDER");
				Document roleDoc = rolesCollection.find(Filters.eq("_id", new ObjectId(sender.get("ROLE").toString())))
						.first();
				String role = roleDoc.get("NAME").toString();
				String chatCreatedTime = chat.get("DATE_CREATED").toString();
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				formatter.setTimeZone(TimeZone.getTimeZone(companyTimezone));
				long timeStamp = formatter.parse(chatCreatedTime).getTime();
				SimpleDateFormat requiredDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
				String chatDisplayDateTime = requiredDateFormat.format(timeStamp);
				if (chat.get("MESSAGE_TYPE").equals("META_DATA")) {
					if (messageChat.length() != 0) {
						messageChat = messageChat + "<br/>" + "(" + chatDisplayDateTime + ")" + " "
								+ "<div class='mat-caption' style='color: #68737D; font-weight: 500;'>"
								+ chatWithoutHtml + "</div>";
					} else {
						messageChat = "(" + chatDisplayDateTime + ")" + " "
								+ "<div class='mat-caption' style='color: #68737D; font-weight: 500;'>"
								+ chatWithoutHtml + "</div>";
					}
				}

				if (chat.get("MESSAGE_TYPE").equals("MESSAGE")) {
					Document name = (Document) chat.get("SENDER");
					if (messageChat.length() == 0) {
						messageChat = "(" + chatDisplayDateTime + ")" + "  " + sender.get("FIRST_NAME").toString()
								+ ": " + chatWithoutHtml + "<br/>";
					} else {
						messageChat = messageChat + "<br/>" + "(" + chatDisplayDateTime + ")" + "  "
								+ sender.get("FIRST_NAME").toString() + ": " + chatWithoutHtml + "<br/>";
					}
				}
			}
			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
			Document userRequestor = usersCollection
					.find(Filters.eq("_id", new ObjectId(chatDoc.get("REQUESTOR").toString()))).first();

			String lastName = "";
			if (userRequestor.get("LAST_NAME") != null) {
				lastName = userRequestor.get("LAST_NAME").toString();
			}
			String userName = userRequestor.get("FIRST_NAME").toString() + " " + lastName;

			chatTranscipt = chatTranscipt.replace("NAME_REPLACE", userName);
			chatTranscipt = chatTranscipt.replace("CHAT_HISTORY_REPLACE", messageChat);

			// FETCHING DATA REQUIRED TO SEND EMAIL
			String to = userRequestor.get("EMAIL_ADDRESS").toString();
			String from = "support@ngdesk.com";
			String subject = "Chat Transcript from ngDesk";
			String body = chatTranscipt;
			sendMail.send(to, from, subject, body);

			log.trace("Enter SendChatTranscriptController.endChat()");

		} catch (ParseException e) {
			e.printStackTrace();
			return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.trace("Exit SendChatTranscriptController.endChat()");
		return new ResponseEntity<Object>(HttpStatus.OK);
	}
}
