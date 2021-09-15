package com.ngdesk.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.websocket.channels.chat.ChatChannel;

public class CustomChatChannelRepositoryImpl implements CustomChatChannelRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<ChatChannel> findChannelById(String id, String collectionName) {

		Criteria criteria = new Criteria();
		Query query = new Query(criteria.where("_id").is(id));

		return Optional.ofNullable(mongoOperations.findOne(query, ChatChannel.class, collectionName));
	}

	@Override
	public Optional<ChatChannel> findChannelByName(String name, String collectionName) {

		Criteria criteria = new Criteria();
		Query query = new Query(criteria.where("NAME").is(name));

		return Optional.ofNullable(mongoOperations.findOne(query, ChatChannel.class, collectionName));
	}

}
