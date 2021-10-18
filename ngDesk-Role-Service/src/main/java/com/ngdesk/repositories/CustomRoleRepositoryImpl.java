package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.role.dao.Role;

@Repository
public class CustomRoleRepositoryImpl implements CustomRoleRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Role> findRoleByName(String name, String collectionName) {
		return Optional.ofNullable(
				mongoOperations.findOne(new Query(Criteria.where("NAME").is(name)), Role.class, collectionName));
	}

}
