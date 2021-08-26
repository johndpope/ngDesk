package com.ngdesk.repositories.userplugin;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.userplugin.dao.UserPlugin;

@Repository
public class CustomUserPluginRepositoryImpl implements CustomUserPluginRepository {
	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<List<UserPlugin>> findAllUserPlugins(Pageable pageable, String collectionName) {
		Query query = new Query();
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, UserPlugin.class, collectionName));
	}

	@Override
	public Optional<List<UserPlugin>> findAllUserPluginsByStatus(String companyId, String status, Pageable pageable,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("status").is(status));
		Query query = new Query(criteria);
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, UserPlugin.class, collectionName));
	}

	@Override
	public Optional<List<UserPlugin>> findAllPublishedUserPlugins(Pageable pageable, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.where("status").is("Published");
		Query query = new Query(criteria);
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, UserPlugin.class, collectionName));
	}

	@Override
	public Optional<List<UserPlugin>> findAllUserPluginsByCompanyId(String companyId, Pageable pageable,
			String collectionName) {
		Query query = new Query(Criteria.where("companyId").is(companyId));
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, UserPlugin.class, collectionName));
	}

	@Override
	public Optional<UserPlugin> findUserPluginByCompanyId(String companyId, String id, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("_id").is(id));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, UserPlugin.class, collectionName));
	}

}
