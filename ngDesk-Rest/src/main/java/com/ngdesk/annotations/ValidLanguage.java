package com.ngdesk.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LanguageValidator.class)
public @interface ValidLanguage {
	String message() default "INVALID_LANGUAGES";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}