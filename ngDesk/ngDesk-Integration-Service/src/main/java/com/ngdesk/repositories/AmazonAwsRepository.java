package com.ngdesk.repositories;

import com.ngdesk.integration.amazom.aws.dao.AwsMessage;

public interface AmazonAwsRepository extends CustomNgdeskRepository<AwsMessage, String>, CustomAmazonAwsRepository {

}
