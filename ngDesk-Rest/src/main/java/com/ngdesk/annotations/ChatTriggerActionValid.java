package com.ngdesk.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = ChatTriggerActionValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ChatTriggerActionValid {
	String message() default "CHAT_TRIGGER_ACTION_INVALID";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}