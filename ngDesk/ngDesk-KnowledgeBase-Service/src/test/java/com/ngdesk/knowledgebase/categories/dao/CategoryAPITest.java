package com.ngdesk.knowledgebase.categories.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthProxy;
import com.ngdesk.knowledgebase.ApplicationTest;

import brave.Tracer;

@ExtendWith(SpringExtension.class)
@WebMvcTest(Category.class)
@ContextConfiguration(classes = { ApplicationTest.class })
public class CategoryAPITest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CategoryApi categoryApi;

	@MockBean
	AuthProxy authProxy;

	@MockBean
	Tracer tracer;

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testCategoryPostSuccess() throws Exception {

		List<String> ids = new ArrayList<String>();

		Category category = new Category(null, "name", "desc", "en", "", "", null, null, true, 1, ids);

		// PREPARE STUB
		given(categoryApi.postCategory(any(Category.class))).willReturn(category);
		try {

			// PERFORM MOCK TEST
			String categoryString = mockMvc
					.perform(post("/category").content(mapper.writeValueAsString(category))
							.contentType(APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

			Category response = mapper.readValue(categoryString, Category.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCategoryPutSuccess() {

		List<String> ids = new ArrayList<String>();

		Category category = new Category(null, "name", "desc", "en", "", "", null, null, true, 1, ids);

		given(categoryApi.putCategory(any(Category.class))).willReturn(category);
		try {
			// PERFORM MOCK TEST
			String categoryString = mockMvc
					.perform(
							put("/category").content(mapper.writeValueAsString(category)).contentType(APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

			Category response = mapper.readValue(categoryString, Category.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCategoryDeleteSuccess() {
		// PERFORM MOCK TEST
		try {
			mockMvc.perform(delete("/category/5fbb7b92fb126a38779f3e44")).andExpect(status().isOk());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCategoryPostNameNotEmpty() throws Exception {

		List<String> ids = new ArrayList<String>();

		Category category = new Category(null, null, "desc", "en", "", "", null, null, true, 1, ids);

		// PREPARE STUB
		given(categoryApi.putCategory(any(Category.class))).willReturn(category);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/category").content(mapper.writeValueAsString(category)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testCategoryPostSourceLanguage() throws Exception {

		List<String> ids = new ArrayList<String>();

		Category category = new Category(null, "xx", "desc", "enx", "", "", null, null, true, 1, ids);

		// PREPARE STUB
		given(categoryApi.putCategory(any(Category.class))).willReturn(category);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/category").content(mapper.writeValueAsString(category)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

}
