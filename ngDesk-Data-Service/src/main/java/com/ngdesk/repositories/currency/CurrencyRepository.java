package com.ngdesk.repositories.currency;

import com.ngdesk.data.currency.dao.Currency;
import com.ngdesk.repositories.CustomNgdeskRepository;

public interface CurrencyRepository extends CustomCurrenyRepository, CustomNgdeskRepository<Currency, String> {

}
