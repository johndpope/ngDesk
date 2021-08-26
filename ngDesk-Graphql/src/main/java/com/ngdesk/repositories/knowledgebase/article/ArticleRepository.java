package com.ngdesk.repositories.knowledgebase.article;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.knowledgebase.article.dao.Article;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface ArticleRepository extends CustomNgdeskRepository<Article, String>, CustomArticleRepository {

}
