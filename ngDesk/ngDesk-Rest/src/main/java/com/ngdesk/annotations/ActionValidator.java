package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.Global;

public class ActionValidator implements ConstraintValidator<ValidAction, String> {

	@Override
	public boolean isValid(String action, ConstraintValidatorContext arg1) {

		if (!Global.actions.contains(action.toLowerCase())) {
			return false;
		}

		return true;
	}
}