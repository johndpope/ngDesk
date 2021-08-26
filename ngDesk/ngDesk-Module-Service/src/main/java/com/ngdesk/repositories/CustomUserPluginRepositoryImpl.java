package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

import com.ngdesk.module.userplugins.dao.UserPlugin;

public class CustomUserPluginRepositoryImpl implements CustomUserPluginRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<UserPlugin> findPluginByName(String name, String collectionName) {
		Assert.notNull(name, "The given plugin name must not be null!");
		return Optional.ofNullable(
				mongoOperations.findOne(new Query(Criteria.where("name").is(name)), UserPlugin.class, collectionName));

	}

	@Override
	public Optional<UserPlugin> findOtherPluginsWithDuplicateName(String name, String pluginId,
			String collectionName) {
		// TODO Auto-generated method stub
		Assert.notNull(name, "User Plugin Name must not be null");
		Assert.notNull(collectionName, "Collection Name must not be null");
		Query query = new Query();
		query.addCriteria(
				new Criteria().andOperator(Criteria.where("_id").ne(pluginId), Criteria.where("name").is(name)));
		return Optional.ofNullable(mongoOperations.findOne(query, UserPlugin.class, collectionName));
	}

}
