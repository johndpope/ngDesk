package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;

import com.ngdesk.module.role.dao.Permission;
import com.ngdesk.module.role.dao.Role;

public class CustomRoleRepositoryImpl implements CustomRoleRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<List<Role>> findAllRoleTemplates(String collectionName) {
		Assert.notNull(collectionName, "Collection name must not be null");

		Query query = new Query();
		return Optional.ofNullable(mongoOperations.find(query, Role.class, collectionName));
	}

	@Override
	public void updatePermissions(Permission permission, String collectionName) {
		Assert.notNull(permission, "Permission must not be null");

		Update update = new Update();

		update.addToSet("PERMISSIONS", permission);

		Query query = new Query();
		query.addCriteria(Criteria.where("NAME").ne("SystemAdmin"));

		mongoOperations.updateMulti(query, update, Role.class, collectionName);
	}

	@Override
	public Optional<List<Role>> findAllRolesByName(List<String> names, String collectionName) {
		Query query = new Query(Criteria.where("NAME").in(names));
		return Optional.ofNullable(mongoOperations.find(query, Role.class, collectionName));

	}

}
