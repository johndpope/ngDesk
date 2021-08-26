package com.ngdesk.dashboard;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.modules.DataService;
import com.ngdesk.modules.list.layouts.ListLayout;
import com.ngdesk.modules.rules.Condition;
import com.ngdesk.resources.MongoUtils;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class DashBoardWidgetService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	private final Logger log = LoggerFactory.getLogger(DashBoardWidgetService.class);

	@Autowired
	private RoleService roleService;

	@Autowired
	private DataService dataService;

	@GetMapping("/dashboards")
	public ResponseEntity<Object> getDashBoards(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {
		JSONArray dashboards = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String userRole = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "dashboards_widget_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			int pgSize = 100;
			int pg = 1;
			int skip = 0;
			totalSize = 0;

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

			List<Document> dashboarddocuments = (List<Document>) collection.find().into(new ArrayList<>());
			for (Document document : dashboarddocuments) {
				List<String> teamUsers = new ArrayList<String>();
				List<String> teamsList = (List<String>) document.get("TEAMS");
				for (String teamId : teamsList) {
					Document team = teamsCollection
							.find(Filters.and(Filters.eq("_id", new ObjectId(teamId)), Filters.eq("DELETED", false)))
							.first();
					if (team == null) {
						continue;
					}

					List<String> users = (List<String>) team.get("USERS");
					for (String teamUser : users) {
						teamUsers.add(teamUser);
					}
				}
				if (teamUsers.contains(userId) || roleService.isSystemAdmin(userRole, companyId)) {
					totalSize++;
					String dashBoardId = document.getObjectId("_id").toString();

					document.remove("_id");
					String widgetString = new ObjectMapper().writeValueAsString(document);
					DashBoardWidget dashBoard = new ObjectMapper().readValue(widgetString, DashBoardWidget.class);
					dashBoard.setDashboardId(dashBoardId);
					JSONObject dashBoardJson = new JSONObject(new ObjectMapper().writeValueAsString(dashBoard));

					dashboards.put(dashBoardJson);
				}

			}

			resultObj.put("DASHBOARDS", dashboards);
			resultObj.put("TOTAL_RECORDS", totalSize);

			return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);

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

	@PostMapping("/dashboards")
	public ResponseEntity<Object> postDashBoard(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody DashBoardWidget dashBoard) {
		try {
			log.trace("Enter DashBoardWidgetService.postDashBoard()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			MongoCollection<Document> collection = mongoTemplate.getCollection("dashboards_widget_" + companyId);
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);
			Document doc = collection.find(Filters.eq("NAME", dashBoard.getName())).first();

			if (doc != null) {

				throw new BadRequestException("DASHBOARD_NAME_ALREADY_EXISTS");
			}

			for (Widget widget : dashBoard.getWidgets()) {
				widget.setWidgetId(UUID.randomUUID().toString());
				String moduleId = widget.getModuleId();
				Document moduleDoc = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				if (moduleDoc == null) {

					throw new BadRequestException("MODULE_NOT_EXIST");
				}
				Document module = modulesCollection.find(
						Filters.and(Filters.elemMatch("FIELDS", Filters.in("FIELD_ID", widget.getCategorisedBy())),
								Filters.eq("_id", new ObjectId(moduleId))))
						.first();

				Document listLayout = modulesCollection.find(
						Filters.and(Filters.elemMatch("LIST_LAYOUTS", Filters.in("LAYOUT_ID", widget.getListLayout())),
								Filters.eq("_id", new ObjectId(moduleId))))
						.first();

				if (module == null && !widget.getType().equals("score")) {

					throw new BadRequestException("INVALID_FIELD");
				}

				if (listLayout == null) {

					throw new BadRequestException("INVALD_LISTLAYOUT");
				}

				if (widget.getType().equals("score") && widget.getCategorisedBy() != null) {

					throw new BadRequestException("CATEGORISED_SHOULD_BE_NULL");
				}
				if (widget.getType().equals("score") && widget.getRepresentedIn() != null) {

					throw new BadRequestException("RESPRESENTED_SHOULD_BE_NULL");
				}

				if (widget.getType().equals("bar") && widget.getRepresentedIn() == null) {

					throw new BadRequestException("RESPRESENTED_NOT_NULL");
				}

			}

			List<String> teamsList = new ArrayList<String>();
			List<String> userDoc = new ArrayList<String>();
			for (String team : dashBoard.getTeams()) {
				Document teamDoc = teamsCollection.find(Filters.eq("_id", new ObjectId(team))).first();
				if (teamDoc == null) {
					throw new BadRequestException("TEAM_DOES_NOT_EXIST");
				}

			}
			dashBoard.setDateCreated(new Timestamp(new Date().getTime()));
			dashBoard.setDateUpdated(new Timestamp(new Date().getTime()));
			dashBoard.setLastUpdatedBy(userId);
			dashBoard.setCreatedBy(userId);

			String payload = new ObjectMapper().writeValueAsString(dashBoard).toString();
			Document dashboardDoc = Document.parse(payload);

			dashboardDoc.remove("DASHBOARD_ID");
			collection.insertOne(dashboardDoc);
			String id = dashboardDoc.getObjectId("_id").toString();
			dashBoard.setDashboardId(id);
			log.trace("Exit DashBoardWidgetService.postDashBoard()");
			return new ResponseEntity<Object>(dashBoard, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/dashboards/{dashboard_id}")
	public DashBoardWidget putDashBoard(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody DashBoardWidget dashBoard, @PathVariable("dashboard_id") String dashboardId) {
		try {
			log.trace("Enter DashBoardWidgetService.putDashBoard()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			Document module = null;
			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);
			MongoCollection<Document> collection = mongoTemplate.getCollection("dashboards_widget_" + companyId);
			Document dashboard = collection.find(Filters.eq("_id", new ObjectId(dashboardId))).first();
			if (dashboard == null) {

				throw new BadRequestException("DASHBOARD_NOT_EXIST");
			}
			Document doc = collection.find(
					Filters.and(Filters.eq("NAME", dashBoard.getName()), Filters.ne("_id", new ObjectId(dashboardId))))
					.first();
			if (doc != null) {

				throw new BadRequestException("DASHBOARD_NAME_ALREADY_EXISTS");
			}
			MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);

			List<String> teamsList = dashBoard.getTeams();
			for (String team : teamsList) {
				Document teamDoc = teamsCollection.find(Filters.in("_id", new ObjectId(team))).first();
				if (teamDoc == null) {
					throw new BadRequestException("TEAM_DOES_NOT_EXIST");
				}
			}

			for (Widget widget : dashBoard.getWidgets()) {
				String moduleId = widget.getModuleId();
				Document moduleDoc = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
				if (moduleDoc == null) {

					throw new BadRequestException("MODULE_NOT_EXIST");
				}
				module = modulesCollection.find(
						Filters.and(Filters.elemMatch("FIELDS", Filters.in("FIELD_ID", widget.getCategorisedBy())),
								Filters.eq("_id", new ObjectId(moduleId))))
						.first();
				Document listLayout = modulesCollection.find(
						Filters.and(Filters.elemMatch("LIST_LAYOUTS", Filters.in("LAYOUT_ID", widget.getListLayout())),
								Filters.eq("_id", new ObjectId(moduleId))))
						.first();

				if (module == null && !widget.getType().equals("score")) {

					throw new BadRequestException("INVALID_FIELD");
				}

				if (listLayout == null) {

					throw new BadRequestException("INVALD_LISTLAYOUT");
				}

				if (widget.getType().equals("score") && widget.getCategorisedBy() != null) {

					throw new BadRequestException("CATEGORISED_SHOULD_BE_NULL");
				}

				if (widget.getType().equals("score") && widget.getRepresentedIn() != null) {

					throw new BadRequestException("RESPRESENTED_SHOULD_BE_NULL");
				}

				if (widget.getType().equals("bar") && widget.getRepresentedIn() == null) {

					throw new BadRequestException("RESPRESENTED_NOT_NULL");
				}

			}

			dashBoard.setDateUpdated(new Timestamp(new Date().getTime()));
			dashBoard.setLastUpdatedBy(userId);

			Document dashDocumentDocument = Document.parse(new ObjectMapper().writeValueAsString(dashBoard));
			collection.findOneAndReplace(Filters.eq("_id", new ObjectId(dashboardId)), dashDocumentDocument);
			dashBoard.setDashboardId(dashboardId);
			log.trace("Exit DashBoardWidgetService.putDashBoard()");
			return dashBoard;

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/dashboards/{dashboard_id}")
	public ResponseEntity<Object> deleteDashBoard(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("dashboard_id") String dashboardId) {
		try {
			log.trace("Enter DashBoardWidgetService.deleteDashBoard()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> collection = mongoTemplate.getCollection("dashboards_widget_" + companyId);
			Document dashboard = collection.find(Filters.eq("_id", new ObjectId(dashboardId))).first();
			if (dashboard == null) {

				throw new BadRequestException("DASHBOARD_NOT_EXIST");
			}
			collection.deleteOne(Filters.eq("_id", new ObjectId(dashboardId)));
			log.trace("Exit DashBoardWidgetService.deleteDashBoard()");

			return new ResponseEntity<>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/dashboards/data")
	public DashBoardWidget calculateteDataForWidget(HttpServletRequest request,
			@RequestParam("authentication_token") String uuid, @RequestBody DashBoardWidget dashboard) {
		try {
			log.trace("Enter DashBoardWidgetService.calculateteDataForWidget()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userRole = user.getString("ROLE");
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");

			List<Widget> widgets = dashboard.getWidgets();

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection("modules_" + companyId);

			for (Widget widget : widgets) {
				String moduleId = widget.getModuleId();
				if (moduleId == null) {
					throw new BadRequestException("INVALID_MODULE_ID");
				} else if (!ObjectId.isValid(moduleId)) {
					throw new BadRequestException("INVALID_MODULE_ID");
				}
				Document module = modulesCollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();

				if (module == null) {
					throw new BadRequestException("INVALID_MODULE_ID");
				}

				String moduleName = module.getString("NAME");
				String listLayoutId = widget.getListLayout();
				if (listLayoutId == null) {
					throw new BadRequestException("LIST_LAYOUT_ID_NULL");
				}

				List<Document> listLayouts = (List<Document>) module.get("LIST_LAYOUTS");

				Document listLayout = null;
				for (Document layout : listLayouts) {
					if (layout.getString("LAYOUT_ID").equals(listLayoutId)) {
						listLayout = layout;
						break;
					}
				}
				if (listLayout == null) {
					continue;
				}
				String listLayoutString = new ObjectMapper().writeValueAsString(listLayout);
				ListLayout layout = new ObjectMapper().readValue(listLayoutString, ListLayout.class);

				List<Condition> layoutConditions = new ArrayList<Condition>();

				if (layout.getConditions() != null) {
					layoutConditions = layout.getConditions();
				}

				List<Bson> allFilters = new ArrayList<Bson>();
				List<Bson> anyFilters = new ArrayList<Bson>();

				allFilters = dataService.generateAllFilter(layoutConditions, moduleId, allFilters, companyId, user);
				anyFilters = dataService.generateAnyFilter(layoutConditions, moduleId, anyFilters, companyId, user);
				allFilters.add(Filters.eq("DELETED", false));
				allFilters.add(Filters.or(Filters.exists("EFFECTIVE_TO", false), Filters.eq("EFFECTIVE_TO", null)));

				if (moduleName.equals("Users")) {
					List<String> emails = new ArrayList<String>();
					emails.add("ghost@ngdesk.com");
					emails.add("system@ngdesk.com");
					emails.add("probe@ngdesk.com");
					emails.add("register_controller@ngdesk.com");
					allFilters.add(Filters.nin("EMAIL_ADDRESS", emails));
				} else if (moduleName.equals("Teams")) {
					List<String> name = new ArrayList<String>();
					name.add("Ghost Team");
					name.add("Public");
					allFilters.add(Filters.nin("NAME", name));
				}
				MongoCollection<Document> entriesCollection = mongoTemplate
						.getCollection(moduleName.replaceAll("\\s+", "_") + "_" + companyId);
				int totalEntries = (int) entriesCollection.countDocuments();
				if (widget.getType().equals("score")) {

					int count = 0;
					if (anyFilters.size() > 0) {
						count = (int) entriesCollection
								.countDocuments(Filters.and(Filters.and(allFilters), Filters.or(anyFilters)));
					} else {
						count = (int) entriesCollection.countDocuments(Filters.and(allFilters));
					}

					HashMap<String, Integer> dataMap = new HashMap<String, Integer>();
					dataMap.put("COUNT", count);

					widget.setData(dataMap);
				} else if (widget.getType().equals("bar")) {
					String fieldId = widget.getCategorisedBy();

					if (fieldId == null) {
						throw new BadRequestException("FIELD_EMPTY");
					}

					Document field = null;
					List<Document> fields = (List<Document>) module.get("FIELDS");

					for (Document moduleField : fields) {
						if (moduleField.getString("FIELD_ID").equals(fieldId)) {
							field = moduleField;
							break;
						}
					}

					if (field == null) {
						throw new BadRequestException("INVALID_FIELD_ID");
					}

					String fieldName = field.getString("NAME");

					Document dataType = (Document) field.get("DATA_TYPE");
					if (!dataType.getString("DISPLAY").equals("Picklist")) {
						throw new BadRequestException("CATEGORIZED_BY_INVALID");
					}

					List<String> picklistValues = (List<String>) field.get("PICKLIST_VALUES");

					HashMap<String, Integer> dataMap = new HashMap<String, Integer>();
					if (picklistValues != null) {
						for (String value : picklistValues) {
							int count = 0;
							if (anyFilters.size() > 0) {
								count = (int) entriesCollection.countDocuments(Filters.and(Filters.and(allFilters),
										Filters.or(anyFilters), Filters.eq(fieldName, value)));
							} else {
								count = (int) entriesCollection.countDocuments(
										Filters.and(Filters.and(allFilters), Filters.eq(fieldName, value)));
							}
							if (totalEntries == 0) {
								count = 0;
							}
							dataMap.put(value, count);
						}
					}
					widget.setData(dataMap);
				}

			}

			log.trace("Exit DashBoardWidgetService.calculateteDataForWidget()");
			return dashboard;

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
