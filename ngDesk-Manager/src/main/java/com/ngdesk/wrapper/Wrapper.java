package com.ngdesk.wrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.data.elastic.ElasticService;
import com.ngdesk.exceptions.InternalErrorException;

@Component
public class Wrapper {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Environment env;

	@Autowired
	Global global;

	@Autowired
	ElasticService elasticService;

	private final Logger log = LoggerFactory.getLogger(Wrapper.class);

	// USED TO POST DATA INTO MONGO AND ELASTIC
	public Document postData(String companyId, String moduleId, String moduleName, String body) {
		RestHighLevelClient elasticClient = null;
		try {
			log.trace("Enter Wrapper.postData()");

			String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
			elasticClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(env.getProperty("elastic.host"), 9200, "http")));

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document bodyDoc = Document.parse(body);
			bodyDoc.put("DATE_CREATED", new Date());
			bodyDoc.put("DATE_UPDATED", new Date());
			bodyDoc.put("EFFECTIVE_FROM", new Date());
			collection.insertOne(bodyDoc);
			String id = bodyDoc.getObjectId("_id").toString();

			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> entryMap = mapper.readValue(mapper.writeValueAsString(bodyDoc), Map.class);
			entryMap.put("_id", id);

			elasticService.postIntoElastic(moduleId, companyId, entryMap);

			return bodyDoc;

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

	// USED TO UPDATE DATA IN MONGO AND ELASTIC
	public String putData(String companyId, String moduleId, String moduleName, String body, String dataId) {
		RestHighLevelClient elasticClient = null;
		try {
			log.trace("Enter Wrapper.putData(): CompanyId: " + companyId + ", ModuleId: " + moduleId + " DataId: "
					+ dataId);
			String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;

			elasticClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(env.getProperty("elastic.host"), 9200, "http")));

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document bodyDoc = Document.parse(body);

			if (moduleName.equals("Users")) {
				Document existingDocument = collection.find(Filters.eq("_id", new ObjectId(dataId))).first();

				// CHECK IF ROLE CHANGED
				if (!bodyDoc.getString("ROLE").equals(existingDocument.getString("ROLE"))) {
					// IF CHANGED UPDATE DEFAULT TEAMS

					MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
					Document existingRole = rolesCollection
							.find(Filters.eq("_id", new ObjectId(existingDocument.getString("ROLE")))).first();
					Document newRole = rolesCollection.find(Filters.eq("_id", new ObjectId(bodyDoc.getString("ROLE"))))
							.first();

					String existingRoleName = existingRole.getString("NAME");
					String newRoleName = newRole.getString("NAME");

					MongoCollection<Document> teamsCollection = mongoTemplate.getCollection("Teams_" + companyId);

					Document oldTeam = teamsCollection.find(Filters.eq("NAME", existingRoleName)).first();
					Document newTeam = teamsCollection.find(Filters.eq("NAME", newRoleName)).first();

					String oldTeamId = oldTeam.getObjectId("_id").toString();
					String newTeamId = newTeam.getObjectId("_id").toString();

					List<String> teams = (List<String>) bodyDoc.get("TEAMS");
					teams.remove(oldTeamId);
					teams.add(newTeamId);

					newTeam.put("TEAMS", teams);

					teamsCollection.updateOne(Filters.eq("NAME", existingRoleName), Updates.pull("USERS", dataId));
					teamsCollection.updateOne(Filters.eq("NAME", newRoleName), Updates.addToSet("USERS", dataId));

				}
			}
			// TEMPORAL DATA LOGIC
			Document existingEntryDoc = collection.find(Filters.eq("_id", new ObjectId(dataId))).first();
			existingEntryDoc.put("DATA_ID", existingEntryDoc.remove("_id").toString());
			if (!moduleName.equalsIgnoreCase("teams")) {
				existingEntryDoc.put("EFFECTIVE_TO", new Date());
				collection.insertOne(existingEntryDoc);
			}
			bodyDoc.put("_id", new ObjectId(dataId));
			collection.findOneAndReplace(Filters.eq("_id", new ObjectId(dataId)), bodyDoc);
			String id = bodyDoc.getObjectId("_id").toString();

			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> entryMap = mapper.readValue(mapper.writeValueAsString(bodyDoc), Map.class);
			entryMap.put("_id", id);

