package com.ngdesk.company.plugin.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public class Tier {
	
	@Schema(description = "Name of the tier", required = true, example = "Free")
	@Field("NAME")
	@JsonProperty("NAME")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "TIER_NAME"})
	@CustomNotNull(message = "NOT_NULL", values = { "TIER_NAME"})
	private String name;
	
	@Schema(description = "Price of the tier", required = true)
	@Field("PRICE")
	@JsonProperty("PRICE")
	private String price;
	
	@Schema(description = "Name of the tier", required = true)
	@Field("SUBSCRIBED")
	@JsonProperty("SUBSCRIBED")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "SUBSCRIBED"})
	@CustomNotNull(message = "NOT_NULL", values = { "SUBSCRIBED"})
	private Boolean subscribed;
	
	@Schema(description = "List of modules in the tier", required = true, example = "TicketModule")
	@Field("MODULES")
	@JsonProperty("MODULES")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "MODULES"})
	@CustomNotNull(message = "NOT_NULL", values = { "MODULES"})
	private List<TierModule> modules;
	
	
	@Schema(description = "List of features in the tier", required = true)
	@Field("FEATURES")
	@JsonProperty("FEATURES")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FEATURES"})
	private List<Feature> features;


	public Tier() {
		super();
	}


	public Tier(String name, String price, Boolean subscribed, List<TierModule> modules, List<Feature> features) {
		super();
		this.name = name;
		this.price = price;
		this.subscribed = subscribed;
		this.modules = modules;
		this.features = features;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getPrice() {
		return price;
	}


	public void setPrice(String price) {
		this.price = price;
	}


	public Boolean getSubscribed() {
		return subscribed;
	}


	public void setSubscribed(Boolean subscribed) {
		this.subscribed = subscribed;
	}


	public List<TierModule> getModules() {
		return modules;
	}


	public void setModules(List<TierModule> modules) {
		this.modules = modules;
	}


	public List<Feature> getFeatures() {
		return features;
	}


	public void setFeatures(List<Feature> features) {
		this.features = features;
	}
	
	
	

}
