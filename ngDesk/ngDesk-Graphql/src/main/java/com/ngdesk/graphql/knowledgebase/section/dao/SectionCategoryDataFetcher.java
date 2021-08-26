package com.ngdesk.graphql.knowledgebase.section.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.categories.dao.Category;
import com.ngdesk.repositories.categories.CategoryRepository;
import com.ngdesk.repositories.section.SectionRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class SectionCategoryDataFetcher implements DataFetcher<Category> {

	@Autowired
	AuthManager authManager;

	@Autowired
	SectionRepository sectionRepository;
	@Autowired
	CategoryRepository categoryRepository;
	@Autowired
	SessionManager sessionManager;

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public Category get(DataFetchingEnvironment environment) {

		Section section = new Section();
		try {
			section = mapper.readValue(mapper.writeValueAsString(environment.getSource()), Section.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String categoryId = section.getCategory();
		Optional<Category> optionalCategoryId = categoryRepository.findById(categoryId,
				"categories_" + authManager.getUserDetails().getCompanyId());
		if (optionalCategoryId.isEmpty()) {
			return null;
		}
		return optionalCategoryId.get();

	}
}