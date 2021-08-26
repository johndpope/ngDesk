package com.ngdesk.knowledgebase.article.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthProxy;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.knowledgebase.NgDeskKnowledgeBaseServiceApplicationTests;

import brave.Tracer;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ArticleAPI.class)
@ContextConfiguration(classes = { NgDeskKnowledgeBaseServiceApplicationTests.class })
public class ArticleAPITest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ArticleAPI articleAPI;

	@MockBean
	AuthProxy authProxy;

	@MockBean
	SessionManager sessionManager;

	@MockBean
	Tracer tracer;

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testArticlePostSuccess() throws Exception {
		String[] visibleTo = { "60fb0dd0212e013fe36425ce" };
		List<String> labels = new ArrayList<String>();
		Article article = new Article(null, "test of API", "This article is to test the api",
				"60fb0dd0212e013fe36425d6", Arrays.asList(visibleTo), false, "en", labels, 0,
				"60fb0dd0212e013fe36425d9", null, null, null, null, false, null, null);

		// Prepare STUB
		given(articleAPI.postArticle(any(Article.class))).willReturn(article);

		// Perform MockTest
		String articleString = mockMvc
				.perform(post("/article").content(mapper.writeValueAsString(article)).contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		Article response = mapper.readValue(articleString, Article.class);
	}

	@Test
	public void testArticlePostTitleNotEmpty() throws Exception {
		String[] visibleTo = { "60fb0dd0212e013fe36425ce" };
		List<String> labels = new ArrayList<String>();
		Article article = new Article(null, "", "This article is to test the api", "60fb0dd0212e013fe36425d6",
				Arrays.asList(visibleTo), false, "en", labels, 0, "60fb0dd0212e013fe36425d9", null, null, null, null,
				false, null, null);

		// Prepare STUB
		given(articleAPI.postArticle(any(Article.class))).willReturn(article);

		// Perform MockTest
		mockMvc.perform(post("/article").content(mapper.writeValueAsString(article)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());

	}

	@Test
	public void testArticlePostBodyNotEmpty() throws Exception {
		String[] visibleTo = { "60fb0dd0212e013fe36425ce" };
		List<String> labels = new ArrayList<String>();
		Article article = new Article(null, "test of API", "", "60fb0dd0212e013fe36425d6", Arrays.asList(visibleTo),
				false, "en", labels, 0, "60fb0dd0212e013fe36425d9", null, null, null, null, false, null, null);

		// Prepare STUB
		given(articleAPI.postArticle(any(Article.class))).willReturn(article);

		// Perform MockTest
		mockMvc.perform(post("/article").content(mapper.writeValueAsString(article)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testArticlePostAuthorNotEmpty() throws Exception {
		String[] visibleTo = { "60fb0dd0212e013fe36425ce" };
		List<String> labels = new ArrayList<String>();
		Article article = new Article(null, "test of API", "This article is to test the api", "",
				Arrays.asList(visibleTo), false, "en", labels, 0, "60fb0dd0212e013fe36425d9", null, null, null, null,
				false, null, null);

		// Prepare STUB
		given(articleAPI.postArticle(any(Article.class))).willReturn(article);

		// Perform MockTest
		mockMvc.perform(post("/article").content(mapper.writeValueAsString(article)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());

	}

	@Test
	public void testArticlePostVisibleToNotEmpty() throws Exception {
		String[] visibleTo = { "60fb0dd0212e013fe36425ce" };
		List<String> labels = new ArrayList<String>();
		Article article = new Article(null, "test of API", "This article is to test the api",
				"60fb0dd0212e013fe36425d6", null, false, "en", labels, 0, "60fb0dd0212e013fe36425d9", null, null, null,
				null, false, null, null);

		// Prepare STUB
		given(articleAPI.postArticle(any(Article.class))).willReturn(article);

		// Perform MockTest
		mockMvc.perform(post("/article").content(mapper.writeValueAsString(article)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());

	}

	@Test
	public void testArticlePostSourceLanguageSize() throws Exception {
		String[] visibleTo = { "60fb0dd0212e013fe36425ce" };
		List<String> labels = new ArrayList<String>();
		Article article = new Article(null, "test of API", "This article is to test the api",
				"60fb0dd0212e013fe36425d6", Arrays.asList(visibleTo), false, "enA", labels, 0,
				"60fb0dd0212e013fe36425d9", null, null, null, null, false, null, null);

		// Prepare STUB
		given(articleAPI.postArticle(any(Article.class))).willReturn(article);

		// Perform MockTest
		mockMvc.perform(post("/article").content(mapper.writeValueAsString(article)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testArticlePostSectionNotEmpty() throws Exception {
		String[] visibleTo = { "60fb0dd0212e013fe36425ce" };
		List<String> labels = new ArrayList<String>();
		Article article = new Article(null, "test of API", "This article is to test the api",
				"60fb0dd0212e013fe36425d6", Arrays.asList(visibleTo), false, "en", labels, 0, "", null, null, null,
				null, false, null, null);

		// Prepare STUB
		given(articleAPI.postArticle(any(Article.class))).willReturn(article);

		// Perform MockTest
		mockMvc.perform(post("/article").content(mapper.writeValueAsString(article)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testArticlePutSuccess() throws Exception {
		String[] visibleTo = { "60fb0dd0212e013fe36425ce" };
		List<String> labels = new ArrayList<String>();
		Article article = new Article("1", "test of API", "This article is to test the api", "60fb0dd0212e013fe36425d6",
				Arrays.asList(visibleTo), false, "en", labels, 0, "60fb0dd0212e013fe36425d9", null, null, null, null,
				false, null, null);

		// Prepare STUB
		given(articleAPI.putArticle(any(Article.class))).willReturn(article);

		// Perform MockTest
		String articleString = mockMvc
				.perform(put("/article").content(mapper.writeValueAsString(article)).contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		Article response = mapper.readValue(articleString, Article.class);

	}

	@Test
	public void testArticleDeleteSuccess() throws Exception {
		String[] visibleTo = { "60fb0dd0212e013fe36425ce" };
		List<String> labels = new ArrayList<String>();
		Article article = new Article(null, "test of API", "This article is to test the api",
				"60fb0dd0212e013fe36425d6", Arrays.asList(visibleTo), false, "en", labels, 0,
				"60fb0dd0212e013fe36425d9", null, null, null, null, false, null, null);

		// Prepare

		// Perform MockTest
		mockMvc.perform(delete("/article/1")).andExpect(status().isOk());

	}

}