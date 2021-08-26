package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.Global;

public class ChatPromptConditionsValidator implements ConstraintValidator<ValidChatPromptConditions, String> {

	@Override
	public boolean isValid(String conditions, ConstraintValidatorContext arg1) {
		if (conditions != null) {
			if (!Global.validChatPromptConditions.contains(conditions)) {
				return false;
			}
		}

		return true;
	}
}