package com.ngdesk.sam.enterprisesearch.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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
import com.ngdesk.sam.ApplicationTest;

import brave.Tracer;

@ExtendWith(SpringExtension.class)

@WebMvcTest(EnterpriseSearchAPI.class)

@ContextConfiguration(classes = { ApplicationTest.class })
public class EnterpriseSearchApiTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	EnterpriseSearchAPI enterpriseSearchAPI;

	@MockBean
	AuthProxy authProxy;

	@MockBean
	Tracer tracer;
	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testEnterpriseSearchPostSuccess() throws Exception {
		List<String> tags = new ArrayList<String>();
		tags.add("Healthcare");
		tags.add("Ssn");

		EnterpriseSearch enterpriseSearch = new EnterpriseSearch(null, "Test_Name", null, tags, "Test_FilePath",
				"Test_Regex", null, null, null, null, null, null);
		// PREPARE STUB
		given(enterpriseSearchAPI.postEnterpriseSearch(any(EnterpriseSearch.class))).willReturn(enterpriseSearch);

		// PERFORM MOCK TEST
		String enterpriseSearchString = mockMvc
				.perform(post("/enterprise_search").content(mapper.writeValueAsString(enterpriseSearch))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		EnterpriseSearch response = mapper.readValue(enterpriseSearchString, EnterpriseSearch.class);
	}

	@Test
	public void testEnterpriseSearchPutSuccess() throws Exception {

		List<String> tags = new ArrayList<String>();
		tags.add("Healthcare");

		EnterpriseSearch enterpriseSearch = new EnterpriseSearch(null, "Test_Name", null, tags, "Test_FilePath",
				"Test_Regex", null, null, null, null, null, null);

		// PREPARE STUB
		given(enterpriseSearchAPI.putEnterpriseSearch(any(EnterpriseSearch.class))).willReturn(enterpriseSearch);

		// PERFORM MOCK TEST
		String enterpriseSearchString = mockMvc
				.perform(put("/enterprise_search").content(mapper.writeValueAsString(enterpriseSearch))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		EnterpriseSearch response = mapper.readValue(enterpriseSearchString, EnterpriseSearch.class);

	}

	@Test
	public void testEnterpriseSearchPostCompanyNameNotEmpty() throws Exception {

		List<String> tags = new ArrayList<String>();
		tags.add("Healthcare");

		EnterpriseSearch enterpriseSearch = new EnterpriseSearch(null, null, null, tags, "Test_FilePath", "Test_Regex",
				null, null, null, null, null, null);
		// PREPARE STUB
		given(enterpriseSearchAPI.postEnterpriseSearch(any(EnterpriseSearch.class))).willReturn(enterpriseSearch);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/enterprise_search").content(mapper.writeValueAsString(enterpriseSearch))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void testEnterpriseSearchPostCompanyTagsNotEmpty() throws Exception {

		EnterpriseSearch enterpriseSearch = new EnterpriseSearch(null, "Test_Name", null, null, "Test_FilePath",
				"Test_Regex", null, null, null, null, null, null);
		// PREPARE STUB
		given(enterpriseSearchAPI.postEnterpriseSearch(any(EnterpriseSearch.class))).willReturn(enterpriseSearch);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/enterprise_search").content(mapper.writeValueAsString(enterpriseSearch))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void testEnterpriseSearchPostCompanyFilePathNotEmpty() throws Exception {

		List<String> tags = new ArrayList<String>();
		tags.add("Healthcare");

		EnterpriseSearch enterpriseSearch = new EnterpriseSearch(null, "Test_Name", null, tags, null, "Test_Regex",
				null, null, null, null, null, null);
		// PREPARE STUB
		given(enterpriseSearchAPI.postEnterpriseSearch(any(EnterpriseSearch.class))).willReturn(enterpriseSearch);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/enterprise_search").content(mapper.writeValueAsString(enterpriseSearch))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void testEnterpriseSearchPostCompanyRegxNotEmpty() throws Exception {

		List<String> tags = new ArrayList<String>();
		tags.add("Healthcare");

		EnterpriseSearch enterpriseSearch = new EnterpriseSearch(null, "Test_Name", null, tags, "Test_FilePath", null,
				null, null, null, null, null, null);
		// PREPARE STUB
		given(enterpriseSearchAPI.postEnterpriseSearch(any(EnterpriseSearch.class))).willReturn(enterpriseSearch);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/enterprise_search").content(mapper.writeValueAsString(enterpriseSearch))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}
}
