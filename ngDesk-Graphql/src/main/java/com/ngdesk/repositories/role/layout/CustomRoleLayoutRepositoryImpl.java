package com.ngdesk.repositories.role.layout;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.role.dao.Role;
import com.ngdesk.graphql.role.layout.dao.RoleLayout;

@Repository
public class CustomRoleLayoutRepositoryImpl implements CustomRoleLayoutRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<List<RoleLayout>> findAllRoleLayoutsInCompany(Pageable pageable, String companyId,
			String collectionName) {
		Query query = new Query(Criteria.where("companyId").is(companyId));
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, RoleLayout.class, collectionName));
	}

	@Override
	public List<RoleLayout> findAllLayoutsByRoleIdAndCompanyId(Pageable pageable, String roleId, String companyId,
			String collection) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("role").is(roleId), Criteria.where("companyId").is(companyId));
		return mongoOperations.find(new Query(criteria).with(pageable), RoleLayout.class, collection);
	}

	@Override
	public Optional<RoleLayout> findByCompanyIdAndId(String companyId, String id, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("_id").is(id));
		Query query = new Query(criteria);
		Optional<RoleLayout> optional = Optional
				.ofNullable(mongoOperations.findOne(query, RoleLayout.class, collectionName));
		return optional;
	}
	
	@Override
	public Optional<RoleLayout> findLayoutByRoleIdAndCompanyId(String companyId, String roleId, String id, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("companyId").is(companyId), Criteria.where("_id").is(id),Criteria.where("role").is(roleId));
		Query query = new Query(criteria);
		Optional<RoleLayout> optional = Optional
				.ofNullable(mongoOperations.findOne(query, RoleLayout.class, collectionName));
		return optional;
	}
	
	@Override
	public Optional<Role> findRoleName(String roleId, String collectionName) {
		return Optional.ofNullable(mongoOperations.findOne(new Query(Criteria.where("_id").is(roleId)),
				Role.class, collectionName));
	}

	@Override
	public int roleLayoutsCount(String companyId, String collectionName) {
		Query query = new Query(Criteria.where("companyId").is(companyId));
		return (int) mongoOperations.count(query, collectionName);
	}
}