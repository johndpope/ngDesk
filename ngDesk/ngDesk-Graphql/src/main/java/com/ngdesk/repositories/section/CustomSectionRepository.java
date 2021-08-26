package com.ngdesk.repositories.section;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.knowledgebase.section.dao.Section;

public interface CustomSectionRepository {

	public Integer count(String categoryId, String collectionName);

	public Optional<Section> findByIdWithTeam(String sectionId, List<String> teams, String collectionName);

	public Optional<Section> findByIdWithPublicTeam(String sectionId,String team, String collectionName);
	public Optional<List<Section>> findAllWithCategoryId(String categoryId, Pageable pageable, String collectionName);

	public Optional<List<Section>> findAllWithCategoryIdAndTeam(String categoryId, List<String> teams,
			Pageable pageable, String collectionName);

	public int sectionsCountByVisibleTo(String categoryId, List<String> teamIds, String collectionName);

	public Optional<List<Section>> findSectionsByPublicTeamId(String categoryId,String teamId, Pageable pageable, String collectionName);

	public int sectionsCountByPublicTeamId(String categoryId,String teamId, String collectionName);

}