			elasticService.postIntoElastic(moduleId, companyId, entryMap);

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

	// ADDS ENTRY TO GLOBAL SEARCH
	public void loadDataForGlobalSearch(Document module, JSONObject body, String companyId, String method) {
		RestHighLevelClient elasticClient = null;

		try {
			log.trace("Enter Wrapper.loadDataForGlobalSearch()");

			elasticClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(env.getProperty("elastic.host"), 9200, "http")));

			String moduleId = module.getObjectId("_id").toString();
			JSONArray teamsArray = new JSONArray();
			if (body.has("TEAMS")) {
				teamsArray = body.getJSONArray("TEAMS");
			}

			String discussionField = "";
			List<Document> fields = (List<Document>) module.get("FIELDS");
			for (Document field : fields) {
				String fieldName = field.getString("NAME");
				Document datatype = (Document) field.get("DATA_TYPE");

				if (body.has(fieldName) && body.get(fieldName) != null) {
					if (datatype.getString("DISPLAY").equals("Discussion")) {
						JSONArray discussionMessages = body.getJSONArray(fieldName);

						for (int i = 0; i < discussionMessages.length(); i++) {
							JSONObject discussionMessage = discussionMessages.getJSONObject(i);
							String message = discussionMessage.getString("MESSAGE");

							discussionField += Jsoup.parse(message).text();
						}
					}
				}
			}

			BulkRequest request = new BulkRequest();
			int count = 0;
			List<String> objList = new ArrayList<String>();
			for (Document field : fields) {
				String fieldName = field.getString("NAME");
				Document datatype = (Document) field.get("DATA_TYPE");
				String displayDatatype = datatype.getString("DISPLAY");

				if (displayDatatype.equals("Text") || displayDatatype.equals("Text Area")
						|| displayDatatype.equals("Text Area Long") || displayDatatype.equals("Text Area Rich")
						|| displayDatatype.equals("Discussion") || displayDatatype.equals("Picklist")
						|| displayDatatype.equals("Email") || displayDatatype.equals("Phone")
						|| displayDatatype.equals("Auto Number") || displayDatatype.equals("Number")
						|| displayDatatype.equals("URL") || displayDatatype.equals("Date")
						|| displayDatatype.equals("Date/Time") || displayDatatype.equals("Street 1")
						|| displayDatatype.equals("Street 2") || displayDatatype.equals("City")
						|| displayDatatype.equals("Country") || displayDatatype.equals("State")
						|| displayDatatype.equals("Zipcode")) {
					if (body.has(fieldName) && body.get(fieldName) != null) {

						JSONObject objIn = new JSONObject();

						if (displayDatatype.equals("Discussion")) {
							objIn.put("input", discussionField);
						} else if (displayDatatype.equals("Phone")) {
							JSONObject phone = body.getJSONObject(fieldName);
							if (!phone.isNull("PHONE_NUMBER") && !phone.isNull("DIAL_CODE")) {
								String phoneNumber = phone.getString("PHONE_NUMBER") + phone.getString("DIAL_CODE");
								objIn.put("input", phoneNumber);
							}
						} else {
							objIn.put("input", body.get(fieldName).toString());
						}

						objIn.put("FIELD_NAME", fieldName);
						objIn.put("TEAMS", teamsArray);
						objIn.put("MODULE_ID", moduleId);
						objIn.put("ENTRY_ID", body.getString("DATA_ID"));
						objIn.put("COMPANY_ID", companyId);

						String obj = objIn.toString();
						objList.add(obj);

					}
				}

			}

			log.trace("Exit Wrapper.loadDataForGlobalSearch()");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
