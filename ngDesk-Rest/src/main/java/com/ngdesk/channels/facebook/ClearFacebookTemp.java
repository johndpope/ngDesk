package com.ngdesk.channels.facebook;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

@Component
public class ClearFacebookTemp {

	private final Logger log = LoggerFactory.getLogger(ClearFacebookTemp.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Scheduled(fixedRate = 60000)
	public void clearTempCollection() {
		try {
			log.trace("Enter job ClearFacebookTemp.clearTempCollection()");

			MongoCollection<Document> facebookTempCollection = mongoTemplate.getCollection("facebook_temp_collection");

			List<Document> facebookTempDocument = facebookTempCollection.find().into(new ArrayList());

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

			for (Document tempDocument : facebookTempDocument) {

				Date date = dateFormat.parse(tempDocument.getString("DATE_CREATED"));
				// CALCULATE DATE AFTER 30 MINS OF CREATED DATE
				Date dateAfter30Minutes = new Date(date.getTime() + (30 * 60000));
				Date currentDate = new Date(new Date().getTime());

				if (currentDate.after(dateAfter30Minutes)) {
					facebookTempCollection.deleteOne(Filters.eq("_id", tempDocument.getObjectId("_id")));
				}
			}
			log.trace("Exit job ClearFacebookTemp.clearTempCollection()");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
