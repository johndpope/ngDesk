package com.ngdesk.sam.normalizationrules;

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
import com.mongodb.client.result.DeleteResult;
import com.ngdesk.commons.managers.AuthProxy;
import com.ngdesk.sam.ApplicationTest;
import com.ngdesk.sam.normalizationrules.dao.NormalizationRule;
import com.ngdesk.sam.normalizationrules.dao.NormalizationRuleAPI;
import com.ngdesk.sam.normalizationrules.dao.Rule;

import brave.Tracer;

@ExtendWith(SpringExtension.class)

@WebMvcTest(NormalizationRuleAPI.class)

@ContextConfiguration(classes = { ApplicationTest.class })

public class NormalizationRuleAPITest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	NormalizationRuleAPI normalizationRuleAPI;

	@MockBean
	AuthProxy authProxy;

	@MockBean
	Tracer tracer;

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testNormalizationRulePostSuccess() {

		Rule rule = new Rule("abc", "Is", "abcde");
		NormalizationRule normalizationRule = new NormalizationRule("", "test", "", "sfjhsjfnsmjnkjzdzb", rule, rule,
				rule, "Approved", null);
		given(normalizationRuleAPI.postNormalizationRule(any(NormalizationRule.class))).willReturn(normalizationRule);
		try {
			String normalizationRuleString = mockMvc
					.perform(post("/normalization_rules").content(mapper.writeValueAsString(normalizationRule))
							.contentType(APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

			NormalizationRule response = mapper.readValue(normalizationRuleString, NormalizationRule.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testNormalizationRulePostKeyNotEmpty() {

		Rule rule = new Rule(" ", "Is", "abcde");
		NormalizationRule normalizationRule = new NormalizationRule("", "test", "", "sfjhsjfnsmjnkjzdzb", rule, rule,
				rule, "Approved", null);
		given(normalizationRuleAPI.postNormalizationRule(any(NormalizationRule.class))).willReturn(normalizationRule);
		try {
			mockMvc.perform(post("/normalization_rules").content(mapper.writeValueAsString(normalizationRule))
					.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testNormalizationRulePostOperatorNotEmpty() {

		Rule rule = new Rule("abc", " ", "abcde");
		NormalizationRule normalizationRule = new NormalizationRule("", "test", "", "sfjhsjfnsmjnkjzdzb", rule, rule,
				rule, "Approved", null);
		given(normalizationRuleAPI.postNormalizationRule(any(NormalizationRule.class))).willReturn(normalizationRule);
		try {
			mockMvc.perform(post("/normalization_rules").content(mapper.writeValueAsString(normalizationRule))
					.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testNormalizationRulePostValueNotEmpty() {

		Rule rule = new Rule("abc", "Is", " ");
		NormalizationRule normalizationRule = new NormalizationRule("", "test", "", "sfjhsjfnsmjnkjzdzb", rule, rule,
				rule, "Approved", null);
		given(normalizationRuleAPI.postNormalizationRule(any(NormalizationRule.class))).willReturn(normalizationRule);
		try {
			mockMvc.perform(post("/normalization_rules").content(mapper.writeValueAsString(normalizationRule))
					.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testNormalizationRuleApprovalPostSuccess() {
		try {
			mockMvc.perform(post("/normalization_rules/approve?rule_id=5fa51d5774895726e55dbe28"))
					.andExpect(status().isOk());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testNormalizationRulePutSuccess() {

		Rule rule = new Rule("abc", "Is", "abcde");
		NormalizationRule normalizationRule = new NormalizationRule("", "test", "", "sfjhsjfnsmjnkjzdzb", rule, rule,
				rule, "Approved", null);
		given(normalizationRuleAPI.putNormalizationRule(any(NormalizationRule.class))).willReturn(normalizationRule);
		try {
			// PERFORM MOCK TEST
			String normalizationRuleString = mockMvc
					.perform(put("/normalization_rules").content(mapper.writeValueAsString(normalizationRule))
							.contentType(APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

			NormalizationRule response = mapper.readValue(normalizationRuleString, NormalizationRule.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testNormalizationRuleDeleteSuccess() {
		given(normalizationRuleAPI.deleteNormalizationRule(any(String.class))).willReturn(any(DeleteResult.class));
		try {
			mockMvc.perform(
					delete("/normalization_rules?rule_id=5fa51d5774895726e55dbe28").contentType(APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
