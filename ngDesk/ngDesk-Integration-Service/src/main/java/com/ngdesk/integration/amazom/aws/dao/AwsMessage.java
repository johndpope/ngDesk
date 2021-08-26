package com.ngdesk.integration.amazom.aws.dao;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AwsMessage {
	@JsonProperty("Type")
	public String type;

	@JsonProperty("MessageId")
	public String messageId;

	@JsonProperty("Token")
	public String token;

	@JsonProperty("TopicArn")
	public String topicArn;

	@JsonProperty("Message")
	public String message;

	@JsonProperty("SubscribeURL")
	public String subscribeURL;

	@JsonProperty("Timestamp")
	public Date timestamp;

	@JsonProperty("SignatureVersion")
	public String signatureVersion;

	@JsonProperty("Signature")
	public String signature;

	@JsonProperty("SigningCertURL")
	public String signingCertURL;

	@JsonProperty("Subject")
	public String subject;

	@JsonProperty("UnsubscribeURL")
	public String unsubscribeURL;

	@JsonProperty("TicketId")
	public String ticketId;

	@JsonProperty("CompanyId")
	public String companyId;

	@JsonProperty("AlarmName")
	public String alarmName;

	@JsonProperty("AWSAccountId")
	public String awsAccountId;

	public AwsMessage() {
		super();
	}

	public AwsMessage(String type, String messageId, String token, String topicArn, String message, String subscribeURL,
			Date timestamp, String signatureVersion, String signature, String signingCertURL, String subject,
			String unsubscribeURL, String ticketId, String companyId, String alarmName, String awsAccountId) {
		super();
		this.type = type;
		this.messageId = messageId;
		this.token = token;
		this.topicArn = topicArn;
		this.message = message;
		this.subscribeURL = subscribeURL;
		this.timestamp = timestamp;
		this.signatureVersion = signatureVersion;
		this.signature = signature;
		this.signingCertURL = signingCertURL;
		this.subject = subject;
		this.unsubscribeURL = unsubscribeURL;
		this.ticketId = ticketId;
		this.companyId = companyId;
		this.alarmName = alarmName;
		this.awsAccountId = awsAccountId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTopicArn() {
		return topicArn;
	}

	public void setTopicArn(String topicArn) {
		this.topicArn = topicArn;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSubscribeURL() {
		return subscribeURL;
	}

	public void setSubscribeURL(String subscribeURL) {
		this.subscribeURL = subscribeURL;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getSignatureVersion() {
		return signatureVersion;
	}

	public void setSignatureVersion(String signatureVersion) {
		this.signatureVersion = signatureVersion;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getSigningCertURL() {
		return signingCertURL;
	}

	public void setSigningCertURL(String signingCertURL) {
		this.signingCertURL = signingCertURL;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getUnsubscribeURL() {
		return unsubscribeURL;
	}

	public void setUnsubscribeURL(String unsubscribeURL) {
		this.unsubscribeURL = unsubscribeURL;
	}

	public String getTicketId() {
		return ticketId;
	}

	public void setTicketId(String ticketId) {
		this.ticketId = ticketId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getAlarmName() {
		return alarmName;
	}

	public void setAlarmName(String alarmName) {
		this.alarmName = alarmName;
	}

	public String getAwsAccountId() {
		return awsAccountId;
	}

	public void setAwsAccountId(String awsAccountId) {
		this.awsAccountId = awsAccountId;
	}

}
