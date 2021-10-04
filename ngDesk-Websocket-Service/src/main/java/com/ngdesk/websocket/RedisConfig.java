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
import com.ngdesk.websocket.channels.chat.dao.ChatStatusMessage;
import com.ngdesk.websocket.notification.dao.Notification;
import com.ngdesk.websocket.notification.dao.NotificationOfAgentDetails;
import com.ngdesk.websocket.subscribers.ChatChannelSubscriber;
import com.ngdesk.websocket.subscribers.ChatSettingsUpdateSubscriber;
import com.ngdesk.websocket.subscribers.ChatStatusSubscriber;
import com.ngdesk.websocket.subscribers.ModuleNotificationSubscriber;
import com.ngdesk.websocket.subscribers.NotificationOfAgentDetailsSubscriber;
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
	NotificationOfAgentDetailsSubscriber agentAvailableSubscriber;

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
	public RedisTemplate<String, NotificationOfAgentDetails> redisNotificationOfAgentDetailsSubscriberTemplate(
			LettuceConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, NotificationOfAgentDetails> redisNotificationOfAgentDetailsTemplate = new RedisTemplate<String, NotificationOfAgentDetails>();
		redisNotificationOfAgentDetailsTemplate.setConnectionFactory(redisConnectionFactory);
		redisNotificationOfAgentDetailsTemplate.setValueSerializer(
				new Jackson2JsonRedisSerializer<NotificationOfAgentDetails>(NotificationOfAgentDetails.class));
		redisNotificationOfAgentDetailsTemplate.setKeySerializer(new StringRedisSerializer());
		return redisNotificationOfAgentDetailsTemplate;
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
	MessageListenerAdapter agentAvailableListener() {
		return new MessageListenerAdapter(agentAvailableSubscriber);
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
		container.addMessageListener(agentAvailableListener(), new PatternTopic("agents_available"));

		return container;
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

}
