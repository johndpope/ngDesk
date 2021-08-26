package com.ngdesk.publicticket;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.ParseEmail;
import com.ngdesk.SendMail;

@RestController
@Component
public class PublicTicketService {
	private final Logger log = LoggerFactory.getLogger(PublicUserEntry.class);

	@Autowired
	private ParseEmail parseEmail;

	@Autowired
	private Environment env;

	@Autowired
	SendMail sendMail;

	final int maxLengthInBytes = 10485760;

	@PostMapping("/public/ticket_create")
	public ResponseEntity<Object> PublicTicketService(@RequestBody @Valid PublicUserEntry ticketDetails,
			HttpServletRequest request) {
		log.trace("Enter PublicTicketService.PublicTicketService()");

		try {
			JSONObject ticket = new JSONObject();
			JSONArray ccEmailArray = null;
			JSONObject ccEmailOb = null;
			JSONArray attachments = new JSONArray();
			HashMap<String, String> inputMap = new HashMap<String, String>();

			String emailPattern = "[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+";

			String toEmail = ticketDetails.getTo();
			String fromEmail = ticketDetails.getFrom();
			String subject = ticketDetails.getSubject();
			String firstName = ticketDetails.getFirstName();
			String lastName = ticketDetails.getLastName();
			String body = ticketDetails.getBody();

			Pattern pattern = Pattern.compile(emailPattern);
			Matcher toMatcher = pattern.matcher(toEmail);
			Matcher fromMatcher = pattern.matcher(fromEmail);

			inputMap.put("TO", toEmail);
			inputMap.put("FROM", fromEmail);
			inputMap.put("SUBJECT", subject);
			inputMap.put("FIRST_NAME", firstName);
			inputMap.put("LAST_NAME", lastName);
			inputMap.put("BODY", body);

			for (String key : inputMap.keySet()) {
				if (key.equalsIgnoreCase("To")) {
					if (toMatcher.find()) {
						ticket.put("TO", decodeUTF8(toEmail.getBytes("ISO-8859-1")));
						ccEmailOb = new JSONObject();
						ccEmailArray = new JSONArray();
						ccEmailOb.put("CC_EMAIL", toEmail);
						ccEmailOb.put("IS_CC", false);
						ccEmailArray.put(ccEmailOb);
						ticket.put("CC_EMAILS", ccEmailArray);
					} else {
						return new ResponseEntity<Object>("Invalid email address", HttpStatus.BAD_REQUEST);
					}
				} else if (key.equalsIgnoreCase("From")) {
					if (fromMatcher.find()) {
						ticket.put("FROM", decodeUTF8(fromEmail.getBytes("ISO-8859-1")));
					} else {
						return new ResponseEntity<Object>("Invalid email address", HttpStatus.BAD_REQUEST);
					}
				} else if (key.equalsIgnoreCase("FIRST_NAME")) {
					ticket.put("FIRST_NAME", firstName);
				} else if (key.equalsIgnoreCase("LAST_NAME")) {
					ticket.put("LAST_NAME", lastName);
				} else if (key.equalsIgnoreCase("SUBJECT")) {
					ticket.put("SUBJECT", subject);
				} else if (key.equalsIgnoreCase("BODY")) {
					ticket.put("BODY", body);
				}
			}

			List<PublicTicketAttachment> attachmentList = ticketDetails.getAttachments();
			if (!attachmentList.isEmpty()) {
				for (PublicTicketAttachment a : attachmentList) {
					JSONObject attachment = new JSONObject();
					String fileName = a.getFileName();
					attachment.put("FILE_NAME", fileName);
					if (fileName.contains(".")) {
						int idx = fileName.lastIndexOf('.');
						attachment.put("FILE_EXTENSION", fileName.substring(idx, fileName.length()));
					}
					byte[] bytes = a.getFile().getBytes();
					String encoded = java.util.Base64.getEncoder().encodeToString(bytes);
					if (encoded.length() < 104000000) {
						attachment.put("FILE", a.getFile());
						attachments.put(attachment);
					}
				}
				ticket.put("ATTACHMENTS", attachments);
			}

			log.trace("Data is entered in ticket :" + ticket);

			if (request.getContentLength() > maxLengthInBytes) {

				sendMail.send(ticket.getString("FROM"), "support@ngdesk.com",
						"RE: Failed to create ticket. " + ticket.getString("SUBJECT"),
						"Hello,<br/><br/>We are unable to process your email as its size has exceeded our limit of 10MB.<br/><br/>"
								+ "Thank you,<br/>" + "ngDesk Support Team<br/>support@ngdesk.com");

			} else {
				// SEND TO CREATE USER
				parseEmail.createUserAndStartWorkflow(inputMap, ticket);
			}
		} catch (

		Exception e) {

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();
			String environment = env.getProperty("env");
			if (environment.equals("prd")) {
				sendMail.send("spencer@allbluesolutions.com", "support@ngdesk.com",
						"RE: Failed to create ticket. " + "Ticket Dropped", sStackTrace);
			}
			e.printStackTrace();
			return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.trace("Enter PublicTicketService.PublicTicketService()");
		return new ResponseEntity<Object>(HttpStatus.OK);
	}

	private String decodeUTF8(byte[] bytes) {
		return new String(bytes, Charset.forName("UTF-8"));
	}
}
