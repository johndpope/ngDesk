package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.company.rolelayout.dao.RoleLayout;

public class CustomRoleLayoutRepositoryImpl implements CustomRoleLayoutRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<List<RoleLayout>> findDefaultRoleLayoutTemplate(String collectionName) {

		Query query = new Query();
		query.fields().exclude("_id");
		return Optional.ofNullable(mongoOperations.find(query, RoleLayout.class, "role_layout_templates"));
	}

}
