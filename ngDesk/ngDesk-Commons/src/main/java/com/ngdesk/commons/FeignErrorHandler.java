package com.ngdesk.commons;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.commons.exceptions.UnauthorizedException;

import feign.Response;
import feign.codec.ErrorDecoder;

public class FeignErrorHandler implements ErrorDecoder {
	private final ErrorDecoder defaultErrorDecoder = new Default();

	@Override
	public Exception decode(String methodKey, Response response) {

		if (response.status() == 400) {
			throw new BadRequestException(response.body().toString(), null);
		} else if (response.status() == 401) {
			throw new UnauthorizedException(response.body().toString());
		} else if (response.status() >= 500 && response.status() <= 599) {
			throw new InternalErrorException("INTERNAL_ERROR");
		}

		return defaultErrorDecoder.decode(methodKey, response);
	}
}
