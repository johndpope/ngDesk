package com.ngdesk.reporting;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.elasticsearch.search.DocValueFormat.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.exceptions.UnauthorizedException;
import com.ngdesk.resources.MongoUtils;
import com.ngdesk.roles.RoleService;
import com.opencsv.CSVWriter;

@Component
@RestController
public class ReportService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private RoleService roleService;

	@Autowired
	private Authentication auth;

	private final Logger log = LoggerFactory.getLogger(ReportService.class);

	@GetMapping("/reports")
	public ResponseEntity<Object> getReports(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONArray reports = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();

		try {
			log.trace("Enter ReportService.getReports()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "reports_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			int pgSize = 100;
			int pg = 1;
			int skip = 0;
			totalSize = (int) collection.countDocuments();

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

			for (Document document : documents) {

				String reportId = document.getObjectId("_id").toString();
				document.remove("_id"); // Check before commit
				Report report = new ObjectMapper().readValue(document.toJson(), Report.class);
				report.setReportId(reportId);
				JSONObject reportJson = new JSONObject(new ObjectMapper().writeValueAsString(report));

				reports.put(reportJson);
			}

			resultObj.put("REPORTS", reports);
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
		log.trace("Exit ReportService.getReports()");
		return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);

	}

	@GetMapping("/reports/{name}")
	public Report getReport(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String name) {
		try {
			log.trace("Enter ReportService.getReport() name: " + name);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "reports_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document reportDocument = collection.find(Filters.eq("NAME", name)).first();

			if (reportDocument != null) {
				String reportId = reportDocument.getObjectId("_id").toString();
				reportDocument.remove("_id");

				Report report = new ObjectMapper().readValue(reportDocument.toJson(), Report.class);
				report.setReportId(reportId);
				log.trace("Exit ReportService.getReport() name: " + name);
				return report;
			} else {
				throw new ForbiddenException("REPORT_DOES_NOT_EXIST");
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

	@PostMapping("/reports/{name}")
	public Report postReport(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String name, @Valid @RequestBody Report report) {

		try {
			log.trace("Enter ReportService.postReport() name: " + name);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			String collectionName = "reports_" + companyId;
			report.setReportName(name);

			if (!global.isExists("NAME", name, collectionName)) {

				String modulesCollectionName = "modules_" + companyId;

				if (global.isDocumentIdExists(report.getModule(), modulesCollectionName)) {

					MongoCollection<Document> modulesCollection = mongoTemplate.getCollection(modulesCollectionName);

					if (new ObjectId().isValid(report.getModule())) {
						Document moduleDocument = modulesCollection
								.find(Filters.eq("_id", new ObjectId(report.getModule()))).first();
						ArrayList<Document> fieldDocuments = (ArrayList) moduleDocument.get("FIELDS");
						ArrayList<String> fieldIds = new ArrayList<String>();
						Map<String, Document> fieldsMap = new HashMap<String, Document>();

						for (Document field : fieldDocuments) {
							fieldIds.add(field.getString("FIELD_ID"));
							fieldsMap.put(field.getString("FIELD_ID"), field);
						}
						for (Field field : report.getFields()) {
							String fieldId = field.getId();
							if (!fieldIds.contains(fieldId)) {
								throw new BadRequestException("FIELD_INVALID");
							}
						}

						for (Filter filter : report.getFilters()) {
							String fieldId = filter.getField().getId();
							if (!fieldIds.contains(fieldId)) {
								throw new BadRequestException("FIELD_INVALID");
							}
							// OPERATOR CHECK
							Document fieldDoc = fieldsMap.get(fieldId);
							Document dataType = (Document) fieldDoc.get("DATA_TYPE");
							String displayDatatype = dataType.getString("DISPLAY");
							String backendDatatype = dataType.getString("BACKEND");
							if (!checkValidOperator(filter.getOperator(), backendDatatype, displayDatatype)) {
								throw new BadRequestException("INVALID_OPERATOR");
							}
						}

						Field sort = report.getSortBy();
						String sortFieldId = sort.getId();
						if (!fieldIds.contains(sortFieldId)) {
							throw new BadRequestException("FIELD_INVALID");
						}

						MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

						report.setDateCreated(new Timestamp(new Date().getTime()));
						report.setDateUpdated(new Timestamp(new Date().getTime()));
						report.setCreatedBy(userId);
						report.setLastUpdated(userId);

						String body = new ObjectMapper().writeValueAsString(report).toString();

						if (report.getSchedules() != null) {
							ReportSchedule schedules = report.getSchedules();
							List<String> emails = schedules.getEmails();
							for (String email : emails) {
								if (!EmailValidator.getInstance().isValid(email)) {
									throw new BadRequestException("EMAIL_INVALID");
								}
							}
						}

						Document reportDocument = Document.parse(body);
						collection.insertOne(reportDocument);

						String reportId = reportDocument.getObjectId("_id").toString();
						report.setReportId(reportId);
						log.trace("Exit ReportService.postReport() name: " + name);
						return report;
					} else {
						throw new BadRequestException("INVALID_ENTRY_ID");
					}
				} else {
					throw new BadRequestException("MODULE_DOES_NOT_EXIST");
				}

			} else {
				throw new BadRequestException("REPORT_NAME_EXISTS");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/reports/{name}")
	public Report updateReport(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String name, @Valid @RequestBody Report report) {
		try {
			log.trace("Enter ReportService.updateReport() name: " + name);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "reports_" + companyId;

			if (!name.equals(report.getReportName())) {
				if (global.isExists("NAME", report.getReportName(), collectionName)) {
					throw new BadRequestException("REPORT_NAME_EXISTS");
				}
			}

			if (!ObjectId.isValid(report.getModule())) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}

			report.setDateUpdated(new Timestamp(new Date().getTime()));
			report.setLastUpdated(userId);
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			String modulesCollectionName = "modules_" + companyId;
			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection(modulesCollectionName);

			if (!ObjectId.isValid(report.getReportId())) {
				throw new BadRequestException("REPORT_DOES_NOT_EXIST");
			}
			if (report.getReportId() == null
					|| collection.find(Filters.eq("_id", new ObjectId(report.getReportId()))).first() != null) {

				Document moduleDocument = modulesCollection.find(Filters.eq("_id", new ObjectId(report.getModule())))
						.first();
				ArrayList<Document> fieldDocuments = (ArrayList) moduleDocument.get("FIELDS");
				ArrayList<String> fieldIds = new ArrayList<String>();
				Map<String, Document> fieldsMap = new HashMap<String, Document>();

				for (Document field : fieldDocuments) {
					fieldIds.add(field.getString("FIELD_ID"));
					fieldsMap.put(field.getString("FIELD_ID"), field);
				}
				for (Field field : report.getFields()) {
					String fieldId = field.getId();
					if (!fieldIds.contains(fieldId)) {
						throw new BadRequestException("FIELD_INVALID");
					}
				}
				for (Filter filter : report.getFilters()) {
					String fieldId = filter.getField().getId();
					if (!fieldIds.contains(fieldId)) {
						throw new BadRequestException("FIELD_INVALID");
					}
					// OPERATOR CHECK
					Document fieldDoc = fieldsMap.get(fieldId);
					Document dataType = (Document) fieldDoc.get("DATA_TYPE");
					String displayDatatype = dataType.getString("DISPLAY");
					String backendDatatype = dataType.getString("BACKEND");
					if (!checkValidOperator(filter.getOperator(), backendDatatype, displayDatatype)) {
						throw new BadRequestException("INVALID_OPERATOR");
					}
				}

				Field sort = report.getSortBy();
				String sortFieldId = sort.getId();
				if (!fieldIds.contains(sortFieldId)) {
					throw new BadRequestException("FIELD_INVALID");
				}

				String reportId = report.getReportId();

				Document existingReport = collection.find(Filters.eq("_id", new ObjectId(report.getReportId())))
						.first();
				report.setCreatedBy(existingReport.getString("CREATED_BY"));

				for (Field fields : report.getFields()) {
					fields.setData(null);
				}

				if (report.getSchedules() != null) {
					ReportSchedule schedules = report.getSchedules();
					List<String> emails = schedules.getEmails();
					for (String email : emails) {
						if (!EmailValidator.getInstance().isValid(email)) {
							throw new BadRequestException("EMAIL_INVALID");
						}
					}
				}

				String body = new ObjectMapper().writeValueAsString(report).toString();
				Document reportDocument = Document.parse(body);
				reportDocument.put("DATE_CREATED", existingReport.getString("DATE_CREAETD"));

				collection.replaceOne(Filters.eq("_id", new ObjectId(reportId)), reportDocument);
				log.trace("Exit ReportService.updateReport() name: " + name);
				return report;

			} else {
				throw new ForbiddenException("REPORT_DOES_NOT_EXIST");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/reports/{name}")
	public ResponseEntity<Object> deleteReport(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String name) {
		try {
			log.trace("Enter ReportService.deleteReport() name: " + name);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "reports_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document reportDocument = collection.find(Filters.eq("NAME", name)).first();

			if (reportDocument != null) {
				collection.deleteOne(Filters.eq("NAME", name));
			} else {
				throw new ForbiddenException("REPORT_DOES_NOT_EXIST");
			}
			log.trace("Exit ReportService.deleteReport() name: " + name);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	// CALLED EVERYTIME A COLUMN IS DRAGGED N DROPPED
	@PostMapping("/reports/{name}/data")
	public ResponseEntity<Object> getData(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String name, @RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page, @Valid @RequestBody Report report)
			throws JsonProcessingException {

		JSONObject resultObj = new JSONObject();
		int totalSize = 0;

		try {
			log.trace("Enter ReportService.getData() name: " + name);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String modulesCollectionName = "modules_" + companyId;

			String userRole = user.getString("ROLE");
			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new UnauthorizedException("UNAUTHORIZED");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection(modulesCollectionName);

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

			String orderBy = report.getOrder();

			Bson sortFilter = null;

			if (global.isDocumentIdExists(report.getModule(), modulesCollectionName)) {
				if (new ObjectId().isValid(report.getModule())) {
					Document moduleDocument = modulesCollection
							.find(Filters.eq("_id", new ObjectId(report.getModule()))).first();
					ArrayList<Document> fieldDocuments = (ArrayList) moduleDocument.get("FIELDS");

					List<String> AllfieldNames = new ArrayList<String>();
					Map<String, Document> relationFields = new HashMap<String, Document>();
					Map<String, Document> fieldsMap = new HashMap<String, Document>();

					for (Document field : fieldDocuments) {

						AllfieldNames.add(field.getString("NAME"));

						fieldsMap.put(field.getString("FIELD_ID"), field);

						Document dataType = (Document) field.get("DATA_TYPE");
						String displayDataType = dataType.getString("DISPLAY");

						if (displayDataType.equals("Relationship")) {
							if (field.getString("RELATIONSHIP_TYPE").equals("One to One")
									|| field.getString("RELATIONSHIP_TYPE").equals("Many to One")) {
								relationFields.put(field.getString("FIELD_ID"), field);

							}
						}
					}

					String sortBy = fieldsMap.get(report.getSortBy().getId()).getString("NAME");
					List<Bson> filters = generateFilter(report, fieldsMap);

					if (orderBy.equals("asc")) {
						sortFilter = Sorts.ascending(sortBy);
					} else if (orderBy.equals("desc")) {
						sortFilter = Sorts.descending(sortBy);
					}
					List<String> fieldNames = new ArrayList<String>();
					List<String> fieldDisplays = new ArrayList<String>();
					List<String> fieldsToAdd = new ArrayList<String>();

					for (Field field : report.getFields()) {

						if (!fieldsMap.containsKey(field.getId())) {
							throw new BadRequestException("FIELD_INVALID");
						}

						String fieldName = fieldsMap.get(field.getId()).getString("NAME");
						String labelName = fieldsMap.get(field.getId()).getString("DISPLAY_LABEL");
						Document fieldDataType = (Document) fieldsMap.get(field.getId()).get("DATA_TYPE");
						String backendDataType = fieldDataType.getString("BACKEND");

						if (!AllfieldNames.contains(fieldName)) {
							throw new BadRequestException("FIELD_INVALID");
						}

						fieldNames.add(fieldName);
						fieldDisplays.add(labelName);
						if (relationFields.containsKey(field.getId())) {
							fieldsToAdd.add(field.getId());
						}
					}

					String module = moduleDocument.getString("NAME");
					String collectionName = module.replaceAll("\\s+", "_") + "_" + companyId;
					MongoCollection<Document> dataCollection = mongoTemplate.getCollection(collectionName);

					List<Document> reportDocuments = new ArrayList<Document>();
					if (filters.size() != 0) {
						totalSize = (int) dataCollection.countDocuments(Filters.and(filters));
						reportDocuments = dataCollection.find(Filters.and(filters)).sort(sortFilter).skip(skip)
								.limit(pgSize)
								.projection(Filters.and(Projections.include(fieldNames), Projections.excludeId()))
								.into(new ArrayList<Document>());

					} else {
						totalSize = (int) dataCollection.countDocuments(Filters.eq("DELETED", false));
						reportDocuments = dataCollection.find(Filters.eq("DELETED", false)).sort(sortFilter).skip(skip)
								.limit(pgSize)
								.projection(Filters.and(Projections.include(fieldNames), Projections.excludeId()))
								.into(new ArrayList<Document>());
					}

					MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
					List<Document> roles = rolesCollection.find().into(new ArrayList<Document>());

					Map<String, String> roleIds = new HashMap<String, String>();
					for (Document role : roles) {
						String roleId = role.getObjectId("_id").toString();
						String roleName = role.getString("NAME");
						roleIds.put(roleId, roleName);
					}

					for (Field field : report.getFields()) {
						List<String> data = new ArrayList<String>();
						for (Document document : reportDocuments) {
							String fieldName = fieldsMap.get(field.getId()).getString("NAME");
							String labelName = fieldsMap.get(field.getId()).getString("DISPLAY_NAME");
							Document dataType = (Document) fieldsMap.get(field.getId()).get("DATA_TYPE");

							String value = "";
							if (document.get(fieldName) != null) {

								if (dataType.get("DISPLAY").toString().equalsIgnoreCase("Discussion")) {
									List<Document> messages = (List<Document>) document.get(fieldName);

									for (Document message : messages) {
										data.add(message.getString("MESSAGE"));
									}

								} else if (dataType.get("DISPLAY").toString().equalsIgnoreCase("Phone")) {
									Document doc = (Document) document.get(fieldName);
									String countryCode = doc.getString("DIAL_CODE");
									String phoneNumber = doc.getString("PHONE_NUMBER");
									if (countryCode != null && phoneNumber != null) {
										value = countryCode + phoneNumber;
									}
								} else {
									value = document.get(fieldName).toString();

								}

							}
							if (fieldsToAdd.contains(field.getId())) {
								if (value != null && value.length() > 0) {
									// RELATIONFIELD
									Document fieldDoc = relationFields.get(field.getId());
									String primaryDisplayField = fieldDoc.getString("PRIMARY_DISPLAY_FIELD");
									Document relationModule = modulesCollection
											.find(Filters.eq("_id", new ObjectId(fieldDoc.getString("MODULE"))))
											.first();
									List<Document> relationModuleFields = (List<Document>) relationModule.get("FIELDS");
									String relationModuleName = relationModule.getString("NAME");

									MongoCollection<Document> relationEntries = mongoTemplate
											.getCollection(relationModuleName + "_" + companyId);
									Document entryDoc = relationEntries.find(Filters.eq("_id", new ObjectId(value)))
											.first();
									String rowValue = null;

									for (Document relationField : relationModuleFields) {
										if (relationField.getString("FIELD_ID").equals(primaryDisplayField)) {
											rowValue = entryDoc.getString(relationField.getString("NAME"));
											break;
										}
									}
									data.add(rowValue);
								} else {
									data.add(null);
								}

							} else {
								if (fieldName.equalsIgnoreCase("ROLE")) {
									value = roleIds.get(value);
								}
								if (!dataType.get("DISPLAY").toString().equals("Discussion")) {
									data.add(value);
								}
							}
						}
						field.setData(data);
					}
					JSONObject reportJson = new JSONObject(new ObjectMapper().writeValueAsString(report));
					resultObj.put("REPORT", reportJson);
					resultObj.put("TOTAL_RECORDS", totalSize);
				} else {
					throw new ForbiddenException("INVALID_ENTRY_ID");
				}
			} else {
				throw new ForbiddenException("MODULE_DOES_NOT_EXIST");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		log.trace("Exit ReportService.getData() name: " + name);
		return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);
	}

	@PostMapping("/reports/{name}/generate")
	public ResponseEntity<Resource> getCsv(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("name") String name, @Valid @RequestBody Report report) {
		try {
			log.trace("Enter ReportService.getCsv() name: " + name);
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");

			String modulesCollectionName = "modules_" + companyId;
			String userRole = user.getString("ROLE");

			if (!roleService.isSystemAdmin(userRole, companyId)) {
				throw new UnauthorizedException("UNAUTHORIZED");
			}

			MongoCollection<Document> modulesCollection = mongoTemplate.getCollection(modulesCollectionName);

			if (global.isDocumentIdExists(report.getModule(), modulesCollectionName)) {

				if (new ObjectId().isValid(report.getModule())) {

					Document moduleDocument = modulesCollection
							.find(Filters.eq("_id", new ObjectId(report.getModule()))).first();
					ArrayList<Document> fieldDocuments = (ArrayList) moduleDocument.get("FIELDS");

					List<String> AllfieldNames = new ArrayList<String>();
					Map<String, Document> relationFields = new HashMap<String, Document>();
					Map<String, Document> fieldsMap = new HashMap<String, Document>();

					for (Document field : fieldDocuments) {

						AllfieldNames.add(field.getString("NAME"));

						fieldsMap.put(field.getString("FIELD_ID"), field);

						Document dataType = (Document) field.get("DATA_TYPE");

						String displayDataType = dataType.getString("DISPLAY");
						if (displayDataType.equals("Relationship")) {
							if (field.getString("RELATIONSHIP_TYPE").equals("One to One")
									|| field.getString("RELATIONSHIP_TYPE").equals("Many to One")) {
								relationFields.put(field.getString("FIELD_ID"), field);
							}
						}

					}

					List<String> fieldNames = new ArrayList<String>();
					List<String> fieldDisplays = new ArrayList<String>();
					List<String> fieldsToAdd = new ArrayList<String>();

					for (Field field : report.getFields()) {
						String fieldName = fieldsMap.get(field.getId()).getString("NAME");
						String displayLabel = fieldsMap.get(field.getId()).getString("DISPLAY_LABEL");
						if (!AllfieldNames.contains(fieldName)) {
							throw new BadRequestException("FIELD_INVALID");
						}
						fieldNames.add(fieldName);
						fieldDisplays.add(displayLabel);
						if (relationFields.containsKey(field.getId())) {
							fieldsToAdd.add(field.getId());
						}
					}

					// INSERT HEADERS
					Writer writer = new StringWriter();
					CSVWriter csvWriter = new CSVWriter(writer);
					csvWriter.writeNext(fieldDisplays.toArray(new String[fieldDisplays.size()]));

					String moduleName = moduleDocument.getString("NAME");
					String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
					MongoCollection<Document> dataCollection = mongoTemplate.getCollection(collectionName);

					List<Bson> filters = generateFilter(report, fieldsMap);

					String sortBy = fieldsMap.get(report.getSortBy().getId()).getString("NAME");
					String orderBy = report.getOrder();

					Bson sortFilter = null;

					if (orderBy.equals("asc")) {
						sortFilter = Sorts.ascending(sortBy);
					} else if (orderBy.equals("desc")) {
						sortFilter = Sorts.descending(sortBy);
					}

					List<Document> reportDocuments = new ArrayList<Document>();

					if (filters.size() != 0) {
						reportDocuments = dataCollection.find(Filters.and(filters)).sort(sortFilter)
								.projection(Filters.and(Projections.include(fieldNames), Projections.excludeId()))
								.into(new ArrayList<Document>());
					} else {
						reportDocuments = dataCollection.find().sort(sortFilter)
								.projection(Filters.and(Projections.include(fieldNames), Projections.excludeId()))
								.into(new ArrayList<Document>());
					}

					MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
					List<Document> roles = rolesCollection.find().into(new ArrayList<Document>());

					Map<String, String> roleIds = new HashMap<String, String>();
					for (Document role : roles) {
						String roleId = role.getObjectId("_id").toString();
						String roleName = role.getString("NAME");
						roleIds.put(roleId, roleName);
					}

					String timeZone = "UTC";
					LocalDateTime dateTime = null;
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss.SSSX");
					MongoCollection<Document> userCollection = mongoTemplate.getCollection("Users_" + companyId);
					MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");
					Document companyData = companiesCollection.find(Filters.eq("_id", new ObjectId(companyId))).first();
					if (userId != null) {
						Document userData = userCollection.find(Filters.eq("_id", new ObjectId(userId))).first();
						if (userData.containsKey("TIMEZONE") && userData.get("TIMEZONE") != null) {
							timeZone = userData.get("TIMEZONE").toString();
						} else if (companyData.containsKey("TIMEZONE") && companyData.get("TIMEZONE") != null) {
							timeZone = companyData.get("TIMEZONE").toString();
						}
					} else if (companyData.containsKey("TIMEZONE") && companyData.get("TIMEZONE") != null) {
						timeZone = companyData.get("TIMEZONE").toString();
					}

					for (Document document : reportDocuments) {
						List<String> row = new ArrayList<String>();
						for (Field field : report.getFields()) {
							String fieldName = fieldsMap.get(field.getId()).getString("NAME");
							Document dataType = (Document) fieldsMap.get(field.getId()).get("DATA_TYPE");
							String value = "";
							if (document.get(fieldName) != null) {
								if (dataType.get("DISPLAY").toString().equalsIgnoreCase("Discussion")) {
									List<Document> messages = (List<Document>) document.get(fieldName);
									String exportMessages = "";
									for (Document message : messages) {
										exportMessages = exportMessages + message.getString("MESSAGE");
									}
									String text = Jsoup.parse(exportMessages).text();
									row.add(text);

								} else if (dataType.get("DISPLAY").toString().equalsIgnoreCase("Date/Time")) {

									Date unconvertedDate = document.getDate(fieldName);
									dateTime = LocalDateTime.ofInstant(unconvertedDate.toInstant(),
											ZoneId.systemDefault());

									ZoneId fromTimeZone = ZoneId.of("UTC"); // Source timezone
									ZoneId toTimeZone = ZoneId.of(timeZone); // Target timezone
									// Zoned date time at source timezone
									ZonedDateTime sourceTime = dateTime.atZone(fromTimeZone);
									// Zoned date time at target timezone
									ZonedDateTime targetTime = sourceTime.withZoneSameInstant(toTimeZone);
									// Format date time
									value = formatter.format(targetTime).toString();

								} else if (dataType.get("DISPLAY").toString().equalsIgnoreCase("Phone")) {
									Document doc = (Document) document.get(fieldName);
									String countryCode = doc.getString("DIAL_CODE");
									String phoneNumber = doc.getString("PHONE_NUMBER");
									if (countryCode != null && phoneNumber != null) {
										value = countryCode + phoneNumber;
									}
								} else {
									value = document.get(fieldName).toString();
								}
							}

							if (fieldsToAdd.contains(field.getId())) {

								if (value != null && value.length() > 0) {
									// RELATIONFIELD
									Document fieldDoc = relationFields.get(field.getId());
									String primaryDisplayField = fieldDoc.getString("PRIMARY_DISPLAY_FIELD");
									Document relationModule = modulesCollection
											.find(Filters.eq("_id", new ObjectId(fieldDoc.getString("MODULE"))))
											.first();
									List<Document> relationModuleFields = (List<Document>) relationModule.get("FIELDS");
									String relationModuleName = relationModule.getString("NAME");

									MongoCollection<Document> relationEntries = mongoTemplate
											.getCollection(relationModuleName + "_" + companyId);
									Document entryDoc = relationEntries.find(Filters.eq("_id", new ObjectId(value)))
											.first();
									String rowValue = null;

									for (Document relationField : relationModuleFields) {
										if (relationField.getString("FIELD_ID").equals(primaryDisplayField)) {
											rowValue = entryDoc.getString(relationField.getString("NAME"));
											break;
										}
									}
									row.add(rowValue);
								} else {
									row.add(null);
								}

							} else {
								if (fieldName.equalsIgnoreCase("ROLE")) {
									value = roleIds.get(value);

								}
								if (!dataType.get("DISPLAY").toString().equals("Discussion")) {
									row.add(value);
								}

							}
						}
						csvWriter.writeNext(row.toArray(new String[row.size()]));
					}
					csvWriter.close();

					HttpHeaders headers = new HttpHeaders();
					headers.add(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=" + report.getReportName() + ".csv");

					InputStream targetStream = IOUtils.toInputStream(writer.toString(), "UTF-8");
					log.trace("Exit ReportService.getCsv() name: " + name);
					return ResponseEntity.ok().headers(headers)
							.contentType(MediaType.parseMediaType("application/octet-stream"))
							.body(new InputStreamResource(targetStream));
				} else {
					throw new ForbiddenException("INVALID_ENTRY_ID");
				}
			} else {
				throw new ForbiddenException("MODULE_DOES_NOT_EXIST");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/reports/schedules/download")
	public ResponseEntity<Resource> getScheduleCSVFile(@RequestParam("uuid") String uuid) {
		try {
			log.trace("Entered getScheduleCSVFile");
			Document report = mongoTemplate.getCollection("generated_reports").find(Filters.eq("UUID", uuid)).first();
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=" + report.getString("FILE_NAME") + ".csv");

			InputStream targetStream = IOUtils.toInputStream(report.getString("FILE"), "UTF-8");
			log.trace("Exit getScheduleCSVFile");
			return ResponseEntity.ok().headers(headers)
					.contentType(MediaType.parseMediaType("application/octet-stream"))
					.body(new InputStreamResource(targetStream));
		} catch (Exception e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public List<Bson> generateFilter(Report report, Map<String, Document> fieldsMap) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSX");
		List<Bson> customFilters = new ArrayList<Bson>();
		List<Filter> filters = report.getFilters();
		try {
			for (Filter filter : filters) {

				Field field = filter.getField();
				if (!fieldsMap.containsKey(field.getId())) {
					throw new BadRequestException("FIELD_INVALID");
				}

				String operator = filter.getOperator();
				String value = filter.getValue();
				Document fieldDoc = fieldsMap.get(field.getId());
				Document dataType = (Document) fieldDoc.get("DATA_TYPE");
				String backendDatatype = dataType.getString("BACKEND");

				String displayDatatype = dataType.getString("DISPLAY");

				if (!checkValidOperator(filter.getOperator(), backendDatatype, displayDatatype)) {
					throw new BadRequestException("INVALID_OPERATOR");
				}

				String fieldName = fieldsMap.get(field.getId()).getString("NAME");
				if (backendDatatype.equals("Timestamp")) {
					Date dateValue = null;
					if (operator.equals("LESS_THAN")) {
						dateValue = formatter.parse(value);
						customFilters.add(Filters.lt(fieldName, dateValue));
					} else if (operator.equals("GREATER_THAN")) {
						dateValue = formatter.parse(value);
						customFilters.add(Filters.gt(fieldName, dateValue));
					} else if (operator.equals("DAYS_BEFORE_TODAY")) {
						Date today = new Date();
						Calendar calender = new GregorianCalendar();
						calender.setTime(today);
						calender.add(Calendar.DAY_OF_MONTH, -Integer.parseInt(value));
						Date pastDate = calender.getTime();
						customFilters.add(Filters.and(Filters.gte(fieldName, pastDate), Filters.lte(fieldName, today)));

					} else {
						throw new BadRequestException("INVALID_OPERATOR");
					}
				} else if (operator.equals("EQUALS_TO")) {
					if (backendDatatype.equals("Integer")) {
						customFilters.add(Filters.eq(fieldName, Integer.parseInt(value)));
					} else if (backendDatatype.equals("Boolean")) {
						customFilters.add(Filters.eq(fieldName, Boolean.parseBoolean(value)));
					} else {
						customFilters.add(Filters.eq(fieldName, value));
					}
				} else if (operator.equals("NOT_EQUALS_TO")) {
					if (backendDatatype.equals("Integer")) {
						customFilters.add(Filters.ne(fieldName, Integer.parseInt(value)));
					} else if (backendDatatype.equals("Boolean")) {
						customFilters.add(Filters.ne(fieldName, Boolean.parseBoolean(value)));
					} else {
						customFilters.add(Filters.ne(fieldName, value));
					}
				} else if (operator.equals("GREATER_THAN")) {
					customFilters.add(Filters.gt(fieldName, Integer.parseInt(value)));
				} else if (operator.equals("LESS_THAN")) {
					customFilters.add(Filters.lt(fieldName, Integer.parseInt(value)));
				} else if (operator.equals("CONTAINS")) {
					customFilters.add(Filters.regex(fieldName, ".*" + value + ".*"));
				} else if (operator.equals("DOES_NOT_CONTAIN")) {
					customFilters.add(Filters.not(Filters.regex(fieldName, ".*" + value + ".*")));
				} else if (operator.equals("REGEX")) {
					Pattern pattern = Pattern.compile("^" + value + "$");
					customFilters.add(Filters.regex(fieldName, pattern));
				} else {
					throw new BadRequestException("INVALID_OPERATOR");
				}

			}
			customFilters.add(Filters.or(Filters.exists("EFFECTIVE_TO", false), Filters.eq("EFFECTIVE_TO", null)));
			customFilters.add(Filters.eq("DELETED", false)); // filter out deleted reports
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return customFilters;
	}

	// TO CHECK IF OPERATOR IS VALID FOR THAT FIELD DATATYPE
	public boolean checkValidOperator(String operator, String backendDataType, String displayDataType) {
		try {

			if (!global.reportingOperators.contains(operator)) {
				return false;
			} else if (!global.validReportingOperators.containsKey(operator)) {
				return false;
			} else if (displayDataType.equalsIgnoreCase("Relationship")) {
				if (backendDataType.equalsIgnoreCase("Boolean")) {
					if (!global.validReportingOperators.get(operator).contains("RelationBoolean")) {
						return false;
					}
				} else if (backendDataType.equalsIgnoreCase("Array")) {
					if (!global.validReportingOperators.get(operator).contains("RelationArray")) {
						return false;
					}
				} else if (backendDataType.equalsIgnoreCase("String")) {
					if (!global.validReportingOperators.get(operator).contains("RelationString")) {
						return false;
					}
				}

			} else if (displayDataType.equalsIgnoreCase("Picklist") && backendDataType.equalsIgnoreCase("Array")) {
				if (!global.validReportingOperators.get(operator).contains("PickListArray")) {
					return false;
				}
			} else if (displayDataType.equalsIgnoreCase("Auto Number") && backendDataType.equalsIgnoreCase("Integer")
					&& backendDataType.equalsIgnoreCase("Formula")) {
				if (!global.validReportingOperators.get(operator).contains("Auto Number")) {
					return false;
				}
			} else if (!displayDataType.equalsIgnoreCase("Relationship") && !displayDataType.equalsIgnoreCase("Text")
					&& backendDataType.equalsIgnoreCase("String")) {
				// FOR PICKLIST, PHONE, EMAIL, DISCUSSION, URL, ZIPCODE
				if (!global.validReportingOperators.get(operator).contains("NonRelationString")) {
					return false;
				}
			} else if (!global.validReportingOperators.get(operator).contains(displayDataType)) {
				return false;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return true;
	}

}
