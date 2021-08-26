package com.ngdesk.data.currency.dao;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

public class Currencies {

	@JsonProperty("FROM_CURRENCY")
	@CustomNotEmpty(message = "FROM_CURRENCY_NOT_EMPTY", values = { "FROM_CURRENCY" })
	private String fromCurrency;

	@JsonProperty("TO_CURRENCY")
	@CustomNotEmpty(message = "TO_CURRENCY_NOT_EMPTY", values = { "TO_CURRENCY" })
	private String toCurrency;

	@JsonProperty("BASE_VALUE")
	@CustomNotNull(message = "BASE_VALUE_NOT_NULL", values = { "BASE_VALUE" })
	private Double baseValue;

	@JsonProperty("CONVERTED_VALUE")
	private Double convertedValue;

	@JsonProperty("EXCHANGE_RATE")
	private Double exchangeRate;

	@JsonProperty("TRANSACTION_DATE")
	private Date transactionDate;

	public Currencies() {

	}

	public Currencies(String fromCurrency, String toCurrency, Double baseValue, Double convertedValue,
			Double exchangeRate, Date transactionDate) {
		super();
		this.fromCurrency = fromCurrency;
		this.toCurrency = toCurrency;
		this.baseValue = baseValue;
		this.convertedValue = convertedValue;
		this.exchangeRate = exchangeRate;
		this.transactionDate = transactionDate;
	}

	public String getFromCurrency() {
		return fromCurrency;
	}

	public void setFromCurrency(String fromCurrency) {
		this.fromCurrency = fromCurrency;
	}

	public String getToCurrency() {
		return toCurrency;
	}

	public void setToCurrency(String toCurrency) {
		this.toCurrency = toCurrency;
	}

	public Double getBaseValue() {
		return baseValue;
	}

	public void setBaseValue(Double baseValue) {
		this.baseValue = baseValue;
	}

	public Double getConvertedValue() {
		return convertedValue;
	}

	public void setConvertedValue(Double convertedValue) {
		this.convertedValue = convertedValue;
	}

	public Double getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(Double exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

}