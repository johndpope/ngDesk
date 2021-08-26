package com.ngdesk.annotations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.Global;
import com.ngdesk.modules.monitors.MonitorAction;
import com.ngdesk.modules.monitors.Value;

public class ValueValidator implements ConstraintValidator<ValidValue, MonitorAction> {

	@Override
	public void initialize(ValidValue constraintAnnotation) {
	}

	@Override
	public boolean isValid(MonitorAction action, ConstraintValidatorContext context) {

		String regex = "\\{\\{[a-zA-z0-9\\s]+\\}\\}";
		Pattern pattern = Pattern.compile(regex);

		if (action.getAction().equalsIgnoreCase("send email")) {
			if (action.getValues().size() != 4) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("INVALID_VALUES").addConstraintViolation();
				return false;
			} else {

				if (action.getValues().get(0).getOrder() == 1) {

					String value = action.getValues().get(0).getValue();
					Matcher matcher = pattern.matcher(value);
					if (matcher.matches()) {
						// DO NOTHING
					} else {
						if (!value.matches(Global.emailAdressregex)) {
							context.disableDefaultConstraintViolation();
							context.buildConstraintViolationWithTemplate("INVALID_FROM_EMAIL_ADDRESS")
									.addConstraintViolation();
							return false;
						}
					}

				} else {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate("INVALID_ORDER_ACTIONS").addConstraintViolation();
					return false;
				}

				if (action.getValues().get(1).getOrder() == 2) {

					String value = action.getValues().get(0).getValue();
					Matcher matcher = pattern.matcher(value);

					if (matcher.matches()) {
						// DO NOTHING
					} else {
						if (!value.matches(Global.emailAdressregex)) {
							context.disableDefaultConstraintViolation();
							context.buildConstraintViolationWithTemplate("INVALID_TO_EMAIL_ADDRESS")
									.addConstraintViolation();
							return false;
						}
					}
				} else {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate("INVALID_ORDER_ACTIONS").addConstraintViolation();
					return false;
				}
				if (action.getValues().get(2).getOrder() == 3) {
					if (action.getValues().get(2).getValue().length() <= 0
							|| action.getValues().get(2).getValue().length() > 255) {
						context.disableDefaultConstraintViolation();
						context.buildConstraintViolationWithTemplate("INVALID_SUBJECT").addConstraintViolation();
						return false;
					}
				} else {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate("INVALID_ORDER_ACTIONS").addConstraintViolation();
					return false;
				}
				if (action.getValues().get(3).getOrder() == 4) {
					if (action.getValues().get(3).getValue().length() <= 0
							|| action.getValues().get(3).getValue().length() > 2621400) {
						context.disableDefaultConstraintViolation();
						context.buildConstraintViolationWithTemplate("INVALID_BODY").addConstraintViolation();
						return false;
					}
				} else {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate("INVALID_ORDER_ACTIONS").addConstraintViolation();
					return false;
				}
			}
		}

		if (action.getAction().equalsIgnoreCase("start escalation")) {
			if (action.getValues().size() != 3) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("INVALID_VALUES").addConstraintViolation();
				return false;
			}

			for (int i = 0; i < action.getValues().size(); i++) {
				Value value = action.getValues().get(i);
				if (value.getOrder() != i + 1) {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate("INVALID_ORDER_ACTIONS").addConstraintViolation();
					return false;
				}

			}

		}

		return true;
	}

}
