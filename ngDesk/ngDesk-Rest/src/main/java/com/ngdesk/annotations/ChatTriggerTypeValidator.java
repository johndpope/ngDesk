package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.Global;

public class ChatTriggerTypeValidator implements ConstraintValidator<ChatTriggerTypeValid, String> {

	@Override
	public boolean isValid(String type, ConstraintValidatorContext arg1) {

		if (Global.chatTriggerTypes.contains(type)) {
			return true;
		}

		return false;
	}

}
