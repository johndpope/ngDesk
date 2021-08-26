package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.schedules.Restriction;

public class RestrictionValidator implements ConstraintValidator<ValidRestriction, Restriction> {

	@Override
	public boolean isValid(Restriction restriction, ConstraintValidatorContext context) {
		if (restriction.getStartDay() != null && restriction.getEndDay() == null) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("START_END_DAY_REQUIRED").addConstraintViolation();
			return false;
		} else if (restriction.getEndDay() != null && restriction.getStartDay() == null) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("START_END_DAY_REQUIRED").addConstraintViolation();
			return false;
		}

		return true;
	}

}
