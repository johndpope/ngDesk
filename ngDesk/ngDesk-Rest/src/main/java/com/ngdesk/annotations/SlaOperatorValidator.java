package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.Global;

public class SlaOperatorValidator implements ConstraintValidator<ValidSlaOperators, String> {
	@Override
	public boolean isValid(String operator, ConstraintValidatorContext arg1) {
		if (operator != null) {
			if (!Global.slaOperators.contains(operator)) {
				return false;
			}
		}

		return true;
	}
}
