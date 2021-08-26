package com.ngdesk.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = ChatTriggerConditionValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ChatTriggerConditionValid {
	String message() default "CHAT_TRIGGER_CONDITION_INVALID";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}