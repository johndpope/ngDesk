package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.Global;

public class ChatTriggerActionValidator implements ConstraintValidator<ChatTriggerActionValid, String> {

	@Override
	public boolean isValid(String action, ConstraintValidatorContext arg1) {

		if (Global.chatTriggerActions.contains(action)) {
			return true;
		}

		return false;
	}

}
