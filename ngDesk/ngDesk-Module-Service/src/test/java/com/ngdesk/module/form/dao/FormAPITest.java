package com.ngdesk.module.form.dao;

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

import javax.validation.constraints.Pattern;

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
import com.ngdesk.module.layout.dao.CreateEditLayout;

import brave.Tracer;

@ExtendWith(SpringExtension.class)

@WebMvcTest(FormAPI.class)

@ContextConfiguration(classes = { NgDeskModulesServiceApplicationTests.class })
public class FormAPITest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private FormAPI formAPI;

	@MockBean
	AuthProxy authProxy;

	@MockBean
	Tracer tracer;

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testFormPostSuccess() throws Exception {

		SaveButton saveButton = new SaveButton(null, "center", "url", null, null);
		Condition condition = new Condition("All", "CONDITION", "CHANGED", null);
		List<Condition> condition1 = new ArrayList<Condition>();
		condition1.add(condition);
		FieldSettings fieldSettings = new FieldSettings(null, condition1);
		List<Grid> grid = new ArrayList<Grid>();
		grid.add(new Grid(true, 10, 25, null, fieldSettings));
		List<List<Grid>> grid1 = new ArrayList<List<Grid>>();
		grid1.add(grid);
		List<FormPanel> panel = new ArrayList<FormPanel>();
		panel.add(new FormPanel(grid1, false, "panel1", "PANEL_1"));

		Form form = new Form(null, "form 101", "form 101", panel, "fill", saveButton, null, null, null, null,
				"60c05f202cb6fa09a2e92878", "5f68d2d296151b30f85b4f10", "/home/smandal/Downloads/E-8323.JPG", null,
				null);

		given(formAPI.postForm(any(Form.class), any(String.class))).willReturn(form);
		String formString = mockMvc
				.perform(post("/modules/5f68d2d296151b30f85b4f10/form").content(mapper.writeValueAsString(form))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

	}

	@Test
	public void testPutFormSuccess() throws Exception {

		SaveButton saveButton = new SaveButton(null, "center", "url", null, null);
		Condition condition = new Condition("All", "CONDITION", "CHANGED", null);
		List<Condition> condition1 = new ArrayList<Condition>();
		condition1.add(condition);
		FieldSettings fieldSettings = new FieldSettings(null, null);
		List<Grid> grid = new ArrayList<Grid>();
		grid.add(new Grid(true, 10, 25, null, fieldSettings));
		List<List<Grid>> grid1 = new ArrayList<List<Grid>>();
		grid1.add(grid);
		List<FormPanel> panel = new ArrayList<FormPanel>();
		panel.add(new FormPanel(grid1, false, "panel1", "PANEL_1"));

		Form form = new Form(null, "form 101", "form 101", panel, "fill", saveButton, null, null, null, null,
				"60c05f202cb6fa09a2e92878", "5f68d2d296151b30f85b4f10", "/home/smandal/Downloads/E-8323.JPG", null,
				null);

		given(formAPI.putForm(any(Form.class), any(String.class))).willReturn(form);
		String formString = mockMvc
				.perform(put("/modules/5f68d2d296151b30f85b4f10/form").content(mapper.writeValueAsString(form))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

	}

	@Test
	public void testPostFormNameNotEmpty() throws Exception {

		SaveButton saveButton = new SaveButton(null, "center", "url", null, null);
		Condition condition = new Condition("All", "CONDITION", "CHANGED", null);
		List<Condition> condition1 = new ArrayList<Condition>();
		condition1.add(condition);
		FieldSettings fieldSettings = new FieldSettings(null, null);
		List<Grid> grid = new ArrayList<Grid>();
		grid.add(new Grid(true, 10, 25, null, fieldSettings));
		List<List<Grid>> grid1 = new ArrayList<List<Grid>>();
		grid1.add(grid);
		List<FormPanel> panel = new ArrayList<FormPanel>();
		panel.add(new FormPanel(grid1, false, "panel1", "PANEL_1"));

		Form form = new Form(null, "", "form 101", panel, "fill", saveButton, null, null, null, null,
				"60c05f202cb6fa09a2e92878", "5f68d2d296151b30f85b4f10", "/home/smandal/Downloads/E-8323.JPG", null,
				null);

		given(formAPI.postForm(any(Form.class), any(String.class))).willReturn(form);
		mockMvc.perform(post("/modules/5f68d2d296151b30f85b4f10/form").content(mapper.writeValueAsString(form))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

	}

	@Test
	public void testPostFormNameNotNull() throws Exception {

		SaveButton saveButton = new SaveButton(null, "center", "url", null, null);
		Condition condition = new Condition("All", "CONDITION", "CHANGED", null);
		List<Condition> condition1 = new ArrayList<Condition>();
		condition1.add(condition);
		FieldSettings fieldSettings = new FieldSettings(null, null);
		List<Grid> grid = new ArrayList<Grid>();
		grid.add(new Grid(true, 10, 25, null, fieldSettings));
		List<List<Grid>> grid1 = new ArrayList<List<Grid>>();
		grid1.add(grid);
		List<FormPanel> panel = new ArrayList<FormPanel>();
		panel.add(new FormPanel(grid1, false, "panel1", "PANEL_1"));

		Form form = new Form(null, null, "form 101", panel, "fill", saveButton, null, null, null, null,
				"60c05f202cb6fa09a2e92878", "5f68d2d296151b30f85b4f10", "/home/smandal/Downloads/E-8323.JPG", null,
				null);

		given(formAPI.postForm(any(Form.class), any(String.class))).willReturn(form);
		mockMvc.perform(post("/modules/5f68d2d296151b30f85b4f10/form").content(mapper.writeValueAsString(form))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

	}

	@Test
	public void testPutFormNameNotEmpty() throws Exception {

		SaveButton saveButton = new SaveButton(null, "center", "url", null, null);
		Condition condition = new Condition("All", "CONDITION", "CHANGED", null);
		List<Condition> condition1 = new ArrayList<Condition>();
		condition1.add(condition);
		FieldSettings fieldSettings = new FieldSettings(null, null);
		List<Grid> grid = new ArrayList<Grid>();
		grid.add(new Grid(true, 10, 25, null, fieldSettings));
		List<List<Grid>> grid1 = new ArrayList<List<Grid>>();
		grid1.add(grid);
		List<FormPanel> panel = new ArrayList<FormPanel>();
		panel.add(new FormPanel(grid1, false, "panel1", "PANEL_1"));

		Form form = new Form(null, "", "form 101", panel, "fill", saveButton, null, null, null, null,
				"60c05f202cb6fa09a2e92878", "5f68d2d296151b30f85b4f10", "/home/smandal/Downloads/E-8323.JPG", null,
				null);

		given(formAPI.putForm(any(Form.class), any(String.class))).willReturn(form);
		mockMvc.perform(put("/modules/5f68d2d296151b30f85b4f10/form").content(mapper.writeValueAsString(form))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

	}

	@Test
	public void testPutFormNameNotNull() throws Exception {

		SaveButton saveButton = new SaveButton(null, "center", "url", null, null);
		Condition condition = new Condition("All", "CONDITION", "CHANGED", null);
		List<Condition> condition1 = new ArrayList<Condition>();
		condition1.add(condition);
		FieldSettings fieldSettings = new FieldSettings(null, null);
		List<Grid> grid = new ArrayList<Grid>();
		grid.add(new Grid(true, 10, 25, null, fieldSettings));
		List<List<Grid>> grid1 = new ArrayList<List<Grid>>();
		grid1.add(grid);
		List<FormPanel> panel = new ArrayList<FormPanel>();
		panel.add(new FormPanel(grid1, false, "panel1", "PANEL_1"));

		Form form = new Form(null, null, "form 101", panel, "fill", saveButton, null, null, null, null,
				"60c05f202cb6fa09a2e92878", "5f68d2d296151b30f85b4f10", "/home/smandal/Downloads/E-8323.JPG", null,
				null);

		given(formAPI.postForm(any(Form.class), any(String.class))).willReturn(form);
		mockMvc.perform(put("/modules/5f68d2d296151b30f85b4f10/form").content(mapper.writeValueAsString(form))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

	}

	@Test
	public void testDeleteFormSuccess() throws Exception {
		try {

			mockMvc.perform(delete("/module/5f68d2d296151b30f85b4f10/form/60c1825e1ae84c2e876541af"))
					.andExpect(status().isOk());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
