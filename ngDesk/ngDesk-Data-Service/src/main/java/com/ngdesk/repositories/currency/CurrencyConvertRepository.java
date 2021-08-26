package com.ngdesk.repositories.currency;

import com.ngdesk.data.currency.dao.Currencies;
import com.ngdesk.repositories.CustomNgdeskRepository;

public interface CurrencyConvertRepository
		extends CustomNgdeskRepository<Currencies, String>, CustomCurrencyConvertRepository {

}
