package com.ngdesk.modules.detail.layouts;

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
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class DetailLayoutService {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(DetailLayoutService.class);

	@GetMapping("/modules/{module_id}/detail_layouts")
	public ResponseEntity<Object> getDetailLayouts(HttpServletRequest request,
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
			log.trace("Enter DetailLayoutService.getDetailLayouts() moduleId: " + moduleId);
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
				ArrayList<Document> layoutDocuments = (ArrayList) module.get("DETAIL_LAYOUTS");
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
					DCELayout detaillayout = new ObjectMapper().readValue(document.toJson(), DCELayout.class);
					detaillayout.setRole(rolesMap.get(detaillayout.getRole()));
					JSONObject detailLayoutJson = new JSONObject(new ObjectMapper().writeValueAsString(detaillayout));
					layouts.put(detailLayoutJson);
				}
			} else {
				throw new BadRequestException("MODULE_NOT_EXISTS");
			}
			if (sort != null && order != null) {
				layouts = sortLayouts(layouts, sort, order);
			}
			resultObj.put("DETAIL_LAYOUTS", layouts);
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
		log.trace("Exit DetailLayoutService.getDetailLayouts() moduleId: " + moduleId);
		return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);
	}

	@GetMapping("/modules/{module_id}/detail_layouts/{layout_id}")
	public DCELayout getDetailLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("layout_id") String layoutId)
			throws JsonParseException, JsonMappingException, IOException {
		try {
			log.trace("Enter DetailLayoutService.getDetailLayout()  moduleId: " + moduleId + ", layoutId: " + layoutId);
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
			if (module != null) {

				String moduleName = module.getString("NAME");
				if (!roleService.isAuthorizedForModule(userId, "GET", moduleId, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}

				if (global.isExistsInModuleLayoutId("DETAIL_LAYOUTS", layoutId, collectionName, moduleName)) {
					ArrayList<Document> detailLayoutDocuments = (ArrayList) module.get("DETAIL_LAYOUTS");
					for (Document layoutDocument : detailLayoutDocuments) {
						if (layoutDocument.getString("LAYOUT_ID").equalsIgnoreCase(layoutId)) {
							DCELayout detailLayout = new ObjectMapper().readValue(layoutDocument.toJson(),
									DCELayout.class);
							log.trace("Exit DetailLayoutService.getDetailLayout()  moduleName: " + moduleName
									+ ", layoutId: " + layoutId);
							return detailLayout;
						}
					}

				} else {
					throw new ForbiddenException("DETAIL_LAYOUT_NOT_EXISTS");
				}
			} else {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/modules/{module_id}/detail_layouts")
	public DCELayout postDetailLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @Valid @RequestBody DCELayout detailLayout) {
		try {
			log.trace("Enter DetailLayoutService.postDetailLayout()  moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			String collectionName = "modules_" + companyId;
			List<String> roleIdList = new ArrayList<>();
			String roleId = detailLayout.getRole();

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}

			List<Document> createLayoutList = (List<Document>) module.get("DETAIL_LAYOUTS");
			for (Document doc : createLayoutList) {
				roleIdList.add(doc.getString("ROLE").toString());
			}
			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "POST", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (roleIdList.contains(roleId)) {
				throw new BadRequestException("LAYOUT_EXISTS_FOR_ROLE");
			}
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			if (!ObjectId.isValid(roleId)) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}

			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();
			if (roleDocument == null) {
				throw new BadRequestException("ROLE_DOES_NOT_EXIST");
			}

			detailLayout.setCreatedBy(userId);
			boolean check = false;
			if (detailLayout.getCustomLayout() != null || detailLayout.getPanels() != null
					|| detailLayout.getPreDefinedTemplate() != null) {
				check = true;
			}

			// LOOP THROUGH THE PANELS AND GRIDS TO CHECK IF THERE IS ANY FIELD EXISTS IN
			// ANY OF THE GRID.
			if (detailLayout.getPanels() != null) {
				check = false;
				for (Panel panel : detailLayout.getPanels()) {
					for (List<Grid> grids : panel.getGrids()) {
						for (Grid grid : grids) {
							if (!grid.isEmpty()) {
								check = true;
								break;
							}
						}
					}
				}
			}

			if (!check) {
				throw new BadRequestException("LAYOUT_FIELDS_EMPTY");
			}

			// Get Document

			if (collection.find(Filters.and(Filters.eq("NAME", moduleName), Filters.elemMatch("DETAIL_LAYOUTS", Filters
					.and(Filters.eq("ROLE", detailLayout.getRole()), Filters.eq("NAME", detailLayout.getName())))))
					.first() != null) {
				throw new BadRequestException("DETAILLAYOUT_NAME_EXISTS");
			}

			detailLayout.setLayoutId(UUID.randomUUID().toString());
			String detailLayoutBody = new ObjectMapper().writeValueAsString(detailLayout).toString();
			Document detailLayoutDocument = Document.parse(detailLayoutBody.toString());

			detailLayoutDocument.put("DATE_CREATED", new Date());
			detailLayoutDocument.put("DATE_UPDATED", new Date());

			collection.updateOne(Filters.eq("NAME", moduleName),
					Updates.addToSet("DETAIL_LAYOUTS", detailLayoutDocument));
			log.trace("Exit DetailLayoutService.postDetailLayout()  moduleName: " + moduleName);
			return detailLayout;

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/modules/{module_id}/detail_layouts/{layout_id}")
	public DCELayout putDetailLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @PathVariable("layout_id") String layoutId,
			@Valid @RequestBody DCELayout detailLayout) {
		try {
			log.trace("Enter DetailLayoutService.putDetailLayout()  moduleId: " + moduleId);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			List<String> roleIdList = new ArrayList<>();
			String roleId = detailLayout.getRole();
			String detailLayoutId = detailLayout.getLayoutId();

			detailLayout.setDateUpdated(new Timestamp(new Date().getTime()));
			detailLayout.setLastUpdatedBy(userId);

			String payload = new ObjectMapper().writeValueAsString(detailLayout).toString();
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			List<Document> createLayoutList = (List<Document>) module.get("DETAIL_LAYOUTS");
			for (Document doc : createLayoutList) {
				if (!detailLayoutId.equals(doc.getString("LAYOUT_ID").toString())) {
					roleIdList.add(doc.getString("ROLE").toString());
				}
			}

			String moduleName = module.getString("NAME");
			if (!roleService.isAuthorizedForModule(userId, "PUT", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (roleIdList.contains(roleId)) {
				throw new BadRequestException("LAYOUT_EXISTS_FOR_ROLE_UPDATE");
			}
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			if (!ObjectId.isValid(roleId)) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}
			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();
			if (roleDocument == null) {
				throw new BadRequestException("ROLE_DOES_NOT_EXIST");
			}

			// LOOP THROUGH THE PANELS AND GRIDS TO CHECK IF THERE IS ANY FIELD EXISTS IN
			// ANY OF THE GRID.
			boolean check = false;
			if (detailLayout.getPanels() != null) {
				for (Panel panel : detailLayout.getPanels()) {
					for (List<Grid> grids : panel.getGrids()) {
						for (Grid grid : grids) {
							if (!grid.isEmpty()) {
								check = true;
								break;
							}
						}
					}
				}
			}

			if (!check) {
				throw new BadRequestException("LAYOUT_FIELDS_EMPTY");
			}

			if (layoutId != null) {
				if (!layoutId.equals(detailLayout.getLayoutId())) {
					throw new BadRequestException("LAYOUT_ID_MISMATCH");
				}
				Document existingDetailLayout = collection.find(
						Filters.and(Filters.eq("DETAIL_LAYOUTS.LAYOUT_ID", layoutId), Filters.eq("NAME", moduleName)))
						.first();
				if (existingDetailLayout != null) {
					if (collection.find(Filters.and(Filters.eq("NAME", moduleName),
							Filters.elemMatch("DETAIL_LAYOUTS", Filters.and(Filters.eq("ROLE", detailLayout.getRole()),
									Filters.ne("LAYOUT_ID", layoutId), Filters.eq("NAME", detailLayout.getName())))))
							.first() != null) {
						throw new BadRequestException("DETAILLAYOUT_NAME_EXISTS");
					}

					Document detailLayoutDocument = Document.parse(payload);
					collection.updateOne(Filters.eq("NAME", moduleName),
							Updates.pull("DETAIL_LAYOUTS", Filters.eq("LAYOUT_ID", detailLayout.getLayoutId())));

					detailLayoutDocument.put("DATE_CREATED", existingDetailLayout.getDate("DATE_CREATED"));
					detailLayoutDocument.put("DATE_UPDATED", new Date());

					collection.updateOne(Filters.eq("NAME", moduleName),
							Updates.push("DETAIL_LAYOUTS", detailLayoutDocument));
					log.trace("Exit DetailLayoutService.putDetailLayout()  moduleName: " + moduleName);
					return detailLayout;

				} else {
					throw new ForbiddenException("DETAIL_LAYOUT_NOT_EXISTS");
				}
			} else {
				throw new BadRequestException("DETAIL_LAYOUT_ID_NULL");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/modules/{module_id}/detail_layouts/{layout_id}")
	public ResponseEntity<Object> deleteDetailLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("layout_id") String layoutId, @PathVariable("module_id") String moduleId) {
		try {
			log.trace("Enter DetailLayoutService.deleteDetailLayout()  moduleId: " + moduleId + ", layoutId: "
					+ layoutId);
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
			if (!roleService.isAuthorizedForModule(userId, "DELETE", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (global.isExistsInModuleLayoutId("DETAIL_LAYOUTS", layoutId, collectionName, moduleName)) {

				collection.updateOne(Filters.eq("NAME", moduleName),
						Updates.pull("DETAIL_LAYOUTS", Filters.eq("LAYOUT_ID", layoutId)));
				Document document = collection.find(Filters.eq("NAME", moduleName)).first();
				log.trace("Exit DetailLayoutService.deleteDetailLayout()  moduleName: " + moduleName + ", layoutId: "
						+ layoutId);
				return new ResponseEntity<Object>(HttpStatus.OK);

			} else {
				throw new ForbiddenException("DETAIL_LAYOUT_NOT_EXISTS");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public JSONArray sortLayouts(JSONArray layouts, String sort, String order) throws JSONException {
		log.trace("Enter DetailLayoutService.sortLayouts()  sort: " + sort + ", order: " + order);
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
		log.trace("Exit DetailLayoutService.sortLayouts()  sort: " + sort + ", order: " + order);
		return sortedLayouts;
	}
}
