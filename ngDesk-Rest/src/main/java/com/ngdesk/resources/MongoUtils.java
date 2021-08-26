package com.ngdesk.resources;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;

@Component
public class MongoUtils {

	private static MongoTemplate mongoTemplate;

	@Autowired
	public void setMongoTemplate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	public static void createFullTextIndex(String collectionName) {
		MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
		IndexOptions options = new IndexOptions();
		options.name("_global");
		collection.createIndex(new BasicDBObject("$**", "text"), options);
	}

	public static Document createFilter(String search) {
		Document filter = null;

		if (search != null) {
			filter = new Document("$text", new Document("$search", search).append("$caseSensitive", false)
					.append("$diacriticSensitive", false));
		} else {
			filter = new Document(); // empty, filter nothing
		}

		return filter;
	}
}
