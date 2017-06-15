package com.globi.rest.api.exceptions;

import com.globi.rest.api.RestApiException;
import com.globi.rest.api.RestApiHttpStatus;

public class ForbiddenRestApiException extends RestApiException
{
	private static final long serialVersionUID = 1L;

	public ForbiddenRestApiException()
	{
		super(RestApiHttpStatus.FORBIDDEN);
	}
}
