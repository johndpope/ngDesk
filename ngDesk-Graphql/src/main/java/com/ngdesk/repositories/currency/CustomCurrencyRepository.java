package com.ngdesk.repositories.currency;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.currency.dao.Currency;

@Repository
public interface CustomCurrencyRepository {

	public Optional<Currency> findCurrencyById(String id, String collectionName);

	public Optional<List<Currency>> findAllCurrencies(Pageable pageable, String collectionName);

}
