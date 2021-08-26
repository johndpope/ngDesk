package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.Global;

public class NodeTypeValidator implements ConstraintValidator<NodeType, String> {

	@Override
	public boolean isValid(String nodeType, ConstraintValidatorContext arg1) {

		if (Global.nodeTypes.contains(nodeType)) {
			return true;
		}

		return false;
	}

}
