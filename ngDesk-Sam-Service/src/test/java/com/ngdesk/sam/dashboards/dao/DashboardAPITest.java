
package com.ngdesk.sam.dashboards.dao;

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
import com.ngdesk.commons.models.DashboardCondition;
import com.ngdesk.commons.models.Dashboard;
import com.ngdesk.commons.models.PieChartWidget;
import com.ngdesk.commons.models.ScoreCardWidget;
import com.ngdesk.commons.models.Widget;
import com.ngdesk.sam.ApplicationTest;

import brave.Tracer;

@ExtendWith(SpringExtension.class)

@WebMvcTest(DashboardAPI.class)

@ContextConfiguration(classes = { ApplicationTest.class })
public class DashboardAPITest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	DashboardAPI dashboardAPI;

	@MockBean
	AuthProxy authProxy;

	@MockBean
	Tracer tracer;
	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testDiscoveryRulePostSuccess() throws Exception {

		List<Widget> widgets = new ArrayList<Widget>();
		List<DashboardCondition> condition = new ArrayList<DashboardCondition>();
		condition.add(new DashboardCondition("f1041c25-c928-4d30-b2fb-9c843dbe9f95", "EQUALS_TO", "5", "All"));
		ScoreCardWidget scoreCardWidget = new ScoreCardWidget();
		scoreCardWidget.setDashboardconditions(condition);
		scoreCardWidget.setModuleId("Test_moduleId");
		scoreCardWidget.setPositionX(20);
		scoreCardWidget.setPositionY(30);
		scoreCardWidget.setTitle("Test_title");
		scoreCardWidget.setType("score");
		scoreCardWidget.setWidgetId("Test_widgetId");
		scoreCardWidget.setAggregateType("count");
		widgets.add(scoreCardWidget);

		Dashboard dashboard = new Dashboard(null, "Test_name", "Test_Role565", null, widgets, null, null, null, null,
				null);

		// PREPARE STUB
		given(dashboardAPI.postDashboard(any(Dashboard.class))).willReturn(dashboard);

		// PERFORM MOCK TEST
		String dashboardRuleString = mockMvc
				.perform(post("/dashboard").content(mapper.writeValueAsString(dashboard)).contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		Dashboard response = mapper.readValue(dashboardRuleString, Dashboard.class);
	}

	@Test
	public void testDashboardRoleNotEmpty() {

		List<Widget> widgets = new ArrayList<Widget>();
		widgets.add(new ScoreCardWidget());

		Dashboard dashboard = new Dashboard(null, "Test_name", null, null, widgets, null, null, null, null, null);

		given(dashboardAPI.postDashboard(any(Dashboard.class))).willReturn(dashboard);

		try {
			mockMvc.perform(
					post("/dashboard").content(mapper.writeValueAsString(dashboard)).contentType(APPLICATION_JSON))
					.andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDashboardNameNotEmpty() {

		List<Widget> widgets = new ArrayList<Widget>();
		widgets.add(new ScoreCardWidget());

		Dashboard dashboard = new Dashboard(null, null, "Test_Role", null, widgets, null, null, null, null, null);

		given(dashboardAPI.postDashboard(any(Dashboard.class))).willReturn(dashboard);

		try {
			mockMvc.perform(
					post("/dashboard").content(mapper.writeValueAsString(dashboard)).contentType(APPLICATION_JSON))
					.andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDashboardWidgetsNotEmpty() {

		Dashboard dashboard = new Dashboard(null, "Test_name", "Test_Role", null, null, null, null, null, null, null);

		given(dashboardAPI.postDashboard(any(Dashboard.class))).willReturn(dashboard);

		try {
			mockMvc.perform(
					post("/dashboard").content(mapper.writeValueAsString(dashboard)).contentType(APPLICATION_JSON))
					.andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDashboardModuleIdNotEmpty() {

		List<Widget> widgets = new ArrayList<Widget>();
		List<DashboardCondition> condition = new ArrayList<DashboardCondition>();
		condition.add(new DashboardCondition("f1041c25-c928-4d30-b2fb-9c843dbe9f95", "EQUALS_TO", "5", "Any"));
		ScoreCardWidget scoreCardWidget = new ScoreCardWidget();
		scoreCardWidget.setDashboardconditions(condition);
		scoreCardWidget.setModuleId("");
		scoreCardWidget.setPositionX(20);
		scoreCardWidget.setPositionY(30);
		scoreCardWidget.setTitle("Test_title");
		scoreCardWidget.setType("score");
		scoreCardWidget.setWidgetId("Test_widgetId");
		widgets.add(scoreCardWidget);

		Dashboard dashboard = new Dashboard(null, "Test_name", "Test_Role", null, widgets, null, null, null, null,
				null);

		given(dashboardAPI.postDashboard(any(Dashboard.class))).willReturn(dashboard);

		try {
			mockMvc.perform(
					post("/dashboard").content(mapper.writeValueAsString(dashboard)).contentType(APPLICATION_JSON))
					.andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDashboardTypeNotEmpty() {

		List<Widget> widgets = new ArrayList<Widget>();
		List<DashboardCondition> condition = new ArrayList<DashboardCondition>();
		condition.add(new DashboardCondition("f1041c25-c928-4d30-b2fb-9c843dbe9f95", "EQUALS_TO", "5", "Any"));
		ScoreCardWidget scoreCardWidget = new ScoreCardWidget();
		scoreCardWidget.setDashboardconditions(condition);
		scoreCardWidget.setModuleId("Test_moduleId");
		scoreCardWidget.setPositionX(20);
		scoreCardWidget.setPositionY(30);
		scoreCardWidget.setTitle("Test_title");
		scoreCardWidget.setType("");
		scoreCardWidget.setWidgetId("Test_widgetId");
		widgets.add(scoreCardWidget);

		Dashboard dashboard = new Dashboard(null, "Test_name", "Test_Role565", null, widgets, null, null, null, null,
				null);

		given(dashboardAPI.postDashboard(any(Dashboard.class))).willReturn(dashboard);

		try {
			mockMvc.perform(
					post("/dashboard").content(mapper.writeValueAsString(dashboard)).contentType(APPLICATION_JSON))
					.andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDashboardFieldNotEmpty() {

		List<Widget> widgets = new ArrayList<Widget>();
		PieChartWidget pieChartWidget = new PieChartWidget();
		pieChartWidget.setField("");
		widgets.add(pieChartWidget);

		Dashboard dashboard = new Dashboard(null, "name", "565", null, widgets, null, null, null, null, null);

		given(dashboardAPI.postDashboard(any(Dashboard.class))).willReturn(dashboard);

		try {
			mockMvc.perform(
					post("/dashboard").content(mapper.writeValueAsString(dashboard)).contentType(APPLICATION_JSON))
					.andExpect(status().isBadRequest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDashboardPutSuccess() {

		List<Widget> widgets = new ArrayList<Widget>();
		List<DashboardCondition> condition = new ArrayList<DashboardCondition>();
		condition.add(new DashboardCondition("f1041c25-c928-4d30-b2fb-9c843dbe9f95", "EQUALS_TO", "5", "All"));
		ScoreCardWidget scoreCardWidget = new ScoreCardWidget();
		scoreCardWidget.setDashboardconditions(condition);
		scoreCardWidget.setModuleId("Test_moduleId");
		scoreCardWidget.setPositionX(20);
		scoreCardWidget.setPositionY(30);
		scoreCardWidget.setTitle("Test_title");
		scoreCardWidget.setType("score");
		scoreCardWidget.setWidgetId("Test_widgetId");
		scoreCardWidget.setAggregateType("count");
		widgets.add(scoreCardWidget);

		Dashboard dashboard = new Dashboard(null, "Test_name", "Test_Role565", null, widgets, null, null, null, null,
				null);

		given(dashboardAPI.putDashboard(any(Dashboard.class))).willReturn(dashboard);
		try { // PERFORM MOCK TEST
			String dashboardString = mockMvc
					.perform(put("/dashboard").content(mapper.writeValueAsString(dashboard))
							.contentType(APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

			Dashboard response = mapper.readValue(dashboardString, Dashboard.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDashboardDeleteSuccess() { // PERFORM MOCK TEST
		try {
			mockMvc.perform(delete("/dashboard/?dashboard_id=5fbb7b92fb126a38779f3e44")).andExpect(status().isOk());

		} catch (

		Exception e) {
			e.printStackTrace();
		}
	}

}
