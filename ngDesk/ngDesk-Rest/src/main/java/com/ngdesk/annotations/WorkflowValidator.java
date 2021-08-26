package com.ngdesk.annotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.workflow.Node;

public class WorkflowValidator implements ConstraintValidator<ValidWorkflow, List<Node>> {

	@Override
	public boolean isValid(List<Node> nodes, ConstraintValidatorContext context) {

		// GET NODES, LOOP THROUGH, CHECK IF COMBO OF TYPE TO NAMES IS UNIQUE
		Map<String, List<String>> map = new HashMap<String, List<String>>() {
		};

		for (Node node : nodes) {
			String type = node.getType();
			String name = node.getName();

			if (map.get(type) != null) {
				if (map.get(type).contains(name)) {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate("NODE_NAMES_NOT_UNIQUE").addConstraintViolation();
					return false;
				} else {
					map.get(type).add(name);
				}
			} else {
				List<String> newList = new ArrayList<String>();
				map.put(type, newList);
				map.get(type).add(name);
			}
		}

		return true;
	}

}
