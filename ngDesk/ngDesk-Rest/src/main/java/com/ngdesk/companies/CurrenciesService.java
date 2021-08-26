package com.ngdesk.companies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.resources.MongoUtils;
import com.ngdesk.roles.RoleService;

@RestController
public class CurrenciesService {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(CurrenciesService.class);

	@GetMapping("/companies/currencies")
	public ResponseEntity<Object> getCurrencies(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order,
			@RequestParam(value = "search", required = false) String search) {
		try {
			
			log.trace("Enter CurrenciesService.getCurrencies()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			JSONObject resultObject = new JSONObject();
			JSONArray currencies = new JSONArray();
			int pgSize = 100;
			int pg = 1;
			int skip = 0;
			Document filter = MongoUtils.createFilter(search);
			MongoCollection<Document> currenciesCollection = mongoTemplate.getCollection("currencies_" + companyId);
			ArrayList<Document> currencyDocuments = new ArrayList<Document>();
			
			if (pageSize != null && page != null) {
				pgSize = Integer.valueOf(pageSize);
				pg = Integer.valueOf(page);

				if (pgSize <= 0) {
					throw new BadRequestException("INVALID_PAGE_SIZE");
				} else if (pg <= 0) {
					throw new BadRequestException("INVALID_PAGE_NUMBER");
				} else {
					skip = (pg - 1) * pgSize;
				}
			}

			if (sort != null && order != null) {
				if (order.equalsIgnoreCase("asc")) {
					currencyDocuments = currenciesCollection.find(filter).sort(Sorts.orderBy(Sorts.ascending(sort)))
							.skip(skip).limit(pgSize).into(new ArrayList<Document>());
				} else if (order.equalsIgnoreCase("desc")) {
					currencyDocuments = currenciesCollection.find(filter).sort(Sorts.orderBy(Sorts.descending(sort)))
							.skip(skip).limit(pgSize).into(new ArrayList<Document>());
				} else {
					throw new BadRequestException("INVALID_SORT_ORDER");
				}

			} else {
				currencyDocuments = currenciesCollection.find(filter).skip(skip).limit(pgSize).into(new ArrayList<Document>());
			}

			for (Document currency : currencyDocuments) {
				String currencyId = currency.remove("_id").toString();
				currency.put("CURRENCY_ID", currencyId);
				currencies.put(currency);
			}
			resultObject.put("CURRENCIES", currencies);
			resultObject.put("TOTAL_RECORDS", currenciesCollection.countDocuments());
			log.trace("Exit CurrenciesService.getCurrencies()");
			return new ResponseEntity<>(resultObject.toString(), Global.postHeaders, HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/companies/currency/{id}")
	public Currencies getCurrency(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("id") String id) {
		try {
			
			log.trace("Enter CurrenciesService.getCurrency()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(id)) {
				throw new BadRequestException("CURRENCY_NOT_FOUND");
			}
			MongoCollection<Document> currenciesCollection = mongoTemplate.getCollection("currencies_" + companyId);
			Document currency = currenciesCollection.find(Filters.eq("_id", new ObjectId(id))).first();
			if (currency == null) {
				throw new BadRequestException("CURRENCY_NOT_FOUND");
			}
			String currencyId = currency.remove("_id").toString();
			currency.put("CURRENCY_ID", currencyId);
			Currencies currencyObject = new ObjectMapper().readValue(currency.toJson(), Currencies.class);
			log.trace("Exit CurrenciesService.getCurrency()");

			return currencyObject;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/companies/currency")
	public Currencies postCurrency(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestBody @Valid Currencies currencies) {
		try {
			log.trace("Enter CurrenciesService.postCurrency()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> currenciesCollection = mongoTemplate.getCollection("currencies_" + companyId);
			Document existingCurrency = currenciesCollection.find(Filters.eq("ISO_CODE", currencies.getIsoCode()))
					.first();
			if (existingCurrency != null) {
				throw new BadRequestException("ISO_CODE_ALREADY_EXISTS");
			}
			String json = new ObjectMapper().writeValueAsString(currencies);
			Document currency = Document.parse(json);
			currenciesCollection.insertOne(currency);
			currencies.setCurrencyId(currency.getObjectId("_id").toString());
			log.trace("Exit CurrenciesService.postCurrency()");
			return currencies;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/companies/currency/{id}")
	public Currencies putCurrency(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid, @PathVariable("id") String id,
			@RequestBody @Valid Currencies currencies) {
		try {
			log.trace("Enter CurrenciesService.putCurrency()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(id)) {
				throw new BadRequestException("CURRENCY_NOT_FOUND");
			}
			MongoCollection<Document> currenciesCollection = mongoTemplate.getCollection("currencies_" + companyId);
			Document existingCurrency = currenciesCollection.find(Filters.eq("_id", new ObjectId(id))).first();
			if (existingCurrency == null) {
				throw new BadRequestException("CURRENCY_NOT_FOUND");
			}

			if (!existingCurrency.getString("ISO_CODE").equalsIgnoreCase(currencies.getIsoCode())) {
				Document uniqueCurrency = currenciesCollection.find(Filters
						.and(Filters.eq("ISO_CODE", currencies.getIsoCode()), Filters.ne("_id", new ObjectId(id))))
						.first();
				if (uniqueCurrency != null) {
					throw new BadRequestException("ISO_CODE_ALREADY_EXISTS");
				}
			}

			String json = new ObjectMapper().writeValueAsString(currencies);
			Document updateCurrency = Document.parse(json);
			currenciesCollection.findOneAndReplace(Filters.eq("_id", new ObjectId(id)), updateCurrency);
			log.trace("Exit CurrenciesService.putCurrency()");

			return currencies;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");

	}
}
