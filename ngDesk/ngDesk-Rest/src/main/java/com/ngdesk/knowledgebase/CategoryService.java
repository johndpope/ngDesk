package com.ngdesk.knowledgebase;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.bson.conversions.Bson;
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
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.resources.MongoUtils;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class CategoryService {

	@Autowired
	private Authentication auth;

	@Autowired
	RoleService role;

	@Autowired
	Global global;

	@Autowired
	MongoTemplate mongoTemplate;

	private final Logger log = LoggerFactory.getLogger(CategoryService.class);

	@GetMapping("categories")
	public ResponseEntity<Object> getAllCategories(HttpServletRequest request,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {
			
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			log.trace("Enter CategoryService.getAllCategories()");

			JSONObject resultObject = new JSONObject();

			String subdomain = request.getAttribute("SUBDOMAIN").toString();
			Document company = global.getCompanyFromSubdomain(subdomain);
			if (company == null) {
				throw new ForbiddenException("COMPANY_DOES_NOT_EXIST");
			}

			String companyId = company.getObjectId("_id").toString();

			int pgSize = 100;
			int pg = 1;
			int skip = 0;

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

			List<Document> docs = new ArrayList<Document>();

			Document filter = MongoUtils.createFilter(search);

			List<String> teams = new ArrayList<String>();
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			Document publicTeamDocument = teamsCollection.find(Filters.eq("NAME", "Public")).first(); 
			teams.add(publicTeamDocument.getObjectId("_id").toString());
			boolean isSystemAdmin = false;
			if (uuid != null) {

				JSONObject user = auth.getUserDetails(uuid);
				String userRole = user.getString("ROLE");
				String userId = user.getString("USER_ID");

				MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
				Document systemAdminRole = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first();
				String systemAdminRoleId = systemAdminRole.getObjectId("_id").toString();
				if (userRole.equals(systemAdminRoleId)) {
					isSystemAdmin = true;
				}

				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
				Document userDocument = usersCollection
						.find(Filters.and(Filters.eq("_id", new ObjectId(userId)), Filters.eq("DELETED", false)))
						.first();

				if (userDocument != null) {
					List<String> userTeams = (List<String>) userDocument.get("TEAMS");
					for (String teamId : userTeams) {
						if (!teams.contains(teamId)) {
							teams.add(teamId);
						}
					}
				}
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("categories_" + companyId);
			if (sort != null && order != null) {

				if (order.equalsIgnoreCase("asc")) {
					if (isSystemAdmin) {
						docs = (List<Document>) collection.find(filter).sort(Sorts.orderBy(Sorts.ascending(sort)))
								.skip(skip).limit(pgSize).into(new ArrayList<Document>());
					} else {
						docs = (List<Document>) collection
								.find(Filters.and(Filters.eq("IS_DRAFT", false), filter,
										Filters.in("VISIBLE_TO", teams)))
								.sort(Sorts.orderBy(Sorts.ascending(sort))).skip(skip).limit(pgSize)
								.into(new ArrayList<Document>());
					}

				} else if (order.equalsIgnoreCase("desc")) {
					if (isSystemAdmin) {
						docs = (List<Document>) collection.find(filter).sort(Sorts.orderBy(Sorts.descending(sort)))
								.skip(skip).limit(pgSize).into(new ArrayList<Document>());
					} else {
						docs = (List<Document>) collection
								.find(Filters.and(Filters.eq("IS_DRAFT", false), filter,
										Filters.in("VISIBLE_TO", teams)))
								.sort(Sorts.orderBy(Sorts.descending(sort))).skip(skip).limit(pgSize)
								.into(new ArrayList<Document>());
					}
				} else {
					throw new BadRequestException("INVALID_SORT_ORDER");
				}

			} else {
				if (isSystemAdmin) {
					docs = collection.find(filter).skip(skip).limit(pgSize).into(new ArrayList<Document>());
				} else {
					docs = collection
							.find(Filters.and(Filters.eq("IS_DRAFT", false), filter, Filters.in("VISIBLE_TO", teams)))
							.skip(skip).limit(pgSize).into(new ArrayList<Document>());
				}

			}

			JSONArray categories = new JSONArray();
			for (Document doc : docs) {
				String id = doc.getObjectId("_id").toString();
				doc.remove("_id");
				
				ObjectMapper mapper = new ObjectMapper();
				
				Category category = new ObjectMapper().readValue(mapper.writeValueAsString(doc), Category.class);
				category.setCategoryId(id);
				JSONObject json = new JSONObject(new ObjectMapper().writeValueAsString(category));
				categories.put(json);
			}

			if (isSystemAdmin) {
				resultObject.put("TOTAL_RECORDS", collection.countDocuments(filter));
			} else {
				resultObject.put("TOTAL_RECORDS", collection.countDocuments(
						Filters.and(Filters.eq("IS_DRAFT", false), filter, Filters.in("VISIBLE_TO", teams))));
			}

			resultObject.put("DATA", categories);

			log.trace("Exit CategoryService.getAllCategories()");
			return new ResponseEntity<>(resultObject.toString(), global.postHeaders, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("categories/{id}")
	public Category getCategoryById(HttpServletRequest request, @PathVariable("id") String id) {
		try {

			String subdomain = request.getAttribute("SUBDOMAIN").toString();
			Document company = global.getCompanyFromSubdomain(subdomain);
			if (company == null) {
				throw new ForbiddenException("COMPANY_DOES_NOT_EXIST");
			}
			String companyId = company.getObjectId("_id").toString();

			MongoCollection<Document> collection = mongoTemplate.getCollection("categories_" + companyId);
			if (new ObjectId().isValid(id)) {
				Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
				if (doc != null) {
					ObjectMapper mapper = new ObjectMapper();
					String categoryId = doc.getObjectId("_id").toString();
					doc.remove("_id");
					Category category = mapper.readValue(mapper.writeValueAsString(doc), Category.class);
					category.setCategoryId(categoryId);
					return category;
				} else {
					throw new BadRequestException("CATEGORY_DOESNT_EXIST");
				}
			} else {
				throw new BadRequestException("CATEGORY_DOESNT_EXIST");
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

	@PostMapping("categories")
	public Category postCategory(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody Category category) {
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			
			if(!global.languages.contains(category.getSourceLanguage())) {
			throw new BadRequestException("UNSUPPORTED_LANGUAGE");
			}
			
			log.trace("Enter CategoryService.postCategory()");

			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("categories_" + companyId);
			Document doc = collection.find(Filters.eq("NAME", category.getName())).first();

			if (doc != null) {
				throw new BadRequestException("CATEGORY_EXISTS");
			}

			List<ObjectId> teamIds = new ArrayList<ObjectId>();
			for (String team : category.getVisibleTo()) {
				if (!new ObjectId().isValid(team)) {
					throw new BadRequestException("TEAM_DOES_NOT_EXIST");
				}
				teamIds.add(new ObjectId(team));
			}
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			List<Document> teams = teamsCollection
					.find(Filters.and(Filters.in("_id", teamIds), Filters.eq("DELETED", false)))
					.into(new ArrayList<Document>());
			if (teams.size() != category.getVisibleTo().size()) {
				throw new BadRequestException("TEAM_DOES_NOT_EXIST");
			}

			int totalCount = (int) collection.countDocuments();
			category.setOrder(totalCount + 1);

			category.setCreatedBy(user.getString("USER_ID"));
			category.setDateCreated(new Timestamp(new Date().getTime()));
			category.setDateUpdated(new Timestamp(new Date().getTime()));
			category.setLastUpdatedBy(user.getString("USER_ID"));

			String json = new ObjectMapper().writeValueAsString(category);
			doc = Document.parse(json);
			collection.insertOne(doc);

			String categoryId = doc.getObjectId("_id").toString();
			doc.remove("_id");
			category.setCategoryId(categoryId);
			log.trace("Exit CategoryService.postCategory()");
			return category;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		log.trace("Exit CategoryService.postCategory()");
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("categories")
	public Category putCategory(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody Category category) {
		try {
			log.trace("Enter CategoryService.putCategory()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			
			if(!global.languages.contains(category.getSourceLanguage())) {
			throw new BadRequestException("UNSUPPORTED_LANGUAGE");
			}
			
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("categories_" + companyId);
			String id = category.getCategoryId();

			if (category.getCategoryId() == null || !new ObjectId().isValid(id)) {
				throw new BadRequestException("CATEGORY_DOESNT_EXIST");
			}

			List<ObjectId> teamIds = new ArrayList<ObjectId>();
			
			for (String team : category.getVisibleTo()) {
				if (!new ObjectId().isValid(team)) {
					throw new BadRequestException("TEAM_DOES_NOT_EXIST");
				}
				teamIds.add(new ObjectId(team));
			}
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			List<Document> teams = teamsCollection
					.find(Filters.and(Filters.in("_id", teamIds), Filters.eq("DELETED", false)))
					.into(new ArrayList<Document>());

			if (teams.size() != category.getVisibleTo().size()) {
				throw new BadRequestException("TEAM_DOES_NOT_EXIST");
			}

			Document doc = collection.find(Filters.and(Filters.eq("_id", new ObjectId(id)))).first();

			if (doc != null) {
				doc.remove("_id");

				Category existingCategory = new ObjectMapper().readValue(doc.toJson(), Category.class);
				category.setCreatedBy(existingCategory.getCreatedBy());
				category.setDateCreated(existingCategory.getDateCreated());
				category.setOrder(existingCategory.getOrder());

				Document document = collection
						.find(Filters.and(Filters.ne("_id", new ObjectId(id)), Filters.eq("NAME", category.getName())))
						.first();

				if (document != null) {
					log.trace("Exit CategoryService.postCategory()");
					throw new BadRequestException("CATEGORY_EXISTS");
				}

				category.setDateUpdated(new Timestamp(new Date().getTime()));
				category.setLastUpdatedBy(user.getString("USER_ID"));

				String json = new ObjectMapper().writeValueAsString(category);
				Document updatedDocument = Document.parse(json);

				collection.findOneAndReplace(Filters.eq("_id", new ObjectId(id)), updatedDocument);

				MongoCollection<Document> sectionsCollection = mongoTemplate.getCollection("sections_" + companyId);
				MongoCollection<Document> articlesCollection = mongoTemplate.getCollection("articles_" + companyId);

				for (String teamId : existingCategory.getVisibleTo()) {
					if (!category.getVisibleTo().contains(teamId)) {
						sectionsCollection.updateMany(Filters.eq("CATEGORY", category.getCategoryId()),
								Updates.pull("VISIBLE_TO", teamId));
						List<Document> sections = sectionsCollection
								.find(Filters.eq("CATEGORY", category.getCategoryId())).into(new ArrayList<Document>());
						for (Document section : sections) {
							String sectionId = section.getObjectId("_id").toString();
							articlesCollection.updateMany(Filters.eq("SECTION", sectionId),
									Updates.pull("VISIBLE_TO", teamId));
						}
					}
				}
				return category;
			} else {
				throw new BadRequestException("CATEGORY_DOESNT_EXIST");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.trace("Exit CategoryService.putCategory()");
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("categories/{id}")
	public ResponseEntity<Object> deleteCategory(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("id") String id) {
		try {
			log.trace("Enter CategoryService.deleteCategory()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("categories_" + companyId);

			if (!new ObjectId().isValid(id)) {
				throw new BadRequestException("CATEGORY_DOESNT_EXIST");
			}

			Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();

			if (doc == null) {
				throw new BadRequestException("CATEGORY_DOESNT_EXIST");
			}

			collection.deleteOne(Filters.eq("_id", new ObjectId(id)));

			MongoCollection<Document> sectionsCollection = mongoTemplate.getCollection("sections_" + companyId);
			List<Document> sections = sectionsCollection.find(Filters.eq("CATEGORY", id))
					.into(new ArrayList<Document>());

			List<String> sectionIds = new ArrayList<String>();
			for (Document section : sections) {
				sectionIds.add(section.getObjectId("_id").toString());
			}
			MongoCollection<Document> articlesCollection = mongoTemplate.getCollection("articles_" + companyId);
			articlesCollection.deleteMany(Filters.in("SECTION", sectionIds));

			sectionsCollection.deleteMany(Filters.eq("CATEGORY", id));
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		log.trace("Exit CategoryService.deleteCategory()");
		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
