package com.globi.rest.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice(annotations=RestController.class)
public class RestControllerAdvice
{
	private static Logger logger = LoggerFactory.getLogger(RestControllerAdvice.class);
	protected final static String JSON = "application/json";

	protected ResponseEntity<RestApiError> createResponseEntity(RestApiError restApiError)
	{
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpStatus responseStatus = HttpStatus.valueOf(restApiError.getHttpStatus());
		ResponseEntity<RestApiError> result = new ResponseEntity<>(restApiError, headers, responseStatus);
		return result;
	}

	@ExceptionHandler(RestApiException.class)
	public ResponseEntity<RestApiError> handleRestApiException(RestApiException e)
	{
		logger.error("Api Error caused by exception", e);
		return this.createResponseEntity(e.getRestApiError());
	}


	@ExceptionHandler(Exception.class)
	public ResponseEntity<RestApiError> handleException(Exception e)
	{
		logger.error("Api Error caused by exception", e);
		RestApiError restApiError = new RestApiError(RestApiHttpStatus.INTERNAL_SERVER_ERROR);
		restApiError.setApiCode(ApiErrorCodes.UNHANDLED_SERVER_EXCEPTION);
		restApiError.setUserMessage("The server encountered an error and could not complete the request");
		restApiError.setDeveloperMessage("The server encountred an unhandled exception and could not complete the request");

		return createResponseEntity(restApiError);
	}

	/**
	 * According to the SpringMVC documentation. A @PathVariable argument can be of any simple type such as int, long, Date, etc. Spring
	 * automatically converts to the appropriate type or throws a TypeMismatchException if it fails to do so
	 * 
	 * @param e
	 * @return
	 */
	@ExceptionHandler(TypeMismatchException.class)
	public ResponseEntity<RestApiError> handleTypeMismatchException(TypeMismatchException e)
	{
		logger.error("Api Error caused by exception", e);
		RestApiError restApiError = new RestApiError(RestApiHttpStatus.BAD_REQUEST);
		restApiError.setApiCode(ApiErrorCodes.TYPE_MISMATCH_EXCEPTION);
		restApiError.setUserMessage("An Error has occured and the request could not be completed");
		restApiError.setDeveloperMessage(e.getMessage());

		return createResponseEntity(restApiError);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<RestApiError> handleHttpMessageNotReadableException(HttpMessageNotReadableException e)
	{
		logger.error("Api Error caused by exception", e);
		RestApiError restApiError = new RestApiError(RestApiHttpStatus.BAD_REQUEST);
		restApiError.setApiCode(ApiErrorCodes.UANBLE_TO_PARSE_REQUEST);
		restApiError.setUserMessage("An Error has occured and the request could not be completed");
		restApiError.setDeveloperMessage(e.getMessage());

		return createResponseEntity(restApiError);
	}
	
	/**
	 * According to the SpringMVC documentation @RequestBody validation errors always result in a MethodArgumentNotValidException being
	 * raised. The exception is handled in the DefaultHandlerExceptionResolver, which sends a 400 error back to the client. We are defining
	 * a handler here to send back a RestAPiError.
	 * 
	 * @param e
	 * @return
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<RestApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException e)
	{
		logger.error("Api Error caused by exception", e);
		BindingResult bindingResult = e.getBindingResult();
		return restApiErrorFromBindingResult(bindingResult, ApiErrorCodes.INVALID_REQUEST_BODY);
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<RestApiError> handleBindException(BindException e)
	{
		logger.error("Api Error caused by exception", e);
		return restApiErrorFromBindingResult(e, ApiErrorCodes.BIND_EXCEPTION);
	}
	


	private ResponseEntity<RestApiError> restApiErrorFromBindingResult(BindingResult bindingResult, UUID apiErrorCode)
	{
		// Create an Api Error and return it
		RestApiError restApiError = new RestApiError(RestApiHttpStatus.BAD_REQUEST);
		restApiError.setApiCode(apiErrorCode);
		restApiError.setUserMessage("An Error has occured and the request could not be completed");
		restApiError.setDeveloperMessage("Invalid request body see validation errors");

		// Extract field errors
		List<FieldError> fieldErrors = bindingResult.getFieldErrors();
		List<RestApiValidationError> restApiFieldErrors = new ArrayList<>();
		for (FieldError fieldError : fieldErrors)
		{
			RestApiValidationError restApiFieldError = new RestApiValidationError();
			restApiFieldError.setFieldName(fieldError.getField());
			restApiFieldError.setMessage(fieldError.getDefaultMessage());
			restApiFieldErrors.add(restApiFieldError);
		}
		if (restApiFieldErrors.isEmpty() == false)
		{
			restApiError.setValidationErros(restApiFieldErrors);
		}

		return createResponseEntity(restApiError);
	}

}
