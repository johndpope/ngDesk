package com.ngdesk.repositories.categories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.categories.dao.Category;

@Repository
public interface CustomCategoryRepository {

	public Optional<List<Category>> findCategoriesByPublicTeamId(String teamId, Pageable pageable,
			String collectionName);

	public Optional<List<Category>> findAllCategories(Pageable pageable, String collectionName);

	public int categoriesCount(String collectionName);

	public Optional<List<Category>> findCategoriesByVisibleTo(List<String> teamIds, Pageable pageable,
			String collectionName);

	public int categoriesCountByVisibleTo(List<String> teamIds, String collectionName);

	public int categoriesCountByPublicTeamId(String teamId, String collectionName);

}
