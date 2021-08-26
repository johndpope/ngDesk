
package com.ngdesk.sam.swidtag;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthProxy;
import com.ngdesk.sam.ApplicationTest;

import brave.Tracer;

@ExtendWith(SpringExtension.class)

@WebMvcTest(SwidtagAPI.class)

@ContextConfiguration(classes = { ApplicationTest.class })

public class SwidtagAPITest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	SwidtagAPI swidtagAPI;

	@MockBean
	AuthProxy authProxy;

	@MockBean
	Tracer tracer;

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testPostSwidtagsSuccess() {

		List<Swidtag> swidtagFiles = new ArrayList<>();
		swidtagFiles.add(new Swidtag(null, "TEST_FILE_NAME.swidtag", "TEST FILE_CONTENT", null, null, null, null, null,
				"TEST_ASSETID", null));
		swidtagFiles.add(new Swidtag(null, "TEST_FILE_NAME.swidtag", "TEST FILE_CONTENT", null, null, null, null, null,
				"TEST_ASSETID", null));
		Swidtags swidtags = new Swidtags(swidtagFiles);
		given(swidtagAPI.postSwidtags(any(Swidtags.class))).willReturn(swidtags);

		String swidtagString;
		try {
			swidtagString = mockMvc
					.perform(post("/swidtags").content(mapper.writeValueAsString(swidtags))
							.contentType(APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
			Swidtags response = mapper.readValue(swidtagString, Swidtags.class);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSwidtagFileNameNotNull() {

		List<Swidtag> swidtagFiles = new ArrayList<>();

		Swidtag swidtag = new Swidtag(null, null, "TEST FILE_CONTENT", null, null, null, null, null, "TEST_ASSETID",
				null);
		swidtagFiles.add(swidtag);
		Swidtags swidtags = new Swidtags(swidtagFiles);

		given(swidtagAPI.postSwidtags(any(Swidtags.class))).willReturn(swidtags);

		try {
			mockMvc.perform(
					post("/swidtags").content(mapper.writeValueAsString(swidtags)).contentType(APPLICATION_JSON))
					.andExpect(status().isBadRequest());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSwidtagFileContentNotNull() {
		List<Swidtag> swidtagFiles = new ArrayList<>();

		Swidtag swidtag = new Swidtag(null, "TEST FILE_NAME.swidtag", null, null, null, null, null, null,
				"TEST_ASSETID", null);
		swidtagFiles.add(swidtag);
		Swidtags swidtags = new Swidtags(swidtagFiles);

		given(swidtagAPI.postSwidtags(any(Swidtags.class))).willReturn(swidtags);

		try {
			mockMvc.perform(
					post("/swidtags").content(mapper.writeValueAsString(swidtags)).contentType(APPLICATION_JSON))
					.andExpect(status().isBadRequest());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
