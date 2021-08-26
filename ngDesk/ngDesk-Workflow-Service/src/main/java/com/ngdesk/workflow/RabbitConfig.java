package com.ngdesk.workflow;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

	@Bean
	public Queue executeSingle() {
		return new Queue("execute-single-workflow", true);
	}

	@Bean
	public Queue executeMultiple() {
		return new Queue("execute-module-workflows", true);
	}

	@Bean
	public Queue executeChatWorkflow() {
		return new Queue("execute-chat-workflows", true);
	}

	@Bean
	public Queue executeWorkflowNodes() {
		return new Queue("execute-nodes", true);
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
