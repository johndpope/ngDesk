package com.ngdesk.sam.discoveryrules.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthProxy;
import com.ngdesk.sam.ApplicationTest;
import com.ngdesk.sam.discoveryrules.dao.DiscoveryRule;
import com.ngdesk.sam.discoveryrules.dao.DiscoveryRuleAPI;

import brave.Tracer;

@ExtendWith(SpringExtension.class)
@WebMvcTest(DiscoveryRuleAPI.class)
@ContextConfiguration(classes = { ApplicationTest.class })
public class DiscoveryRuleAPITest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private DiscoveryRuleAPI discoveryRuleAPI;

	@MockBean
	AuthProxy authProxy;
	
	@MockBean
	Tracer tracer;

	private final ObjectMapper mapper = new ObjectMapper();

	@DisplayName("Happy Path Test for Sam Discovery rule Post")
	@Test
	public void testDiscoveryRulePostSuccess() throws Exception {
		List<String> command = new ArrayList<String>();
		
		DiscoveryRule discoveryRule = new DiscoveryRule(null, null, null , null , "COMMAND",
				"Windows", null, null, command, null, null, null, null, null);

		// PREPARE STUB
		given(discoveryRuleAPI.postDiscoveryRule(any(DiscoveryRule.class))).willReturn(discoveryRule);

		// PERFORM MOCK TEST
		String discoveryRuleString = mockMvc
				.perform(post("/rule").content(mapper.writeValueAsString(discoveryRule)).contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		DiscoveryRule response = mapper.readValue(discoveryRuleString, DiscoveryRule.class);
	}

	@DisplayName("Happy Path test for Discovery rules Get")
	@Test
	public void testDiscoveryRuleGetSuccess() throws Exception {

		List<String> command = new ArrayList<String>();
		DiscoveryRule discoveryRule = new DiscoveryRule(null, "6545678", "Test Discovery Rule ", "hash", "rule",
				"windows",null, null, command, null, null, null, null, null);

		// PREPARE STUB
		given(discoveryRuleAPI.getDiscoveryRuleById(any(String.class))).willReturn(discoveryRule);

		// PERFORM MOCK TEST
		String discoveryRuleString = mockMvc
				.perform(get("/rule/testId").contentType(mapper.writeValueAsString(discoveryRule))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		DiscoveryRule response = mapper.readValue(discoveryRuleString, DiscoveryRule.class);
	}

	@DisplayName("Happy Path test for Discovery rules GetAll")
	@Test
	public void testDiscoveryRuleGetAllSuccess() throws Exception {

		List<String> command = new ArrayList<String>();
		DiscoveryRule discoveryRule = new DiscoveryRule(null, "6545678", "Test Discovery Rule ", "hash", "rule",
				"windows", null, null, command, null, null, null, null, null);

		List<DiscoveryRule> discoveryRules = new ArrayList<DiscoveryRule>();
		discoveryRules.add(discoveryRule);

		Page<DiscoveryRule> pages = new PageImpl<>(discoveryRules);

		// PREPARE STUB
		given(discoveryRuleAPI.getDiscoveryRules(any(Pageable.class))).willReturn(pages);

		// PERFORM MOCK TEST
		mockMvc.perform(get("/rules").contentType(APPLICATION_JSON)).andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	}

	@DisplayName("Happy Path Test for Discovery rule Put")
	@Test
	public void testDiscoveryRulePutSuccess() throws Exception {

		List<String> command = new ArrayList<String>();
		DiscoveryRule discoveryRule = new DiscoveryRule("987654", "6545678", "Test Put Discovery rule", "hash", "COMMAND",
				"Windows", null, null, command, null, null, null, null, null);

		// PREPARE STUB
		given(discoveryRuleAPI.putDiscoveryRule(any(DiscoveryRule.class))).willReturn(discoveryRule);

		// PERFORM MOCK TEST
		String discoveryRuleString = mockMvc
				.perform(put("/rule").content(mapper.writeValueAsString(discoveryRule)).contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		DiscoveryRule response = mapper.readValue(discoveryRuleString, DiscoveryRule.class);

	}

	@DisplayName("Happy Path Test for Discovery Rule Delete")
	@Test
	public void testDiscoveryRuleDeleteSuccess() throws Exception {
		// PERFORM MOCK TEST
		mockMvc.perform(delete("/rule/testId")).andExpect(status().isOk());
	}

	// TODO: Happy Paths for GET ALL, PUT, DELETE

	@DisplayName("Test to check companyID NotEmpty Validation")
	@Test
	public void testDiscoveryRulePostCompanyIdNotEmpty() throws Exception {

		List<String> command = new ArrayList<String>();
		DiscoveryRule discoveryRule = new DiscoveryRule(null, null, "Test Discovery Rule ", "hash", "rule", "Windows",
				null, null, command, null, null, null, null, null);

		// PREPARE STUB
		given(discoveryRuleAPI.postDiscoveryRule(any(DiscoveryRule.class))).willReturn(discoveryRule);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/rule").content(mapper.writeValueAsString(discoveryRule)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@DisplayName("Test to check File Path NotEmpty Validation")
	@Test
	public void testDiscoveryRulePostNameNotEmpty() throws Exception {

		List<String> command = new ArrayList<String>();
		DiscoveryRule discoveryRule = new DiscoveryRule(null, "2345654", null, "hash", "rule", "Windows", null, null,
				command, null, null, null, null, null);

		// PREPARE STUB
		given(discoveryRuleAPI.postDiscoveryRule(any(DiscoveryRule.class))).willReturn(discoveryRule);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/rule").content(mapper.writeValueAsString(discoveryRule)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@DisplayName("Test to check Vendor NotEmpty Validation")
	@Test
	public void testDiscoveryRulePostVendorNotEmpty() throws Exception {

		List<String> command = new ArrayList<String>();
		DiscoveryRule discoveryRule = new DiscoveryRule(null, "2345654", "Test discovery rule", "hash", "rule",
				"Windows", null, null, command, null, null, null, null, null);

		// PREPARE STUB
		given(discoveryRuleAPI.postDiscoveryRule(any(DiscoveryRule.class))).willReturn(discoveryRule);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/rule").content(mapper.writeValueAsString(discoveryRule)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@DisplayName("Test to check OS NotEmpty Validation")
	@Test
	public void testDiscoveryRulePostOSNotEmpty() throws Exception {

		List<String> command = new ArrayList<String>();
		DiscoveryRule discoveryRule = new DiscoveryRule(null, "2345654", "Test discovery rule", "hash", "rule", null,
				null, null, command, null, null, null, null, null);

		// PREPARE STUB
		given(discoveryRuleAPI.postDiscoveryRule(any(DiscoveryRule.class))).willReturn(discoveryRule);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/rule").content(mapper.writeValueAsString(discoveryRule)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@DisplayName("Test to check rule type NotEmpty Validation")
	@Test
	public void testDiscoveryRulePostRuleTypeNotEmpty() throws Exception {

		List<String> command = new ArrayList<String>();
		DiscoveryRule discoveryRule = new DiscoveryRule(null, "2345654", "Test discovery rule", "hash", null, "Windows",
				null, null, command, null, null, null, null, null);

		// PREPARE STUB
		given(discoveryRuleAPI.postDiscoveryRule(any(DiscoveryRule.class))).willReturn(discoveryRule);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/rule").content(mapper.writeValueAsString(discoveryRule)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

}
