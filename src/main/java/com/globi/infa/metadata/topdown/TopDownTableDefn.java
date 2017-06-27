package com.globi.infa.metadata.topdown;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class TopDownTableDefn {
	
	@Size(min = 3, max = 3)
	@NotNull
	@NotBlank
	private String source;
	
	@NotBlank
	@NotNull
	private String tableName;
	

}
