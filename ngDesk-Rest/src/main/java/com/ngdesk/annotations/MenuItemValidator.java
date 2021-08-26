package com.ngdesk.annotations;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.companies.MenuItem;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.schedules.Layer;
import com.ngdesk.schedules.Restriction;

public class MenuItemValidator implements ConstraintValidator<ValidMenuItem, MenuItem> {

	@Override
	public boolean isValid(MenuItem menuItem, ConstraintValidatorContext context) {
		if (menuItem.isIsmodule()) {
			return !menuItem.getModule().isEmpty();
		} else if (!menuItem.isIsmodule() && (menuItem.getModule().equals("guide")
				|| menuItem.getModule().equals("escalations") || menuItem.getModule().equals("schedules"))) {
			return !menuItem.getModule().isEmpty();
		} else {
			return menuItem.getModule().isEmpty();
		}
	}

}
