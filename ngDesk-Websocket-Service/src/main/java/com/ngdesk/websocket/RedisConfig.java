package com.ngdesk.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.ngdesk.data.dao.WorkflowPayload;
import com.ngdesk.websocket.channels.chat.dao.ChatChannelMessage;
import com.ngdesk.websocket.channels.chat.dao.ChatNotification;
import com.ngdesk.websocket.channels.chat.dao.ChatStatusMessage;
import com.ngdesk.websocket.channels.chat.dao.ChatTicketStatusMessage;
import com.ngdesk.websocket.channels.chat.dao.ChatVisitedPagesNotification;
import com.ngdesk.websocket.notification.dao.Notification;
import com.ngdesk.websocket.subscribers.ChatChannelSubscriber;
import com.ngdesk.websocket.subscribers.ChatNotificationSubscriber;
import com.ngdesk.websocket.subscribers.ChatSettingsUpdateSubscriber;
import com.ngdesk.websocket.subscribers.ChatStatusSubscriber;
import com.ngdesk.websocket.subscribers.ChatTicketStatusSubscriber;
import com.ngdesk.websocket.subscribers.ChatVisitedPagesSubscriber;
import com.ngdesk.websocket.subscribers.ModuleNotificationSubscriber;
import com.ngdesk.websocket.subscribers.NotificationSubscriber;

@Configuration
public class RedisConfig {

	@Value("${spring.redis.host}")
	String redisHost;

	@Value("${spring.redis.port}")
	int redisPort;

	@Value("${spring.redis.password}")
	String redisPassword;

	@Autowired
	ModuleNotificationSubscriber moduleNotificationSubscriber;

	@Autowired
	NotificationSubscriber notificationSubscriber;

	@Autowired
	ChatSettingsUpdateSubscriber chatSettingsUpdateSubscriber;

	@Autowired
	ChatStatusSubscriber chatStatusSubscriber;

	@Autowired
	ChatChannelSubscriber chatChannelSubscriber;

	@Autowired
	ChatNotificationSubscriber chatNotificationSubscriber;

	@Autowired
	ChatTicketStatusSubscriber chatTicketStatusSubscriber;

	@Autowired
	ChatVisitedPagesSubscriber chatVisitedPagesSubscriber;

