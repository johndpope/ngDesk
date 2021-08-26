package com.ngdesk.repositories.currency;

import java.util.Date;
import java.util.Optional;

import com.ngdesk.data.currency.dao.Currencies;

public interface CustomCurrencyConvertRepository {

	public Optional<Currencies> findExchangeRateByDate(String fromCurrency, String toCurrency, Date date);
}
