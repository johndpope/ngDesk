package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.Global;

public class TimezoneValidator implements ConstraintValidator<Timezone, String> {

	@Override
	public boolean isValid(String timezone, ConstraintValidatorContext arg1) {

		if (Global.timezones.contains(timezone)) {
			return true;
		}

		return false;
	}

}
