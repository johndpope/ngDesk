package com.ngdesk.tracking;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;

@RestController
@Component
public class EmailTrackingService {

	private Logger log = LoggerFactory.getLogger(EmailTrackingService.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@GetMapping(value = "getImage/{campaign_day}/{campaign_id}/{user_uuid}", produces = MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody byte[] getImage(@PathVariable("user_uuid") String userUUID,
			@PathVariable("campaign_day") String campaignDay, @PathVariable("campaign_id") String campaignId,
			HttpServletRequest request) {
		try {
			log.trace("Enter EmailTrackingService.getImage(): campaignId:" + campaignId + " userUUID:" + userUUID
					+ "Day: " + campaignDay);
			if (campaignId != null && new ObjectId().isValid(campaignId)) {
				MongoCollection<Document> dripCampaignCollection = mongoTemplate.getCollection("drip_campaigns");

				// GET ONLY SINGLE RECORD FROM ARRAY
				Document emailRecord = dripCampaignCollection.find(Filters.eq("_id", new ObjectId(campaignId)))
						.projection((Projections.elemMatch("CAMPAIGN_DAYS_" + campaignDay,
								Filters.eq("USER_UUID", userUUID))))
						.first();

				// CAMPAIGN DAYS VALUES 1,3,5,7,10
				if (emailRecord != null && emailRecord.containsKey("CAMPAIGN_DAYS_" + campaignDay)) {
					Document emailDoc = ((List<Document>) emailRecord.get("CAMPAIGN_DAYS_" + campaignDay)).get(0);

					emailDoc.put("EMAIL_OPENED", true);
					dripCampaignCollection.updateOne(Filters.eq("_id", new ObjectId(campaignId)),
							Updates.pull("CAMPAIGN_DAYS_" + campaignDay, Filters.eq("USER_UUID", userUUID)));

					dripCampaignCollection.updateOne(Filters.eq("_id", new ObjectId(campaignId)),
							Updates.addToSet("CAMPAIGN_DAYS_" + campaignDay, emailDoc));
				}
			}

			// FOR GETTING FILE IN WINDOWS
			String operatingSystem = System.getProperty("os.name");
			Resource res = null;
			if (operatingSystem.equalsIgnoreCase("Windows 10")) {
				res = new ClassPathResource("165X41-0172-dpi-black-01.png");
			} else {
				res = new ClassPathResource("classpath:165X41-0172-dpi-black-01.png");
			}
			InputStream in;
			in = res.getInputStream();
			return IOUtils.toByteArray(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.trace("Exit EmailTrackingService.getImage(): campaignId:" + campaignId + " userUUID:" + userUUID + "Day: "
				+ campaignDay);
		return null;
	}
}
