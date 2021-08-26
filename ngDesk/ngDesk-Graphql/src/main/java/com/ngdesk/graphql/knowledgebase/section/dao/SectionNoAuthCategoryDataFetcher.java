package com.ngdesk.graphql.knowledgebase.section.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.categories.dao.Category;
import com.ngdesk.repositories.categories.CategoryRepository;
import com.ngdesk.repositories.company.CompanyRepository;
import com.ngdesk.repositories.section.SectionRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class SectionNoAuthCategoryDataFetcher implements DataFetcher<Category> {

	@Autowired
	SectionRepository sectionRepository;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	CategoryRepository categoryRepository;

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public Category get(DataFetchingEnvironment environment) {
		String companyId = (String) sessionManager.getSessionInfo().get("companyId");

		Section section = new Section();
		try {
			section = mapper.readValue(mapper.writeValueAsString(environment.getSource()), Section.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String categoryId = section.getCategory().toString();

		Optional<Category> optionalCategory = categoryRepository.findById(categoryId, "categories_" + companyId);
		if (optionalCategory.isEmpty()) {
			return null;
		}
		return optionalCategory.get();

	}

}
