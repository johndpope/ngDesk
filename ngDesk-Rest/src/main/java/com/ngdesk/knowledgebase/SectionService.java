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
public class SectionService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Authentication auth;

	@Autowired
	RoleService role;

	private final Logger log = LoggerFactory.getLogger(SectionService.class);

	@GetMapping("/sections")
	public ResponseEntity<Object> getAllSections(HttpServletRequest request,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "category", required = false) String categoryId,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			log.trace("Enter SectionService.getAllSections()");

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
			Bson filter = new Document();

			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			Document publicTeamDocument = teamsCollection.find(Filters.eq("NAME", "Public")).first();

			List<String> teams = new ArrayList<String>();
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
				} else {
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
			}

			if (categoryId != null) {
				if (!new ObjectId().isValid(categoryId)) {
					throw new BadRequestException("CATEGORY_DOES_NOT_EXISTS");
				} else {
					MongoCollection<Document> categoriesCollection = mongoTemplate
							.getCollection("categories_" + companyId);
					Document categoryDocument = categoriesCollection.find(Filters.eq("_id", new ObjectId(categoryId)))
							.first();

					if (categoryDocument == null) {
						throw new BadRequestException("CATEGORY_DOES_NOT_EXISTS");
					}
					filter = Filters.eq("CATEGORY", categoryId);
				}
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("sections_" + companyId);
			if (sort != null && order != null) {

				if (order.equalsIgnoreCase("asc")) {
					if (isSystemAdmin) {
						docs = (List<Document>) collection.find(filter).sort(Sorts.orderBy(Sorts.ascending(sort)))
								.skip(skip).limit(pgSize).into(new ArrayList<Document>());
						resultObject.put("TOTAL_RECORDS", collection.countDocuments(filter));
					} else {
						docs = (List<Document>) collection
								.find(Filters.and(Filters.in("VISIBLE_TO", teams), filter,
										Filters.eq("IS_DRAFT", false)))
								.sort(Sorts.orderBy(Sorts.ascending(sort))).skip(skip).limit(pgSize)
								.into(new ArrayList<Document>());
						resultObject.put("TOTAL_RECORDS", collection.countDocuments(
								Filters.and(Filters.in("VISIBLE_TO", teams), filter, Filters.eq("IS_DRAFT", false))));
					}
				} else if (order.equalsIgnoreCase("desc")) {
					if (isSystemAdmin) {
						docs = (List<Document>) collection.find(filter).sort(Sorts.orderBy(Sorts.descending(sort)))
								.skip(skip).limit(pgSize).into(new ArrayList<Document>());
						resultObject.put("TOTAL_RECORDS", collection.countDocuments(filter));
					} else {
						docs = (List<Document>) collection
								.find(Filters.and(Filters.in("VISIBLE_TO", teams), filter,
										Filters.eq("IS_DRAFT", false)))
								.sort(Sorts.orderBy(Sorts.descending(sort))).skip(skip).limit(pgSize)
								.into(new ArrayList<Document>());
						resultObject.put("TOTAL_RECORDS", collection.countDocuments(
								Filters.and(Filters.in("VISIBLE_TO", teams), filter, Filters.eq("IS_DRAFT", false))));
					}

				} else {
					throw new BadRequestException("INVALID_SORT_ORDER");
				}

			} else {
				if (isSystemAdmin) {
					docs = collection.find(filter).skip(skip).limit(pgSize).into(new ArrayList<Document>());
					resultObject.put("TOTAL_RECORDS", collection.countDocuments(filter));
				} else {
					docs = collection
							.find(Filters.and(Filters.in("VISIBLE_TO", teams), filter, Filters.eq("IS_DRAFT", false)))
							.skip(skip).limit(pgSize).into(new ArrayList<Document>());
					resultObject.put("TOTAL_RECORDS", collection.countDocuments(
							Filters.and(Filters.in("VISIBLE_TO", teams), filter, Filters.eq("IS_DRAFT", false))));
				}

			}

			JSONArray sections = new JSONArray();
			for (Document doc : docs) {
				String id = doc.getObjectId("_id").toString();
				doc.remove("_id");
				ObjectMapper mapper = new ObjectMapper();
				Section section = mapper.readValue(mapper.writeValueAsString(doc), Section.class);
				section.setId(id);
				JSONObject json = new JSONObject(mapper.writeValueAsString(section));
				sections.put(json);
			}
			resultObject.put("DATA", sections);

			log.trace("Exit SectionService.getAllSections()");
			return new ResponseEntity<>(resultObject.toString(), global.postHeaders, HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/sections/{id}")
	public Section getSection(HttpServletRequest request, @PathVariable("id") String id) {
		try {
			log.trace("Enter SectionService.getSection() id: " + id);

			String subdomain = request.getAttribute("SUBDOMAIN").toString();
			Document company = global.getCompanyFromSubdomain(subdomain);
			if (company == null) {
				throw new ForbiddenException("COMPANY_DOES_NOT_EXIST");
			}
			String companyId = company.getObjectId("_id").toString();

			if (!new ObjectId().isValid(id)) {
				throw new BadRequestException("SECTION_DOES_NOT_EXISTS");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("sections_" + companyId);

			Document sectionDocument = collection.find(Filters.eq("_id", new ObjectId(id))).first();
			if (sectionDocument == null) {
				throw new BadRequestException("SECTION_DOES_NOT_EXISTS");
			}
			String sectionId = sectionDocument.getObjectId("_id").toString();
			sectionDocument.remove("_id");
			ObjectMapper mapper = new ObjectMapper();
			Section section = new ObjectMapper().readValue(mapper.writeValueAsString(sectionDocument), Section.class);
			section.setId(sectionId);
			log.trace("Exit SectionService.getSection()");

			return section;

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

	@PostMapping("/sections")
	public Section postSection(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody Section section) {
		try {
			log.trace("Enter SectionService.postSection()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			if (!global.languages.contains(section.getLanguage())) {
				throw new BadRequestException("UNSUPPORTED_LANGUAGE");
			}

			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userRole = user.getString("ROLE");

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			List<ObjectId> teamIds = new ArrayList<ObjectId>();
			for (String team : section.getVisibleTo()) {
				if (!new ObjectId().isValid(team)) {
					throw new BadRequestException("TEAM_DOES_NOT_EXIST");
				}
				teamIds.add(new ObjectId(team));
			}
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			List<Document> teams = teamsCollection
					.find(Filters.and(Filters.in("_id", teamIds), Filters.eq("DELETED", false)))
					.into(new ArrayList<Document>());

			if (teams.size() != section.getVisibleTo().size()) {
				throw new BadRequestException("TEAM_DOES_NOT_EXIST");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("sections_" + companyId);
			Document sectionDoc = collection.find(Filters.eq("NAME", section.getName())).first();
			if (sectionDoc != null) {
				throw new BadRequestException("SECTION_NAME_EXISTS");
			}

			if (section.getCategory() == null || !new ObjectId().isValid(section.getCategory())) {
				throw new BadRequestException("CATEGORY_DOES_NOT_EXISTS");
			}

			MongoCollection<Document> categoryCollection = mongoTemplate.getCollection("categories_" + companyId);
			Document categoryDoc = categoryCollection.find(Filters.eq("_id", new ObjectId(section.getCategory())))
					.first();
			if (categoryDoc == null) {
				throw new BadRequestException("CATEGORY_DOES_NOT_EXISTS");
			}

			List<String> categoryVisibleTo = (List<String>) categoryDoc.get("VISIBLE_TO");
			for (String visilbeTo : section.getVisibleTo()) {
				if (!categoryVisibleTo.contains(visilbeTo)) {
					throw new BadRequestException("TEAM_MUST_BE_SAME_AS_PARENT");
				}
			}

			List<ObjectId> managedByTeamIds = new ArrayList<ObjectId>();
			for (String roleId : section.getManagedBy()) {
				if (!new ObjectId().isValid(roleId)) {
					throw new BadRequestException("INVALID_ROLE_ID");
				}
				managedByTeamIds.add(new ObjectId(roleId));
			}

			List<Document> managedByTeams = teamsCollection.find(Filters.in("_id", managedByTeamIds))
					.into(new ArrayList<Document>());
			if (managedByTeams.size() != section.getManagedBy().size()) {
				throw new BadRequestException("TEAM_DOES_NOT_EXIST");
			}

			int totalCount = (int) collection.countDocuments(Filters.eq("CATEGORY", section.getCategory()));
			section.setOrder(totalCount + 1);

			section.setCreated(user.getString("USER_ID"));
			section.setDateCreated(new Timestamp(new Date().getTime()));
			section.setDateUpdated(new Timestamp(new Date().getTime()));
			section.setLastUpdated(user.getString("USER_ID"));

			String sectionBody = new ObjectMapper().writeValueAsString(section).toString();
			Document document = Document.parse(sectionBody);

			collection.insertOne(document);
			String sectionId = document.getObjectId("_id").toString();
			section.setId(sectionId);
			log.trace("Exit SectionService.postSection()");

			return section;

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");

	}

	@PutMapping("/sections")
	public Section putSection(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody Section section) {
		try {
			log.trace("Enter SectionService.putSection() ");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			if (!global.languages.contains(section.getLanguage())) {
				throw new BadRequestException("UNSUPPORTED_LANGUAGE");
			}

			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String categoryCollectionName = "categories_" + companyId;

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (section.getCategory() == null || !new ObjectId().isValid(section.getCategory())) {
				throw new BadRequestException("CATEGORY_DOES_NOT_EXISTS");
			}

			if (section.getId() == null || !new ObjectId().isValid(section.getId())) {
				throw new BadRequestException("SECTION_DOES_NOT_EXISTS");
			}

			List<ObjectId> teamIds = new ArrayList<ObjectId>();
			for (String team : section.getVisibleTo()) {
				if (!new ObjectId().isValid(team)) {
					throw new BadRequestException("TEAM_DOES_NOT_EXIST");
				}
				teamIds.add(new ObjectId(team));
			}
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			List<Document> teams = teamsCollection
					.find(Filters.and(Filters.in("_id", teamIds), Filters.eq("DELETED", false)))
					.into(new ArrayList<Document>());

			if (teams.size() != section.getVisibleTo().size()) {
				throw new BadRequestException("TEAM_DOES_NOT_EXIST");
			}

			List<ObjectId> managedByTeamIds = new ArrayList<ObjectId>();
			for (String roleId : section.getManagedBy()) {
				if (!new ObjectId().isValid(roleId)) {
					throw new BadRequestException("INVALID_ROLE_ID");
				}
				managedByTeamIds.add(new ObjectId(roleId));
			}

			List<Document> managedByTeams = teamsCollection.find(Filters.in("_id", managedByTeamIds))
					.into(new ArrayList<Document>());
			if (managedByTeams.size() != section.getManagedBy().size()) {
				throw new BadRequestException("TEAM_DOES_NOT_EXIST");
			}

			MongoCollection<Document> categoryCollection = mongoTemplate.getCollection(categoryCollectionName);
			Document categoryDoc = categoryCollection.find(Filters.eq("_id", new ObjectId(section.getCategory())))
					.first();
			if (categoryDoc == null) {
				throw new BadRequestException("CATEGORY_DOES_NOT_EXISTS");
			}

			List<String> categoryVisibleTo = (List<String>) categoryDoc.get("VISIBLE_TO");
			for (String visilbeTo : section.getVisibleTo()) {
				if (!categoryVisibleTo.contains(visilbeTo)) {
					throw new BadRequestException("TEAM_MUST_BE_SAME_AS_PARENT");
				}
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("sections_" + companyId);

			Document sectionDoc = collection.find(Filters.and(Filters.eq("NAME", section.getName()),
					Filters.ne("_id", new ObjectId(section.getId())))).first();
			if (sectionDoc != null) {
				throw new BadRequestException("SECTION_NAME_EXISTS");
			}

			Document sectionDocument = collection.find(Filters.eq("_id", new ObjectId(section.getId()))).first();

			if (sectionDocument == null) {
				throw new BadRequestException("SECTION_DOES_NOT_EXISTS");
			}

			String sectionId = sectionDocument.getObjectId("_id").toString();
			sectionDocument.remove("_id");

			Section existingSection = new ObjectMapper().readValue(sectionDocument.toJson(), Section.class);

			section.setDateCreated(existingSection.getDateCreated());
			section.setDateUpdated(new Timestamp(new Date().getTime()));
			section.setLastUpdated(userId);
			section.setCreated(existingSection.getCreated());
			section.setOrder(existingSection.getOrder());

			Document sectionUpdate = Document.parse(new ObjectMapper().writeValueAsString(section));
			collection.findOneAndReplace(Filters.eq("_id", new ObjectId(sectionId)), sectionUpdate);
			log.trace("Enter SectionService.putSection() ");

			MongoCollection<Document> articlesCollection = mongoTemplate.getCollection("articles_" + companyId);
			for (String teamId : existingSection.getVisibleTo()) {
				if (!section.getVisibleTo().contains(teamId)) {
					articlesCollection.updateMany(Filters.eq("SECTION", section.getId()),
							Updates.pull("VISIBLE_TO", teamId));
				}
			}

			return section;
		} catch (JSONException e) {

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/sections/{id}")
	public ResponseEntity<Object> deleteSection(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("id") String id) {
		try {
			log.trace("Enter SectionService.deleteSection() id: " + id);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "sections_" + companyId;
			String userRole = user.getString("ROLE");

			if (!role.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!new ObjectId().isValid(id)) {
				throw new BadRequestException("SECTION_DOES_NOT_EXISTS");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document sectionDocument = collection.find(Filters.eq("_id", new ObjectId(id))).first();
			if (sectionDocument == null) {
				throw new BadRequestException("SECTION_DOES_NOT_EXISTS");
			}

			collection.findOneAndDelete(Filters.eq("_id", new ObjectId(id)));

			MongoCollection<Document> articlesCollection = mongoTemplate.getCollection("articles_" + companyId);
			articlesCollection.deleteMany(Filters.eq("SECTION", id));

			log.trace("Enter SectionService.deleteSection() id: " + id);

			return new ResponseEntity(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
