package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;

import com.ngdesk.company.role.dao.Role;

public class CustomRoleRepositoryImpl implements CustomRoleRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<List<Role>> findAllRolesByCollectionName(String collectionName) {
		return Optional.ofNullable(mongoOperations.findAll(Role.class, collectionName));
	}

}
