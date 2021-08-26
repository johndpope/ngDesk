package com.ngdesk.module.layouts.edit.dao;

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
import com.ngdesk.module.layout.dao.Panel;
import com.ngdesk.module.layout.dao.PreDefinedTemplate;
import com.ngdesk.module.layout.dao.PreDefinedTemplateField;

import brave.Tracer;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EditLayoutAPI.class)
@ContextConfiguration(classes = { NgDeskModulesServiceApplicationTests.class })
public class CreateEditLayoutAPITesting {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private EditLayoutAPI editLayoutAPI;

	@MockBean
	AuthProxy authProxy;
	
	@MockBean
	Tracer tracer;


	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testEditLayoutPostSuccess() throws Exception {
		String fieldId = null;

		List<PreDefinedTemplate> predefinedTemplates = new ArrayList<PreDefinedTemplate>();
		List<PreDefinedTemplateField> predefineTemplateFields = new ArrayList<PreDefinedTemplateField>();
		predefineTemplateFields.add(new PreDefinedTemplateField(fieldId));
		predefinedTemplates.add(new PreDefinedTemplate(null, predefineTemplateFields, null));

		List<Panel> panel = new ArrayList<Panel>();
		panel.add(new Panel(null, null, null, null, null));

		CreateEditLayout editLayout = new CreateEditLayout(null, "TEST EDITLAYOUT", "TEST DESCRIPTION", panel, null,
				predefinedTemplates, null, "TEST ROLE", "TEST LAYOUT STYLE", null, null, null, null);

		// PREPARE STUB
		given(editLayoutAPI.postEditLayout(any(CreateEditLayout.class), any(String.class))).willReturn(editLayout);

		// PERFORM MOCK TEST
		String editLayoutString = mockMvc
				.perform(post("/modules/module_id/edit_layout").content(mapper.writeValueAsString(editLayout))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		CreateEditLayout response = mapper.readValue(editLayoutString, CreateEditLayout.class);

	}

	@Test
	public void testEditLayoutGetSuccess() throws Exception {
		String fieldId = null;

		List<PreDefinedTemplate> predefinedTemplates = new ArrayList<PreDefinedTemplate>();
		List<PreDefinedTemplateField> predefineTemplateFields = new ArrayList<PreDefinedTemplateField>();
		predefineTemplateFields.add(new PreDefinedTemplateField(fieldId));
		predefinedTemplates.add(new PreDefinedTemplate(null, predefineTemplateFields, null));

		List<Panel> panel = new ArrayList<Panel>();
		panel.add(new Panel(null, null, null, null, null));

		CreateEditLayout editLayout = new CreateEditLayout(null, null, "TEST DESCRIPTION", panel, null,
				predefinedTemplates, null, "TEST ROLE", "TEST LAYOUT STYLE", null, null, null, null);


		// PREPARE STUB
		given(editLayoutAPI.getOneEditLayout(any(String.class), any(String.class))).willReturn(editLayout);
		// PERFORM MOCK TEST
		String editLayoutString = mockMvc.perform(get("/modules/module_id/edit_layout/layout_id").contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		CreateEditLayout response = mapper.readValue(editLayoutString, CreateEditLayout.class);
	}

	@Test
	public void testEditLayoutPostNameNotNull() throws Exception {

		String fieldId = null;

		List<PreDefinedTemplate> predefinedTemplates = new ArrayList<PreDefinedTemplate>();
		List<PreDefinedTemplateField> predefineTemplateFields = new ArrayList<PreDefinedTemplateField>();
		predefineTemplateFields.add(new PreDefinedTemplateField(fieldId));
		predefinedTemplates.add(new PreDefinedTemplate(null, predefineTemplateFields, null));

		List<Panel> panel = new ArrayList<Panel>();
		panel.add(new Panel(null, null, null, null, null));

		CreateEditLayout editLayout = new CreateEditLayout(null, null, "TEST DESCRIPTION", panel, null,
				predefinedTemplates, null, "TEST ROLE", "TestLayoutStyle", null, null, null, null);
		// PREPARE STUB
		given(editLayoutAPI.postEditLayout(any(CreateEditLayout.class), any(String.class))).willReturn(editLayout);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/modules/module_id/edit_layout").content(mapper.writeValueAsString(editLayout))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

	}

	@Test
	public void testEditLayoutPostNameNotEmpty() throws Exception {

		String fieldId = null;

		List<PreDefinedTemplate> predefinedTemplates = new ArrayList<PreDefinedTemplate>();
		List<PreDefinedTemplateField> predefineTemplateFields = new ArrayList<PreDefinedTemplateField>();
		predefineTemplateFields.add(new PreDefinedTemplateField(fieldId));
		predefinedTemplates.add(new PreDefinedTemplate(null, predefineTemplateFields, null));

		List<Panel> panel = new ArrayList<Panel>();
		panel.add(new Panel(null, null, null, null, null));

		CreateEditLayout editLayout = new CreateEditLayout(null, null, "TEST DESCRIPTION", panel, null,
				predefinedTemplates, null, "TEST ROLE", "TEST LAYOUT STYLE", null, null, null, null);
		// PREPARE STUB
		given(editLayoutAPI.postEditLayout(any(CreateEditLayout.class), any(String.class))).willReturn(editLayout);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/modules/module_id/edit_layout").content(mapper.writeValueAsString(editLayout))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

	}

	@Test
	public void testEditLayoutPostDescriptionNotNull() throws Exception {

		String fieldId = null;

		List<PreDefinedTemplate> predefinedTemplates = new ArrayList<PreDefinedTemplate>();
		List<PreDefinedTemplateField> predefineTemplateFields = new ArrayList<PreDefinedTemplateField>();
		predefineTemplateFields.add(new PreDefinedTemplateField(fieldId));
		predefinedTemplates.add(new PreDefinedTemplate(null, predefineTemplateFields, null));

		List<Panel> panel = new ArrayList<Panel>();
		panel.add(new Panel(null, null, null, null, null));

		CreateEditLayout editLayout = new CreateEditLayout(null, "TEST EDIT LAYOUT", null, panel, null,
				predefinedTemplates, null, "TEST ROLE", "TEST LAYOUT STYLE", null, null, null, null);
		// PREPARE STUB
		given(editLayoutAPI.postEditLayout(any(CreateEditLayout.class), any(String.class))).willReturn(editLayout);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/modules/module_id/edit_layout").content(mapper.writeValueAsString(editLayout))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

	}

	@Test
	public void testEditLayoutPostRoleNotNull() throws Exception {

		String fieldId = null;

		List<PreDefinedTemplate> predefinedTemplates = new ArrayList<PreDefinedTemplate>();
		List<PreDefinedTemplateField> predefineTemplateFields = new ArrayList<PreDefinedTemplateField>();
		predefineTemplateFields.add(new PreDefinedTemplateField(fieldId));
		predefinedTemplates.add(new PreDefinedTemplate(null, predefineTemplateFields, null));

		List<Panel> panel = new ArrayList<Panel>();
		panel.add(new Panel(null, null, null, null, null));

		CreateEditLayout editLayout = new CreateEditLayout(null, "TEST EDIT LAYOUT", "TEST DESCRIPTION", panel, null,
				predefinedTemplates, null, null, "TEST LAYOUT STYLE", null, null, null, null);
		// PREPARE STUB
		given(editLayoutAPI.postEditLayout(any(CreateEditLayout.class), any(String.class))).willReturn(editLayout);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/modules/module_id/edit_layout").content(mapper.writeValueAsString(editLayout))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

	}

	@Test
	public void testEditLayoutPostLayoutStyleNotNull() throws Exception {

		String fieldId = null;

		List<PreDefinedTemplate> predefinedTemplates = new ArrayList<PreDefinedTemplate>();
		List<PreDefinedTemplateField> predefineTemplateFields = new ArrayList<PreDefinedTemplateField>();
		predefineTemplateFields.add(new PreDefinedTemplateField(fieldId));
		predefinedTemplates.add(new PreDefinedTemplate(null, predefineTemplateFields, null));

		List<Panel> panel = new ArrayList<Panel>();
		panel.add(new Panel(null, null, null, null, null));

		CreateEditLayout editLayout = new CreateEditLayout(null, "TEST EDIT LAYOUT", "TEST DESCRIPTION", panel, null,
				predefinedTemplates, null, "TEST ROLE", null, null, null, null, null);
		// PREPARE STUB
		given(editLayoutAPI.postEditLayout(any(CreateEditLayout.class), any(String.class))).willReturn(editLayout);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/modules/module_id/edit_layout").content(mapper.writeValueAsString(editLayout))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

	}

}
