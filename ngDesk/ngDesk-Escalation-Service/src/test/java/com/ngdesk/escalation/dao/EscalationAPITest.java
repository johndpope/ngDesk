package com.ngdesk.escalation.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.escalation.ApplicationTest;

import brave.Tracer;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EscalationAPI.class)
@ContextConfiguration(classes = { ApplicationTest.class })
public class EscalationAPITest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private EscalationAPI escalationAPI;

	@MockBean
	AuthProxy authProxy;
	
	@MockBean
	SessionManager sessionManager;
	
	@MockBean
	Tracer tracer;
	
	
	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testEscalationPostSuccess() throws Exception {

		List<String> ids = new ArrayList<String>();
		List<EscalationRule> rules = new ArrayList<EscalationRule>();
		rules.add(new EscalationRule(1, 1, new EscalateTo(ids, ids, ids)));

		Escalation escalation = new Escalation(null, "Test Escalation", "Test Description", rules, null, null, null,
				null);

		// PREPARE STUB
		given(escalationAPI.postEscalation(any(Escalation.class))).willReturn(escalation);

		// PERFORM MOCK TEST
		String escalationString = mockMvc
				.perform(post("/escalation").content(mapper.writeValueAsString(escalation))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		Escalation response = mapper.readValue(escalationString, Escalation.class);
	}

	@Test
	public void testEscalationGetSuccess() throws Exception {

		List<String> ids = new ArrayList<String>();
		List<EscalationRule> rules = new ArrayList<EscalationRule>();
		rules.add(new EscalationRule(1, 1, new EscalateTo(ids, ids, ids)));

		Escalation escalation = new Escalation(null, "Test Escalation", "Test Description", rules, null, null, null,
				null);

		// PREPARE STUB
		given(escalationAPI.getEscalationById(any(String.class))).willReturn(escalation);

		// PERFORM MOCK TEST
		String escalationString = mockMvc.perform(get("/escalations/testId").contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		Escalation response = mapper.readValue(escalationString, Escalation.class);
	}	

	@Test
	public void testEscalationPostNameNotEmpty() throws Exception {

		List<String> ids = new ArrayList<String>();
		List<EscalationRule> rules = new ArrayList<EscalationRule>();
		rules.add(new EscalationRule(1, 1, new EscalateTo(ids, ids, ids)));

		Escalation escalation = new Escalation(null, null, "Test Description", rules, null, null, null, null);

		// PREPARE STUB
		given(escalationAPI.postEscalation(any(Escalation.class))).willReturn(escalation);

		// PERFORM MOCK TEST
		mockMvc.perform(
				post("/escalation").content(mapper.writeValueAsString(escalation)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testEscalationPostDescriptionNotNull() throws Exception {

		List<String> ids = new ArrayList<String>();
		List<EscalationRule> rules = new ArrayList<EscalationRule>();
		rules.add(new EscalationRule(1, 1, new EscalateTo(ids, ids, ids)));

		Escalation escalation = new Escalation(null, "Test Escalation", null, rules, null, null, null, null);

		// PREPARE STUB
		given(escalationAPI.postEscalation(any(Escalation.class))).willReturn(escalation);

		// PERFORM MOCK TEST
		mockMvc.perform(
				post("/escalation").content(mapper.writeValueAsString(escalation)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testEscalationPostRulesNotNull() throws Exception {
		Escalation escalation = new Escalation(null, "Test Escalation Name", "Test Description", null, null, null, null,
				null);
		// PREPARE STUB
		given(escalationAPI.postEscalation(any(Escalation.class))).willReturn(escalation);
		// PERFORM MOCK TEST
		mockMvc.perform(
				post("/escalation").content(mapper.writeValueAsString(escalation)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testEscalationPostRulesSizeMin() throws Exception {
		List<EscalationRule> rules = new ArrayList<EscalationRule>();
		Escalation escalation = new Escalation(null, "Test Escalation Name", "Test Description", rules, null, null,
				null, null);
		// PREPARE STUB
		given(escalationAPI.postEscalation(any(Escalation.class))).willReturn(escalation);
		// PERFORM MOCK TEST
		mockMvc.perform(
				post("/escalation").content(mapper.writeValueAsString(escalation)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testEscalationPostRuleEscalateToNotNull() throws Exception {

		List<String> ids = new ArrayList<String>();
		List<EscalationRule> rules = new ArrayList<EscalationRule>();
		rules.add(new EscalationRule(null, 1, new EscalateTo(ids, ids, ids)));

		Escalation escalation = new Escalation(null, "Test Escalation Name", "Test Description", rules, null, null,
				null, null);

		// PREPARE STUB
		given(escalationAPI.postEscalation(any(Escalation.class))).willReturn(escalation);

		// PERFORM MOCK TEST
		mockMvc.perform(
				post("/escalation").content(mapper.writeValueAsString(escalation)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testEscalationPostRuleOrderNotNull() throws Exception {

		List<String> ids = new ArrayList<String>();
		List<EscalationRule> rules = new ArrayList<EscalationRule>();
		rules.add(new EscalationRule(1, null, new EscalateTo(ids, ids, ids)));

		Escalation escalation = new Escalation(null, "Test Escalation Name", "Test Description", rules, null, null,
				null, null);

		// PREPARE STUB
		given(escalationAPI.postEscalation(any(Escalation.class))).willReturn(escalation);

		// PERFORM MOCK TEST
		mockMvc.perform(
				post("/escalation").content(mapper.writeValueAsString(escalation)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testEscalationPostRuleMinsAfterNotNull() throws Exception {

		List<String> ids = new ArrayList<String>();
		List<EscalationRule> rules = new ArrayList<EscalationRule>();
		rules.add(new EscalationRule(1, 1, null));

		Escalation escalation = new Escalation(null, "Test Escalation Name", "Test Description", rules, null, null,
				null, null);

		// PREPARE STUB
		given(escalationAPI.postEscalation(any(Escalation.class))).willReturn(escalation);

		// PERFORM MOCK TEST
		mockMvc.perform(
				post("/escalation").content(mapper.writeValueAsString(escalation)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

}
