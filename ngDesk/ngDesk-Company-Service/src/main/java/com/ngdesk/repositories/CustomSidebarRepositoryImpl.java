package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

import com.ngdesk.company.sidebar.dao.CustomSidebar;
import com.ngdesk.company.sidebar.dao.MenuItem;

public class CustomSidebarRepositoryImpl implements CustomSidebarRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<CustomSidebar> findDefaultCustomSidebarTemplate() {

		Query query = new Query();
		query.fields().exclude("_id");

		return Optional.ofNullable(mongoOperations.findOne(query, CustomSidebar.class, "sidebar_templates"));
	}

	@Override
	public Optional<CustomSidebar> findCustomSidebarByCompanyId(String companyId, String collectionName) {

		Assert.notNull(companyId, "Company id must not be null");
		Assert.notNull(collectionName, "Collection name must not be null");

		Query query = new Query(Criteria.where("COMPANY_ID").is(companyId));

		return Optional.ofNullable(mongoOperations.findOne(query, CustomSidebar.class, "companies_sidebar"));
	}

	@Override
	public Optional<List<MenuItem>> findAllMenuItemsByPluginAndRole(String role, List<String> plugins) {

		Assert.notNull(role, "Role must not be null");
		Assert.notNull(plugins, "Plugins must not be null");

		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("ROLES").in(role), Criteria.where("PLUGIN").in(plugins));
		query.addCriteria(criteria);
		query.fields().exclude("_id");
		query.fields().exclude("PLUGIN");
		query.fields().exclude("ROLES");
		return Optional.ofNullable(mongoOperations.find(query, MenuItem.class, "sidebar_menu_templates"));
	}

}
