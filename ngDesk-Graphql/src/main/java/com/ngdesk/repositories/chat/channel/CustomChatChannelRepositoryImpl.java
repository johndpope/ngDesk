package com.ngdesk.repositories.chat.channel;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.graphql.chat.channel.dao.ChatChannel;

public class CustomChatChannelRepositoryImpl implements CustomChatChannelRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<List<ChatChannel>> findAllChannels(Pageable pageable, String collectionName) {
		Query query = new Query();
		query.with(pageable);
		return Optional.ofNullable(mongoOperations.find(query, ChatChannel.class, collectionName));
	}

	@Override
	public Optional<ChatChannel> findByChannelName(String name, String collectionName) {

		Criteria criteria = new Criteria();
		Query query = new Query(criteria.where("NAME").is(name));

		return Optional.ofNullable(mongoOperations.findOne(query, ChatChannel.class, collectionName));
	}

}
