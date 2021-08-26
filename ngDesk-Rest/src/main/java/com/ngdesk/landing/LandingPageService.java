package com.ngdesk.landing;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.sns.model.InternalErrorException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;

@Component
@RestController
public class LandingPageService {

	private final Logger log = LoggerFactory.getLogger(LandingPageService.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@GetMapping("/marketing/conversions")
	public ResponseEntity<Object> getLandingPageDescriptions(HttpServletRequest request,
			@RequestParam("language") String language) {

		log.trace("Enter LandingPageService.getLandingPageDescriptions()");
		try {
			int landingPageDescriptionId = 3;
			HashMap<String, Integer> conversionOptions = new HashMap<String, Integer>();
			JSONObject optionsMap = new JSONObject();

			if (language == null || language.trim().length() == 0) {
				language = "en";
			}

			if (!language.equals("en") && !language.equals("es") && !language.equals("zh")) {
				language = "en";
			}

			MongoCollection<Document> descriptionCollection = mongoTemplate.getCollection("landing_page_descriptions");

			List<Document> descriptionDocuments = descriptionCollection
					.find(Filters.eq("LANDING_PAGE_DESCRIPTION_ID", landingPageDescriptionId))
					.into(new ArrayList<Document>());

			HashMap<String, Integer> pageDescription = new HashMap<String, Integer>();

			for (Document descriptionDocument : descriptionDocuments) {
				pageDescription.put(descriptionDocument.getString("OPTION_1_DESCRIPTION"), 1);
				pageDescription.put(descriptionDocument.getString("OPTION_2_DESCRIPTION"), 2);
				pageDescription.put(descriptionDocument.getString("OPTION_3_DESCRIPTION"), 3);
				pageDescription.put(descriptionDocument.getString("OPTION_4_DESCRIPTION"), 4);
			}

			MongoCollection<Document> optionsCollection = mongoTemplate
					.getCollection("landing_page_option_descriptions");

			int i = 1;

			for (String key : pageDescription.keySet()) {
				List<Document> optionDocuments = optionsCollection
						.find(Filters.and(Filters.eq("LANDING_PAGE_DESCRIPTION_ID", landingPageDescriptionId),
								Filters.eq("OPTION_ID", pageDescription.get(key)), Filters.eq("LANGUAGE", language)))
						.into(new ArrayList<Document>());

				int cumulativeWeight = 0;
				Random random = new Random();
				int rand = random.nextInt(101);

				for (Document optionDocument : optionDocuments) {

					cumulativeWeight += optionDocument.getInteger("WEIGHT");

					if (cumulativeWeight >= rand) {

						conversionOptions.put("OPTION_" + i,
								optionDocument.getInteger("LANDING_PAGE_OPTION_DESCRIPTION_ID"));
						i++;

						optionsMap.put(key.toUpperCase(), optionDocument.getString("VALUE"));
						break;
					}
				}
			}

			String browser = request.getHeader("user-agent");
			String referrer = request.getHeader("Referer");

			String ipAddress = request.getRemoteAddr().toString();
			String country = request.getLocale().getCountry();

			JSONObject conversion = new JSONObject();
			conversion.put("LANDING_PAGE_DESCRIPTION_ID", landingPageDescriptionId);
			conversion.put("REFERER", referrer);
			conversion.put("DATE_CREATED", global.getFormattedDate(new Timestamp(new Date().getTime())));
			conversion.put("TIME_SPENT_ON_PAGE", 0);
			conversion.put("IP_ADDRESS", ipAddress);
			conversion.put("BROWSER", browser);
			conversion.put("LANGUAGE", language);
			conversion.put("COUNTRY", country);
			conversion.put("CONVERTED", "N");
			conversion.put("OPTION_1", conversionOptions.get("OPTION_1"));
			conversion.put("OPTION_2", conversionOptions.get("OPTION_2"));
			conversion.put("OPTION_3", conversionOptions.get("OPTION_3"));
			conversion.put("OPTION_4", conversionOptions.get("OPTION_4"));

			MongoCollection<Document> conversionsCollection = mongoTemplate.getCollection("signup_conversions");

			Document document = Document.parse(conversion.toString());
			conversionsCollection.insertOne(document);

			optionsMap.put("CONVERSION_ID", document.getObjectId("_id"));
			log.trace("Exit LandingPageService.getLandingPageDescriptions()");
			return new ResponseEntity<>(optionsMap.toString(), global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/marketing/conversions")
	public ResponseEntity<Object> postConversionStatus(@RequestParam("converted") String converted,
			@RequestParam("conversion_id") String conversionId) {
		log.trace("Enter LandingPageService.postConversionStatus()");
		if (converted.equals("Y") || converted.equals("N")) {
			MongoCollection<Document> collection = mongoTemplate.getCollection("signup_conversions");

			if (new ObjectId().isValid(conversionId)) {
				Document conversion = collection.find(Filters.eq("_id", new ObjectId(conversionId))).first();

				if (conversion != null) {
					collection.updateOne(Filters.eq("_id", new ObjectId(conversionId)),
							Updates.set("CONVERTED", converted));
					log.trace("Exit LandingPageService.postConversionStatus()");
					return new ResponseEntity<>(HttpStatus.OK);
				} else {
					throw new BadRequestException("CONVERSION_RECORD_DOES_NOT_EXIST");
				}
			} else {
				throw new BadRequestException("INVALID_ENTRY_ID");
			}
		} else {
			throw new BadRequestException("INVALID_CONVERSION_VALUE");
		}
	}
}
