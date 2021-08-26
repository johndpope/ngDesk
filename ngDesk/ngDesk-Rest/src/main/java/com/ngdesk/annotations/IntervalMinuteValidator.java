package com.ngdesk.annotations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IntervalMinuteValidator implements ConstraintValidator<MinutesValidator, String> {

	@Override
	public boolean isValid(String minutes, ConstraintValidatorContext arg1) {

		if (minutes != null) {
			Pattern p = Pattern.compile(
					"^[0-5][0-9]?$|\\*|^[0-5][0-9]?(,[0-5][0-9]?)*$|^[0-5][0-9]?-[0-5][0-9]?(,[0-5][0-9]?-[0-5][0-9]?)*$|^(([0-5][0-9]?|\\*)\\/[0-5][0-9]?)|([0-5][0-9]?-[0-5][0-9]?\\/[0-5][0-9]?)$");
			Matcher m = p.matcher(minutes);

			if (m.matches()) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}

	}

}
