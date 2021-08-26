package com.ngdesk.campaigns;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.companies.GalleryImage;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.resources.MongoUtils;
import com.ngdesk.roles.RoleService;
import com.ngdesk.tracking.button.ButtonClick;
import com.ngdesk.tracking.button.ClickedBy;

@RestController
public class CampaignsService {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(CampaignsService.class);

	@GetMapping("/companies/campaigns")
	public ResponseEntity<Object> getCampaigns(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		int totalSize = 0;
		JSONObject resultObject = new JSONObject();
		JSONArray campaigns = new JSONArray();

		try {
			log.trace("Enter CampaignsService.getCampaigns()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> campaignsCollection = mongoTemplate.getCollection("campaigns_" + companyId);

			int lowerLimit = 0;
			int pgSize = 100;
			int pg = 1;
			int skip = 0;
			totalSize = (int) campaignsCollection.countDocuments();

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

			List<Document> campaignsDoc = null;
			Document filter = MongoUtils.createFilter(search);

			if (sort != null && order != null) {

				if (order.equalsIgnoreCase("asc")) {
					campaignsDoc = (List<Document>) campaignsCollection.find(filter)
							.sort(Sorts.orderBy(Sorts.ascending(sort))).skip(skip).limit(pgSize)
							.into(new ArrayList<Document>());
				} else if (order.equalsIgnoreCase("desc")) {
					campaignsDoc = (List<Document>) campaignsCollection.find(filter)
							.sort(Sorts.orderBy(Sorts.descending(sort))).skip(skip).limit(pgSize)
							.into(new ArrayList<Document>());
				} else {
					throw new BadRequestException("INVALID_SORT_ORDER");
				}

			} else {
				campaignsDoc = (List<Document>) campaignsCollection.find(filter).skip(skip).limit(pgSize)
						.into(new ArrayList<Document>());
			}

			for (Document campaign : campaignsDoc) {
				String campaignId = campaign.remove("_id").toString();
				campaign.put("CAMPAIGN_ID", campaignId);
				campaigns.put(campaign);
			}
			resultObject.put("CAMPAIGNS", campaigns);
			resultObject.put("TOTAL_RECORDS", campaignsCollection.countDocuments());
			log.trace("Exit CampaignsService.getCampaigns()");

			return new ResponseEntity<>(resultObject.toString(), Global.postHeaders, HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/companies/campaign/{id}")
	public Campaigns getCampaign(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("id") String id) {
		try {
			log.trace("Enter CampaignsService.getCampaign()");
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
				throw new BadRequestException("CAMPAIGN_NOT_FOUND");
			}

			MongoCollection<Document> campaignsCollection = mongoTemplate.getCollection("campaigns_" + companyId);
			Document campaign = campaignsCollection.find(Filters.eq("_id", new ObjectId(id))).first();
			if (campaign == null) {
				throw new BadRequestException("CAMPAIGN_NOT_FOUND");
			}
			String campaignId = campaign.remove("_id").toString();
			campaign.put("CAMPAIGN_ID", campaignId);
			Campaigns campaignObject = new ObjectMapper().readValue(campaign.toJson(), Campaigns.class);
			log.trace("Exit CampaignsService.getCampaign()");

			return campaignObject;
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

	@PostMapping("/companies/campaign/{name}")
	public Campaigns postCampaign(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String name, @RequestBody @Valid Campaigns campaigns) {
		try {
			log.trace("Enter CampaignsService.postCampaign()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			List<CampaignTracking> tracking = new ArrayList<CampaignTracking>();
			List<ButtonClick> buttonClickTracking = setButtonTracking(campaigns);

			campaigns.setDateCreated(new Timestamp(new Date().getTime()));
			campaigns.setDateUpdated(new Timestamp(new Date().getTime()));
			campaigns.setCreatedBy(userId);
			campaigns.setLastUpdatedBy(userId);
			campaigns.setTracking(tracking);
			campaigns.setButtonClicks(buttonClickTracking);

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> campaignsCollection = mongoTemplate.getCollection("campaigns_" + companyId);
			String collectionName = "campaigns_" + companyId;

			if (global.isExists("NAME", name, collectionName)) {
				throw new BadRequestException("CAMPAIGN_ALREADY_EXISTS");
			}

			String json = new ObjectMapper().writeValueAsString(campaigns);
			Document campaign = Document.parse(json);
			campaignsCollection.insertOne(campaign);
			campaigns.setCampaignId(campaign.getObjectId("_id").toString());

			log.trace("Exit CampaignsService.postCampaign()");
			return campaigns;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/companies/campaign/{id}")
	public Campaigns putCampaign(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid, @PathVariable("id") String id,
			@RequestBody @Valid Campaigns campaigns) {
		try {
			log.trace("Enter CampaignsService.putCampaign()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			List<ButtonClick> buttonClicks = setButtonTracking(campaigns);
			campaigns.setButtonClicks(buttonClicks);

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(id)) {
				throw new BadRequestException("CAMPAIGN_NOT_FOUND");
			}
			MongoCollection<Document> campaignsCollection = mongoTemplate.getCollection("campaigns_" + companyId);
			Document existingCampaign = campaignsCollection.find(Filters.eq("_id", new ObjectId(id))).first();
			if (existingCampaign == null) {
				throw new BadRequestException("CAMPAIGN_NOT_FOUND");
			}

			String json = new ObjectMapper().writeValueAsString(campaigns);
			Document updateCampaign = Document.parse(json);
			campaignsCollection.findOneAndReplace(Filters.eq("_id", new ObjectId(id)), updateCampaign);
			log.trace("Exit CampaignsService.putCampaign()");

			return campaigns;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");

	}

	@DeleteMapping("/companies/campaign/{id}")
	public ResponseEntity<Object> deleteCampaign(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("id") String id) {
		log.trace("Enter CampaignsService.deleteCampaign()");

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!ObjectId.isValid(id)) {
				throw new BadRequestException("CAMPAIGN_NOT_FOUND");
			}
			MongoCollection<Document> campaignCollection = mongoTemplate.getCollection("campaigns_" + companyId);
			Document removed = campaignCollection.findOneAndDelete(Filters.eq("_id", new ObjectId(id)));
			if (removed == null) {
				throw new BadRequestException("CAMPAIGN_NOT_FOUND");
			}
			log.trace("Exit CampaignsService.deleteCampaign()");
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/companies/gallery")
	public ResponseEntity<Object> getImageGallery(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {
			log.trace("Enter CampaignsService.getImageGallery()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");

			JSONObject resultObject = new JSONObject();
			JSONArray gallery = new JSONArray();
			MongoCollection<Document> galleryCollection = mongoTemplate.getCollection("image_gallery");
			ArrayList<Document> galleryDocs = galleryCollection.find(Filters.eq("COMPANY_ID", companyId))
					.into(new ArrayList<Document>());
			resultObject.put("TOTAL_RECORDS", galleryDocs.size());
			for (Document image : galleryDocs) {
				image.remove("COMPANY_ID");
				String imageId = image.remove("_id").toString();
				image.put("IMAGE_ID", imageId);
				gallery.put(image);
			}
			resultObject.put("GALLERY", gallery);
			log.trace("Exit CampaignsService.getImageGallery()");

			return new ResponseEntity<>(resultObject.toString(), Global.postHeaders, HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/companies/gallery/image/{id}")
	public HttpEntity<byte[]> getImage(HttpServletRequest request,
			@RequestParam(value = "email_address", required = false) String email,
			@RequestParam(value = "user_company_id", required = false) String userCompanyId,
			@RequestParam(value = "company_id", required = false) String companyId,
			@RequestParam(value = "campaign_id", required = false) String campaignId, @PathVariable("id") String id) {
		try {
			log.trace("Enter CampaignsService.getImageGallery()");
			if (campaignId != null) {
				MongoCollection<Document> campaignsCollection = mongoTemplate.getCollection("campaigns_" + companyId);
				Document campaignDoc = campaignsCollection.find(Filters.eq("_id", new ObjectId(campaignId))).first();
				campaignDoc.remove("_id");
				campaignDoc.put("CAMPAIGN_ID", campaignId);
				Campaigns campaign = new ObjectMapper().readValue(campaignDoc.toJson(), Campaigns.class);

				List<CampaignTracking> trackingList = new ArrayList<CampaignTracking>();
				Boolean userFound = false;
				if (campaign.getTracking() != null) {
					trackingList = campaign.getTracking();
				}
				for (CampaignTracking trackedUser : trackingList) {
					if (trackedUser.getEmailAddress().equals(email)
							&& trackedUser.getCompanyId().equals(userCompanyId)) {
						userFound = true;
					}
				}

				if (userFound.equals(false)) {
					CampaignTracking trackingUser = new CampaignTracking();
					trackingUser.setCompanyId(userCompanyId);
					trackingUser.setEmailAddress(email);
					trackingList.add(trackingUser);

					campaign.setTracking(trackingList);

					String json = new ObjectMapper().writeValueAsString(campaign);
					Document updateCampaign = Document.parse(json);
					campaignsCollection.findOneAndReplace(Filters.eq("_id", new ObjectId(campaignId)), updateCampaign);
				}
			}

			MongoCollection<Document> galleryCollection = mongoTemplate.getCollection("image_gallery");
			Document imageDoc = galleryCollection
					.find(Filters.and(Filters.eq("COMPANY_ID", companyId), Filters.eq("_id", new ObjectId(id))))
					.first();
			String imageId = imageDoc.remove("_id").toString();
			imageDoc.put("IMAGE_ID", imageId);
			GalleryImage imageObj = new ObjectMapper().readValue(imageDoc.toJson(), GalleryImage.class);

			String base64Image = imageObj.getLogo().getFile().split(",")[1];
			byte[] decodedBytes = Base64.getDecoder().decode(base64Image);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_JPEG);
			headers.setContentLength(decodedBytes.length);
			log.trace("Exit CampaignsService.getImageGallery()");
			return new HttpEntity<byte[]>(decodedBytes, headers);
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

	@PostMapping("/companies/gallery/image")
	public GalleryImage postGalleryImage(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestBody @Valid GalleryImage image) {
		try {
			log.trace("Enter CampaignsService.postGalleryImage()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			image.setCompanyId(companyId);
			MongoCollection<Document> galleryCollection = mongoTemplate.getCollection("image_gallery");

			String json = new ObjectMapper().writeValueAsString(image);
			Document imageDoc = Document.parse(json);
			galleryCollection.insertOne(imageDoc);
			image.setImageId(imageDoc.getObjectId("_id").toString());

			log.trace("Exit CampaignsService.postGalleryImage()");
			return image;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/companies/campaign/tracking")
	public ResponseEntity<Object> updateCampaignTracking(HttpServletRequest request,
			@RequestParam(value = "email_address", required = false) String email,
			@RequestParam(value = "user_company_id", required = false) String userCompanyId,
			@RequestParam(value = "company_id", required = false) String companyId,
			@RequestParam(value = "campaign_id", required = false) String campaignId) {
		try {
			log.trace("Enter CampaignsService.updateCampaignTracking()");

			if (campaignId != null) {
				MongoCollection<Document> campaignsCollection = mongoTemplate.getCollection("campaigns_" + companyId);
				Document campaignDoc = campaignsCollection.find(Filters.eq("_id", new ObjectId(campaignId))).first();
				campaignDoc.remove("_id");
				campaignDoc.put("CAMPAIGN_ID", campaignId);
				Campaigns campaign = new ObjectMapper().readValue(campaignDoc.toJson(), Campaigns.class);

				List<CampaignTracking> trackingList = new ArrayList<CampaignTracking>();
				Boolean userFound = false;
				if (campaign.getTracking() != null) {
					trackingList = campaign.getTracking();
				}
				for (CampaignTracking trackedUser : trackingList) {
					if (trackedUser.getEmailAddress().equals(email)
							&& trackedUser.getCompanyId().equals(userCompanyId)) {
						userFound = true;
					}
				}

				if (userFound.equals(false)) {
					CampaignTracking trackingUser = new CampaignTracking();
					trackingUser.setCompanyId(userCompanyId);
					trackingUser.setEmailAddress(email);
					trackingList.add(trackingUser);

					campaign.setTracking(trackingList);

					String json = new ObjectMapper().writeValueAsString(campaign);
					Document updateCampaign = Document.parse(json);
					campaignsCollection.findOneAndReplace(Filters.eq("_id", new ObjectId(campaignId)), updateCampaign);
				}
			} else {
				throw new BadRequestException("CAMPAIGN_NOT_FOUND");
			}

			log.trace("Exit CampaignsService.updateCampaignTracking()");
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

	@PostMapping("/companies/users/marketing/emails/unsubscribe")
	public ResponseEntity<Object> unsubcriptionToMarketingEmail(HttpServletRequest request,
			@RequestParam(value = "uuid", required = true) String uuid,
			@RequestParam(value = "email", required = true) String email) {
		log.trace("Enter CampaignsService.unsubcriptionToMarketingEmail()");
		String subdomain = request.getAttribute("SUBDOMAIN").toString();
		MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
		Document companyDocument = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
		if (companyDocument == null)
			throw new ForbiddenException("COMPANY_NOT_EXISTS");

		String companyId = companyDocument.getObjectId("_id").toString();

		MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
		Document userDocument = usersCollection
				.find(Filters.and(Filters.eq("EMAIL_ADDRESS", email), Filters.eq("USER_UUID", uuid))).first();
		if (userDocument != null) {

			usersCollection.updateOne(Filters.and(Filters.eq("EMAIL_ADDRESS", email), Filters.eq("USER_UUID", uuid)),
					Updates.set("SUBSCRIPTION_ON_MARKETING_EMAIL", false));
			log.trace("Exit CampaignsService.unsubcriptionToMarketingEmail()");
			return new ResponseEntity<Object>(HttpStatus.OK);

		} else {
			throw new ForbiddenException("USER_NOT_EXISTS");
		}

	}

	public List<ButtonClick> setButtonTracking(Campaigns campaign) {

		List<ButtonClick> buttonClicks = new ArrayList<ButtonClick>();
		if (campaign.getButtonClicks() != null && !campaign.getButtonClicks().isEmpty()) {
			buttonClicks = campaign.getButtonClicks();
		}
		List<Row> elementsList = campaign.getRows();

		for (Row row : elementsList) {
			for (Column element: row.getColumns()) {
				if (element.getType().equals("BUTTON")) {
					ButtonClick buttonClick = new ButtonClick();
					List<ClickedBy> clickedByUsers = new ArrayList<ClickedBy>();
					buttonClick.setClickedBy(clickedByUsers);
					buttonClick.setTotalClicks(0);
	
					final String buttonId = element.getSettings().getId();
					buttonClick.setButtonId(buttonId);

					buttonClick.setLink(element.getSettings().getLinkValue());

					Boolean buttonExists = buttonClicks.stream().filter(o -> o.getButtonId().equals(buttonId))
							.findFirst().isPresent();
					if (buttonExists.equals(false)) {
						buttonClicks.add(buttonClick);
					}
				}
			}
		}

		return buttonClicks;
	}

}
