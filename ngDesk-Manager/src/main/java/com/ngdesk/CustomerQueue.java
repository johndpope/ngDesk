package com.ngdesk;

import java.sql.Timestamp;
import java.util.Map;

public class CustomerQueue {

	public String entryId;

	public String moduleId;

	public String queueId;

	public String companyId;

	public String customerTopic;

	public Map<String, Object> inputMessage;

	public Timestamp lastSeen;

	public CustomerQueue() {

	}

	public CustomerQueue(String entryId, String moduleId, String queueId, String companyId, String customerTopic,
			Map<String, Object> inputMessage, Timestamp lastSeen) {
		super();
		this.entryId = entryId;
		this.moduleId = moduleId;
		this.queueId = queueId;
		this.companyId = companyId;
		this.customerTopic = customerTopic;
		this.inputMessage = inputMessage;
		this.lastSeen = lastSeen;
	}

	public String getEntryId() {
		return entryId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getQueueId() {
		return queueId;
	}

	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCustomerTopic() {
		return customerTopic;
	}

	public void setCustomerTopic(String customerTopic) {
		this.customerTopic = customerTopic;
	}

	public Map<String, Object> getInputMessage() {
		return inputMessage;
	}

	public void setInputMessage(Map<String, Object> inputMessage) {
		this.inputMessage = inputMessage;
	}

	public Timestamp getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(Timestamp lastSeen) {
		this.lastSeen = lastSeen;
	}

}
