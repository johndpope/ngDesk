package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.redisson.executor.CronExpression;


public class CronValidator implements ConstraintValidator<CronValid, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (CronExpression.isValidExpression(value)) {
			return true;
		}

		return false;
	}

}
