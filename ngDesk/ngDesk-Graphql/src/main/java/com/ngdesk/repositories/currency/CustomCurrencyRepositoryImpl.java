package com.ngdesk.repositories.currency;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.currency.dao.Currency;

@Repository
public class CustomCurrencyRepositoryImpl implements CustomCurrencyRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Currency> findCurrencyById(String id, String collectionName) {

		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(id));

		return Optional.ofNullable(mongoOperations.findOne(query, Currency.class, collectionName));
	}

	@Override
	public Optional<List<Currency>> findAllCurrencies(Pageable pageable, String collectionName) {
		Query query = new Query();
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, Currency.class, collectionName));
	}
}
