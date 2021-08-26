package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.integration.zoom.dao.ZoomIntegrationData;

public class CustomZoomRepositoryImpl implements CustomZoomRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<ZoomIntegrationData> findZoomDataByCompany(String companyId) {
		Query query = new Query(Criteria.where("COMPANY_ID").is(companyId));

		return Optional.ofNullable(mongoOperations.findOne(query, ZoomIntegrationData.class, "zoom_integrations"));
	}

	@Override
	public void removeZoomData(String accountId, String userId) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("ZOOM_USER_INFORMATION.USER_ID").is(userId),
				Criteria.where("ZOOM_USER_INFORMATION.ACCOUNT_ID").is(accountId));
		Query query = new Query(criteria);

		mongoOperations.remove(query, "zoom_integrations");
	}

}
