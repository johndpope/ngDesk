package com.ngdesk.channels.interval;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.stereotype.Component;
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
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.resources.MongoUtils;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class IntervalService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(IntervalService.class);

	private static String channelType = "interval";

	@GetMapping("/channels/interval")
	public ResponseEntity<Object> getChannel(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONObject resultObj = new JSONObject();
		// List<IntervalChannel> channels = new ArrayList<IntervalChannel>();
		JSONArray channels = new JSONArray();
		int totalSize = 0;

		try {
			log.trace("Enter IntervalService.getChannel()");

			if (global.isValidSourceType(channelType)) {
				// GET COMPANY ID
				if (request.getHeader("authentication_token") != null) {
					uuid = request.getHeader("authentication_token").toString();
				}
				JSONObject user = auth.getUserDetails(uuid);
				String userId = user.getString("USER_ID");
				String userRole = user.getString("ROLE");
				String companyId = user.getString("COMPANY_ID");

				// ACCESS DB
				String collectionName = "channels_" + channelType + "_" + companyId;
				MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

				if (!roleService.isSystemAdmin(userRole, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}

				// BY DEFAULT RETURN ALL DOCUMENTS
				int lowerLimit = 0;
				int pgSize = 100;
				int pg = 1;
				int skip = 0;
				totalSize = (int) collection.countDocuments();

				if (pageSize != null && page != null) {
					pgSize = Integer.valueOf(pageSize);
					pg = Integer.valueOf(page);

					// CALCULATION TO FIND HOW MANY DOCUMENTS TO SKIP
					skip = (pg - 1) * pgSize;

					if (pgSize < 0) {
						throw new BadRequestException("INVALID_PAGE_SIZE");
					}

					if (pg < 0) {
						throw new BadRequestException("INVALID_PAGE");
					}
				}

				// GET ALL MODULES FROM COLLECTION
				List<Document> documents = null;
				Document filter = MongoUtils.createFilter(search);

				if (sort != null && order != null) {

					if (order.equalsIgnoreCase("asc")) {
						documents = (List<Document>) collection.find(filter).sort(Sorts.orderBy(Sorts.ascending(sort)))
								.skip(skip).limit(pgSize).into(new ArrayList<Document>());
					} else if (order.equalsIgnoreCase("desc")) {
						documents = (List<Document>) collection.find(filter).sort(Sorts.orderBy(Sorts.descending(sort)))
								.skip(skip).limit(pgSize).into(new ArrayList<Document>());
					} else {
						throw new BadRequestException("INVALID_SORT_ORDER");
					}

				} else {
					documents = (List<Document>) collection.find(filter).skip(skip).limit(pgSize)
							.into(new ArrayList<Document>());
				}

				for (Document document : documents) {
					String channelId = document.getObjectId("_id").toString();
					document.remove("_id");
					IntervalChannel intervalChannel = new ObjectMapper().readValue(document.toJson(),
							IntervalChannel.class);
					intervalChannel.setChannelId(channelId);
					JSONObject intervalChannelJson = new JSONObject(
							new ObjectMapper().writeValueAsString(intervalChannel));
					channels.put(intervalChannelJson);
				}

				resultObj.put("CHANNELS", channels);
				resultObj.put("TOTAL_RECORDS", totalSize);

				log.trace("Exit IntervalService.getChannel()");

				return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);
			} else {
				throw new BadRequestException("NOT_VALID_SOURCE_TYPE");
			}

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

	@GetMapping("/channels/interval/{name}")
	public IntervalChannel getIntervalChannel(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String channelName) {
		IntervalChannel intervalChannel;
		try {
			log.trace("Enter IntervalService.getIntervalChannel(), ChannelName: " + channelName);
			if (global.isValidSourceType(channelType)) {
				// GET COMPANY ID
				if (request.getHeader("authentication_token") != null) {
					uuid = request.getHeader("authentication_token").toString();
				}
				JSONObject user = auth.getUserDetails(uuid);
				String userId = user.getString("USER_ID");
				String userRole = user.getString("ROLE");
				String companyId = user.getString("COMPANY_ID");

				// ACCESS DB
				String collectionName = "channels_" + channelType + "_" + companyId;
				MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

				if (!roleService.isSystemAdmin(userRole, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}

				// CHECK CHANNEL
				if (global.isExists("NAME", channelName, collectionName)) {
					// GET SPECIFIC CHANNEL
					Document intervalChannelDocument = collection.find(Filters.eq("NAME", channelName)).first();

					// RETURN
					String channelId = intervalChannelDocument.getObjectId("_id").toString();
					intervalChannelDocument.remove("_id");
					intervalChannel = new ObjectMapper().readValue(intervalChannelDocument.toJson(),
							IntervalChannel.class);
					intervalChannel.setChannelId(channelId);

					log.trace("Exit IntervalService.getIntervalChannel(), ChannelName: " + channelName);
					return intervalChannel;

				} else {
					throw new ForbiddenException("CHANNEL_DOES_NOT_EXIST");
				}
			} else {
				throw new BadRequestException("NOT_VALID_SOURCE_TYPE");
			}

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

	@PostMapping("channels/interval/{name}")
	public IntervalChannel createIntervalChannel(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String channelName, @Valid @RequestBody IntervalChannel intervalChannel) {

		try {
			log.trace("Enter IntervalService.createIntervalChannel(), ChannelName: " + channelName);
			intervalChannel.setDateCreated(new Timestamp(new Date().getTime()));
			intervalChannel.setDateUpdated(new Timestamp(new Date().getTime()));

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			String channelJson = new ObjectMapper().writeValueAsString(intervalChannel);
			String collectionName = "channels_" + channelType + "_" + companyId;

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String sourceType = intervalChannel.getSourceType();

			if (channelType.equals(sourceType)) {

				if (global.isExists("NAME", channelName, collectionName)) {
					throw new BadRequestException("CHANNEL_NOT_UNIQUE");
				} else {

					if (channelName.equals(intervalChannel.getName())) {
						MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
						if (collection.countDocuments() == 0) {
							MongoUtils.createFullTextIndex(collectionName);
						}
						Document channelDocument = Document.parse(channelJson);
						collection.insertOne(channelDocument);

						String channelId = channelDocument.getObjectId("_id").toString();
						intervalChannel.setChannelId(channelId);
						log.trace("Exit IntervalService.createIntervalChannel(), ChannelName: " + channelName);
						return intervalChannel;
					} else {
						throw new BadRequestException("CHANNEL_NAME_MISMATCH");
					}
				}

			} else {
				throw new BadRequestException("CHANNEL_TYPE_MISMATCH");
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("channels/interval/{name}")
	public IntervalChannel updateIntervalChannel(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String channelName, @Valid @RequestBody IntervalChannel intervalChannel) {
		try {
			log.trace("Enter IntervalService.updateIntervalChannel(), ChannelName: " + channelName);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String collectionName = "channels_" + channelType + "_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			intervalChannel.setDateUpdated(new Timestamp(new Date().getTime()));
			intervalChannel.setLastUpdated(userId);
			String channelId = intervalChannel.getChannelId();

			if (channelId != null) {
				if (new ObjectId().isValid(channelId)) {
					Document oldChannelDocument = collection.find(Filters.eq("_id", new ObjectId(channelId))).first();
					if (oldChannelDocument != null) {
						if (intervalChannel.getMonth() != null && intervalChannel.getMinutes() != null
								&& intervalChannel.getHours() != null && intervalChannel.getDayOfMonth() != null
								&& intervalChannel.getDayOfWeek() != null) {
							if (channelType.equals(intervalChannel.getSourceType())) {
								String channelJson = new ObjectMapper().writeValueAsString(intervalChannel);
								Document channelDocument = Document.parse(channelJson);
								collection.findOneAndReplace(Filters.eq("_id", new ObjectId(channelId)),
										channelDocument);
								log.trace("Exit IntervalService.updateIntervalChannel(), ChannelName: " + channelName);
								return intervalChannel;
							} else {
								throw new BadRequestException("CHANNEL_TYPE_MISMATCH");
							}
						} else {
							throw new BadRequestException("VALUES_NULL");
						}
					} else {
						throw new ForbiddenException("CHANNEL_DOES_NOT_EXIST");
					}
				} else {
					throw new BadRequestException("INVALID_ENTRY_ID");
				}
			} else {
				throw new BadRequestException("CHANNEL_ID_NULL");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/channels/interval/{name}")
	public ResponseEntity<Object> deleteIntervalChannel(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String channelName) {

		try {
			log.trace("Enter IntervalService.deleteIntervalChannel(), ChannelName: " + channelName);
			// GET COMPANY ID
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			String collectionName = "channels_" + channelType + "_" + companyId;
			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (global.isExists("NAME", channelName, collectionName)) {
				// ACCESS MONGO
				MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
				collection.findOneAndDelete(Filters.eq("NAME", channelName));
			} else {
				throw new ForbiddenException("CHANNEL_DOES_NOT_EXIST");
			}

			log.trace("Exit IntervalService.deleteIntervalChannel(), ChannelName: " + channelName);
			// RETURN
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

}
