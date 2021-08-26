package com.ngdesk.graphql.categories.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.company.dao.Company;
import com.ngdesk.repositories.categories.CategoryRepository;
import com.ngdesk.repositories.company.CompanyRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class CategoryNoAuthDataFetcher implements DataFetcher<Category> {

	@Autowired
	AuthManager authManager;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	SessionManager sessionManager;

	@Override
	public Category get(DataFetchingEnvironment environment) {
		String companyId = (String) sessionManager.getSessionInfo().get("companyId");

		String categoryId = environment.getArgument("categoryId");

		Optional<Category> optionalCategoryId = categoryRepository.findById(categoryId, "categories_" + companyId);
		if (optionalCategoryId.isEmpty()) {
			return null;
		}
		return optionalCategoryId.get();

	}
}
