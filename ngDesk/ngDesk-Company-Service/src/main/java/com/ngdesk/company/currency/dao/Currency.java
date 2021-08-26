package com.ngdesk.company.currency.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Currency {

	@JsonProperty("ISO_CODE")
	@Field("ISO_CODE")
	private String isoCode;

	@JsonProperty("CURRENCY_NAME")
	@Field("CURRENCY_NAME")
	private String currencyName;

	@JsonProperty("CURRENCY_SYMBOL")
	@Field("CURRENCY_SYMBOL")
	private String currencySymbol;

	@JsonProperty("CONVERTION_RATE")
	@Field("CONVERTION_RATE")
	private String conversionRate;

	@JsonProperty("STATUS")
	@Field("STATUS")
	private String status;

	public Currency(String isoCode, String currencyName, String currencySymbol, String conversionRate, String status) {
		super();
		this.isoCode = isoCode;
		this.currencyName = currencyName;
		this.currencySymbol = currencySymbol;
		this.conversionRate = conversionRate;
		this.status = status;
	}

	public Currency() {
		super();
	}

	public String getIsoCode() {
		return isoCode;
	}

	public void setIsoCode(String isoCode) {
		this.isoCode = isoCode;
	}

	public String getCurrencyName() {
		return currencyName;
	}

	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}

	public String getCurrencySymbol() {
		return currencySymbol;
	}

	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	public String getConversionRate() {
		return conversionRate;
	}

	public void setConversionRate(String conversionRate) {
		this.conversionRate = conversionRate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
