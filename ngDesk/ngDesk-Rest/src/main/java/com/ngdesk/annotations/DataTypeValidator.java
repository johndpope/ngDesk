package com.ngdesk.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.modules.fields.DataType;

public class DataTypeValidator implements ConstraintValidator<ValidDataType, DataType> {

	@Override
	public boolean isValid(DataType type, ConstraintValidatorContext context) {

		if (type.getDisplay().equals("Email") || type.getDisplay().equals("Text")
				|| type.getDisplay().equals("Text Area") || type.getDisplay().equals("Text Area Long")
				|| type.getDisplay().equals("Text Area Rich") || type.getDisplay().equals("Text Encrypted")
				|| type.getDisplay().equals("URL") || type.getDisplay().equals("Encrypted Text")
				|| type.getDisplay().equals("Street 1") || type.getDisplay().equals("Street 2")
				|| type.getDisplay().equals("City") || type.getDisplay().equals("State")
				|| type.getDisplay().equals("Zipcode") || type.getDisplay().equals("Country")) {
			if (!type.getBackend().equals("String")) {
				return false;
			}
		} else if (type.getDisplay().equals("Formula")) {
			if (!type.getBackend().equals("Formula")) {
				return false;
			}
		} else if (type.getDisplay().equals("Picklist")) {
			if (!type.getBackend().equals("String")) {
				return false;
			}
		} else if (type.getDisplay().equals("Picklist (Multi-Select)")) {
			if (!type.getBackend().equals("Array")) {
				return false;
			}
		} else if (type.getDisplay().equals("List Text")) {
			if (!type.getBackend().equals("Array")) {
				return false;
			}
		} else if (type.getDisplay().equals("Auto Number") || type.getDisplay().equals("Number")) {
			if (!type.getBackend().equals("Integer")) {
				return false;
			}
		} else if (type.getDisplay().equals("Currency")) {
			if (!type.getBackend().equals("Float")) {
				return false;
			}
		} else if (type.getDisplay().equals("Percent")) {
			if (!type.getBackend().equals("Double")) {
				return false;
			}
		} else if (type.getDisplay().equals("File Upload")) {
			if (!type.getBackend().equals("BLOB")) {
				return false;
			}
		} else if (type.getDisplay().equals("Geolocation")) {
			if (!type.getBackend().equals("json")) {
				return false;
			}
		} else if (type.getDisplay().equals("Checkbox")) {
			if (!type.getBackend().equals("Boolean")) {
				return false;
			}
		} else if (type.getDisplay().equals("Date") || type.getDisplay().equals("Date/Time")
				|| type.getDisplay().equals("Time")) {
			if (!type.getBackend().equals("Timestamp")) {
				return false;
			}
		}
		return true;
	}
}
