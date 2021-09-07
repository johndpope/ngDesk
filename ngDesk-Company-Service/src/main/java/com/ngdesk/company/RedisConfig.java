package com.ngdesk.company;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.ngdesk.company.settings.dao.ChatSettingsMessage;

@Configuration
public class RedisConfig {

	@Value("${spring.redis.host}")
	String redisHost;

	@Value("${spring.redis.port}")
	int redisPort;

	@Value("${spring.redis.password}")
	String redisPassword;

	@Bean
	public RedisTemplate<String, ChatSettingsMessage> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, ChatSettingsMessage> redisTemplate = new RedisTemplate<String, ChatSettingsMessage>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate
				.setValueSerializer(new Jackson2JsonRedisSerializer<ChatSettingsMessage>(ChatSettingsMessage.class));
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
