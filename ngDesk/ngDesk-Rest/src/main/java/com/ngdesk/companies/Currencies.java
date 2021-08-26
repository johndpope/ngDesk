package com.ngdesk.companies;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties("_class")
public class Currencies {

	@JsonProperty("CURRENCY_ID")
	private String currencyId;

	@JsonProperty("CURRENCY_NAME")
	@NotNull(message = "CURRENCY_NAME_NOT_NULL")
	private String currencyName;

	@JsonProperty("ISO_CODE")
	@NotNull(message = "ISO_CODE_NOT_NULL")
	@Size(max = 3, message = "ISO_CODE_MUST_BE_3_CHAR")
	@Pattern(regexp = "^[A-Z]{3}$", message = "ISO_CODE_MUST_BE_CHAR")
	private String isoCode;

	@JsonProperty("CURRENCY_SYMBOL")
	@NotNull(message = "CURRENCY_SYMBOL_NOT_NULL")
	@Size(max = 1, message = "CURRENCY_SYMBOL_MUST_BE_1_CHAR")
	private String currencySymbol;

	@JsonProperty("CONVERTION_RATE")
	@NotNull(message = "CONVERTION_RATE_NOT_NULL")
	@Pattern(regexp = "^[0-9]+([.][0-9]+)?$", message = "CONVERTION_RATE_MUST_BE_INT")
	private String convertionRate;

	@JsonProperty("STATUS")
	@Pattern(regexp = "^(Active|Inactive)$", message = "STATUS_MUST_BE_ACTIVE_OR_INACTIVE")
	private String status;

	public Currencies() {
	}

	public Currencies(String currencyId, String currencyName,
			@NotNull(message = "ISO_CODE_NOT_NULL") @Size(max = 3, message = "ISO_CODE_MUST_BE_3_CHAR") @Pattern(regexp = "^[A-Z]{3}$", message = "ISO_CODE_MUST_BE_CHAR") String isoCode,
			@NotNull(message = "CURRENCY_SYMBOL_NOT_NULL") @Size(max = 1, message = "CURRENCY_SYMBOL_MUST_BE_1_CHAR") String currencySymbol,
			@NotNull(message = "CONVERTION_RATE_NOT_NULL") @Pattern(regexp = "^[0-9]+([.][0-9]+)?$", message = "CONVERTION_RATE_MUST_BE_INT") String convertionRate,
			@Pattern(regexp = "^(Active|Inactive)$", message = "STATUS_MUST_BE_ACTIVE_OR_INACTIVE") String status) {
		super();
		this.currencyId = currencyId;
		this.currencyName = currencyName;
		this.isoCode = isoCode;
		this.currencySymbol = currencySymbol;
		this.convertionRate = convertionRate;
		this.status = status;
	}

	public String getCurrencyId() {
		return currencyId;
	}

	public void setCurrencyId(String currencyId) {
		this.currencyId = currencyId;
	}

	public String getCurrencyName() {
		return currencyName;
	}

	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}

	public String getIsoCode() {
		return isoCode;
	}

	public void setIsoCode(String isoCode) {
		this.isoCode = isoCode;
	}

	public String getCurrencySymbol() {
		return currencySymbol;
	}

	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	public String getConvertionRate() {
		return convertionRate;
	}

	public void setConvertionRate(String convertionRate) {
		this.convertionRate = convertionRate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
