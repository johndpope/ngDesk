package com.ngdesk.integration.microsoft.teams.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.Global;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.MessageAttachment;
import com.ngdesk.data.dao.PublishDiscussionMessage;
import com.ngdesk.data.dao.Sender;
import com.ngdesk.integration.amazom.aws.dao.DataProxy;
import com.ngdesk.integration.company.dao.Company;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.ModuleEntryRepository;

@Component
public class MicrosoftTeamsService {

	@Autowired
	Global global;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	DataProxy dataProxy;

	@Autowired
	CompanyRepository companyRepository;

	public String getCompanyIdBySubDomain(String subDomain) {

		Optional<Company> optionalCompany = companyRepository.getCompanyBySubdomain(subDomain);
		return optionalCompany.get().getComapnyId();
	}

	public Map<String, Object> getUserUUID(String emailAddress, String companyId) {

		Optional<Map<String, Object>> optionalUser = moduleEntryRepository.findEntryByVariable("EMAIL_ADDRESS",
				emailAddress, "Users_" + companyId);
		Map<String, Object> user = optionalUser.get();

		return user;
	}

	public void addMetaDataMessage(String status,Map<String,Object> user, String emailAddress, String name, String companyId, String subDomain,
			String moduleId, String dataId) {

		String message = global.getFile("microsoftteams_message.html");

		Pattern p = Pattern.compile("<div class='header'>(.*)?<\\/div>", Pattern.DOTALL);
		String metadataMessage = "";
		Matcher match = p.matcher(message);
		if (match.find()) {
			metadataMessage = match.group(1);
			metadataMessage = metadataMessage.replace("MODULE_NAME_REPLACE", "Ticket");
			metadataMessage = metadataMessage.replace("NEW_VALUE_REPLACE", status);
			metadataMessage = metadataMessage.replace("EMAIL_ID_REPLACE", emailAddress);
			metadataMessage = metadataMessage.replace("NAME_REPLACE", name);
		}
		DiscussionMessage discussion = buildDiscussionPayload(metadataMessage, user, companyId, moduleId, dataId);
		addToDiscussionQueue(new PublishDiscussionMessage(discussion, subDomain, user.get("_id").toString(), true));

	}

	public DiscussionMessage buildDiscussionPayload(String metadataMessage, Map<String, Object> user, String companyId,
			String moduleId, String dataId) {

		Sender sender = getSender(user, companyId);
		DiscussionMessage message = new DiscussionMessage();
		message.setAttachments(new ArrayList<MessageAttachment>());
		message.setDateCreated(new Date());
		message.setMessage(metadataMessage);
		message.setMessageId(UUID.randomUUID().toString());
		message.setMessageType("META_DATA");
		message.setSender(sender);
		message.setModuleId(moduleId);
		message.setDataId(dataId);
		message.setType(null);

		return message;

	}

	private Sender getSender(Map<String, Object> user, String companyId) {

		String contactDetails = user.get("CONTACT").toString();
		Optional<Map<String, Object>> optionalUserContact = moduleEntryRepository.findById(contactDetails,
				"Contacts_" + companyId);
		Map<String, Object> userContact = optionalUserContact.get();
		Sender sender = new Sender(userContact.get("FIRST_NAME").toString(), userContact.get("LAST_NAME").toString(),
				user.get("USER_UUID").toString(), user.get("ROLE").toString());

		return sender;
	}

	public void addToDiscussionQueue(PublishDiscussionMessage message) {

		try {
			rabbitTemplate.convertAndSend("publish-discussion", message);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
