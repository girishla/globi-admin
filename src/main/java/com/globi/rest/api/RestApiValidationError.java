package com.globi.rest.api;

/**
 * class to store field validation errors which are attached to a RestApiError
 * 
 * @author Girish lakshmanan
 * 
 */
public class RestApiValidationError
{
	private String fieldName;
	private String message;

	public String getFieldName()
	{
		return fieldName;
	}

	public void setFieldName(String fieldName)
	{
		this.fieldName = fieldName;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
}
