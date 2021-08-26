package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.Global;

public class ReportingOperatorValidator implements ConstraintValidator<ReportingOperators, String> {

	@Override
	public boolean isValid(String operator, ConstraintValidatorContext context) {

		if (Global.reportingOperators.contains(operator)) {
			return true;
		}

		return false;
	}

}
