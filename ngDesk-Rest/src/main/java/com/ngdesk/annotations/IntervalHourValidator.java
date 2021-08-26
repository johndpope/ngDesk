package com.ngdesk.annotations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IntervalHourValidator implements ConstraintValidator<HourValidator, String> {

	@Override
	public boolean isValid(String hour, ConstraintValidatorContext arg1) {

		if (hour != null) {
			Pattern p = Pattern.compile(
					"^[0-2][0-3]?$|\\*|^[0-2][0-3]?(,[0-2][0-3]?)*$|^[0-2][0-3]?-[0-2][0-3]?(,[0-2][0-3]?-[0-2][0-3]?)*$|^(([0-2][0-3]?|\\*)\\/[0-2][0-3]?)|([0-2][0-3]?-[0-2][0-3]?\\/[0-2][0-3]?)$");
			Matcher m = p.matcher(hour);
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
