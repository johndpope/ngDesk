package com.ngdesk.modules.public_layouts;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.detail.layouts.DCELayout;
import com.ngdesk.roles.RoleService;

public class PublicLayoutService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(PublicLayoutService.class);

	@GetMapping("/modules/{module_id}/public_layouts")
	public ResponseEntity<Object> getPublicLayouts(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONArray layouts = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();

		try {

			log.trace("Enter PublicLayoutService.getPublicLayouts() moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);

			List<Document> rolesList = rolesCollection.find().into(new ArrayList<Document>());
			Map<String, String> rolesMap = new HashMap<String, String>();

			for (Document role : rolesList) {
				String roleId = role.getObjectId("_id").toString();
				String roleName = role.getString("NAME");
				rolesMap.put(roleId, roleName);
			}

			// Get Document
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module != null) {
				String moduleName = module.getString("NAME");
				if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
				// Get All Layouts From Module
				ArrayList<Document> layoutDocuments = (ArrayList) module.get("PUBLIC_LAYOUTS");
				totalSize = layoutDocuments.size();

				// by default return all documents
				int lowerLimit = 0;
				int maxLimit = layoutDocuments.size();

				if (pageSize != null && page != null) {
					int pgSize = Integer.valueOf(pageSize);
					int pg = Integer.valueOf(page);
					if (maxLimit >= pgSize) {
						maxLimit = pgSize * pg;
						lowerLimit = maxLimit - pgSize;
					}

					if (pgSize < 1) {
						throw new BadRequestException("INVALID_PAGE_SIZE");
					}
					if (pg < 0) {
						throw new BadRequestException("INVALID_PAGE_NUMBER");
					}
				}
				for (int i = lowerLimit; i < maxLimit; i++) {
					if (i == totalSize) {
						break;
					}
					Document document = layoutDocuments.get(i);
					DCELayout publiclayout = new ObjectMapper().readValue(document.toJson(), DCELayout.class);
					publiclayout.setRole(rolesMap.get(publiclayout.getRole()));
					JSONObject publicLayoutJson = new JSONObject(new ObjectMapper().writeValueAsString(publiclayout));
					layouts.put(publicLayoutJson);
				}
			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			if (sort != null && order != null) {
				layouts = sortLayouts(layouts, sort, order);
			}
			resultObj.put("PUBLIC_LAYOUTS", layouts);
			resultObj.put("TOTAL_RECORDS", totalSize);

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.trace("Exit PublicLayoutService.getPublicLayouts() moduleId: " + moduleId);
		return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);
	}

	@GetMapping("/modules/{module_id}/public_layouts/{layout_id}")
	public DCELayout getPublicLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("layout_id") String layoutId)
			throws JsonParseException, JsonMappingException, IOException {
		try {
			log.trace("Enter PublicLayoutService.getPublicLayout()  moduleId: " + moduleId + ", layoutId: " + layoutId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (global.isExistsInModuleLayoutId("PUBLIC_LAYOUTS", layoutId, collectionName, moduleName)) {
				ArrayList<Document> publicLayoutDocuments = (ArrayList) module.get("PUBLIC_LAYOUTS");
				for (Document layoutDocument : publicLayoutDocuments) {
					if (layoutDocument.getString("LAYOUT_ID").equalsIgnoreCase(layoutId)) {
						DCELayout publicLayout = new ObjectMapper().readValue(layoutDocument.toJson(), DCELayout.class);
						log.trace("Exit PublicLayoutService.getPublicLayout()  moduleName: " + moduleName
								+ ", layoutId: " + layoutId);
						return publicLayout;
					}
				}

			} else {
				throw new ForbiddenException("PUBLIC_LAYOUT_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/modules/{module_id}/public_layouts/{layout_name}")
	public DCELayout postPublicLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("layout_name") String layoutName,
			@Valid @RequestBody DCELayout publicLayout) {
		try {

			log.trace("Enter PublicLayoutService.postPublicLayout()  moduleId: " + moduleId + ", layoutName: "
					+ layoutName);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "modules_" + companyId;
			String userId = user.getString("USER_ID");
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document moduleDocument = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (moduleDocument == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}

			String moduleName = moduleDocument.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "POST", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			publicLayout.setDateCreated(new Timestamp(new Date().getTime()));
			publicLayout.setCreatedBy(userId);

			String roleId = publicLayout.getRole();
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			if (!ObjectId.isValid(roleId)) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}
			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();
			if (roleDocument == null) {
				throw new BadRequestException("ROLE_DOES_NOT_EXIST");
			}


			if (publicLayout.getCustomLayout() != null) {
				throw new BadRequestException("LAYOUT_FIELDS_EMPTY");
			}


			if (collection
					.find(Filters
							.and(Filters.eq("NAME", moduleName),
									Filters.elemMatch("PUBLIC_LAYOUTS",
											Filters.and(Filters.eq("ROLE", publicLayout.getRole())))))
					.first() != null) {
				throw new BadRequestException("LAYOUT_EXISTS_FOR_ROLE");
			}

			publicLayout.setLayoutId(UUID.randomUUID().toString());
			String publicLayoutBody = new ObjectMapper().writeValueAsString(publicLayout).toString();
			Document publicLayoutDocument = Document.parse(publicLayoutBody.toString());
			collection.updateOne(Filters.eq("NAME", moduleName),
					Updates.addToSet("PUBLIC_LAYOUTS", publicLayoutDocument));
			log.trace("Exit PublicLayoutService.postPublicLayout()  moduleName: " + moduleName + ", layoutName: "
					+ layoutName);
			return publicLayout;

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/modules/{module_id}/public_layouts/{layout_id}")
	public DCELayout putPublicLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("layout_id") String layoutId,
			@Valid @RequestBody DCELayout publicLayout) {
		try {
			log.trace("Enter PublicLayoutService.putPublicLayout()  moduleId: " + moduleId + ", layoutId: " + layoutId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");

			publicLayout.setDateUpdated(new Timestamp(new Date().getTime()));
			publicLayout.setLastUpdatedBy(userId);

			String payload = new ObjectMapper().writeValueAsString(publicLayout).toString();
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document moduleDocument = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

			if (moduleDocument == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}

			String moduleName = moduleDocument.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "PUT", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String roleId = publicLayout.getRole();
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			if (!ObjectId.isValid(roleId)) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}
			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();
			if (roleDocument == null) {
				throw new BadRequestException("ROLE_DOES_NOT_EXIST");
			}

			if (publicLayout.getCustomLayout() != null) {
				throw new BadRequestException("LAYOUT_FIELDS_EMPTY");
			}


			if (publicLayout.getLayoutId() != null) {
				if (collection.find(Filters.and(Filters.eq("PUBLIC_LAYOUTS.LAYOUT_ID", publicLayout.getLayoutId()),
						Filters.eq("_id", new ObjectId(moduleId)))).first() != null) {

					if (collection
							.find(Filters.and(Filters.eq("_id", new ObjectId(moduleId)),
									Filters.elemMatch("PUBLIC_LAYOUTS",
											Filters.and(Filters.eq("ROLE", publicLayout.getRole()),
													Filters.ne("LAYOUT_ID", publicLayout.getLayoutId())))))
							.first() != null) {
						throw new BadRequestException("LAYOUT_EXISTS_FOR_ROLE");
					}

					Document publicLayoutDocument = Document.parse(payload);
					collection.updateOne(Filters.eq("NAME", moduleName),
							Updates.pull("PUBLIC_LAYOUTS", Filters.eq("LAYOUT_ID", publicLayout.getLayoutId())));
					collection.updateOne(Filters.eq("NAME", moduleName),
							Updates.push("PUBLIC_LAYOUTS", publicLayoutDocument));
					log.trace("Exit PublicLayoutService.putPublicLayout()  moduleName: " + moduleName + ", layoutId: "
							+ layoutId);
					return publicLayout;
				} else {
					throw new ForbiddenException("PUBLIC_LAYOUT_NOT_EXISTS");
				}
			} else {
				throw new BadRequestException("PUBLIC_LAYOUT_ID_NULL");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/modules/{module_id}/public_layouts/{layout_id}")
	public ResponseEntity<Object> deletePublicLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("layout_id") String layoutId, @PathVariable("module_id") String moduleId) {
		try {
			log.trace("Enter PublicLayoutService.deletePublicLayout()  moduleId: " + moduleId + ", layoutId: "
					+ layoutId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			String collectionName = "modules_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "DELETE", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (global.isExistsInModuleLayoutId("PUBLIC_LAYOUTS", layoutId, collectionName, moduleName)) {
				collection.updateOne(Filters.eq("NAME", moduleName),
						Updates.pull("PUBLIC_LAYOUTS", Filters.eq("LAYOUT_ID", layoutId)));
				log.trace("Exit PublicLayoutService.deletePublicLayout()  moduleName: " + moduleName + ", layoutId: "
						+ layoutId);
				return new ResponseEntity<Object>(HttpStatus.OK);

			} else {
				throw new ForbiddenException("PUBLIC_LAYOUT_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public JSONArray sortLayouts(JSONArray layouts, String sort, String order) throws JSONException {
		log.trace("Enter PublicLayoutService.sortLayouts()  sort: " + sort + ", order: " + order);
		JSONArray sortedLayouts = new JSONArray();

		List<JSONObject> jsonValues = new ArrayList<JSONObject>();
		for (int i = 0; i < layouts.length(); i++) {
			jsonValues.add(layouts.getJSONObject(i));
		}
		for (int j = 0; j < layouts.length(); j++) {
			JSONObject obj = layouts.getJSONObject(j);
			if (obj.has(sort)) {
				continue;
			} else {
				throw new BadRequestException("INVALID_SORT");
			}
		}
		final String KEY_NAME = sort;
		Collections.sort(jsonValues, new Comparator<JSONObject>() {

			@Override
			public int compare(JSONObject a, JSONObject b) {
				String valA = new String();
				String valB = new String();

				try {
					valA = (String) a.get(KEY_NAME);
					valB = (String) b.get(KEY_NAME);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (order.equals("desc")) {
					return valB.compareTo(valA);
				}
				return valA.compareTo(valB);
			}
		});

		for (int i = 0; i < layouts.length(); i++) {
			sortedLayouts.put(jsonValues.get(i));
		}
		log.trace("Exit PublicLayoutService.sortLayouts()  sort: " + sort + ", order: " + order);
		return sortedLayouts;
	}
}
