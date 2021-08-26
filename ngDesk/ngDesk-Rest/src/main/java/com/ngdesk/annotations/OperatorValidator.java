package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.Global;

public class OperatorValidator implements ConstraintValidator<ValidOperator, String> {

	@Override
	public boolean isValid(String operator, ConstraintValidatorContext arg1) {
		if (operator != null) {
			if (!Global.operators.contains(operator)) {
				return false;
			}
		}

		return true;
	}
}