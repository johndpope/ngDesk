package com.ngdesk.workflow;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.ngdesk.workflow.notification.dao.Notification;

@Configuration
public class RedisConfig {

	@Value("${spring.redis.host}")
	String redisHost;

	@Value("${spring.redis.port}")
	int redisPort;

	@Value("${spring.redis.password}")
	String redisPassword;

	@Bean
	public RedisTemplate<String, Notification> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
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

}
