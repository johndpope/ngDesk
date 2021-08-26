package com.ngdesk.report.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthProxy;
import com.ngdesk.report.NgDeskReportServiceApplicationTests;

import brave.Tracer;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ReportApi.class)
@ContextConfiguration(classes = { NgDeskReportServiceApplicationTests.class })
public class ReportApiTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ReportApi reportApi;

	@MockBean
	AuthProxy authProxy;

	@MockBean
	Tracer tracer;

	private final ObjectMapper mapper = new ObjectMapper();
	public static HttpHeaders postHeaders;

	@DisplayName("Test to determine success of post")
	@Test
	public void testReportPostSuccess() throws Exception {

		List<Filter> filters = new ArrayList<Filter>();
		List<ReportField> fields = new ArrayList<ReportField>();
		ReportSchedule schedules = new ReportSchedule("cron", null);
		List<String> emails = new ArrayList<String>();
		ReportField field = new ReportField("id", emails);
		Filter filter = new Filter(field, "EQUALS_TO", "All", "value");
		filters.add(filter);
		fields.add(field);
		Report report = new Report("id", "name", "reportDescription", "Module id", fields, filters, null, null, null,
				null, field, "list", "asc", schedules);

		// PREPARE STUB
		given(reportApi.postReport(any(Report.class))).willReturn(report);

		// PERFORM MOCK TEST
		String reportString = mockMvc
				.perform(post("/reports").content(mapper.writeValueAsString(report)).contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		Report response = mapper.readValue(reportString, Report.class);
	}

	@Test
	public void testReportNameNotEmpty() {

		List<Filter> filters = new ArrayList<Filter>();
		List<ReportField> fields = new ArrayList<ReportField>();
		ReportSchedule schedules = new ReportSchedule("cron", null);
		List<String> emails = new ArrayList<String>();
		ReportField field = new ReportField("id", emails);
		Filter filter = new Filter(field, "EQUALS_TO", "All", "value");
		filters.add(filter);
		fields.add(field);
		Report report = new Report("id", null, "reportDescription", "Module id", fields, filters, null, null, null,
				null, field, "list", "asc", schedules);

		// PREPARE STUB
		given(reportApi.postReport(any(Report.class))).willReturn(report);

		try {
			mockMvc.perform(post("/reports").content(mapper.writeValueAsString(report)).contentType(APPLICATION_JSON))
					.andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testReportModuleNotEmpty() {

		List<Filter> filters = new ArrayList<Filter>();
		List<ReportField> fields = new ArrayList<ReportField>();
		ReportSchedule schedules = new ReportSchedule("cron", null);
		List<String> emails = new ArrayList<String>();
		ReportField field = new ReportField("id", emails);
		Filter filter = new Filter(field, "EQUALS_TO", "All", "value");
		filters.add(filter);
		fields.add(field);
		Report report = new Report("id", "name", "reportDescription", null, fields, filters, null, null, null, null,
				field, "list", "asc", schedules);

		// PREPARE STUB
		given(reportApi.postReport(any(Report.class))).willReturn(report);

		try {
			mockMvc.perform(post("/reports").content(mapper.writeValueAsString(report)).contentType(APPLICATION_JSON))
					.andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testReportTypeNotEmpty() {

		List<Filter> filters = new ArrayList<Filter>();
		List<ReportField> fields = new ArrayList<ReportField>();
		ReportSchedule schedules = new ReportSchedule("cron", null);
		List<String> emails = new ArrayList<String>();
		ReportField field = new ReportField("id", emails);
		Filter filter = new Filter(field, "EQUALS_TO", "All", "value");
		filters.add(filter);
		fields.add(field);
		Report report = new Report("id", "name", "reportDescription", "module Id", fields, filters, null, null, null,
				null, field, null, "asc", schedules);

		// PREPARE STUB
		given(reportApi.postReport(any(Report.class))).willReturn(report);

		try {
			mockMvc.perform(post("/reports").content(mapper.writeValueAsString(report)).contentType(APPLICATION_JSON))
					.andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testReportOrderNotEmpty() {

		List<Filter> filters = new ArrayList<Filter>();
		List<ReportField> fields = new ArrayList<ReportField>();
		ReportSchedule schedules = new ReportSchedule("cron", null);
		List<String> emails = new ArrayList<String>();
		ReportField field = new ReportField("id", emails);
		Filter filter = new Filter(field, "EQUALS_TO", "All", "value");
		filters.add(filter);
		fields.add(field);
		Report report = new Report("id", "name", "reportDescription", "module Id", fields, filters, null, null, null,
				null, field, "list", "asced", schedules);

		// PREPARE STUB
		given(reportApi.postReport(any(Report.class))).willReturn(report);

		try {
			mockMvc.perform(post("/reports").content(mapper.writeValueAsString(report)).contentType(APPLICATION_JSON))
					.andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testReportFieldNotEmpty() {

		List<Filter> filters = new ArrayList<Filter>();
		List<ReportField> fields = new ArrayList<ReportField>();
		ReportSchedule schedules = new ReportSchedule("cron", null);
		List<String> emails = new ArrayList<String>();
		ReportField field = new ReportField("id", emails);
		Filter filter = new Filter(field, "EQUALS_TO", "All", "value");
		filters.add(filter);
		fields.add(field);
		Report report = new Report("id", "name", "reportDescription", "module Id", null, filters, null, null, null,
				null, field, "list", "asc", schedules);

		// PREPARE STUB
		given(reportApi.postReport(any(Report.class))).willReturn(report);

		try {
			mockMvc.perform(post("/reports").content(mapper.writeValueAsString(report)).contentType(APPLICATION_JSON))
					.andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testReportFiltersNotEmpty() {

		List<Filter> filters = new ArrayList<Filter>();
		List<ReportField> fields = new ArrayList<ReportField>();
		ReportSchedule schedules = new ReportSchedule("cron", null);
		List<String> emails = new ArrayList<String>();
		ReportField field = new ReportField("id", emails);
		Filter filter = new Filter(field, "EQUALS_TO", "All", "value");
		filters.add(filter);
		fields.add(field);
		Report report = new Report("id", "name", "reportDescription", "module Id", fields, null, null, null, null, null,
				field, "list", "asc", schedules);

		// PREPARE STUB
		given(reportApi.postReport(any(Report.class))).willReturn(report);

		try {
			mockMvc.perform(post("/reports").content(mapper.writeValueAsString(report)).contentType(APPLICATION_JSON))
					.andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testReportPutSuccess() {

		List<Filter> filters = new ArrayList<Filter>();
		List<ReportField> fields = new ArrayList<ReportField>();
		ReportSchedule schedules = new ReportSchedule("cron", null);
		List<String> emails = new ArrayList<String>();
		ReportField field = new ReportField("id", emails);
		Filter filter = new Filter(field, "EQUALS_TO", "All", "value");
		filters.add(filter);
		fields.add(field);
		Report report = new Report("id", "name", "reportDescription", "Module id", fields, filters, null, null, null,
				null, field, "list", "asc", schedules);

		given(reportApi.putReport(any(Report.class))).willReturn(report);
		try {
			// PERFORM MOCK TEST
			String reportString = mockMvc
					.perform(put("/reports").content(mapper.writeValueAsString(report)).contentType(APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

			Report response = mapper.readValue(reportString, Report.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testReportDeleteSuccess() {
		// PERFORM MOCK TEST
		try {
			mockMvc.perform(delete("/reports/?report_id=5fbb7b92fb126a38779f3e44")).andExpect(status().isOk());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
