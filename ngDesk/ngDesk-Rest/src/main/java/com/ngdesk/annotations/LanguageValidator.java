package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.ngdesk.Global;

public class LanguageValidator implements ConstraintValidator<ValidLanguage, String> {
	@Autowired
	Global global;

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {

		if (global.languages.contains(value)) {
			return true;
		}

		return false;
	}

}
