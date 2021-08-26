package com.ngdesk.jobs;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.bson.Document;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.ngdesk.email.SendEmail;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.sam.controllers.Controller;
import com.ngdesk.sam.controllers.ControllerLog;

@Component
public class CheckControllerStatus {

	private final static Logger log = LoggerFactory.getLogger(CheckControllerStatus.class);

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Value("${email.host}")
	private String host;
	
	@Value("${elastic.host}")
	private String elasticHost;
	
	@Value("${env}")
	private String environment;


	// Run once every 1 minutes
	@Scheduled(fixedRate = 60000)
	public void statusCheck() {
		log.trace("Enter CheckControllerStauts.statusCheck()");
		try {

			MongoCollection<Document> controllersCollection = mongoTemplate.getCollection("controllers");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.MINUTE, -2);
			Date previousDate = calendar.getTime();

			List<Document> controllersToSetOffline = new ArrayList<Document>();

			controllersToSetOffline = controllersCollection
					.find(Filters.and(Filters.eq("STATUS", "Online"), Filters.lte("LAST_SEEN", previousDate)))
					.into(new ArrayList<Document>());

			List<Document> subappsSetToOffline = controllersCollection
					.find(Filters.elemMatch("SUB_APPS",
							Filters.and(Filters.eq("STATUS", "Online"), Filters.lte("LAST_SEEN", previousDate))))
					.into(new ArrayList<Document>());

			List<Document> updatersSetToOffline = controllersCollection.find(
					Filters.and(Filters.eq("UPDATER_STATUS", "Online"), Filters.lte("UPDATER_LAST_SEEN", previousDate)))
					.into(new ArrayList<Document>());

			controllersToSetOffline.addAll(subappsSetToOffline);
			controllersToSetOffline.addAll(updatersSetToOffline);

			List<Document> distinctControllers = controllersToSetOffline.stream().distinct()
					.collect(Collectors.toList());

			controllersCollection.updateMany(
					Filters.and(Filters.eq("STATUS", "Online"), Filters.lte("LAST_SEEN", previousDate)),
					Updates.set("STATUS", "Offline"));

			controllersCollection
					.updateMany(
							Filters.elemMatch("SUB_APPS",
									Filters.and(Filters.eq("STATUS", "Online"),
											Filters.lte("LAST_SEEN", previousDate))),
							Updates.set("SUB_APPS.$[subapp].STATUS", "Offline"), new UpdateOptions()
									.arrayFilters(Arrays.asList(Filters.lte("subapp.LAST_SEEN", previousDate))));

			controllersCollection.updateMany(
					Filters.and(Filters.eq("UPDATER_STATUS", "Online"), Filters.lte("UPDATER_LAST_SEEN", previousDate)),
					Updates.set("UPDATER_STATUS", "Offline"));

			if (distinctControllers != null && distinctControllers.size() > 0) {
				for (Document oldController : distinctControllers) {
					Document controller = controllersCollection
							.find(Filters.eq("_id", oldController.getObjectId("_id"))).first();
					String controllerId = controller.remove("_id").toString();
					controller.put("CONTROLLER_ID", controllerId);
					controller.remove("_class");
					Controller controllerObject = new ObjectMapper().readValue(controller.toJson(), Controller.class);
					putControllerToElastic(controllerObject);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();

			if (environment.equals("prd")) {
				SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com", "support@ngdesk.com",
						"Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToSpencer.sendEmail();

				SendEmail sendEmailToShashank = new SendEmail("shashank.shankaranand@allbluesolutions.com",
						"support@ngdesk.com", "Internal Error: Stack Trace", sStackTrace,
						host);
				sendEmailToShashank.sendEmail();
			}
		}
		log.trace("Exit CheckControllerStauts.statusCheck()");
	}

	private void putControllerToElastic(Controller controllerObject) {
		RestHighLevelClient elasticClient = null;
		try {
			elasticClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(elasticHost, 9200, "http")));

			DeleteByQueryRequest request = new DeleteByQueryRequest("controllers");

			BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
			queryBuilder.must().add(QueryBuilders.matchQuery("CONTROLLER_ID", controllerObject.getId()));
			queryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", controllerObject.getCompanyId()));
			request.setQuery(queryBuilder);

			elasticClient.deleteByQuery(request, RequestOptions.DEFAULT);

			controllerObject.setLogs(new ArrayList<ControllerLog>());
			IndexRequest indexRequest = new IndexRequest("controllers")
					.source(new ObjectMapper().writeValueAsString(controllerObject), XContentType.JSON);
			elasticClient.index(indexRequest, RequestOptions.DEFAULT);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticClient.close();
			} catch (IOException e) {
				throw new InternalErrorException("INTERNAL_ERROR");
			}
		}
	}

}
