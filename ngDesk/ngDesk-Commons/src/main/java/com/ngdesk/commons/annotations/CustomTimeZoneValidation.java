package com.ngdesk.commons.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = {CustomTimeZoneValidationImpl.class})
@Retention(RUNTIME)
@Target({ FIELD, METHOD, CONSTRUCTOR, PARAMETER })
public @interface CustomTimeZoneValidation {

	String message() default "";

	String[] values() default {};

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
	
}
