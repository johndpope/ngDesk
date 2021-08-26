package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import com.ngdesk.company.currency.dao.Currency;

public interface CustomCurrencyRepository {

	Optional<List<Currency>> findAllCurrencies(String collectionName);

	public void saveAllCurrencies(List<Currency> currencies, String collectionName);
}
