package com.ngdesk.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Position {

	@JsonProperty("COL")
	public Integer col;

	@JsonProperty("ROW")
	public Integer row;

	@JsonProperty("X_AXIS")
	public Integer xAxis;

	@JsonProperty("Y_AXIS")
	public Integer yAxis;

	public Position() {

	}

	public Position(Integer col, Integer row, Integer xAxis, Integer yAxis) {
		super();
		this.col = col;
		this.row = row;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
	}

	public Integer getCol() {
		return col;
	}

	public Integer getRow() {
		return row;
	}

	public Integer getxAxis() {
		return xAxis;
	}

	public Integer getyAxis() {
		return yAxis;
	}

	public void setCol(Integer col) {
		this.col = col;
	}

	public void setRow(Integer row) {
		this.row = row;
	}

	public void setxAxis(Integer xAxis) {
		this.xAxis = xAxis;
	}

	public void setyAxis(Integer yAxis) {
		this.yAxis = yAxis;
	}

	
}
