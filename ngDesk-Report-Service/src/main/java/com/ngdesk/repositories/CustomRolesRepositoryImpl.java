package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.report.role.Role;

@Repository
public class CustomRolesRepositoryImpl implements CustomRolesRepository {
	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Role> findRoleName(String name, String collectionName) {
		return Optional.ofNullable(
				mongoOperations.findOne(new Query(Criteria.where("NAME").is(name)), Role.class, collectionName));
	}

	@Override
	public Optional<List<Role>> findAllRoles(String collectionName) {
		return Optional.ofNullable(mongoOperations.find(new Query(), Role.class, collectionName));
	}
}
