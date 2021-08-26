package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.Global;
import com.ngdesk.channels.chat.triggers.Condition;
import com.ngdesk.schedules.Restriction;

public class TriggerConditionValidator implements ConstraintValidator<TriggerCondition, Condition> {

	@Override
	public boolean isValid(Condition condition, ConstraintValidatorContext arg1) {

		String dataType = condition.getDataType();

		if (dataType.equals("String")) {
			if (Global.chatTriggerStringOperators.contains(condition.getOperator())) {
				return true;
			}
		} else if (dataType.equals("Integer")) {
			if (Global.chatTriggerIntegerOperators.contains(condition.getOperator())) {
				return true;
			}
		}
		return false;
	}

}
