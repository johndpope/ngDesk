package com.ngdesk.repositories.currency;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ngdesk.data.currency.dao.Currency;

public interface CustomCurrenyRepository {

	public void removeCurrencyById(String Id, String collectionName);

	public Optional<List<Currency>> findAllCurrencies(String collectionName);

	public Optional<Map<String, Object>> findTeamByName(String name, String collectionName);

}
