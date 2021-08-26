package com.ngdesk.repositories.currency;

import java.util.Date;
import java.util.Optional;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.data.currency.dao.Currencies;

public class CustomCurrencyConvertRepositoryImpl implements CustomCurrencyConvertRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Currencies> findExchangeRateByDate(String fromCurrency, String toCurrency, Date transactionDate) {

		Asserts.notNull(fromCurrency, "The given fromCurrency must not be null!");
		Asserts.notNull(toCurrency, "The given toCurrency must not be null!");
		Asserts.notNull(transactionDate, "The given transactionDate must not be null!");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("transactionDate").is(transactionDate),
				Criteria.where("fromCurrency").is(fromCurrency), Criteria.where("toCurrency").is(toCurrency));
		Query query = new Query(criteria);

		return Optional.ofNullable(mongoOperations.findOne(query, Currencies.class, "currency_conversion_table"));
	}

}