	@Bean
	public RedisTemplate<String, WorkflowPayload> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, WorkflowPayload> redisTemplate = new RedisTemplate<String, WorkflowPayload>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<WorkflowPayload>(WorkflowPayload.class));
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		return redisTemplate;
	}

	@Bean
	public RedisTemplate<String, ChatStatusMessage> redisChatStatusTemplate(
			LettuceConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, ChatStatusMessage> redisChatStatusTemplate = new RedisTemplate<String, ChatStatusMessage>();
		redisChatStatusTemplate.setConnectionFactory(redisConnectionFactory);
		redisChatStatusTemplate
				.setValueSerializer(new Jackson2JsonRedisSerializer<ChatStatusMessage>(ChatStatusMessage.class));
		redisChatStatusTemplate.setKeySerializer(new StringRedisSerializer());
		return redisChatStatusTemplate;
	}

	@Bean
	public RedisTemplate<String, ChatChannelMessage> redisChatChannelTemplate(
			LettuceConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, ChatChannelMessage> redisChatChannelTemplate = new RedisTemplate<String, ChatChannelMessage>();
		redisChatChannelTemplate.setConnectionFactory(redisConnectionFactory);
		redisChatChannelTemplate
				.setValueSerializer(new Jackson2JsonRedisSerializer<ChatChannelMessage>(ChatChannelMessage.class));
		redisChatChannelTemplate.setKeySerializer(new StringRedisSerializer());
		return redisChatChannelTemplate;
	}

	@Bean
	public RedisTemplate<String, ChatNotification> redisChatNotificationTemplate(
			LettuceConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, ChatNotification> redisChatNotificationTemplate = new RedisTemplate<String, ChatNotification>();
		redisChatNotificationTemplate.setConnectionFactory(redisConnectionFactory);
		redisChatNotificationTemplate
				.setValueSerializer(new Jackson2JsonRedisSerializer<ChatNotification>(ChatNotification.class));
		redisChatNotificationTemplate.setKeySerializer(new StringRedisSerializer());
		return redisChatNotificationTemplate;
	}

	@Bean
	public RedisTemplate<String, ChatTicketStatusMessage> redisChatTicketStatusTemplate(
			LettuceConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, ChatTicketStatusMessage> redisChatTicketStatusTemplate = new RedisTemplate<String, ChatTicketStatusMessage>();
		redisChatTicketStatusTemplate.setConnectionFactory(redisConnectionFactory);
		redisChatTicketStatusTemplate.setValueSerializer(
				new Jackson2JsonRedisSerializer<ChatTicketStatusMessage>(ChatTicketStatusMessage.class));
		redisChatTicketStatusTemplate.setKeySerializer(new StringRedisSerializer());
		return redisChatTicketStatusTemplate;
	}

	@Bean
	public RedisTemplate<String, ChatVisitedPagesNotification> redisChatVisitedPagesTemplate(
			LettuceConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, ChatVisitedPagesNotification> redisChatVisitedPagesTemplate = new RedisTemplate<String, ChatVisitedPagesNotification>();
		redisChatVisitedPagesTemplate.setConnectionFactory(redisConnectionFactory);
		redisChatVisitedPagesTemplate.setValueSerializer(
				new Jackson2JsonRedisSerializer<ChatVisitedPagesNotification>(ChatVisitedPagesNotification.class));
		redisChatVisitedPagesTemplate.setKeySerializer(new StringRedisSerializer());
		return redisChatVisitedPagesTemplate;
	}

	@Bean
	public RedisTemplate<String, Notification> redisNotificationTemplate(
			LettuceConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Notification> redisTemplate = new RedisTemplate<String, Notification>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<Notification>(Notification.class));
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		return redisTemplate;
	}

	@Bean
	public LettuceConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);
		configuration.setPassword(redisPassword);
		return new LettuceConnectionFactory(configuration);
	}

	@Bean
	MessageListenerAdapter moduleNotificationListener() {
		return new MessageListenerAdapter(moduleNotificationSubscriber);
	}

	@Bean
	MessageListenerAdapter notificationListener() {
		return new MessageListenerAdapter(notificationSubscriber);
	}

	@Bean
	MessageListenerAdapter chatSettingsUpdateListner() {
		return new MessageListenerAdapter(chatSettingsUpdateSubscriber);
	}

	@Bean
	MessageListenerAdapter chatStatusListner() {
		return new MessageListenerAdapter(chatStatusSubscriber);
	}

	@Bean
	MessageListenerAdapter chatChannelListner() {
		return new MessageListenerAdapter(chatChannelSubscriber);
	}

	@Bean
	MessageListenerAdapter chatNotificationListener() {
		return new MessageListenerAdapter(chatNotificationSubscriber);
	}

	@Bean
	MessageListenerAdapter chatTicketStatusListener() {
		return new MessageListenerAdapter(chatTicketStatusSubscriber);
	}

	@Bean
	MessageListenerAdapter chatVisitedPagesListener() {
		return new MessageListenerAdapter(chatVisitedPagesSubscriber);
	}

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(
			LettuceConnectionFactory redisConnectionFactory) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		container.addMessageListener(notificationListener(), new PatternTopic("notification"));
		container.addMessageListener(moduleNotificationListener(), new PatternTopic("module_notification"));
		container.addMessageListener(chatSettingsUpdateListner(), new PatternTopic("chat_settings_update"));
		container.addMessageListener(chatStatusListner(), new PatternTopic("chat_status"));
		container.addMessageListener(chatChannelListner(), new PatternTopic("chat_channel"));
		container.addMessageListener(chatNotificationListener(), new PatternTopic("chat_notification"));
		container.addMessageListener(chatTicketStatusListener(), new PatternTopic("chat_ticket_status"));
		container.addMessageListener(chatVisitedPagesListener(), new PatternTopic("chat_visited_pages"));

		return container;
	}

}
