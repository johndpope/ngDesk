package com.ngdesk.annotations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IntervalDayOfMonthValidator implements ConstraintValidator<DayOfMonthValidator, String> {

	@Override
	public boolean isValid(String dayOfMonth, ConstraintValidatorContext context) {

		if (dayOfMonth != null) {
			Pattern p = Pattern.compile(
					"^[1-3]?[0-9]?$|\\*|^[1-3][0-9]?(,[1-3][0-9]?)*$|^[1-3][0-9]?-[1-3][0-9]?(,[1-3][0-9]?-[1-3][0-9]?)*$|^(([1-3][0-9]?|\\*)\\/[1-3][0-9]?)|([1-3][0-9]?-[1-3][0-9]?\\/[1-3][0-9]?)$");
			Matcher m = p.matcher(dayOfMonth);

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
