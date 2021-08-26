package com.ngdesk.modules.importcsv;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
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

@Component
@RestController
public class CsvImportDataService {

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@GetMapping(value = "import/csv")
	public ResponseEntity<Object> getLogs(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "company_id", required = false) String companyId,
			@RequestParam(value = "user_uuid", required = false) String userUUID,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {
		JSONObject data = new JSONObject();
		String role = null;
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			// CHECK UUID
			if (uuid != null) {
				JSONObject user = auth.getUserDetails(uuid);
				userUUID = user.getString("USER_UUID");
				role = user.getString("ROLE");
				companyId = user.getString("COMPANY_ID");
			} else {
				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
				Document user = usersCollection.find(Filters.eq("USER_UUID", userUUID)).first();
				role = user.getString("ROLE");
			}

			if (role != null) {
				if (!roleService.isSystemAdmin(role, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
			}

			MongoCollection<Document> csvCollection = mongoTemplate.getCollection("csv_import");
			
			// BY DEFAULT RETURN ALL DOCUMENTS
			int lowerLimit = 0;
			int pgSize = 100;
			int pg = 1;
			int skip = 0;

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
			long size = csvCollection.countDocuments(Filters.eq("COMPANY_ID", companyId));
			if (sort != null && order != null) {

				if (order.equalsIgnoreCase("asc")) {
					documents = (List<Document>) csvCollection
							.find(Filters.eq("COMPANY_ID", companyId))
							.sort(Sorts.orderBy(Sorts.ascending(sort))).skip(skip).limit(pgSize)
							.into(new ArrayList<Document>());
				} else if (order.equalsIgnoreCase("desc")) {
					documents = (List<Document>) csvCollection
							.find(Filters.eq("COMPANY_ID", companyId))
							.sort(Sorts.orderBy(Sorts.descending(sort))).skip(skip).limit(pgSize)
							.into(new ArrayList<Document>());
				} else {
					throw new BadRequestException("INVALID_SORT_ORDER");
				}

			} else {
				documents = (List<Document>) csvCollection.find(Filters.eq("COMPANY_ID", companyId))
						.skip(skip).limit(pgSize).into(new ArrayList<Document>());
			}
			
			for (Document csvDocument : documents) {
				String dataId = csvDocument.remove("_id").toString();
				String status = csvDocument.getString("STATUS").substring(0, 1) + csvDocument.getString("STATUS").substring(1).toLowerCase();
				csvDocument.put("DATA_ID", dataId);
				csvDocument.put("STATUS", status);
			}
			data.put("TOTAL_SIZE", size);
			data.put("DATA", documents);
			return new ResponseEntity<>(data.toString(), Global.postHeaders, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}
	
	@GetMapping(value = "import/csv/{data_id}")
	public ResponseEntity<Object> getLog(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "company_id", required = false) String companyId,
			@RequestParam(value = "user_uuid", required = false) String userUUID, 
			@PathVariable("data_id") String dataId) {
		JSONObject data = new JSONObject();
		String role = null;
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			// CHECK UUID
			if (uuid != null) {
				JSONObject user = auth.getUserDetails(uuid);
				userUUID = user.getString("USER_UUID");
				role = user.getString("ROLE");
				companyId = user.getString("COMPANY_ID");
			} else {
				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
				Document user = usersCollection.find(Filters.eq("USER_UUID", userUUID)).first();
				role = user.getString("ROLE");
			}

			if (role != null) {
				if (!roleService.isSystemAdmin(role, companyId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
			}

			if (!ObjectId.isValid(dataId)) {
				throw new BadRequestException("INVALID_ENTRY_ID");
			}
			MongoCollection<Document> csvCollection = mongoTemplate.getCollection("csv_import");
			Document csvDocument = csvCollection.find(Filters.eq("_id", new ObjectId(dataId))).first();
			if (csvDocument == null) {
				throw new BadRequestException("INVALID_ENTRY_ID");
			}
			csvDocument.remove("_id");
			data = new JSONObject(csvDocument.toJson().toString());
			
			return new ResponseEntity<>(data.toString(), Global.postHeaders, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping(value = "/modules/{module_id}/csv")
	public ResponseEntity<Object> importFromCSV(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "company_id", required = false) String companyId,
			@RequestParam(value = "user_uuid", required = false) String userUUID,
			@PathVariable("module_id") String moduleId, @RequestBody CsvImportData body) {
		JSONObject data = new JSONObject();
		String role = null;
		String createdById = "";

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			// CHECK UUID
			if (uuid != null) {
				JSONObject user = auth.getUserDetails(uuid);
				userUUID = user.getString("USER_UUID");
				role = user.getString("ROLE");
				companyId = user.getString("COMPANY_ID");
				createdById = user.getString("USER_ID");
			} else {
				MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
				Document user = usersCollection.find(Filters.eq("USER_UUID", userUUID)).first();
				createdById = user.getObjectId("_id").toString();
				role = user.getString("ROLE");
			}

			if (role != null) {
				if (!roleService.isSystemAdmin(role, companyId)) {
					if (!roleService.isAuthorizedForRecord(role, "POST", moduleId, companyId)) {
						throw new ForbiddenException("FORBIDDEN");
					}
				}
			}

			String moduleCollectionName = "modules_" + companyId;
			MongoCollection<Document> modulecollection = mongoTemplate.getCollection(moduleCollectionName);
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			Document moduledoc = modulecollection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (moduledoc == null) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			String json = new ObjectMapper().writeValueAsString(body);
			JSONObject csvData = new JSONObject(json);
			data.put("MODULE_ID", moduleId);
			data.put("NAME", body.getFileName());
			data.put("CSV_IMPORT_DATA", csvData);
			data.put("STATUS", "QUEUED");
			data.put("COMPANY_ID", companyId);
			data.put("LOGS", new ArrayList<Document>());
			data.put("CREATED_BY", createdById);
			data.put("DATE_CREATED", global.getFormattedDate(new Timestamp(new Date().getTime())));
			Document csvDocument = Document.parse(data.toString());
			if (!mongoTemplate.collectionExists("csv_import")) {
				mongoTemplate.createCollection("csv_import");
			} else {
				MongoCollection<Document> csvCollection = mongoTemplate.getCollection("csv_import");
				csvCollection.insertOne(csvDocument);
			}
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping(value = "/modules/{module_id}/csvheaders")
	public ResponseEntity<Object> getCSVHeaders(HttpServletRequest request, @PathVariable("module_id") String moduleId,
			@RequestBody CsvImportData body) {
		InputStream is = null;
		BufferedReader br = null;
		try {
			String file = body.getFile();

			Base64.Decoder dec = Base64.getDecoder();
			byte[] decbytes = dec.decode(file);
			is = new ByteArrayInputStream(decbytes);
			List<String> headers = new ArrayList<String>();

			if (body.getFileType().equals("xlsx")) {
				// DECODING IF XLSX
				Workbook workbook = new XSSFWorkbook(is);
				Sheet datatypeSheet = workbook.getSheetAt(0);
				Iterator<Row> iterator = datatypeSheet.iterator();
				while (iterator.hasNext()) {
					Row currentRow = iterator.next();
					Iterator<Cell> cellIterator = currentRow.iterator();
					while (cellIterator.hasNext()) {
						Cell currentCell = cellIterator.next();
						headers.add(currentCell.toString());
					}
					workbook.close();
					break;
				}
			} else if (body.getFileType().equals("xls")) {
				Workbook workbook = new HSSFWorkbook(is);
				Sheet datatypeSheet = workbook.getSheetAt(0);
				Iterator<Row> iterator = datatypeSheet.iterator();
				while (iterator.hasNext()) {
					Row currentRow = iterator.next();
					Iterator<Cell> cellIterator = currentRow.iterator();
					while (cellIterator.hasNext()) {
						Cell currentCell = cellIterator.next();
						headers.add(currentCell.toString());
					}
					workbook.close();
					break;
				}
			} else if (body.getFileType().equals("csv")) {
				// DECODING THE BYTE STRING SENT FROM FRONT-END
				br = new BufferedReader(new InputStreamReader(is));
				String line = "";
				int i = 0;

				while ((line = br.readLine()) != null) {
					if (i == 0) {
						// SPLITTING THE FIRST LINE OF THE FILE TO GET HEADERS
						headers = Arrays.asList(line.split(","));
						i++;
					}
				}
			}

			return new ResponseEntity<>(headers, Global.postHeaders, HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
