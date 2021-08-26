package com.ngdesk.commons.models;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ngdesk.commons.annotations.CustomNotEmpty;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = ScoreCardWidget.class, name = "score"),
		@JsonSubTypes.Type(value = MultiScoreCardWidget.class, name = "multi-score"),
		@JsonSubTypes.Type(value = BarChartWidget.class, name = "bar-horizontal"),
		@JsonSubTypes.Type(value = PieChartWidget.class, name = "pie"),
		@JsonSubTypes.Type(value = AdvancedPieChartWidget.class, name = "advanced-pie") })

public abstract class Widget {

	private String widgetId;

	private String title;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "MODULE_ID" })
	private String moduleId;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "TYPE" })

	@Pattern(regexp = "score|bar-horizontal|pie|multi-score|advanced-pie", message = "INVALID_NODE_TYPE")
	private String type;

	private Integer positionX;

	private Integer positionY;

	private Integer width;

	private Integer height;

	@Valid
	private List<DashboardCondition> dashboardconditions;

	@Valid
	private OrderBy orderBy;

	private Integer limit;

	private Boolean limitEntries;

	private List<ScoreCardWidget> multiScorecards;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "AGGREGATION_TYPE" })
	@Pattern(regexp = "sum|count|max|average|min", message = "NOT_VALID_AGGREGATION_TYPE")
	private String aggregateType;

	private String aggregateField;

	public Widget() {

	}

	public Widget(String widgetId, String title, String moduleId,
			@Pattern(regexp = "score|bar-horizontal|pie|multi-score|advanced-pie", message = "INVALID_NODE_TYPE") String type,
			Integer positionX, Integer positionY, Integer width, Integer height,
			@Valid List<DashboardCondition> dashboardconditions, @Valid OrderBy orderBy, Integer limit,
			Boolean limitEntries, List<ScoreCardWidget> multiScorecards,
			@Pattern(regexp = "sum|count|max|average|min", message = "NOT_VALID_AGGREGATION_TYPE") String aggregateType,
			String aggregateField) {
		super();
		this.widgetId = widgetId;
		this.title = title;
		this.moduleId = moduleId;
		this.type = type;
		this.positionX = positionX;
		this.positionY = positionY;
		this.width = width;
		this.height = height;
		this.dashboardconditions = dashboardconditions;
		this.orderBy = orderBy;
		this.limit = limit;
		this.limitEntries = limitEntries;
		this.multiScorecards = multiScorecards;
		this.aggregateType = aggregateType;
		this.aggregateField = aggregateField;
	}

	public String getWidgetId() {
		return widgetId;
	}

	public void setWidgetId(String widgetId) {
		this.widgetId = widgetId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getPositionX() {
		return positionX;
	}

	public void setPositionX(Integer positionX) {
		this.positionX = positionX;
	}

	public Integer getPositionY() {
		return positionY;
	}

	public void setPositionY(Integer positionY) {
		this.positionY = positionY;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public List<DashboardCondition> getDashboardconditions() {
		return dashboardconditions;
	}

	public void setDashboardconditions(List<DashboardCondition> dashboardconditions) {
		this.dashboardconditions = dashboardconditions;
	}

	public OrderBy getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(OrderBy orderBy) {
		this.orderBy = orderBy;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Boolean getLimitEntries() {
		return limitEntries;
	}

	public void setLimitEntries(Boolean limitEntries) {
		this.limitEntries = limitEntries;
	}

	public List<ScoreCardWidget> getMultiScorecards() {
		return multiScorecards;
	}

	public void setMultiScorecards(List<ScoreCardWidget> multiScorecards) {
		this.multiScorecards = multiScorecards;
	}

	public String getAggregateType() {
		return aggregateType;
	}

	public void setAggregateType(String aggregateType) {
		this.aggregateType = aggregateType;
	}

	public String getAggregateField() {
		return aggregateField;
	}

	public void setAggregateField(String aggregateField) {
		this.aggregateField = aggregateField;
	}

}
