
package com.ngdesk.module.layouts.create.dao;

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
import com.ngdesk.module.NgDeskModulesServiceApplicationTests;
import com.ngdesk.module.layout.dao.CreateEditLayout;
import com.ngdesk.module.layout.dao.Grid;
import com.ngdesk.module.layout.dao.Panel;
import com.ngdesk.module.layout.dao.PreDefinedTemplate;
import com.ngdesk.module.layout.dao.PreDefinedTemplateField;

import brave.Tracer;

@ExtendWith(SpringExtension.class)

@WebMvcTest(CreateLayoutAPI.class)

@ContextConfiguration(classes = { NgDeskModulesServiceApplicationTests.class })
public class CreatelayoutAPITesting {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	CreateLayoutAPI createLayoutAPI;

	@MockBean
	AuthProxy authProxy;

	@MockBean
	Tracer tracer;

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testCreateLayoutPostSuccess() throws Exception {

		List<PreDefinedTemplate> predefinedTemplates = new ArrayList<PreDefinedTemplate>();
		List<PreDefinedTemplateField> predefineTemplateFields = new ArrayList<PreDefinedTemplateField>();
		predefineTemplateFields.add(new PreDefinedTemplateField(null));
		predefinedTemplates.add(new PreDefinedTemplate(null, predefineTemplateFields, null));

		List<Panel> panel = new ArrayList<Panel>();
		List<List<Grid>> grid = null;
		panel.add(new Panel(null, null, null, grid, null));

		CreateEditLayout createLayout = new CreateEditLayout(null, "TEST EDITLAYOUT", "TEST DESCRIPTION", panel, null,
				predefinedTemplates, null, "TEST ROLE", "TestLayoutStyle", null, null, null, null);

		given(createLayoutAPI.postCreateLayout(any(CreateEditLayout.class), any(String.class)))
				.willReturn(createLayout);

		String createlayoutString = mockMvc
				.perform(post("/modules/module_id/create_layout").content(mapper.writeValueAsString(createLayout))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		CreateEditLayout response = mapper.readValue(createlayoutString, CreateEditLayout.class);
	}

	@Test
	public void testcreateLayoutPostNameNotNUll() throws Exception {

		List<PreDefinedTemplate> predefinedTemplates = new ArrayList<PreDefinedTemplate>();
		List<PreDefinedTemplateField> predefineTemplateFields = new ArrayList<PreDefinedTemplateField>();
		predefineTemplateFields.add(new PreDefinedTemplateField(null));
		predefinedTemplates.add(new PreDefinedTemplate(null, predefineTemplateFields, null));

		List<Panel> panel = new ArrayList<Panel>();
		List<List<Grid>> grid = null;
		panel.add(new Panel(null, null, null, grid, null));

		CreateEditLayout createLayout = new CreateEditLayout(null, null, "TEST DESCRIPTION", panel, null,
				predefinedTemplates, null, "TEST ROLE", "TestLayoutStyle", null, null, null, null);

		given(createLayoutAPI.postCreateLayout(any(CreateEditLayout.class), any(String.class)))
				.willReturn(createLayout);

		mockMvc.perform(post("/modules/module_id/create_layout").content(mapper.writeValueAsString(createLayout))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void testCreateLayoutPostDescriptionNotNull() throws Exception {

		List<PreDefinedTemplate> predefinedTemplates = new ArrayList<PreDefinedTemplate>();
		List<PreDefinedTemplateField> predefineTemplateFields = new ArrayList<PreDefinedTemplateField>();
		predefineTemplateFields.add(new PreDefinedTemplateField(null));
		predefinedTemplates.add(new PreDefinedTemplate(null, predefineTemplateFields, null));

		List<Panel> panel = new ArrayList<Panel>();
		List<List<Grid>> grid = null;
		panel.add(new Panel(null, null, null, grid, null));

		CreateEditLayout createLayout = new CreateEditLayout(null, "TEST EDITLAYOUT", null, panel, null,
				predefinedTemplates, null, "TEST ROLE", "TestLayoutStyle", null, null, null, null);

		given(createLayoutAPI.postCreateLayout(any(CreateEditLayout.class), any(String.class)))
				.willReturn(createLayout);

		mockMvc.perform(post("/modules/module_id/create_layout").content(mapper.writeValueAsString(createLayout))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

	}

	@Test
	public void testCreateLayoutGetSuccess() throws Exception {

		List<PreDefinedTemplate> predefinedTemplates = new ArrayList<PreDefinedTemplate>();
		List<PreDefinedTemplateField> predefineTemplateFields = new ArrayList<PreDefinedTemplateField>();
		predefineTemplateFields.add(new PreDefinedTemplateField(null));
		predefinedTemplates.add(new PreDefinedTemplate(null, predefineTemplateFields, null));

		List<Panel> panel = new ArrayList<Panel>();
		List<List<Grid>> grid = null;
		panel.add(new Panel(null, null, null, grid, null));

		CreateEditLayout createLayout = new CreateEditLayout(null, "TEST EDITLAYOUT", "TEST DESCRIPTION", panel, null,
				predefinedTemplates, null, "TEST ROLE", "TestLayoutStyle", null, null, null, null);
		given(createLayoutAPI.getOneCreateLayout(any(String.class), any(String.class))).willReturn(createLayout);

		String createlayoutString = mockMvc
				.perform(get("/modules/module_id/create_layout/layout_id").contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		CreateEditLayout response = mapper.readValue(createlayoutString, CreateEditLayout.class);
	}
}
