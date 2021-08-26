package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.Global;

public class ChatTriggerConditionValidator implements ConstraintValidator<ChatTriggerConditionValid, String> {

	@Override
	public boolean isValid(String condition, ConstraintValidatorContext arg1) {

		if (Global.chatTriggerConditions.contains(condition)) {
			return true;
		}

		return false;
	}

}
