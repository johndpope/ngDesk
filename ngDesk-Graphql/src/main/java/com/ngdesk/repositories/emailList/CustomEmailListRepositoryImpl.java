package com.ngdesk.repositories.emailList;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.emailList.dao.EmailList;

@Repository
public class CustomEmailListRepositoryImpl implements CustomEmailListRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<List<EmailList>> findAllEmailLists(Pageable pageable, String collectionName) {
		Query query = new Query();
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, EmailList.class, collectionName));

	}

	@Override
	public Optional<EmailList> findEmailListById(String id, String collectionName) {
		Criteria criteria = new Criteria();
		Query query = new Query(Criteria.where("_id").is(id));
		return Optional.ofNullable(mongoOperations.findOne(query, EmailList.class, collectionName));
	}

	@Override
	public Integer findEmailListCount(String collectionName) {

		return (int) mongoOperations.count(new Query(), collectionName);
	}
}
