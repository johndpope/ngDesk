package com.ngdesk.sam.controllers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

class MongoDateConverter extends JsonDeserializer<String> {
	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	@Override
	public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		JsonNode node = jp.readValueAsTree();

		try {
			return formatter.format(node.get("$date").asLong());
		} catch (Exception e) {
			return null;
		}
	}
}
