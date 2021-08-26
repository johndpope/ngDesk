package com.ngdesk.module.catalogue.dao;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthProxy;
import com.ngdesk.module.NgDeskModulesServiceApplicationTests;

import brave.Tracer;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ServicecatalogueAPI.class)
@ContextConfiguration(classes = { NgDeskModulesServiceApplicationTests.class })
public class ServicecatalogueAPITest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ServicecatalogueAPI servicecatalogueAPI;

	@MockBean
	AuthProxy authProxy;

	@MockBean
	Tracer tracer;

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testCataloguePostSuccess() throws Exception {

		List<CatalogueForm> catalogueForms = new ArrayList<CatalogueForm>();
		catalogueForms.add(new CatalogueForm("047e4272-9f4d-4398-b84b-d88244341498", "1e29c72f-ed54-48f0-9895-957ca906cf39"));

		Catalogue catalogue = new Catalogue(null, "Test Catalogue", "Test Description", catalogueForms,
				"1e29c72f-ed54-48f0-9895-957ca906cf39", null, null, null, null, null, null);

		// PREPARE STUB
		given(servicecatalogueAPI.postCatalogue(any(Catalogue.class))).willReturn(catalogue);

		// PERFORM MOCK TEST
		String catalogueString = mockMvc
				.perform(post("/catalogue").content(mapper.writeValueAsString(catalogue)).contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		Catalogue response = mapper.readValue(catalogueString, Catalogue.class);

	}

	@Test
	public void testEscalationPostNameNotEmpty() throws Exception {

		List<CatalogueForm> catalogueForms = new ArrayList<CatalogueForm>();
		catalogueForms.add(new CatalogueForm("047e4272-9f4d-4398-b84b-d88244341498", "1e29c72f-ed54-48f0-9895-957ca906cf39"));

		Catalogue catalogue = new Catalogue(null, null, "Test Description", catalogueForms,
				"1e29c72f-ed54-48f0-9895-957ca906cf39", null, null, null, null, null, null);

		// PREPARE STUB
		given(servicecatalogueAPI.postCatalogue(any(Catalogue.class))).willReturn(catalogue);

		// PERFORM MOCK TEST
		mockMvc.perform(post("/catalogue").content(mapper.writeValueAsString(catalogue)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testputCatalogueSuccess() throws Exception {

		List<CatalogueForm> catalogueForms = new ArrayList<CatalogueForm>();
		catalogueForms.add(new CatalogueForm("047e4272-9f4d-4398-b84b-d88244341498", "1e29c72f-ed54-48f0-9895-957ca906cf39"));

		Catalogue catalogue = new Catalogue("123", "Test Catalogue", "Test Description", catalogueForms,
				"1e29c72f-ed54-48f0-9895-957ca906cf39", null, null, null, null, null, null);

		given(servicecatalogueAPI.putCatalogue(any(Catalogue.class))).willReturn(catalogue);

		// PERFORM MOCK TEST
		String catalogueString = mockMvc
				.perform(put("/catalogue").content(mapper.writeValueAsString(catalogue)).contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

	}

	@Test
	public void testCatalogueNameNotEmptyForput() throws Exception {

		List<CatalogueForm> catalogueForms = new ArrayList<CatalogueForm>();
		catalogueForms.add(new CatalogueForm("047e4272-9f4d-4398-b84b-d88244341498", "1e29c72f-ed54-48f0-9895-957ca906cf39"));

		Catalogue catalogue = new Catalogue("123", null, "Test Description", catalogueForms,
				"1e29c72f-ed54-48f0-9895-957ca906cf39", null, null, null, null, null, null);

		// PREPARE STUB
		given(servicecatalogueAPI.putCatalogue(any(Catalogue.class))).willReturn(catalogue);

		// PERFORM MOCK TEST
		mockMvc.perform(put("/catalogue").content(mapper.writeValueAsString(catalogue)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testDeleteCatalogueSuccess() throws Exception {

		List<CatalogueForm> catalogueForms = new ArrayList<CatalogueForm>();

		Catalogue catalogue = new Catalogue(null, "Test Catalogue", "Test Description", catalogueForms, null, null, null, null,
				null, null, null);
		try {
			mockMvc.perform(delete("/catalogue/id")).andExpect(status().isOk());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
