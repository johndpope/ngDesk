package com.ngdesk.websocket.channels.chat.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.Sender;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.modules.dao.Module;

@Component
public class ChatTicketCreationService {

	@Autowired
	CompaniesRepository companiesRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	DataProxy dataProxy;

	@Autowired
	ChatUserEntryService chatUserEntryService;

	@Autowired
	RedisTemplate<String, ChatTicketStatusMessage> redisTemplate;

	public void chatTicketCreation(ChatTicket chatTicket) {

		String companyId = null;
		try {
			Optional<Company> optionalCompany = companiesRepository
					.findCompanyBySubdomain(chatTicket.getCompanySubdomain());
			if (optionalCompany.isPresent()) {
				Company company = optionalCompany.get();
				companyId = company.getId();
				Optional<Module> optionalTicketModule = modulesRepository.findModuleByName("Tickets",
						"modules_" + companyId);
				if (optionalTicketModule.isPresent()) {
					ChatUser chatUser = new ChatUser(chatTicket.getSender().getFirstName(),
							chatTicket.getSender().getLastName(), chatTicket.getSender().getEmailAddress(),
							chatTicket.getSessionUUId(), "UserCreation", chatTicket.getCompanySubdomain());
					Map<String, Object> user = chatUserEntryService.createOrGetUser(company, chatUser);
					String contactId = user.get("CONTACT").toString();
					Optional<Map<String, Object>> optionalContact = moduleEntryRepository.findById(contactId,
							"Contacts_" + companyId);
					Map<String, Object> contact = optionalContact.get();
					Sender sender = new Sender(contact.get("FIRST_NAME").toString(),
							contact.get("LAST_NAME").toString(), user.get("USER_UUID").toString(),
							user.get("ROLE").toString());
					List<DiscussionMessage> messages = new ArrayList<DiscussionMessage>();
					DiscussionMessage discussionMessage = new DiscussionMessage(chatTicket.getMessage(), new Date(),
							UUID.randomUUID().toString(), chatTicket.getMessageType(), chatTicket.getAttachments(),
							sender, null, null, null);
					messages.add(discussionMessage);
					HashMap<String, Object> entry = new HashMap<String, Object>();
					entry.put("MESSAGES", messages);
					entry.put("STATUS", "New");
					entry.put("REQUESTOR", contact.get("_id").toString());
					entry.put("SUBJECT", chatTicket.getSubject());
					ChatTicketStatusMessage chatTicketStatusMessage = new ChatTicketStatusMessage(companyId,
							chatTicket.getSessionUUId(), "CHAT_TICKET_STATUS", "SUCCESS",
							"Ticket has been created Succuessfully, Agent will reach back to you");
					addToChatTicketStatusQueue(chatTicketStatusMessage);
					dataProxy.postModuleEntry(entry, optionalTicketModule.get().getModuleId(), false, companyId,
							user.get("USER_UUID").toString());

				}
			}
		} catch (Exception e) {

			ChatTicketStatusMessage chatTicketStatusMessage = new ChatTicketStatusMessage(companyId,
					chatTicket.getSessionUUId(), "CHAT_TICKET_STATUS", "FAILED",
					"Ticket has not been created, Please try again");
			addToChatTicketStatusQueue(chatTicketStatusMessage);

		}
	}

	public void addToChatTicketStatusQueue(ChatTicketStatusMessage chatTicketStatusMessage) {
		redisTemplate.convertAndSend("chat_ticket_status", chatTicketStatusMessage);
	}

}