package com.ngdesk.data.currency.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.repositories.currency.CurrencyConvertRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class ConversionAPI {

	@Autowired
	CurrencyConvertRepository currencyConvertRepository;

	@Autowired
	ModulesRepository moduleRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	Global global;
	
	@Value("${currency.access.key}")
	private String currencyAccessKey;
	
	@Value("${currency.url}")
	private String currencyBaseUrl;
	
	@Value("${currency.end.point}")
	private String currencyEndPoint;

	static CloseableHttpClient httpClient = HttpClients.createDefault();

	@PostMapping("modules/{module_id}/{field_id}/currency_convert")
	public String postCurrencyConversion(
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
			@Parameter(description = "Field ID", required = true) @PathVariable("field_id") String fieldId,
			@RequestBody HashMap<String, Object> entry) {
		Module module = moduleRepository.findById(moduleId, "modules_" + authManager.getUserDetails().getCompanyId())
				.orElse(null);
		if (module == null) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		ModuleField currencyField = module.getFields().stream().filter(field -> field.getFieldId().equals(fieldId))
				.findFirst().orElse(null);

		if (currencyField == null) {
			throw new BadRequestException("INVALID_FIELD_ID", null);
		}

		ModuleField toCurrencyField = module.getFields().stream()
				.filter(field -> field.getFieldId().equals(currencyField.getToCurrency())).findFirst().orElse(null);

		ModuleField fromCurrencyField = module.getFields().stream()
				.filter(field -> field.getFieldId().equals(currencyField.getFromCurrency())).findFirst().orElse(null);

		ModuleField dateIncurredField = module.getFields().stream()
				.filter(field -> field.getFieldId().equals(currencyField.getDateIncurred())).findFirst().orElse(null);

		Map<String, Object> result = new HashMap<String, Object>();
		if (entry.get(fromCurrencyField.getName()) == null || entry.get(toCurrencyField.getName()) == null) {
			return "0.0";
		}
		String fromCurrency = entry.get(fromCurrencyField.getName()).toString();
		String toCurrency = entry.get(toCurrencyField.getName()).toString();
		String fromCurrencyIsoCode = global.getIsoCode(fromCurrency);
		String toCurrencyIsoCode = global.getIsoCode(toCurrency);
		String date = "";
		Double exchangeRate = 0.0;
		String formatedDate = null;
		Date convertedDate = null;

		date = getDateIncurred(dateIncurredField, entry);
		formatedDate = convertDate(date);

		convertedDate = formatDate(date);

		Optional<Currencies> optionalCurrencyConversion = currencyConvertRepository.findExchangeRateByDate(fromCurrency,
				toCurrency, convertedDate);

		if (!optionalCurrencyConversion.isEmpty()) {

			Currencies CurrencyConversion = optionalCurrencyConversion.get();
			exchangeRate = CurrencyConversion.getExchangeRate();
			return exchangeRate.toString();

		} else {

			result = sendRequest(fromCurrencyIsoCode, toCurrencyIsoCode, formatedDate);
			if (result != null) {
				boolean isApiRequestSucess = (boolean) result.get("success");

				if (isApiRequestSucess) {
					exchangeRate = Double.valueOf(((Map<String, Object>) result.get("info")).get("quote").toString());
					if (!exchangeRate.toString().isEmpty()) {
						Currencies convertedCurrency = new Currencies(fromCurrency, toCurrency, null, null,
								exchangeRate, convertedDate);
						currencyConvertRepository.save(convertedCurrency, "currency_conversion_table");
					}
				}
				return exchangeRate.toString();
			}
			return "0.0";
		}
	}

	private Map<String, Object> sendRequest(String fromCurrency, String toCurrency, String date) {

		try {
			String url = currencyBaseUrl + currencyEndPoint + "?access_key=" + currencyAccessKey
					+ ("&from=" + fromCurrency + "&to=" + toCurrency + "&amount=1" + "&date=" + date);

			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders httpHeaders = new HttpHeaders();

			HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(httpHeaders);
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity,
					(Class<Map<String, Object>>) (Class) Map.class);
			return response.getBody();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	public Date formatDate(String date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Date formatedDate = null;
		try {
			formatedDate = dateFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new BadRequestException("INVALID_DATE_FORMAT", null);

		}
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTime(formatedDate);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		formatedDate = cal.getTime();
		return formatedDate;
	}

	public String getDateIncurred(ModuleField dateIncurredField, HashMap<String, Object> entry) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		String date = null;
		if (entry.get(dateIncurredField.getName()) != null) {
			date = entry.get(dateIncurredField.getName()).toString();
		} else {
			date = dateFormat.format(new Date());
		}
		return date;
	}

	public String convertDate(String date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		SimpleDateFormat requiredDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return requiredDateFormat.format(dateFormat.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
			throw new BadRequestException("INVALID_DATE_FORMAT", null);
		}
	}

}
