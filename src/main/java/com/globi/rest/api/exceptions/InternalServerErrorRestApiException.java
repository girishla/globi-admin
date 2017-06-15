package com.globi.rest.api.exceptions;

import com.globi.rest.api.RestApiException;
import com.globi.rest.api.RestApiHttpStatus;

public class InternalServerErrorRestApiException extends RestApiException
{
	private static final long serialVersionUID = 1L;

	public InternalServerErrorRestApiException()
	{
		super(RestApiHttpStatus.INTERNAL_SERVER_ERROR);
	}
}
