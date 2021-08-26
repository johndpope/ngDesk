package com.ngdesk.commons.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.ngdesk.commons.Global;

public class CustomTimeZoneValidationImpl  implements ConstraintValidator<CustomTimeZoneValidation,Object>{

	@Autowired 
	Global global;
	
	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		try{
			//validate if not null and not empty to avoid problem
			if(value != null && value.toString().trim().length() > 0) {
				//validate if present in the document
				if(global.timezones.contains(value)) {
					return true;	
				} 
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
