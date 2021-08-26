package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.Global;

public class TimesValidator implements ConstraintValidator<ValidTimes, String> {

	@Override
	public boolean isValid(String time, ConstraintValidatorContext arg1) {

		if (Global.times.contains(time)) {
			return true;
		}

		return false;
	}

}
