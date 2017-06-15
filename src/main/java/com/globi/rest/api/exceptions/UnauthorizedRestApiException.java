package com.globi.rest.api.exceptions;

import com.globi.rest.api.RestApiException;
import com.globi.rest.api.RestApiHttpStatus;

public class UnauthorizedRestApiException extends RestApiException
{
	private static final long serialVersionUID = 1L;

	public UnauthorizedRestApiException()
	{
		super(RestApiHttpStatus.UNAUTHORIZED);
	}
}
