package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.ngdesk.workflow.Node;

public class NodeValidator implements ConstraintValidator<ValidNode, Node> {

	private static MongoTemplate mongoTemplate;

	@Autowired
	public void setMongoTemplate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public boolean isValid(Node node, ConstraintValidatorContext context) {

		// GET NODE TYPE
		String type = node.getType();

		if (type.equals("SendEmail")) {
			if (node.getValues().getTo() == null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("INCORRECT_EMAIL_NODE_VALUE_TO").addConstraintViolation();
				return false;
			} else if (node.getValues().getFrom() == null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("INCORRECT_EMAIL_NODE_VALUE_FROM")
						.addConstraintViolation();
				return false;
			} else if (node.getValues().getSubject() == null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("INCORRECT_EMAIL_NODE_VALUE_SUBJECT")
						.addConstraintViolation();
				return false;
			} else if (node.getValues().getBody() == null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("INCORRECT_EMAIL_NODE_VALUE_BODY")
						.addConstraintViolation();
				return false;
			}
		} else if (type.equals("HttpRequest")) {
			if (node.getValues().getUrl() == null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("INCORRECT_HTTP_REQUEST_NODE_VALUE_URL")
						.addConstraintViolation();
				return false;
			} else if (node.getValues().getBody() == null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("INCORRECT_HTTP_REQUEST_NODE_VALUE_BODY")
						.addConstraintViolation();
				return false;
			} else if (node.getValues().getType() == null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("INCORRECT_HTTP_REQUEST_NODE_VALUE_TYPE")
						.addConstraintViolation();
				return false;
			} else if (node.getValues().getHeaders() == null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("INCORRECT_HTTP_REQUEST_NODE_VALUE_HEADERS")
						.addConstraintViolation();
				return false;
			} else if (node.getValues().getUrl() != null) {
				if (node.getValues().getUrl().contains("localhost")
						|| node.getValues().getUrl().contains("127.0.0.1")) {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate("BLOCKED_URL").addConstraintViolation();
					return false;
				}
			}

		} else if (type.equals("Javascript")) {
			if (node.getValues().getCode() == null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("INCORRECT_JAVASCRIPT_NODE_VALUES")
						.addConstraintViolation();
				return false;
			}
		} else if (type.equals("Say")) {
			if (node.getValues().getMessage() == null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("INCORRECT_SAY_NODE_VALUES").addConstraintViolation();
				return false;
			}
		} else if (type.equals("Route")) {
			// if (node.getValues().getVariable() == null ||
			// node.getValues().getConditions() == null) {
			if (node.getValues().getVariable() == null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("INCORRECT_ROUTE_NODE_VALUE_VARIABLE")
						.addConstraintViolation();
				return false;
			} else if (node.getValues().getConditions() == null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("INCORRECT_ROUTE_NODE_VALUE_CONDITION")
						.addConstraintViolation();
				return false;
			}
		}

		return true;
	}
}