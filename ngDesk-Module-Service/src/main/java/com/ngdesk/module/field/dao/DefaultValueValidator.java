package com.ngdesk.module.field.dao;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.dao.Phone;

@Service
public class DefaultValueValidator {
	public void isValidDefaultValue(ModuleField field, Module module) {

		if (field.getDefaultValue() != null) {

			switch (field.getDataType().getDisplay()) {
			case "Email":
				isValidEmail(field.getDefaultValue());
				break;
			case "Number":
				isValidNumber(field);
				break;
			case "Percent":
			case "Currency":
				isValidDoubleValue(field);
				break;
			case "Phone":
				isValidPhoneValue(field);
				break;
			case "URL":
				isValidUrl(field);
				break;
			case "Date":
			case "Date/Time":
			case "Time":
				isValidDateTimeValue(field);
				break;
			case "Picklist":
				isValidPicklistValue(field);
				break;
			default:
				break;
			}
		}
	}

	private void isValidPicklistValue(ModuleField field) {
		if (!field.getPicklistValues().contains(field.getDefaultValue())) {
			throw new BadRequestException("DEFAULT_PICKLIST_VALUE_INVALID", null);
		}
	}

	private void isValidUrl(ModuleField field) {
		String[] schemes = { "http", "https" }; // DEFAULT schemes = "http", "https", "ftp"
		UrlValidator urlValidator = new UrlValidator(schemes);
		String value = field.getDefaultValue();
		if (!urlValidator.isValid(value)) {
			throw new BadRequestException("INVALID_DEFAULT_URL", null);
		}
	}

	private void isValidDoubleValue(ModuleField field) {
		Double defaultValue = Double.parseDouble(field.getDefaultValue());
		if (BigDecimal.valueOf(defaultValue).scale() > 3) {
			String[] vars = { field.getDataType().getDisplay() };
			throw new BadRequestException("INVALID_DEFAULT_FIELD", vars);
		}
	}

	private void isValidEmail(String email) {
		if (!EmailValidator.getInstance().isValid(email)) {
			throw new BadRequestException("INVALID_DEFAULT_EMAIL", null);
		}
	}

	private void isValidDateTimeValue(ModuleField field) {
		try {
			String dateString = field.getDefaultValue();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSX");
			df.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
			String[] vars = { field.getDataType().getDisplay() };
			throw new BadRequestException("DEFAULT_DATE_TIME_INVALID", vars);
		}
	}

	private void isValidNumber(ModuleField field) {
		try {
			int defaultValue = Integer.parseInt(field.getDefaultValue());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			String[] vars = { field.getDataType().getDisplay() };
			throw new BadRequestException("INVALID_DEFAULT_FIELD", vars);
		}
	}

	private void isValidPhoneValue(ModuleField field) {
		ObjectMapper mapper = new ObjectMapper();

		PhoneNumberUtil phoneInstance = PhoneNumberUtil.getInstance();
		try {
			Phone phone = mapper.readValue(field.getDefaultValue(), Phone.class);
			if (!phone.getDialCode().isBlank() && !phone.getPhoneNumber().isBlank()) {
				String phoneString = phone.getDialCode() + phone.getPhoneNumber();
				try {
					PhoneNumber phoneNumber = phoneInstance.parse(phoneString, null);
					if (!phoneInstance.isValidNumber(phoneNumber)) {
						throw new BadRequestException("INVALID_DEFAULT_PHONE_NUMBER", null);
					}
				} catch (NumberParseException e) {
					e.printStackTrace();
					throw new BadRequestException("INVALID_DEFAULT_PHONE_NUMBER", null);
				}
			} else if (field.getRequired()) {
				String[] vars = { "DEFAULT_VALUE" };
				throw new BadRequestException("DAO_VARIABLE_REQUIRED", vars);
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new BadRequestException("INVALID_DEFAULT_PHONE_VALUE", null);
		}
	}

	private int getChronometerValueInMinutes(String input) {
		try {
			Pattern periodPattern = Pattern.compile("(\\d+)(mo|w|d|m|h)");
			Matcher matcher = periodPattern.matcher(input);
			int chronometerValueInSecond = 0;
			while (matcher.find()) {
				int num = Integer.parseInt(matcher.group(1));
				String typ = matcher.group(2);
				switch (typ) {
				case "mo":
					chronometerValueInSecond = num * 9600;
					break;
				case "w":
					chronometerValueInSecond = chronometerValueInSecond + (num * 2400);
					break;
				case "d":
					chronometerValueInSecond = chronometerValueInSecond + (num * 480);
					break;
				case "h":
					chronometerValueInSecond = chronometerValueInSecond + (num * 60);
					break;
				case "m":
					chronometerValueInSecond = chronometerValueInSecond + num;
					break;

				}
			}
			return chronometerValueInSecond;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
