package com.globi.rest.api;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * An exception class that contains a rest api error which will be turned into a
 * json object and returned to the client. There are several sub classes of this
 * class that make the error type specific to one of the legal api response
 * codes.
 * 
 * @author Girish lakshmanan
 * 
 */
public class RestApiException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final RestApiError  restApiError;
	
	public RestApiException(RestApiError restApiError) {
		this.restApiError = restApiError;
	}
	
	public RestApiException(RestApiHttpStatus httpStatus) 
	{
		this.restApiError = new RestApiError(httpStatus);
	}
	
	public RestApiException apiCode(UUID apiCode) {
		this.restApiError.setApiCode(apiCode); 
		return this; 
	}
	
	public RestApiException userMessage(String userMessage)
	{
		this.restApiError.setUserMessage(userMessage);
		return this; 
	}
	
	public RestApiException userMessage(String userMessage, Object... userMessageArgs)
	{
		this.userMessage(String.format(userMessage, userMessageArgs));
		return this; 
	}
	
	public RestApiException developerMessage(String developerMessage)
	{
		this.restApiError.setDeveloperMessage(developerMessage);
		return this; 
	}
	
	public RestApiException developerMessage(String developerMessage, Object... developerMessageArgs)
	{
		this.developerMessage(String.format(developerMessage, developerMessageArgs));
		return this; 
	}
	
	public RestApiException validationErrors(List<RestApiValidationError> validationErrors) 
	{
		this.restApiError.setValidationErros(validationErrors);
		return this;
	}
	
	public RestApiException validationErrors(RestApiValidationError... validationErrors) 
	{
		this.restApiError.setValidationErros(Arrays.asList(validationErrors));
		return this;
	}
	

	public RestApiError getRestApiError() {		
		return restApiError ;
	}
}
