package com.ngdesk.data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.ngdesk.data.dao.WorkflowPayload;


@Configuration
public class RedisConfig {
	
	@Value("${spring.redis.host}")
	String redisHost;

	@Value("${spring.redis.port}")
	int redisPort;

	@Value("${spring.redis.password}")
	String redisPassword;
	
	@Bean
	public RedisTemplate<String, WorkflowPayload> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, WorkflowPayload> redisTemplate = new RedisTemplate<String, WorkflowPayload>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<WorkflowPayload>(WorkflowPayload.class));
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
