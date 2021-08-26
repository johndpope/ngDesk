package com.ngdesk.module.slas.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.data.dao.SingleWorkflowPayload;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.field.dao.DataType;
import com.ngdesk.module.field.dao.ModuleField;
import com.ngdesk.module.field.dao.Sender;
import com.ngdesk.repositories.ModuleEntryRepository;

@Component
public class DataService {

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	ModuleEntryRepository entryRepository;

	public void addToWorkflowQueue(SingleWorkflowPayload singleWorkflowPayload) {
		try {
			rabbitTemplate.convertAndSend("execute-single-workflow", singleWorkflowPayload);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DiscussionMessage buildMetaDataPayload(String message, SLAInstance instance) {

		Optional<Map<String, Object>> optionalUser = entryRepository.findEntryByName("EMAIL_ADDRESS",
				"system@ngdesk.com", "Users_" + instance.getCompanyId());
		Map<String, Object> systemUser = optionalUser.get();
		String contactId = systemUser.get("CONTACT").toString();
		Optional<Map<String, Object>> optionalContact = entryRepository.findById(contactId,
				"Contacts_" + instance.getCompanyId());
		Map<String, Object> contact = optionalContact.get();
		Sender sender = new Sender(contact.get("FIRST_NAME").toString(), contact.get("LAST_NAME").toString(),
				systemUser.get("USER_UUID").toString(), systemUser.get("ROLE").toString());
		return new DiscussionMessage(message, new Date(), UUID.randomUUID().toString(), "META_DATA",
				new ArrayList<MessageAttachment>(), sender);

	}

	public void addMetaData(DiscussionMessage discussion, String dataId, String moduleName, String companyId) {
		Optional<Map<String, Object>> optionalExistingEntry = entryRepository.findById(dataId,
				moduleName + "_" + companyId);
		if (!optionalExistingEntry.isEmpty()) {
			entryRepository.updateMetadataEvents(dataId, discussion, moduleName + "_" + companyId);
		}

	}

	public ModuleField getDiscussionField(List<ModuleField> fields) {

		for (ModuleField field : fields) {
			DataType dataType = field.getDataType();
			if (dataType.getDisplay().toString().equalsIgnoreCase("Discussion")) {
				return field;
			}
		}
		return null;
	}

	public String getSlaFieldName(String fieldName) {
		fieldName = fieldName.toUpperCase();
		fieldName = fieldName.trim();
		fieldName = fieldName.replaceAll("\\s+", "_");
		return fieldName;
	}

}
