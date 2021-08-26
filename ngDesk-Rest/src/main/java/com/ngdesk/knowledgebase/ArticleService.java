package com.ngdesk.knowledgebase;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.http.HttpHost;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.ngdesk.modules.Attachment;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class ArticleService {

	@Autowired
	private Authentication auth;

	@Autowired
	RoleService role;

	@Autowired
	Global global;

	@Autowired
	MongoTemplate mongoTemplate;

	@Value("${elastic.host}")
	private String elasticHost;

	private final Logger log = LoggerFactory.getLogger(ArticleService.class);

	@GetMapping("/articles")
	public ResponseEntity<Object> getArticles(HttpServletRequest request,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "published_only", required = true) boolean published_only,
			@RequestParam(value = "section", required = false) String sectionId,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			log.trace("Enter ArticleService.getArticles()");

			JSONObject resultObject = new JSONObject();

			String subdomain = request.getAttribute("SUBDOMAIN").toString();
			Document company = global.getCompanyFromSubdomain(subdomain);
			if (company == null) {
				throw new ForbiddenException("COMPANY_DOES_NOT_EXIST");
			}
			String companyId = company.getObjectId("_id").toString();

			int pgSize = 200;
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

			List<String> teams = new ArrayList<String>();

			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			Document publicTeam = teamsCollection.find(Filters.eq("NAME", "Public")).first();
			if (publicTeam != null) {
				String publicTeamId = publicTeam.getObjectId("_id").toString();
				teams.add(publicTeamId);
			}

			boolean isSystemAdmin = false;
			boolean isAgent = false;
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			if (uuid != null) {
				JSONObject user = auth.getUserDetails(uuid);
				String userId = user.getString("USER_ID");
				String userRole = user.getString("ROLE");

				MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
				Document systemAdminRole = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first();
				String systemAdminRoleId = systemAdminRole.getObjectId("_id").toString();

				Document agentnRole = rolesCollection.find(Filters.eq("NAME", "Agent")).first();
				String agentRoleId = agentnRole.getObjectId("_id").toString();

				if (userRole.equals(systemAdminRoleId)) {
					isSystemAdmin = true;
				} else {

					if (userRole.equals(agentRoleId)) {
						isAgent = true;
					}

					MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
					Document userDocument = usersCollection.find(Filters.eq("_id", new ObjectId(userId))).first();

					List<String> userTeams = (List<String>) userDocument.get("TEAMS");
					for (String teamId : userTeams) {
						teams.add(teamId);
					}
				}
			}

			if (sectionId != null && !new ObjectId().isValid(sectionId)) {
				throw new BadRequestException("SECTION_DOES_NOT_EXISTS");
			}

			Bson sectionFilter = new Document();
			if (sectionId != null) {
				MongoCollection<Document> sectionsCollection = mongoTemplate.getCollection("sections_" + companyId);
				Document sectionDocument = sectionsCollection.find(Filters.eq("_id", new ObjectId(sectionId))).first();

				if (sectionDocument == null) {
					throw new BadRequestException("SECTION_DOES_NOT_EXISTS");
				}

				if (isSystemAdmin) {
					if (published_only) {
						sectionFilter = Filters.and(Filters.eq("SECTION", sectionId), Filters.eq("PUBLISH", true));
					} else {
						sectionFilter = Filters.eq("SECTION", sectionId);
					}
				} else if (isAgent) {
					if (published_only) {
						sectionFilter = Filters.and(Filters.eq("SECTION", sectionId), Filters.in("VISIBLE_TO", teams),
								Filters.eq("PUBLISH", true));
					} else {
						sectionFilter = Filters.and(Filters.eq("SECTION", sectionId), Filters.in("VISIBLE_TO", teams));
					}
				} else {
					sectionFilter = Filters.and(Filters.eq("SECTION", sectionId), Filters.in("VISIBLE_TO", teams),
							Filters.eq("PUBLISH", true));
				}
			} else {
				if (isSystemAdmin) {
					if (published_only) {
						sectionFilter = Filters.eq("PUBLISH", true);
					} else {
						sectionFilter = Filters.or(Filters.eq("PUBLISH", true), Filters.eq("PUBLISH", false));
					}
				} else if (isAgent) {
					if (published_only) {
						sectionFilter = Filters.and(Filters.in("VISIBLE_TO", teams), Filters.eq("PUBLISH", true));
					} else {
						sectionFilter = Filters.in("VISIBLE_TO", teams);
					}
				} else {
					sectionFilter = Filters.and(Filters.in("VISIBLE_TO", teams), Filters.eq("PUBLISH", true));
				}
			}

			List<Document> docs = new ArrayList<Document>();
			List<ObjectId> ids = new ArrayList<ObjectId>();
			Bson searchFilter = new Document();
			if (search != null && search.length() > 0) {
				ids = getIdsFromElastic(companyId, search, teams, sectionId, isSystemAdmin);
				searchFilter = Filters.in("_id", ids);
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("articles_" + companyId);
			if (sort != null && order != null) {

				if (order.equalsIgnoreCase("asc")) {
					docs = (List<Document>) collection.find(Filters.and(searchFilter, sectionFilter))
							.sort(Sorts.orderBy(Sorts.ascending(sort))).skip(skip).limit(pgSize)
							.into(new ArrayList<Document>());
					resultObject.put("TOTAL_RECORDS",
							collection.countDocuments(Filters.and(sectionFilter, searchFilter)));
				} else if (order.equalsIgnoreCase("desc")) {
					docs = (List<Document>) collection.find(Filters.and(searchFilter, sectionFilter))
							.sort(Sorts.orderBy(Sorts.descending(sort))).skip(skip).limit(pgSize)
							.into(new ArrayList<Document>());
					resultObject.put("TOTAL_RECORDS",
							collection.countDocuments(Filters.and(searchFilter, sectionFilter)));
				} else {
					throw new BadRequestException("INVALID_SORT_ORDER");
				}

			} else {
				docs = collection.find(Filters.and(searchFilter, sectionFilter)).skip(skip).limit(pgSize)
						.into(new ArrayList<Document>());
				resultObject.put("TOTAL_RECORDS", collection.countDocuments(Filters.and(sectionFilter, searchFilter)));
			}

			JSONArray articles = new JSONArray();
			for (Document doc : docs) {
				String id = doc.getObjectId("_id").toString();
				doc.remove("_id");
				ObjectMapper mapper = new ObjectMapper();
				Article article = mapper.readValue(mapper.writeValueAsString(doc), Article.class);
				article.setArticleId(id);
				JSONObject json = new JSONObject(mapper.writeValueAsString(article));
				articles.put(json);
			}
			resultObject.put("DATA", articles);

			log.trace("Exit ArticleService.getArticles()");
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

	@GetMapping("/articles/{id}")
	public Article getArticle(HttpServletRequest request, @PathVariable String id,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {
			log.trace("Enter ArticleService.getArticle() id: " + id);

			String subdomain = request.getAttribute("SUBDOMAIN").toString();
			Document company = global.getCompanyFromSubdomain(subdomain);
			if (company == null) {
				throw new ForbiddenException("COMPANY_DOES_NOT_EXIST");
			}
			String companyId = company.getObjectId("_id").toString();

			if (!new ObjectId().isValid(id)) {
				throw new BadRequestException("ARTICLE_DOES_NOT_EXISTS");
			}
			String userRole = null;

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}

			if (uuid != null) {

				JSONObject user = auth.getUserDetails(uuid);
				String userId = user.getString("USER_ID");
				ObjectMapper mapper = new ObjectMapper();
				String role = user.getString("ROLE");
				MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
				userRole = rolesCollection.find(Filters.eq("_id", new ObjectId(role))).first().getString("NAME");
			}
			MongoCollection<Document> collection = mongoTemplate.getCollection("articles_" + companyId);

			Document articleDocument = collection.find(Filters.eq("_id", new ObjectId(id))).first();
			if (articleDocument == null) {
				throw new BadRequestException("ARTICLE_DOES_NOT_EXISTS");
			}
			String sectionId = articleDocument.getObjectId("_id").toString();
			articleDocument.remove("_id");
			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
			if (articleDocument.containsKey("AUTHOR") && articleDocument.getString("AUTHOR") != null) {
				String author = articleDocument.getString("AUTHOR");
				if (ObjectId.isValid(author)) {

					Document user = usersCollection
							.find(Filters.and(Filters.eq("_id", new ObjectId(author)), Filters.eq("DELETED", false)))
							.first();
					String contactId = user.getString("CONTACT");
					MongoCollection<Document> contactsCollection = mongoTemplate.getCollection("Contacts_" + companyId);
					Document contactDetails = contactsCollection.find(Filters.eq("_id", new ObjectId(contactId)))
							.first();
					user.put("FIRST_NAME", contactDetails.getString("FIRST_NAME"));
					user.put("LAST_NAME", contactDetails.getString("LAST_NAME"));

					if (uuid != null) {
						ObjectMapper mapper = new ObjectMapper();
						String authorName = user.getString("FIRST_NAME") + " " + user.getString("LAST_NAME");
						// TO CHECK IF USER IS NOT SYSTEM ADMIN AND AGENT
						if (uuid != null && (userRole.equals("SystemAdmin") || userRole.equals("Agent"))) {
							// REPLACE AUTHOR WITH USER OBJECT
							String dataId = user.getObjectId("_id").toString();
							user.remove("_id");
							user.put("DATA_ID", dataId);
							articleDocument.put("AUTHOR", user.toJson());
						} else {
							// REPLACE AUTHOR WITH NAME
							articleDocument.put("AUTHOR", authorName);
						}
					} else {
						String dataId = user.getObjectId("_id").toString();
						user.remove("_id");
						user.put("DATA_ID", dataId);
						String authorName = user.getString("FIRST_NAME") + " " + user.getString("LAST_NAME");
						articleDocument.put("AUTHOR", authorName);
					}
				}
			}

			if (articleDocument.get("COMMENTS") != null) {

				List<Document> comments = (List<Document>) articleDocument.get("COMMENTS");
				for (Document comment : comments) {
					if (comment.containsKey("SENDER") && comment.getString("SENDER") != null) {
						String sender = comment.getString("SENDER");

						if (ObjectId.isValid(sender)) {
							Document user = usersCollection.find(
									Filters.and(Filters.eq("_id", new ObjectId(sender)), Filters.eq("DELETED", false)))
									.first();
							String contactId = user.getString("CONTACT");
							MongoCollection<Document> contactsCollection = mongoTemplate
									.getCollection("Contacts_" + companyId);
							Document contactDetails = contactsCollection
									.find(Filters.eq("_id", new ObjectId(contactId))).first();
							user.put("FIRST_NAME", contactDetails.getString("FIRST_NAME"));
							user.put("LAST_NAME", contactDetails.getString("LAST_NAME"));
							if (user != null) {
								String senderName = user.getString("FIRST_NAME") + " " + user.getString("LAST_NAME");

								// TO CHECK IF USER IS NOT SYSTEM ADMIN AND AGENT
								if (uuid != null && (userRole.equals("SystemAdmin") || userRole.equals("Agent"))) {
									// REPLACE SENDER WITH USER OBJECT
									String dataId = user.getObjectId("_id").toString();
									user.remove("_id");
									user.put("DATA_ID", dataId);
									comment.put("SENDER", user.toJson());
								} else {
									// REPLACE SENDER WITH NAME
									comment.put("SENDER", senderName);
								}
							}
						}
					}
				}
			}

			ObjectMapper mapper = new ObjectMapper();
			Article article = mapper.readValue(mapper.writeValueAsString(articleDocument), Article.class);
			article.setArticleId(sectionId);

			MongoCollection<Document> attachmentsCollection = mongoTemplate.getCollection("attachments_" + companyId);

			if (article.getAttachments() != null) {
				for (Attachment attachment : article.getAttachments()) {
					Document attachmentDoc = attachmentsCollection.find(Filters.eq("HASH", attachment.getHash()))
							.first();
					if (attachmentDoc != null) {
						attachment.setAttachmentUuid(attachmentDoc.getString("ATTACHMENT_UUID"));
					}
				}
			}

			log.trace("Exit ArticleService.getArticle()");

			return article;

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

	@PostMapping("/articles")
	public Article postArticle(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody Article article) {
		try {
			log.trace("Enter ArticleService.postSection()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userRole = user.getString("ROLE");
			String userId = user.getString("USER_ID");

			MongoCollection<Document> collection = mongoTemplate.getCollection("articles_" + companyId);
			Document articleDoc = collection.find(Filters.eq("TITLE", article.getTitle())).first();
			if (articleDoc != null) {
				throw new BadRequestException("ARTICLE_NAME_EXISTS");
			}

			if (article.getSection() == null || !new ObjectId().isValid(article.getSection())) {
				throw new BadRequestException("SECTION_DOES_NOT_EXISTS");
			}

			MongoCollection<Document> sectionsCollection = mongoTemplate.getCollection("sections_" + companyId);
			Document sectionDoc = sectionsCollection.find(Filters.eq("_id", new ObjectId(article.getSection())))
					.first();
			if (sectionDoc == null) {
				throw new BadRequestException("SECTION_DOES_NOT_EXISTS");
			}

			List<ObjectId> teamIds = new ArrayList<ObjectId>();
			for (String team : article.getVisibleTo()) {
				if (!new ObjectId().isValid(team)) {
					throw new BadRequestException("TEAM_DOES_NOT_EXIST");
				}
				teamIds.add(new ObjectId(team));
			}
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			List<Document> teams = teamsCollection
					.find(Filters.and(Filters.in("_id", teamIds), Filters.eq("DELETED", false)))
					.into(new ArrayList<Document>());

			if (teams.size() != article.getVisibleTo().size()) {
				throw new BadRequestException("TEAM_DOES_NOT_EXIST");
			}

			List<String> sectionVisibleTo = (List<String>) sectionDoc.get("VISIBLE_TO");
			for (String visilbeTo : article.getVisibleTo()) {
				if (!sectionVisibleTo.contains(visilbeTo)) {
					throw new BadRequestException("TEAM_MUST_BE_SAME_AS_PARENT");
				}
			}

			List<String> teamsWhoCanManageArticle = (List<String>) sectionDoc.get("MANAGED_BY");
			boolean isAuthorized = false;

			if (role.isSystemAdmin(userRole, companyId)) {
				isAuthorized = true;
			} else {
				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
				Document userDocument = usersCollection
						.find(Filters.and(Filters.eq("DELETED", false), Filters.eq("_id", new ObjectId(userId))))
						.first();
				if (userDocument != null) {
					List<String> userTeams = (List<String>) userDocument.get("TEAMS");
					for (String team : userTeams) {
						if (teamsWhoCanManageArticle.contains(team)) {
							isAuthorized = true;
							break;
						}
					}
				}
			}

			if (!isAuthorized) {
				throw new ForbiddenException("FORBIDDEN");
			}

			int totalCount = (int) collection.countDocuments(Filters.eq("SECTION", article.getSection()));
			article.setOrder(totalCount + 1);

			article.setCreated(user.getString("USER_ID"));
			article.setDateCreated(new Timestamp(new Date().getTime()));
			article.setDateUpdated(new Timestamp(new Date().getTime()));
			article.setLastUpdated(user.getString("USER_ID"));

			MongoCollection<Document> attachmentsCollection = mongoTemplate.getCollection("attachments_" + companyId);
			if (article.getAttachments() != null) {

				for (Attachment attachment : article.getAttachments()) {
					if (attachment.getFile() == null) {
						throw new BadRequestException("ATTACHMENT_FILE_REQUIRED");
					}
				}

				for (Attachment attachment : article.getAttachments()) {
					String hash = global.passwordHash(attachment.getFile());
					Document attachmentDoc = attachmentsCollection.find(Filters.eq("HASH", hash)).first();

					if (attachmentDoc == null) {
						JSONObject newAttachment = new JSONObject();
						newAttachment.put("HASH", hash);
						newAttachment.put("ATTACHMENT_UUID", UUID.randomUUID().toString());
						newAttachment.put("FILE", attachment.getFile());
						attachmentsCollection.insertOne(Document.parse(newAttachment.toString()));
						attachment.setHash(hash);
					} else {
						attachment.setHash(attachmentDoc.getString("HASH"));
					}
					attachment.setFile(null);
				}
			}

			String articleBody = new ObjectMapper().writeValueAsString(article).toString();

			String id = postArticleToElasticAndMongo(companyId, articleBody);
			String articleId = id;
			article.setArticleId(articleId);
			log.trace("Exit ArticleService.postSection()");

			return article;

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/articles/{article_id}/comments")
	public Comment postComment(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("article_id") String articleId, @Valid @RequestBody Comment comment) {
		try {
			log.trace("Enter ArticleService.postComment() ");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");

			if (!new ObjectId().isValid(articleId)) {
				throw new BadRequestException("ARTICLE_DOES_NOT_EXISTS");
			}

			MongoCollection<Document> articlesCollection = mongoTemplate.getCollection("articles_" + companyId);
			Document articleDocument = articlesCollection.find(Filters.eq("_id", new ObjectId(articleId))).first();

			if (articleDocument == null) {
				throw new BadRequestException("ARTICLE_DOES_NOT_EXISTS");
			}

			comment.setSender(userId);
			comment.setDateCreated(new Timestamp(new Date().getTime()));
			comment.setMessageId(UUID.randomUUID().toString());

			String commentJson = new ObjectMapper().writeValueAsString(comment);
			articlesCollection.updateOne(Filters.eq("_id", new ObjectId(articleId)),
					Updates.addToSet("COMMENTS", Document.parse(commentJson)));

			postCommentsToElastic(articleId, companyId);

			log.trace("Exit ArticleService.postComment() ");
			return comment;
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

	@PutMapping("/articles")
	public Article putArticle(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody Article article) {
		try {
			log.trace("Enter ArticleService.putSection() ");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");

			if (article.getSection() == null || !new ObjectId().isValid(article.getSection())) {
				throw new BadRequestException("SECTION_DOES_NOT_EXISTS");
			}

			if (article.getArticleId() == null || !new ObjectId().isValid(article.getArticleId())) {
				throw new BadRequestException("ARTICLE_DOES_NOT_EXISTS");
			}

			MongoCollection<Document> sectionsCollection = mongoTemplate.getCollection("sections_" + companyId);
			Document sectionDoc = sectionsCollection.find(Filters.eq("_id", new ObjectId(article.getSection())))
					.first();
			if (sectionDoc == null) {
				throw new BadRequestException("SECTION_DOES_NOT_EXISTS");
			}

			sectionDoc.remove("_id");
			Section section = new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(sectionDoc),
					Section.class);
			section.setId(article.getSection());

			List<String> sectionVisibleTo = (List<String>) sectionDoc.get("VISIBLE_TO");
			for (String visilbeTo : article.getVisibleTo()) {
				if (!sectionVisibleTo.contains(visilbeTo)) {
					throw new BadRequestException("TEAM_MUST_BE_SAME_AS_PARENT");
				}
			}

			List<ObjectId> teamIds = new ArrayList<ObjectId>();
			for (String team : article.getVisibleTo()) {
				if (!new ObjectId().isValid(team)) {
					throw new BadRequestException("TEAM_DOES_NOT_EXIST");
				}
				teamIds.add(new ObjectId(team));
			}
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			List<Document> teams = teamsCollection
					.find(Filters.and(Filters.in("_id", teamIds), Filters.eq("DELETED", false)))
					.into(new ArrayList<Document>());

			if (teams.size() != article.getVisibleTo().size()) {
				throw new BadRequestException("TEAM_DOES_NOT_EXIST");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("articles_" + companyId);

			Document articleDoc = collection.find(Filters.and(Filters.eq("TITLE", article.getTitle()),
					Filters.ne("_id", new ObjectId(article.getArticleId())))).first();
			if (articleDoc != null) {
				throw new BadRequestException("ARTICLE_NAME_EXISTS");
			}

			Document articleDocument = collection.find(Filters.eq("_id", new ObjectId(article.getArticleId()))).first();
			if (articleDocument == null) {
				throw new BadRequestException("ARTICLE_DOES_NOT_EXISTS");
			}

			String articleId = articleDocument.getObjectId("_id").toString();
			articleDocument.remove("_id");

			Article existingArticle = new ObjectMapper().readValue(articleDocument.toJson(), Article.class);

			boolean isAuthorized = false;

			if (role.isSystemAdmin(userRole, companyId)) {
				isAuthorized = true;
			} else {
				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
				Document userDocument = usersCollection
						.find(Filters.and(Filters.eq("_id", new ObjectId(userId)), Filters.eq("DELETED", false)))
						.first();
				if (userDocument != null) {
					List<String> userTeams = (List<String>) userDocument.get("TEAMS");
					for (String team : userTeams) {
						if (section.getManagedBy().contains(team)) {
							isAuthorized = true;
							break;
						}
					}
				}
			}

			if (!isAuthorized) {
				throw new ForbiddenException("FORBIDDEN");
			}

			article.setComments(existingArticle.getComments());
			article.setDateCreated(existingArticle.getDateCreated());
			article.setDateUpdated(new Timestamp(new Date().getTime()));
			article.setLastUpdated(userId);
			article.setCreated(existingArticle.getCreated());
			article.setOrder(existingArticle.getOrder());

			MongoCollection<Document> attachmentsCollection = mongoTemplate.getCollection("attachments_" + companyId);
			if (article.getAttachments() != null) {

				for (Attachment attachment : article.getAttachments()) {
					if (attachment.getFile() == null && attachment.getHash() == null) {
						throw new BadRequestException("ATTACHMENT_FILE_REQUIRED");
					}
				}

				for (Attachment attachment : article.getAttachments()) {
					if (attachment.getFile() != null) {
						String hash = global.passwordHash(attachment.getFile());
						Document attachmentDoc = attachmentsCollection.find(Filters.eq("HASH", hash)).first();

						if (attachmentDoc == null) {
							JSONObject newAttachment = new JSONObject();
							newAttachment.put("HASH", hash);
							newAttachment.put("ATTACHMENT_UUID", UUID.randomUUID().toString());
							newAttachment.put("FILE", attachment.getFile());
							attachmentsCollection.insertOne(Document.parse(newAttachment.toString()));
							attachment.setHash(hash);
						} else {
							attachment.setHash(attachmentDoc.getString("HASH"));
						}
						attachment.setFile(null);
					}
				}
			}

			String body = new ObjectMapper().writeValueAsString(article);

			putArticleToElasticAndMongo(companyId, body, articleId);

			log.trace("Enter ArticleService.putArticle() ");

			return article;
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

	@DeleteMapping("/articles/{id}")
	public ResponseEntity<Object> deleteArticle(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid, @PathVariable String id) {
		try {
			log.trace("Enter ArticleService.deleteArticle() id: " + id);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userRole = user.getString("ROLE");
			String userId = user.getString("USER_ID");

			if (!new ObjectId().isValid(id)) {
				throw new BadRequestException("ARTICLE_DOES_NOT_EXISTS");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("articles_" + companyId);

			Document articleDocument = collection.find(Filters.eq("_id", new ObjectId(id))).first();
			if (articleDocument == null) {
				throw new BadRequestException("ARTICLE_DOES_NOT_EXISTS");
			}

			MongoCollection<Document> sectionsCollection = mongoTemplate.getCollection("sections_" + companyId);
			Document sectionDocument = sectionsCollection
					.find(Filters.eq("_id", new ObjectId(articleDocument.getString("SECTION")))).first();

			List<String> teamsWhoCanManage = (List<String>) sectionDocument.get("MANAGED_BY");
			boolean isAuthorized = false;

			if (role.isSystemAdmin(userRole, companyId)) {
				isAuthorized = true;
			} else {
				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
				Document userDocument = usersCollection
						.find(Filters.and(Filters.eq("_id", new ObjectId(userId)), Filters.eq("DELETED", false)))
						.first();
				if (userDocument != null) {
					List<String> teams = (List<String>) userDocument.get("TEAMS");

					for (String team : teams) {
						if (teamsWhoCanManage.contains(team)) {
							isAuthorized = true;
							break;
						}
					}

				}
			}

			if (!isAuthorized) {
				throw new ForbiddenException("FORBIDDEN");
			}

			deleteArticleFromElasticAndMongo(companyId, id);
			log.trace("Enter ArticleService.deleteArticle() id: " + id);

			return new ResponseEntity(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	private List<ObjectId> getIdsFromElastic(String companyId, String value, List<String> teams, String sectionId,
			boolean isSystemAdmin) throws IOException {
		RestHighLevelClient elasticClient = null;
		List<ObjectId> ids = new ArrayList<ObjectId>();
		try {
			log.trace("Enter ArticleService.getIdsFromElastic()");

			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

			BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("INPUT", value));

			if (!isSystemAdmin) {
				boolQueryBuilder.must().add(QueryBuilders.termsQuery("VISIBLE_TO", teams));
			}

			boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));
			if (sectionId != null) {
				boolQueryBuilder.must().add(QueryBuilders.matchQuery("SECTION", sectionId));
			}

			sourceBuilder.query(boolQueryBuilder);

			SearchRequest searchRequest = new SearchRequest();
			searchRequest.indices("articles");
			searchRequest.source(sourceBuilder);

			SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);

			SearchHits hits = searchResponse.getHits();

			SearchHit[] searchHits = hits.getHits();
			for (SearchHit hit : searchHits) {
				Map<String, Object> sourceAsMap = hit.getSourceAsMap();
				String dataId = sourceAsMap.get("ARTICLE_ID").toString();
				ids.add(new ObjectId(dataId));
			}
			log.trace("Exit ArticleService.getIdsFromElastic()" + companyId);
		} catch (JSONException e) {

		} finally {
			elasticClient.close();
		}
		return ids;

	}

	public String postArticleToElasticAndMongo(String companyId, String body) {
		RestHighLevelClient elasticClient = null;
		try {
			log.trace("Enter ArticleService.getIdsFromSearch(): " + companyId);
			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			MongoCollection<Document> collection = mongoTemplate.getCollection("articles_" + companyId);
			Document bodyDoc = Document.parse(body);
			collection.insertOne(bodyDoc);
			String id = bodyDoc.getObjectId("_id").toString();

			// INSERT
			insertArticlesToElastic(body, companyId, id);

			log.trace("Exit ArticleService.postArticleToElastic(): " + companyId);
			return id;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	// USED TO UPDATE DATA IN MONGO AND ELASTIC
	public String putArticleToElasticAndMongo(String companyId, String body, String articleId) {
		RestHighLevelClient elasticClient = null;
		try {

			String collectionName = "articles_" + companyId;

			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document bodyDoc = Document.parse(body);

			collection.findOneAndReplace(Filters.eq("_id", new ObjectId(articleId)), bodyDoc);

			DeleteByQueryRequest request = new DeleteByQueryRequest("articles");

			BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("ARTICLE_ID", articleId));
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));
			request.setQuery(boolQueryBuilder);
			elasticClient.deleteByQuery(request, RequestOptions.DEFAULT);
			insertArticlesToElastic(body, companyId, articleId);

			return bodyDoc.toJson();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	// DELETE DATA FROM MONGO AND ELASTIC
	public String deleteArticleFromElasticAndMongo(String companyId, String articleId) {
		RestHighLevelClient elasticClient = null;
		try {
			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			MongoCollection<Document> collection = mongoTemplate.getCollection("articles_" + companyId);

			collection.findOneAndDelete(Filters.eq("_id", new ObjectId(articleId)));

			DeleteByQueryRequest request = new DeleteByQueryRequest("articles");

			BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("ARTICLE_ID", articleId));
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));
			request.setQuery(boolQueryBuilder);
			elasticClient.deleteByQuery(request, RequestOptions.DEFAULT);

			return articleId;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public void postCommentsToElastic(String articleId, String companyId) {
		RestHighLevelClient elasticClient = null;
		try {
			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));
			DeleteByQueryRequest request = new DeleteByQueryRequest("articles");

			BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("ARTICLE_ID", articleId));
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("FIELD_NAME", "COMMENTS"));
			request.setQuery(boolQueryBuilder);
			elasticClient.deleteByQuery(request, RequestOptions.DEFAULT);

			MongoCollection<Document> collection = mongoTemplate.getCollection("articles_" + companyId);

			Document article = collection.find(Filters.eq("_id", new ObjectId(articleId))).first();

			JSONObject bodyJson = new JSONObject(article.toJson());

			JSONArray comments = bodyJson.getJSONArray("COMMENTS");
			String commentConverted = "";
			Map<String, Object> entryMap = new HashMap<String, Object>();

			if (bodyJson.has("VISIBLE_TO")) {
				entryMap.put("VISIBLE_TO", bodyJson.get("VISIBLE_TO"));
			}
			if (bodyJson.has("SECTION")) {
				entryMap.put("SECTION", bodyJson.get("SECTION"));
			}

			for (int i = 0; i < comments.length(); i++) {
				JSONObject message = comments.getJSONObject(i);
				commentConverted += Jsoup.parse(message.getString("MESSAGE")).text();
				commentConverted += " ";
			}
			entryMap.put("INPUT", commentConverted);
			entryMap.put("FIELD_NAME", "COMMENTS");
			entryMap.put("COMPANY_ID", companyId);
			entryMap.put("ARTICLE_ID", articleId);

			IndexRequest indexRequest = new IndexRequest("articles").source(entryMap, XContentType.JSON);
			elasticClient.index(indexRequest, RequestOptions.DEFAULT);
			return;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	private void insertArticlesToElastic(String body, String companyId, String id) throws IOException {
		RestHighLevelClient elasticClient = null;
		try {
			elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticHost, 9200, "http")));
			JSONObject bodyJson = new JSONObject(body);
			Map<String, Object> entryMap = new HashMap<String, Object>();
			if (bodyJson.has("VISIBLE_TO")) {
				entryMap.put("VISIBLE_TO", bodyJson.get("VISIBLE_TO"));
			}
			if (bodyJson.has("SECTION")) {
				entryMap.put("SECTION", bodyJson.get("SECTION"));
			}
			for (String key : bodyJson.keySet()) {
				if (key.equals("TITLE") || key.equals("BODY")) {
					entryMap.put("FIELD_NAME", key);
					if (key.equals("BODY")) {
						entryMap.put("INPUT", Jsoup.parse(bodyJson.getString(key)).text());
					} else {
						entryMap.put("INPUT", bodyJson.get(key));
					}
					entryMap.put("COMPANY_ID", companyId);
					entryMap.put("ARTICLE_ID", id);
					IndexRequest titleIndexRequest = new IndexRequest("articles").source(entryMap, XContentType.JSON);
					elasticClient.index(titleIndexRequest, RequestOptions.DEFAULT);
				} else if (key.equals("COMMENTS")) {
					JSONArray comments = bodyJson.getJSONArray("COMMENTS");
					String commentConverted = "";
					for (int i = 0; i < comments.length(); i++) {
						JSONObject message = comments.getJSONObject(i);
						commentConverted += Jsoup.parse(message.getString("MESSAGE")).text();
					}
					entryMap.put("INPUT", commentConverted);
					entryMap.put("FIELD_NAME", key);
					entryMap.put("COMPANY_ID", companyId);
					entryMap.put("ARTICLE_ID", id);

					IndexRequest indexRequest = new IndexRequest("articles").source(entryMap, XContentType.JSON);
					elasticClient.index(indexRequest, RequestOptions.DEFAULT);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			elasticClient.close();
		}
	}
}
