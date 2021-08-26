package com.ngdesk.sam.discoverymap.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.ngdesk.sam.normalizationrules.dao.Rule;

import brave.Tracer;

@ExtendWith(SpringExtension.class)

@WebMvcTest(DiscoveryMapAPI.class)

@ContextConfiguration(classes = { ApplicationTest.class })

public class DiscoveryMapAPITest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	DiscoveryMapAPI discoveryMapAPI;

	@MockBean
	AuthProxy authProxy;

	@MockBean
	Tracer tracer;

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testDiscoveryMapPostSuccess() {

		Rule rule = new Rule("abc", "Is", "1.0");

		DiscoveryMap discoveryMap = new DiscoveryMap(null, "abcd", null, null, null, rule, rule, "Windows", null, null,
				null, null, null, null, null);

		given(discoveryMapAPI.postDiscoveryMap(any(DiscoveryMap.class))).willReturn(discoveryMap);
		try {
			String discoveryMapString = mockMvc
					.perform(post("/discovery_map").content(mapper.writeValueAsString(discoveryMap))
							.contentType(APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

			DiscoveryMap response = mapper.readValue(discoveryMapString, DiscoveryMap.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDiscoveryMapPutSuccess() {

		Rule rule = new Rule("abc", "Is", "1.0");

		DiscoveryMap discoveryMap = new DiscoveryMap(null, "abcd", null, null, null, rule, rule, "Windows", null, null,
				null, null, null, null, null);

		given(discoveryMapAPI.putDiscoveryMap(any(DiscoveryMap.class))).willReturn(discoveryMap);
		try {
			// PERFORM MOCK TEST
			String discoveryMapString = mockMvc
					.perform(put("/discovery_map").content(mapper.writeValueAsString(discoveryMap))
							.contentType(APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

			DiscoveryMap response = mapper.readValue(discoveryMapString, DiscoveryMap.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDiscoveryMapPlatformNotEmpty() {

		Rule rule = new Rule("abc", "Is", "1.0");

		DiscoveryMap discoveryMap = new DiscoveryMap(null, "abcd", null, null, null, rule, rule, "jnchncr", null, null,
				null, null, null, null, null);

		given(discoveryMapAPI.postDiscoveryMap(any(DiscoveryMap.class))).willReturn(discoveryMap);
		try {
			mockMvc.perform(post("/discovery_map").content(mapper.writeValueAsString(discoveryMap))
					.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDiscoveryMapNameNotEmpty() {

		Rule rule = new Rule("abc", "Is", "1.0");

		DiscoveryMap discoveryMap = new DiscoveryMap(null, "", null, null, null, rule, rule, "jnchncr", null, null,
				null, null, null, null, null);

		given(discoveryMapAPI.postDiscoveryMap(any(DiscoveryMap.class))).willReturn(discoveryMap);
		try {
			mockMvc.perform(post("/discovery_map").content(mapper.writeValueAsString(discoveryMap))
					.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDiscoveryMapKeyNotEmpty() {

		Rule rule = new Rule("", "Is", "1.0");

		DiscoveryMap discoveryMap = new DiscoveryMap(null, "abcd", null, null, null, rule, rule, "Windows", null, null,
				null, null, null, null, null);

		given(discoveryMapAPI.postDiscoveryMap(any(DiscoveryMap.class))).willReturn(discoveryMap);
		try {
			mockMvc.perform(post("/discovery_map").content(mapper.writeValueAsString(discoveryMap))
					.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDiscoveryMapOperatorNotEmpty() {

		Rule rule = new Rule("abc", "", "1.0");

		DiscoveryMap discoveryMap = new DiscoveryMap(null, "abcd", null, null, null, rule, rule, "Windows", null, null,
				null, null, null, null, null);

		given(discoveryMapAPI.postDiscoveryMap(any(DiscoveryMap.class))).willReturn(discoveryMap);
		try {
			mockMvc.perform(post("/discovery_map").content(mapper.writeValueAsString(discoveryMap))
					.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDiscoveryMapValueNotEmpty() {

		Rule rule = new Rule("abc", "Is", "");

		DiscoveryMap discoveryMap = new DiscoveryMap(null, "abcd", null, null, null, rule, rule, "Windows", null, null,
				null, null, null, null, null);

		given(discoveryMapAPI.postDiscoveryMap(any(DiscoveryMap.class))).willReturn(discoveryMap);
		try {
			mockMvc.perform(post("/discovery_map").content(mapper.writeValueAsString(discoveryMap))
					.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Test
	public void testDiscoveryMapApprovalPostSuccess() {
		// PERFORM MOCK TEST
		try {
			mockMvc.perform(post("/discovery_map/approval/?discovery_map_id=5faca6c64ab1f16929922775"))
					.andExpect(status().isOk());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDiscoveryMapDeleteSuccess() {
		// PERFORM MOCK TEST
		try {
			mockMvc.perform(delete("/discovery_map/?discovery_map_id=5faca6c64ab1f16929922775"))
					.andExpect(status().isOk());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
