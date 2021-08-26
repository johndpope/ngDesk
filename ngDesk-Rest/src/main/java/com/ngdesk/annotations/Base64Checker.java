package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.codec.binary.Base64;

public class Base64Checker implements ConstraintValidator<Base64Validator, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return Base64.isBase64(value);
	}

}
