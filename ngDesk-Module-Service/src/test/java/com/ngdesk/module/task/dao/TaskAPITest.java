package com.ngdesk.module.task.dao;

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
import com.ngdesk.module.NgDeskModulesServiceApplicationTests;

import brave.Tracer;

@ExtendWith(SpringExtension.class)

@WebMvcTest(TaskAPI.class)

@ContextConfiguration(classes = { NgDeskModulesServiceApplicationTests.class })
public class TaskAPITest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private TaskAPI taskApi;

	@MockBean
	AuthProxy authProxy;

	@MockBean
	Tracer tracer;

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testAddTaskSuccess() throws Exception {

		List<Condition> condition = new ArrayList<Condition>();
		Interval interval = new Interval();
		List<Action> actions = new ArrayList<Action>();

		Task task = new Task(null, condition, "12", true, interval, new Date(), actions, "testingName", null,"America/Chicago",new Date());
		// PREPARE STUB
		given(taskApi.addTask(any(Task.class), any(String.class))).willReturn(task);

		// PERFORM MOCK TEST
		String notificationString = mockMvc
				.perform(post("/module/12/task").content(mapper.writeValueAsString(task)).contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		Task response = mapper.readValue(notificationString, Task.class);
	}

	@Test
	public void testTaskNameNotEmpty() throws Exception {

		List<Condition> condition = new ArrayList<Condition>();
		Interval interval = new Interval();
		List<Action> actions = new ArrayList<Action>();

		Task task = new Task(null, condition, "12", true, interval, new Date(), actions, null, null,"America/Chicago",new Date());

		// PREPARE STUB
		given(taskApi.addTask(any(Task.class), any(String.class))).willReturn(task);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/module/12/task").content(mapper.writeValueAsString(task)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());

	}

	private String eq(String string) {
		// TODO Auto-generated method stub
		return null;
	}


	@Test
	public void testTaskModuleIdNotEmpty() throws Exception {

		List<Condition> condition = new ArrayList<Condition>();
		Interval interval = new Interval();
		List<Action> actions = new ArrayList<Action>();

		Task task = new Task(null, condition, null, true, interval, new Date(), actions, "taskName", null,"America/Chicago",new Date());

		// PREPARE STUB
		given(taskApi.addTask(any(Task.class), any(String.class))).willReturn(task);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/module/12/task").content(mapper.writeValueAsString(task)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());

	}

	@Test
	public void testUpdateTaskSuccess() throws Exception {

		List<Condition> condition = new ArrayList<Condition>();
		Interval interval = new Interval();
		List<Action> actions = new ArrayList<Action>();

		Task task = new Task("2", condition, "12", true, interval, new Date(), actions, "testingName", null,"America/Chicago",new Date());
		// PREPARE STUB
		given(taskApi.updateTask(any(Task.class), any(String.class))).willReturn(task);

		// PERFORM MOCK TEST
		String notificationString = mockMvc
				.perform(put("/module/12/task").content(mapper.writeValueAsString(task)).contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

	}

	@Test
	public void testTaskNameNotEmptyForUpdate() throws Exception {

		List<Condition> condition = new ArrayList<Condition>();
		Interval interval = new Interval();
		List<Action> actions = new ArrayList<Action>();

		Task task = new Task(null, condition, "12", true, interval, new Date(), actions, null, null,"America/Chicago",new Date());

		// PREPARE STUB
		given(taskApi.updateTask(any(Task.class), any(String.class))).willReturn(task);

		// PERFORM MOCK TEST
		mockMvc.perform(put("/module/12/task").content(mapper.writeValueAsString(task)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());

	}

	@Test
	public void testTaskModuleIdNotEmptyForUpdate() throws Exception {

		List<Condition> condition = new ArrayList<Condition>();
		Interval interval = new Interval();
		List<Action> actions = new ArrayList<Action>();

		Task task = new Task(null, condition, null, true, interval, new Date(), actions, "taskName", null,"America/Chicago",new Date());

		// PREPARE STUB
		given(taskApi.updateTask(any(Task.class), any(String.class))).willReturn(task);

		// PERFORM MOCK TEST
		mockMvc.perform(put("/module/12/task").content(mapper.writeValueAsString(task)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());

	}


	@Test
	public void testDeleteTaskSuccess() throws Exception {

		try {
			mockMvc.perform(delete("/module/12/task/taskId=14"));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
