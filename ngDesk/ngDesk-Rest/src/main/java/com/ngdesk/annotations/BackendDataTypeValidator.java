package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.Global;

public class BackendDataTypeValidator implements ConstraintValidator<ValidBackEndType, String> {

	@Override
	public boolean isValid(String backend, ConstraintValidatorContext arg1) {

		if (Global.backendDataTypes.contains(backend)) {
			return true;
		}
		return false;
	}
}
