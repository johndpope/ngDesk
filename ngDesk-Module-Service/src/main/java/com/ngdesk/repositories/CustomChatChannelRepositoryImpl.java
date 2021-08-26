package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.ngdesk.module.channels.chat.ChatChannel;
import com.ngdesk.module.channels.chat.ChatPrompt;

public class CustomChatChannelRepositoryImpl implements CustomChatChannelRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<ChatChannel> findChannelByName(String name, String collectionName) {

		Criteria criteria = new Criteria();
		Query query = new Query(criteria.where("NAME").is(name));

		return Optional.ofNullable(mongoOperations.findOne(query, ChatChannel.class, collectionName));
	}

	@Override
	public ChatChannel deleteByName(String name, String collectionName) {

		Criteria criteria = new Criteria();
		Query query = new Query(criteria.where("NAME").is(name));
		return mongoOperations.findAndRemove(query, ChatChannel.class, collectionName);
	}

	@Override
	public void updateChatPrompt(String name, ChatPrompt chatPrompt, String collectionName) {
		Criteria criteria = new Criteria();
		Query query = new Query(criteria.where("NAME").is(name));
		Update update = new Update();
		update.addToSet("CHAT_PROMPTS", chatPrompt);
		mongoOperations.updateFirst(query, update, collectionName);
	}

	@Override
	public void updateChatPrompts(String name, List<ChatPrompt> chatPrompts, String collectionName) {
		Criteria criteria = new Criteria();
		Query query = new Query(criteria.where("NAME").is(name));
		Update update = new Update();
		update.set("CHAT_PROMPTS", chatPrompts);
		mongoOperations.updateFirst(query, update, collectionName);

	}

	@Override
	public void deleteChatPrompts(String name, String promptId, String collectionName) {
		Update update = new Update();
		update = update.pull("CHAT_PROMPTS", Query.query(Criteria.where("PROMPT_ID").is(promptId)));
		mongoOperations.updateFirst(new Query(Criteria.where("NAME").is(name)), update, collectionName);
	}

}
