package com.ngdesk.spf;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
public class Spf {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Authentication auth;

	@Autowired
	RoleService roleService;

	@Autowired
	Global global;

	@GetMapping("/spf/records/all")
	public ResponseEntity<Object> getSpfRecords(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONArray spfRecords = new JSONArray();
		int totalSize = 0;
		JSONObject resultObj = new JSONObject();

		try {

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}

			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "spf_records_" + companyId;
			String roleId = user.getString("ROLE");

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (!roleService.isSystemAdmin(roleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			int lowerLimit = 0;
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

				String spfId = document.getObjectId("_id").toString();
				document.remove("_id");
				JSONObject record = new JSONObject(document.toJson());
				record.put("SPF_ID", spfId);
				spfRecords.put(record);
			}

			resultObj.put("SPF_RECORDS", spfRecords);
			resultObj.put("TOTAL_RECORDS", totalSize);

			return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/spf/{spf_id}")
	public ResponseEntity<Object> getSpfRecord(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("spf_id") String spfId) {
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "spf_records_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (!ObjectId.isValid(spfId)) {
				throw new BadRequestException("INVALID_SPF_ID");
			}
			Document spfDocument = collection.find(Filters.eq("_id", new ObjectId(spfId))).first();

			if (spfDocument != null) {
				spfDocument.remove("_id");

				JSONObject result = new JSONObject(spfDocument.toJson());
				result.put("SPF_ID", spfId);
				return new ResponseEntity<>(result.toString(), global.postHeaders, HttpStatus.OK);
			} else {
				throw new ForbiddenException("SPF_RECORD_DOES_NOT_EXISTS");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PostMapping("/spf")
	public ResponseEntity<Object> postSpf(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam("domain") String domain) {
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			String role = user.getString("ROLE");

			MongoCollection<Document> collection = mongoTemplate.getCollection("spf_records_" + companyId);

			Document spf = collection.find(Filters.eq("DOMAIN", domain)).first();

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (spf != null) {
				throw new BadRequestException("SPF_RECORD_EXISTS");
			}
			
			if(!DomainValidator.getInstance().isValid(domain)){
				throw new BadRequestException("DOMAIN_INVALID");
			}

//			MongoCollection<Document> emailChannelsCollection = mongoTemplate
//					.getCollection("channels_email_" + companyId);
//			Document emailChannel = emailChannelsCollection.find(Filters.and(Filters.eq("DOMAIN", domain),
//					Filters.eq("TYPE", "External"), Filters.eq("IS_VERIFIED", true))).first();
//			if (emailChannel == null) {
//				throw new BadRequestException("EMAIL_CHANNEL_NOT_EXISTS");
//			}

//			String domain = email.split("@")[1];

			if (checkSPF(domain)) {
				JSONObject spfRecord = new JSONObject();
				spfRecord.put("DOMAIN", domain);
				spfRecord.put("DATE_CREATED", global.getFormattedDate(new Timestamp(new Date().getTime())));
				spfRecord.put("DATE_UPDATED", global.getFormattedDate(new Timestamp(new Date().getTime())));
				spfRecord.put("CREATED_BY", userId);
				spfRecord.put("LAST_UPDATED_BY", userId);

				Document spfDoc = Document.parse(spfRecord.toString());
				collection.insertOne(spfDoc);
				spfRecord.put("SPF_ID", spfDoc.getObjectId("_id").toString());

				return new ResponseEntity<>(spfRecord.toString(), global.postHeaders, HttpStatus.OK);
			} else {
				String textRecord = checkTextRecord(domain);
				JSONObject result = new JSONObject();
				result.put("ERROR", textRecord);
				return new ResponseEntity<>(result.toString(), HttpStatus.BAD_REQUEST);
			}

		}
		catch (JSONException e) {
			e.printStackTrace();
		} 
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/spf/{spf_id}")
	public ResponseEntity<Object> updateSpf(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam("domain") String domain, @PathVariable("spf_id") String spfId) {

		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			String role = user.getString("ROLE");

			MongoCollection<Document> collection = mongoTemplate.getCollection("spf_records_" + companyId);
			if (!ObjectId.isValid(spfId)) {
				throw new BadRequestException("INVALID_SPF_ID");
			}
			Document spf = collection.find(Filters.eq("_id", new ObjectId(spfId))).first();
			String dateCreated = spf.getString("DATE_CREATED");
			String createdBy = spf.getString("CREATED_BY");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (spf == null) {
				throw new ForbiddenException("SPF_RECORD_DOES_NOT_EXISTS");
			}

			if (!DomainValidator.getInstance().isValid(domain)) {
				throw new BadRequestException("DOMAIN_INVALID");
			}

//			MongoCollection<Document> emailChannelsCollection = mongoTemplate
//					.getCollection("channels_email_" + companyId);
//			Document emailChannel = emailChannelsCollection.find(Filters.and(Filters.eq("EMAIL_ADDRESS", email),
//					Filters.eq("TYPE", "External"), Filters.eq("IS_VERIFIED", true))).first();
//
//			if (emailChannel == null) {
//				throw new BadRequestException("EMAIL_CHANNEL_NOT_EXISTS");
//			}

//			String domain = email.split("@")[1];

			if (checkSPF(domain)) {
				JSONObject spfRecord = new JSONObject();
				spfRecord.put("DOMAIN", domain);
				spfRecord.put("DATE_CREATED", dateCreated);
				spfRecord.put("DATE_UPDATED", global.getFormattedDate(new Timestamp(new Date().getTime())));
				spfRecord.put("CREATED_BY", createdBy);
				spfRecord.put("LAST_UPDATED_BY", userId);

				Document spfDoc = Document.parse(spfRecord.toString());
				collection.findOneAndReplace(Filters.eq("_id", new ObjectId(spfId)), spfDoc);

				return new ResponseEntity<>(spfRecord.toString(), global.postHeaders, HttpStatus.OK);
			} else {
				String textRecord = checkTextRecord(domain);
				JSONObject result = new JSONObject();
				result.put("ERROR", textRecord);
				return new ResponseEntity<>(result.toString(), HttpStatus.BAD_REQUEST);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/spf/{spf_id}")
	public ResponseEntity<Object> deleteSpfRecord(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("spf_id") String spfId) {
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String userId = user.getString("USER_ID");
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String collectionName = "spf_records_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}
			if (!ObjectId.isValid(spfId)) {
				throw new BadRequestException("INVALID_SPF_ID");
			}
			Document spfDocument = collection.find(Filters.eq("_id", new ObjectId(spfId))).first();

			if (spfDocument != null) {
				collection.deleteOne(Filters.eq("_id", new ObjectId(spfId)));
			} else {
				throw new ForbiddenException("SPF_RECORD_DOES_NOT_EXISTS");
			}
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

	public static Boolean checkSPF(String domain) {
		java.util.Hashtable<String, String> env = new java.util.Hashtable<String, String>();
		env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
		try {
			javax.naming.directory.DirContext dirContext = new javax.naming.directory.InitialDirContext(env);
			javax.naming.directory.Attributes attrs = dirContext.getAttributes(domain, new String[] { "TXT" });
			NamingEnumeration<? extends javax.naming.directory.Attribute> a = attrs.getAll();

			javax.naming.directory.Attribute att = a.next();
			String txtRecord = att.toString();
			if (txtRecord.contains("include:ngdesk.com") || txtRecord.contains("+include:ngdesk.com")) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	public static String checkTextRecord(String uri) {
		java.util.Hashtable<String, String> env = new java.util.Hashtable<String, String>();
		env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
		try {
			javax.naming.directory.DirContext dirContext = new javax.naming.directory.InitialDirContext(env);
			javax.naming.directory.Attributes attrs = dirContext.getAttributes(uri, new String[] { "TXT" });
			NamingEnumeration<? extends javax.naming.directory.Attribute> a = attrs.getAll();

			javax.naming.directory.Attribute att = a.next();
			String txtRecord = att.toString();

			return txtRecord.substring(txtRecord.indexOf(":") + 1, txtRecord.length()).replace("\"", "");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

}
