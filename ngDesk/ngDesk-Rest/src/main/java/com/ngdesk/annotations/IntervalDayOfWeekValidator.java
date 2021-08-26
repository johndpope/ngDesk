package com.ngdesk.annotations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IntervalDayOfWeekValidator implements ConstraintValidator<DayOfWeekValidator, String> {

	@Override
	public boolean isValid(String dayOfWeek, ConstraintValidatorContext context) {

		if (dayOfWeek != null) {
			Pattern p = Pattern.compile(
					"^[0-6]$|\\*|^[0-6](,[0-6])*$|^[0-6]-[0-6](,[0-6]-[0-6])*$|^(([0-6]|\\*)\\/[0-6])|([0-6]-[0-6]\\/[0-6]?)$");
			Matcher m = p.matcher(dayOfWeek);
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
