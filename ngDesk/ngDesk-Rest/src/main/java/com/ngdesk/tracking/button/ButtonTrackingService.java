package com.ngdesk.tracking.button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.campaigns.Campaigns;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.InternalErrorException;

@RestController
public class ButtonTrackingService {
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	private final Logger log = LoggerFactory.getLogger(ButtonTrackingService.class);
	
	@PutMapping("/companies/campaign/{campaign_id}/tracking/button/{button_id}")
	public ResponseEntity<Object> updateCampaignButtonTracking(HttpServletRequest request,
			@PathVariable("campaign_id") String campaignId,
			@PathVariable("button_id") String buttonId,
			@RequestParam(value = "email_address", required = false) String email,
			@RequestParam(value = "user_company_id", required = false) String userCompanyId,
			@RequestParam(value = "company_id", required = false) String companyId,
			@RequestParam(value = "link", required = false) String link) {
		try {
			log.trace("Enter ButtonTrackingService.updateCampaignButtonTracking()");
			
			int totalClicks = 0;
			int clicksByUser = 0;
			if (campaignId != null) {
				MongoCollection<Document> campaignsCollection = mongoTemplate.getCollection("campaigns_" + companyId);
				Document campaignDoc = campaignsCollection.find(Filters.eq("_id", new ObjectId(campaignId))).first();
				campaignDoc.remove("_id");
				campaignDoc.put("CAMPAIGN_ID", campaignId);
				Campaigns campaign = new ObjectMapper().readValue(campaignDoc.toJson(), Campaigns.class);
				
				List<ButtonClick> buttonClicksList = campaign.getButtonClicks();
				
				for (ButtonClick buttonClickItem: buttonClicksList) {
					if (buttonClickItem.getButtonId().equals(buttonId)) {
						totalClicks = buttonClickItem.getTotalClicks();
						buttonClickItem.setTotalClicks(totalClicks + 1);
						List<ClickedBy> userClicksList = new ArrayList<ClickedBy>();
						Boolean userFound = false;
						if (buttonClickItem.getClickedBy().size() > 0) {
							userClicksList = buttonClickItem.getClickedBy();
							for (ClickedBy clickedByUser: userClicksList) {
								if (clickedByUser.getEmailAddress().equals(email) && clickedByUser.getCompanyId().equals(userCompanyId)) {
									userFound = true;
									clicksByUser = clickedByUser.getClicks();
									clickedByUser.setClicks(clicksByUser + 1);
									break;
								}
							}
						}
						
						if (userFound.equals(false)) {
							ClickedBy clickedByUser = new ClickedBy();
							clickedByUser.setClicks(clicksByUser + 1);
							clickedByUser.setCompanyId(userCompanyId);
							clickedByUser.setEmailAddress(email);
							userClicksList.add(clickedByUser);
						}
						buttonClickItem.setClickedBy(userClicksList);
					}
				}
				
				campaign.setButtonClicks(buttonClicksList);
				
				String json = new ObjectMapper().writeValueAsString(campaign);
				Document updateCampaign = Document.parse(json);
				campaignsCollection.findOneAndReplace(Filters.eq("_id", new ObjectId(campaignId)), updateCampaign);
			} else {
				throw new BadRequestException("CAMPAIGN_NOT_FOUND");
			}
			

			log.trace("Exit ButtonTrackingService.updateCampaignButtonTracking()");
			return new ResponseEntity<>(Global.postHeaders, HttpStatus.OK);
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

}
