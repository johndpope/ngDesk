package com.ngdesk.commons.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CustomNotEmptyImpl implements ConstraintValidator<CustomNotEmpty, Object> {

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		try {
			if (value != null && value.toString().trim().length() > 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
