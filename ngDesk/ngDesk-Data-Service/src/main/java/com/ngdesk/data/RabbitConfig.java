package com.ngdesk.data;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

	@Bean
	public Queue deleteEntriesQueue() {
		return new Queue("delete-entries", true);
	}

	@Bean
	public Queue elasticConsumerQueue() {
		return new Queue("post-module-data-to-elastic", true);
	}

	@Bean
	public Queue samListenerQueue() {
		return new Queue("new-sam-entry", true);
	}

	@Bean
	public Queue elasticUpdatesQueue() {
		return new Queue("elastic-updates", true);
	}

	@Bean
	public Queue manyToManyRelationshipQueue() {
		return new Queue("many-to-many-updates", true);
	}

	@Bean
	public Queue addMetadataEntriesQueue() {
		return new Queue("add-events", true);
	}

	@Bean
	public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
		final var rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
		return rabbitTemplate;
	}

	@Bean
	public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
