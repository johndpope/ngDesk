package com.ngdesk.company.currency.dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.repositories.CurrencyRepository;

@Component
public class CurrencyService {

	@Autowired
	CurrencyRepository currencyRepository;

	@Autowired
	Global global;

	public void postCurrency(String companyId) {
		try {
			String currenciesJson = global.getFile("currencies.json");
			ObjectMapper mapper = new ObjectMapper();
			Currency curreny = mapper.readValue(currenciesJson, Currency.class);
			currencyRepository.save(curreny, "currencies_" + companyId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("CURRENCIES_POST_FAILED", null);
		}

	}
}
