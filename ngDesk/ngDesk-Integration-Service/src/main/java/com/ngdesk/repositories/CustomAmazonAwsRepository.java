package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ngdesk.integration.amazom.aws.dao.AwsMessage;

public interface CustomAmazonAwsRepository {

	public AwsMessage saveMessage(AwsMessage message, String collectionName);

	public Optional<Map<String, Object>> findByAlarmNameAndAccountId(String alarmName, String awsAccountId, String companyId,
			String collectionName);

	public Optional<List<Map<String, Object>>> findAllAwsEntry(String alarmName, String awsAccountId, String companyId,
			String collectionName);

	public Optional<Map<String, Object>> updateAwsEntry(Map<String, Object> entry, String collectionName);

}
