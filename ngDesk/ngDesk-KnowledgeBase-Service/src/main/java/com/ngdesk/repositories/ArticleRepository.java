package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.knowledgebase.article.dao.Article;

@Repository
public interface ArticleRepository extends CustomNgdeskRepository<Article, String>, CustomArticleRepository {

}
