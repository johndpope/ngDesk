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
import com.ngdesk.websocket.channels.chat.dao.ChatStatusMessage;
import com.ngdesk.websocket.channels.chat.dao.PageLoad;
import com.ngdesk.websocket.notification.dao.Notification;
import com.ngdesk.websocket.subscribers.ChatSettingsUpdateSubscriber;
import com.ngdesk.websocket.subscribers.ChatStatusSubscriber;
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

	@Bean
	public RedisTemplate<String, WorkflowPayload> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, WorkflowPayload> redisTemplate = new RedisTemplate<String, WorkflowPayload>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<WorkflowPayload>(WorkflowPayload.class));
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		return redisTemplate;
	}

	@Bean
	public RedisTemplate<String, PageLoad> redisPageLoadTemplate(LettuceConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, PageLoad> redisPageLoadTemplate = new RedisTemplate<String, PageLoad>();
		redisPageLoadTemplate.setConnectionFactory(redisConnectionFactory);
		redisPageLoadTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<PageLoad>(PageLoad.class));
		redisPageLoadTemplate.setKeySerializer(new StringRedisSerializer());
		return redisPageLoadTemplate;
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
	public RedisMessageListenerContainer redisMessageListenerContainer(
			LettuceConnectionFactory redisConnectionFactory) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		container.addMessageListener(notificationListener(), new PatternTopic("notification"));
		container.addMessageListener(moduleNotificationListener(), new PatternTopic("module_notification"));
		container.addMessageListener(chatSettingsUpdateListner(), new PatternTopic("chat_settings_update"));
		container.addMessageListener(chatStatusListner(), new PatternTopic("chat_status"));

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
