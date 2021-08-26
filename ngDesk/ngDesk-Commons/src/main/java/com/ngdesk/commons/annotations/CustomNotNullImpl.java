package com.ngdesk.commons.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CustomNotNullImpl implements ConstraintValidator<CustomNotNull, Object> {

	@Override
	public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
		return object != null;
	}

}
