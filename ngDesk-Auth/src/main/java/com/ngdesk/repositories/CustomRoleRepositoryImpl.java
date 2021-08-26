package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.auth.company.dao.Role;

@Repository
public class CustomRoleRepositoryImpl implements CustomRoleRepository {
	
	@Autowired
	MongoOperations mongoOperations;
	
	@Override
	public Optional<Role> findRoleByName(String roleName, String collectionName) {
		Assert.notNull(roleName, "The given role name must not be null!");
		Query query = new Query(Criteria.where("NAME").is(roleName));
		return Optional.ofNullable(mongoOperations.findOne(query, Role.class, collectionName));
	}
	
}
