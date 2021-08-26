package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.Global;

public class ChatPromptTriggersValidator implements ConstraintValidator<ValidChatPromptTriggers, String> {
	@Override
	public boolean isValid(String triggers, ConstraintValidatorContext arg1) {
		if (triggers != null) {
			if (!Global.validChatPromptTriggers.contains(triggers)) {
				return false;
			}
		}

		return true;
	}

}
