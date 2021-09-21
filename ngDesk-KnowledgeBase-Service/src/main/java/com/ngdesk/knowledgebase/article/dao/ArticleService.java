package com.ngdesk.knowledgebase.article.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.ArticleRepository;
import com.ngdesk.repositories.ModuleEntryRepository;

@Component
public class ArticleService {

	@Autowired
	AuthManager authManager;

	@Autowired
	ArticleRepository articleRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	Global global;

	@Value("${elastic.host}")
	private String elasticHost;

	// set the article comments to contain the old ones
	public Article SetCommentsPut(Article article, Article oldArticle) {
		article = SetCommentsUUID(article);
		if (oldArticle.getComments() != null && !oldArticle.getComments().isEmpty()) {
			List<CommentMessage> allComment = oldArticle.getComments();
			if (article.getComments() != null) {
				allComment.addAll(article.getComments());

			}
			article.setComments(allComment);
		}
		return article;

	}

	public Article SetCommentsUUID(Article article) {
		if (article.getComments() != null && !article.getComments().isEmpty()) {
			for (int i = 0; i < article.getComments().size(); i++) {
				if (article.getComments().get(i).getMessageId() == null
						|| article.getComments().get(i).getMessageId().equals(""))
					article.getComments().get(i).setMessageId(UUID.randomUUID().toString());
			}
		}
		return article;
	}

	public Article setDefaultValuesForUpdate(Article article, Article oldArticle) {
		article.setCreatedBy(oldArticle.getCreatedBy());
		article.setDateCreated(oldArticle.getDateCreated());
		article.setDateUpdated(new Date());
		article.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		return article;
	}

	public Article setDefaultValues(Article article) {
		int totalCount = articleRepository.getCount("articles_" + authManager.getUserDetails().getCompanyId());

		article.setOrder(totalCount + 1);
		article.setCreatedBy(authManager.getUserDetails().getUserId());
		article.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		article.setDateCreated(new Date());
		article.setDateUpdated(new Date());
		return article;
	}

	public Article setAttachments(Article article) {
		List<Attachment> attachmentList = new ArrayList<Attachment>();

		for (Attachment attachment : article.getAttachments()) {
			if (attachment.getFile() == null) {
				throw new NotFoundException("ATTACHMENT_FILE_REQUIRED", null);
			}
		}

		for (Attachment attachment : article.getAttachments()) {
			String hash = global.passwordHash(attachment.getFile());
			Optional<Attachment> optionalHash = articleRepository.findHashById(hash,
					"attachments_" + authManager.getUserDetails().getCompanyId());
			Attachment newAttachment = new Attachment();
			if (optionalHash.isEmpty() == true) {

				newAttachment.setAttachmentUuid(UUID.randomUUID().toString());
				newAttachment.setHash(hash);
				articleRepository.saveAttachment(newAttachment,
						"attachments_" + authManager.getUserDetails().getCompanyId());
				attachmentList.add(newAttachment);
				newAttachment.setFile(null);
			} else {
				newAttachment.setHash(optionalHash.get().getHash());
				newAttachment.setAttachmentUuid(optionalHash.get().getAttachmentUuid());
				attachmentList.add(newAttachment);
			}

			article.setAttachments(attachmentList);
		}
		return article;
	}

	public void postArticleToElastic(Article article, String companyId) {
		RestHighLevelClient elasticClient = null;
		try {
			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));
			Map<String, Object> entryMap = new HashMap<String, Object>();
			if (!article.getVisibleTo().isEmpty()) {
				entryMap.put("VISIBLE_TO", article.getVisibleTo());
			}
			if (!article.getSection().isEmpty()) {
				entryMap.put("SECTION", article.getSection());
			}
			Map<String, Object> articleBody = new ObjectMapper()
					.readValue(new ObjectMapper().writeValueAsString(article), Map.class);
			for (String key : articleBody.keySet()) {
				if (key.equals("title") || key.equals("body")) {
					entryMap.put("FIELD_NAME", key);
					if (key.equals("body")) {
						entryMap.put("INPUT", Jsoup.parse((String) articleBody.get(key)).text());
					} else {
						entryMap.put("INPUT", articleBody.get(key));
					}
					entryMap.put("COMPANY_ID", companyId);
					entryMap.put("ARTICLE_ID", articleBody.get("articleId"));
					IndexRequest titleIndexRequest = new IndexRequest("articles").source(entryMap, XContentType.JSON);
					elasticClient.index(titleIndexRequest, RequestOptions.DEFAULT);
				} else if (key.equalsIgnoreCase("comments")) {
					String commentConverted = "";
					List<CommentMessage> allComment = article.getComments();
					for (int i = 0; i < allComment.size(); i++) {
						CommentMessage message = allComment.get(i);
						commentConverted += Jsoup.parse(message.getMessage()).text();
					}
					entryMap.put("INPUT", commentConverted);
					entryMap.put("FIELD_NAME", key);
					entryMap.put("COMPANY_ID", companyId);
					entryMap.put("ARTICLE_ID", article.getArticleId());
					IndexRequest indexRequest = new IndexRequest("articles").source(entryMap, XContentType.JSON);
					elasticClient.index(indexRequest, RequestOptions.DEFAULT);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				elasticClient.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public void putArticleToElastic(Article article, String companyId) {
		RestHighLevelClient elasticClient = null;
		try {
			String articleId = article.getArticleId();
			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			DeleteByQueryRequest request = new DeleteByQueryRequest("articles");

			BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("ARTICLE_ID", articleId));
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));
			request.setQuery(boolQueryBuilder);
			elasticClient.deleteByQuery(request, RequestOptions.DEFAULT);
			postArticleToElastic(article, companyId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void postCommentsToElastic(Article article, String companyId) {
		RestHighLevelClient elasticClient = null;
		try {
			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));
			DeleteByQueryRequest request = new DeleteByQueryRequest("articles");
			String articleId = article.getArticleId();
			BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("ARTICLE_ID", articleId));
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("FIELD_NAME", "COMMENTS"));
			request.setQuery(boolQueryBuilder);
			elasticClient.deleteByQuery(request, RequestOptions.DEFAULT);

			Map<String, Object> entryMap = new HashMap<String, Object>();

			if (!article.getVisibleTo().isEmpty()) {
				entryMap.put("VISIBLE_TO", article.getVisibleTo());
			}
			if (!article.getSection().isEmpty()) {
				entryMap.put("SECTION", article.getSection());
			}
			String commentConverted = "";
			List<CommentMessage> allComment = article.getComments();
			for (int i = 0; i < allComment.size(); i++) {
				CommentMessage message = allComment.get(i);
				commentConverted += Jsoup.parse(message.getMessage()).text();
				commentConverted += " ";
			}

			entryMap.put("INPUT", commentConverted);
			entryMap.put("FIELD_NAME", "COMMENTS");
			entryMap.put("COMPANY_ID", companyId);
			entryMap.put("ARTICLE_ID", articleId);

			IndexRequest indexRequest = new IndexRequest("articles").source(entryMap, XContentType.JSON);
			elasticClient.index(indexRequest, RequestOptions.DEFAULT);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void deleteArticleFromElastic(String articleId, String companyId) {
		RestHighLevelClient elasticClient = null;
		try {
			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			DeleteByQueryRequest request = new DeleteByQueryRequest("articles");

			BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("ARTICLE_ID", articleId));
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));
			request.setQuery(boolQueryBuilder);
			elasticClient.deleteByQuery(request, RequestOptions.DEFAULT);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
