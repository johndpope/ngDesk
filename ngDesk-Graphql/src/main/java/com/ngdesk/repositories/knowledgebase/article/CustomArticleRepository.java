package com.ngdesk.repositories.knowledgebase.article;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.knowledgebase.article.dao.Article;

public interface CustomArticleRepository {

	public List<Article> findAllWithPageableAndTeam(List<String> teams, Pageable pageable, String collectionName);

	public List<Article> findAllWithPageable(Pageable pageable, String collectionName);

	public Optional<Article> findByIdWithTeam(String articleId, List<String> teams, String collectionName);

	public Optional<Article> findArticleByPublicTeamId(String articleId, String publicTeamId, String collectionName);

	public List<Article> findAllArticlesByTeam(String publicTeamId, Pageable pageable, String collectionName);

	public int findArticleCountByPublicTeamId(String teamId, String collectionName);

	public int articlesCount(String collectionName);

	public int articlesCountByVisibleTo(List<String> teams, String collectionName);

	public List<Article> findAllWithPageableAndSearch(Pageable pageable, List<ObjectId> ids, String collectionName);

	public List<Article> findAllArticlesWithSearch(Pageable pageable, List<ObjectId> ids, String collectionName);

	public int articlesCountBySearch(List<ObjectId> ids, String collectionName);

	public int findArticleCountBySearch(List<ObjectId> ids, String collectionName);

}
