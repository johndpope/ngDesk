package com.ngdesk.annotations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IntervalMonthValidator implements ConstraintValidator<MonthValidator, String> {

	@Override
	public boolean isValid(String month, ConstraintValidatorContext arg1) {

		if (month != null) {
			Pattern p = Pattern.compile(
					"^[1-9][0-2]?$|\\*|^[1-9][0-2]?(,[1-9][0-2]?)*$|^[1-9][0-2]?-[1-9][0-2]?(,[1-9][0-2]?-[1-9][0-2]?)*$|^(([1-9][0-2]?|\\*)\\/[1-9][0-2]?)|([1-9][0-2]?-[1-9][0-2]?\\/[1-9][0-2]?)$");
			Matcher m = p.matcher(month);

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
