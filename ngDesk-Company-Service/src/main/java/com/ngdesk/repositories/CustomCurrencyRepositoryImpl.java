package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.company.currency.dao.Currency;

public class CustomCurrencyRepositoryImpl implements CustomCurrencyRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<List<Currency>> findAllCurrencies(String collectionName) {

		return Optional.ofNullable(mongoOperations.find(new Query(), Currency.class, collectionName));
	}

	@Override
	public void saveAllCurrencies(List<Currency> currencies, String collectionName) {

		Optional.ofNullable(mongoOperations.insert(currencies, collectionName));
	}

}
