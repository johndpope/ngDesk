package com.ngdesk.knowledgebase.section.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Date;
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
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.knowledgebase.NgDeskKnowledgeBaseServiceApplicationTests;
import brave.Tracer;

@ExtendWith(SpringExtension.class)
@WebMvcTest(SectionAPI.class)
@ContextConfiguration(classes = { NgDeskKnowledgeBaseServiceApplicationTests.class })
public class TestSectionAPI {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SectionAPI sectionAPI;

	@MockBean
	AuthProxy authProxy;

	@MockBean
	SessionManager sessionManager;

	@MockBean
	Tracer tracer;

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testSectionsPostSuccess() throws Exception {

		List<String> visibleTo = new ArrayList<String>();
		visibleTo.add("demo");
		List<String> managedBy = new ArrayList<String>();
		managedBy.add("test");
		Section section = new Section(null, "en", "Section1", null, "Manually", "1234564", new Date(), new Date(), null,
				null, 0, false, visibleTo, managedBy);

		given(sectionAPI.postSection(any(Section.class))).willReturn(section);

		String sectionString = mockMvc
				.perform(post("/sections").content(mapper.writeValueAsString(section)).contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		Section response = mapper.readValue(sectionString, Section.class);
	}

	@Test
	public void testUpdateSectionSuccess() throws Exception {

		List<String> visibleTo = new ArrayList<String>();
		visibleTo.add("demo");
		List<String> managedBy = new ArrayList<String>();
		managedBy.add("test");

		Section section = new Section(null, "en", "Section1", null, "Manually", "1234564", new Date(), new Date(), null,
				null, 0, false, visibleTo, managedBy);
		// PREPARE STUB
		given(sectionAPI.updateSection(any(Section.class))).willReturn(section);

		// PERFORM MOCK TEST
		String notificationString = mockMvc
				.perform(put("/sections").content(mapper.writeValueAsString(section)).contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

	}

	@Test
	public void testDeleteSectionSuccess() throws Exception {

		try {
			mockMvc.perform(delete("/sections/12")).andExpect(status().isOk());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testSectionPostNameNotEmpty() throws Exception {
		
		Section section = new Section(null, null, null, null, null, null, new Date(), new Date(), null,
				null, 0, false, null, null);
		// PREPARE STUB
		given(sectionAPI.postSection(any(Section.class))).willReturn(section);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/sections").content(mapper.writeValueAsString(section)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
}
