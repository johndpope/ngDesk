package com.ngdesk.sam.rules;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthProxy;
import com.ngdesk.sam.ApplicationTest;
import com.ngdesk.sam.rules.dao.RuleAPI;
import com.ngdesk.sam.rules.dao.SamFileRule;

import brave.Tracer;

@ExtendWith(SpringExtension.class)

@WebMvcTest(RuleAPI.class)

@ContextConfiguration(classes = { ApplicationTest.class })
public class RulesAPITest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	RuleAPI ruleAPI;

	@MockBean
	AuthProxy authProxy;

	@MockBean
	Tracer tracer;

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testSamFileRulePostSuccess() throws Exception {

		SamFileRule samFileRule = new SamFileRule(null, null, null, "TEST_RULE_CONDITION", null, null,
				"TEST_RULE_VERSION", "TEST_RULE_PUBLISHER", null, "TEST_RULE_SOFTWARE_NAME", null, null, null, null);
		// PREPARE STUB
		given(ruleAPI.postSamFileRule(any(SamFileRule.class))).willReturn(samFileRule);

		// PERFORM MOCK TEST
		String ruleString = mockMvc
				.perform(post("/software/probe/rules").content(mapper.writeValueAsString(samFileRule))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		SamFileRule response = mapper.readValue(ruleString, SamFileRule.class);
	}

	@Test
	public void testSamFileFileRulePutSuccess() throws Exception {

		SamFileRule samFileRule = new SamFileRule(null, null, null, "TEST_RULE_CONDITION", null, null,
				"TEST_RULE_VERSION", "TEST_RULE_PUBLISHER", null, "TEST_RULE_SOFTWARE_NAME", null, null, null, null);

		// PREPARE STUB
		given(ruleAPI.putSamFileRule(any(SamFileRule.class))).willReturn(samFileRule);

		// PERFORM MOCK TEST
		String ruleString = mockMvc
				.perform(put("/software/probe/rules").content(mapper.writeValueAsString(samFileRule))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		SamFileRule response = mapper.readValue(ruleString, SamFileRule.class);

	}

	@Test
	public void testSamFileRuleGetAllSuccess() throws Exception {

		SamFileRule samFileRule = new SamFileRule(null, null, null, "TEST_RULE_CONDITION", null, null,
				"TEST_RULE_VERSION", "TEST_RULE_PUBLISHER", null, "TEST_RULE_SOFTWARE_NAME", null, null, null, null);
		List<SamFileRule> samFileRules = new ArrayList<SamFileRule>();
		samFileRules.add(samFileRule);
		// PREPARE STUB
		given(ruleAPI.getAllSamFileRules(any(Pageable.class))).willReturn(samFileRules);

		// PERFORM MOCK TEST
		mockMvc.perform(get("/software/probe/rules").contentType(APPLICATION_JSON)).andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

	}

	@Test
	public void testSamFileRuleGetSuccess() throws Exception {

		SamFileRule samFileRule = new SamFileRule(null, null, null, "TEST_RULE_CONDITION", null, null,
				"TEST_RULE_VERSION", "TEST_RULE_PUBLISHER", null, "TEST_RULE_SOFTWARE_NAME", null, null, null, null);
		// PREPARE STUB
		given(ruleAPI.getSamFileRuleById(any(String.class))).willReturn(samFileRule);

		// PERFORM MOCK TEST
		String ruleString = mockMvc
				.perform(get("/software/probe/rules/rule_id").contentType(mapper.writeValueAsString(samFileRule))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		SamFileRule response = mapper.readValue(ruleString, SamFileRule.class);
	}

	@Test
	public void testSamFileRulePostRuleConditionNotEmpty() throws Exception {

		SamFileRule samFileRule = new SamFileRule(null, null, null, null, null, null, "TEST_RULE_VERSION",
				"TEST_RULE_PUBLISHER", null, "TEST_RULE_SOFTWARE_NAME", null, null, null, null);
		// PREPARE STUB
		given(ruleAPI.postSamFileRule(any(SamFileRule.class))).willReturn(samFileRule);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/software/probe/rules").content(mapper.writeValueAsString(samFileRule))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void testSamFileRulePostVersionNotEmpty() throws Exception {

		SamFileRule samFileRule = new SamFileRule(null, null, null, "TEST_RULE_CONDITION", null, null, null,
				"TEST_RULE_PUBLISHER", null, "TEST_RULE_SOFTWARE_NAME", null, null, null, null);
		// PREPARE STUB
		given(ruleAPI.postSamFileRule(any(SamFileRule.class))).willReturn(samFileRule);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/software/probe/rules").content(mapper.writeValueAsString(samFileRule))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void testSamFileRulePostPublisherNotEmpty() throws Exception {

		SamFileRule samFileRule = new SamFileRule(null, null, null, "TEST_RULE_CONDITION", null, null,
				"TEST_RULE_VERSION", null, null, "TEST_RULE_SOFTWARE_NAME", null, null, null, null);

		given(ruleAPI.postSamFileRule(any(SamFileRule.class))).willReturn(samFileRule);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/software/probe/rules").content(mapper.writeValueAsString(samFileRule))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void testSamFileRulePostSoftwarNameNotEmpty() throws Exception {

		SamFileRule samFileRule = new SamFileRule(null, null, null, "TEST_RULE_CONDITION", null, null,
				"TEST_RULE_VERSION", "TEST_RULE_PUBLISHER", null, null, null, null, null, null);

		given(ruleAPI.postSamFileRule(any(SamFileRule.class))).willReturn(samFileRule);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/software/probe/rules").content(mapper.writeValueAsString(samFileRule))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}
}
