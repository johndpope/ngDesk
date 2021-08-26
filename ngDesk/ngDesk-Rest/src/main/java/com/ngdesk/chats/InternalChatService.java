package com.ngdesk.chats;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.BsonArray;
import org.bson.Document;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;

@Component
@RestController
public class InternalChatService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	private final Logger log = LoggerFactory.getLogger(InternalChatService.class);

	@PostMapping("/chats")
	public InternalChat createOrUpdateChat(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody InternalChat chat) {
		try {
			log.trace("Enter InternalChatService.createOrUpdateChat()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject userDetails = auth.getUserDetails(uuid);
			String companyId = userDetails.getString("COMPANY_ID");
			String collectionName = "internal_chats_" + companyId;
			String chatString = new ObjectMapper().writeValueAsString(chat);
			JSONObject chatJson = new JSONObject(chatString);
			JSONArray participants = chatJson.getJSONArray("PARTICIPANTS");
			BsonArray participantsDocument = BsonArray.parse(participants.toString());

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document document = collection.find(Filters.all("PARTICIPANTS", participantsDocument)).first();

			if (document != null) {
				String chatId = document.getObjectId("_id").toString();
				document.remove("_id");
				InternalChat existingChat = new ObjectMapper().readValue(document.toJson(), InternalChat.class);
				existingChat.setChatId(chatId);
				return existingChat;

			} else {
				chat.setDateCreated(new Timestamp(new Date().getTime()));
				String newChatString = new ObjectMapper().writeValueAsString(chat);

				document = Document.parse(newChatString);
				collection.insertOne(document);
				String chatId = document.getObjectId("_id").toString();
				chat.setChatId(chatId);
				log.trace("Exit InternalChatService.createOrUpdateChat()");
				return chat;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/chats/participants/add")
	public ResponseEntity<Object> addParticipantToChat(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody Participant participant) {
		try {
			log.trace("Enter InternalChatService.addParticipantToChat()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject userDetails = auth.getUserDetails(uuid);
			String companyId = userDetails.getString("COMPANY_ID");
			String collectionName = "internal_chats_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			if (new ObjectId().isValid(participant.getChatId())) {
				Document document = collection.find(Filters.eq("_id", new ObjectId(participant.getChatId()))).first();

				if (document != null) {

					List<String> participants = (List<String>) document.get("PARTICIPANTS");

					if (!participants.contains(participant.getParticipantId())) {
						collection.updateOne(Filters.eq("_id", new ObjectId(participant.getChatId())),
								Updates.addToSet("PARTICIPANTS", participant.getParticipantId()));
						log.trace("Exit InternalChatService.addParticipantToChat()");
						return new ResponseEntity<>(HttpStatus.OK);
					} else {
						throw new BadRequestException("PARTICIPANT_EXISTS");
					}

				} else {
					throw new ForbiddenException("CHAT_DOES_NOT_EXIST");
				}
			} else {
				throw new BadRequestException("INVALID_ENTRY_ID");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@PutMapping("/chats/participants/leave")
	public ResponseEntity<Object> removeParticipantFromChat(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@Valid @RequestBody Participant participant) {
		try {

			log.trace("Enter InternalChatService.removeParticipantFromChat()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject userDetails = auth.getUserDetails(uuid);
			String companyId = userDetails.getString("COMPANY_ID");
			String collectionName = "internal_chats_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

			Document document = collection.find(Filters.eq("_id", new ObjectId(participant.getChatId()))).first();

			if (document != null) {

				List<String> participants = (List<String>) document.get("PARTICIPANTS");

				if (participants.contains(participant.getParticipantId())) {
					collection.updateOne(Filters.eq("_id", new ObjectId(participant.getChatId())),
							Updates.pull("PARTICIPANTS", participant.getParticipantId()));
				} else {
					throw new BadRequestException("PARTICIPANT_DOES_NOT_EXIST");
				}
				log.trace("Exit InternalChatService.removeParticipantFromChat()");
				return new ResponseEntity<>(HttpStatus.OK);
			} else {
				throw new ForbiddenException("CHAT_DOES_NOT_EXIST");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
