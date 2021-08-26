package com.ngdesk.knowledgebase.categories.dao;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.CategoryRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Component
@RestController
public class CategoryApi {

	@Autowired
	AuthManager authManager;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	CategoryService categoryService;

	@PostMapping("/category")
	@Operation(summary = "Post Category", description = "Post a single Category")
	public Category postCategory(@Valid @RequestBody Category category) {
		categoryService.setdefalutValuesForCategoryPost(category);
		return categoryRepository.save(category, "categories_" + authManager.getUserDetails().getCompanyId());
	}

	@PutMapping("/category")
	@Operation(summary = "Put Category", description = "Update a Category")
	public Category putCategory(@Valid @RequestBody Category category) {
		categoryService.setdefalutValuesForCategoryPut(category);
		return categoryRepository.save(category, "categories_" + authManager.getUserDetails().getCompanyId());

	}

	// TODO: On delete category, delete sections and articles related to that
	// category
	@DeleteMapping("/category/{id}")
	@Operation(summary = "Delete Category", description = "Delete a Category by ID")
	public void deleteCategory(@Parameter(description = "Category ID", required = true) @PathVariable("id") String id) {
		Optional<Category> optional = categoryRepository.findById(id,
				"categories_" + authManager.getUserDetails().getCompanyId());

		if (optional.isEmpty()) {
			String vars[] = { "CATEGORY" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}

		categoryRepository.deleteById(id, "categories_" + authManager.getUserDetails().getCompanyId());
	}
}
