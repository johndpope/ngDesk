package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.company.plugin.dao.Plugin;

@Repository
public class CustomPluginRepositoryImpl implements CustomPluginRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Plugin> findPluginByName(String name) {
		Assert.notNull(name, "The given plugin name must not be null!");
		return Optional.ofNullable(
				mongoOperations.findOne(new Query(Criteria.where("NAME").is(name)), Plugin.class, "plugins"));
	}

	@Override
	public Optional<List<Plugin>> findAllPlugins() {
		return Optional.ofNullable(mongoOperations.find(new Query(), Plugin.class, "plugins"));
	}

}
