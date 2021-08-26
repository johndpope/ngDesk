package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.sam.swidtag.Swidtag;

@Repository
public class CustomSwidtagRepositoryImpl implements CustomSwidtagRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Swidtag> findExistingSwidtag(String fileName, String companyId, String assetId,
			String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(criteria.where("COMPANY_ID").is(companyId), criteria.where("ASSET_ID").is(assetId),
				criteria.where("FILE_NAME").is(fileName));
		Query query = new Query(criteria);

		return Optional.ofNullable(mongoOperations.findOne(query, Swidtag.class, "swidtag_files"));

	}

}
