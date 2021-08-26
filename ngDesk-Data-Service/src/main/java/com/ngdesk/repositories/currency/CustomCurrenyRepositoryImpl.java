package com.ngdesk.repositories.currency;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.data.currency.dao.Currency;

public class CustomCurrenyRepositoryImpl implements CustomCurrenyRepository {
	@Autowired
	MongoOperations mongoOperations;

	@Override
	public void removeCurrencyById(String Id, String collectionName) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(Id));
		mongoOperations.remove(query, collectionName);
	}

	@Override
	public Optional<List<Currency>> findAllCurrencies(String collectionName) {

		return Optional.ofNullable(mongoOperations.find(new Query(), Currency.class, collectionName));
	}

	@Override
	public Optional<Map<String, Object>> findTeamByName(String name, String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("NAME").is(name));
		Query query = new Query(criteria);

		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

}
