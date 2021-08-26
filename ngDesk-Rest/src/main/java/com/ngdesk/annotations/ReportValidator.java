package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.reporting.Field;
import com.ngdesk.reporting.Report;

public class ReportValidator implements ConstraintValidator<ValidReport, Report> {

	@Override
	public boolean isValid(Report report, ConstraintValidatorContext context) {

		String sortById = report.getSortBy().getId();

		for (Field field : report.getFields()) {

			if (field.getId().equals(sortById)) {
				return true;
			}
		}
		return false;
	}

}
