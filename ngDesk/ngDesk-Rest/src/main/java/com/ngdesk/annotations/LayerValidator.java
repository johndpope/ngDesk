package com.ngdesk.annotations;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.schedules.Layer;
import com.ngdesk.schedules.Restriction;

public class LayerValidator implements ConstraintValidator<ValidLayer, Layer> {

	@Override
	public boolean isValid(Layer layer, ConstraintValidatorContext context) {

		List<Restriction> restrictions = layer.getRestrictions();

		if (layer.isHasRestrictions()) {
			if (layer.getRestrictions().size() < 1) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("LAYER_SHOULD_HAVE_ATLEAST_ONE_RESTRICTION")
						.addConstraintViolation();
				return false;

			} else if (layer.getRestrictionType() == null) {

				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("RESTRICTION_TYPE_NOT_NULL").addConstraintViolation();
				return false;
			}
		} else {
			if (layer.getRestrictions().size() > 0) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("RESTRICTION_TYPE_NOT_NULL").addConstraintViolation();
				return false;
			}
		}
		return true;
	}

}
