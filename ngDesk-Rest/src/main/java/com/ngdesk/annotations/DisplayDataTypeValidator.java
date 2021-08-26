package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.Global;

public class DisplayDataTypeValidator implements ConstraintValidator<ValidDisplayDataType, String> {

	@Override
	public boolean isValid(String display, ConstraintValidatorContext arg1) {
		if (Global.displayDataTypes.contains(display)) {
			return true;
		}
		return false;
	}
}
