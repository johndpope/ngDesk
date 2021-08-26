package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.ngdesk.role.layout.dao.RoleLayout;

@Repository
public class CustomRoleLayoutRepositoryImpl implements CustomRoleLayoutRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<RoleLayout> findDuplicateRoleLayoutName(String name, String roleId, String companyId,
			String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("name").is(name), Criteria.where("role").is(roleId),
				Criteria.where("companyId").is(companyId));

		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), RoleLayout.class, collectionName));
	}

	@Override
	public Optional<RoleLayout> findOtherRoleLayoutWithDuplicateName(String name, String roleId, String companyId,
			String roleLayoutId, String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("name").is(name), Criteria.where("_id").ne(roleLayoutId),
				Criteria.where("role").is(roleId), Criteria.where("companyId").is(companyId));

		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), RoleLayout.class, collectionName));
	}

	@Override
	public Optional<RoleLayout> findDuplicateRoleName(String name, String roleLayoutId, String collection) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").not().is(roleLayoutId), Criteria.where("name").is(name));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), RoleLayout.class, collection));
	}

	@Override
	public Page<RoleLayout> findAllLayoutsByRoleIdAndCompanyId(Pageable pageable, String roleId, String companyId,
			String collection) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("role").is(roleId), Criteria.where("companyId").is(companyId));

		long count = mongoOperations.count(new Query(criteria), collection);

		List<RoleLayout> roleLayouts = mongoOperations.find(new Query(criteria).with(pageable), RoleLayout.class,
				collection);
		return new PageImpl<RoleLayout>(roleLayouts, pageable, count);
	}

	@Override
	public Optional<List<RoleLayout>> findAllRoleLayoutsInCompany(Pageable pageable, String companyId,
			String collectionName) {
		Query query = new Query(Criteria.where("companyId").is(companyId));
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, RoleLayout.class, collectionName));
	}

	@Override
	public void updateRoleLayout(RoleLayout roleLayout, String collectionName) {
		Update update = new Update();
		update.set("defaultLayout", false);
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(roleLayout.getLayoutId()),
				Criteria.where("companyId").is(roleLayout.getCompanyId()));
		Query query = new Query(criteria);
		mongoOperations.updateFirst(query, update, collectionName);
	}

}
