package com.ngdesk.schedule.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthProxy;
import com.ngdesk.schedule.ApplicationTest;
import com.ngdesk.schedule.dao.Layer;
import com.ngdesk.schedule.dao.OnCallUser;
import com.ngdesk.schedule.dao.Restriction;
import com.ngdesk.schedule.dao.Schedule;
import com.ngdesk.schedule.dao.ScheduleAPI;

import brave.Tracer;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ScheduleAPI.class)
@ContextConfiguration(classes={ApplicationTest.class})
public class ScheduleAPITest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	Tracer tracer;

	@MockBean
	private ScheduleAPI scheduleAPI;
	
	@MockBean
	AuthProxy authProxy;
	
	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testSchedulePost() throws Exception {
		
	
	}

//	@Test
//	public void testGetEscalation() throws Exception {
//
//		String escalationString = mockMvc.perform(get("/escalations").contentType(APPLICATION_JSON))
//				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
//
//		List<Escalation> escalationList = mapper.readValue(escalationString, new TypeReference<List<Escalation>>() {
//		});
//	}
//
//	@Test
//	public void testNameRequiredValidation() throws Exception {
//		Escalation escalation = new Escalation(null, null, "", List.of(new EscalationRule()), null, null, null, null);
//		mockMvc.perform(
//				post("/escalation").content(mapper.writeValueAsString(escalation)).contentType(APPLICATION_JSON))
//				.andExpect(status().isBadRequest());
//	}
//
//	@Test
//	public void testEscalationRuleRequiredValidation() throws Exception {
//		Escalation escalation = new Escalation(null, "Test Escalation", "", null, null, null, null, null);
//		String error = mockMvc
//				.perform(post("/books").content(mapper.writeValueAsString(escalation))
//						.header("authentication_token", "").contentType(APPLICATION_JSON))
//				.andExpect(status().isBadRequest()).andReturn().getResolvedException().getMessage();
//
//		assertThat(StringUtils.contains(error, "RULES_NOT_NULL"));
//	}

	// TODO: Add test cases for get by id, put and delete by id api calls

}
