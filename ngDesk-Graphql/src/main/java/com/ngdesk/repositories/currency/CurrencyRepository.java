package com.ngdesk.repositories.currency;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.currency.dao.Currency;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface CurrencyRepository extends CustomCurrencyRepository, CustomNgdeskRepository<Currency, String> {

}
