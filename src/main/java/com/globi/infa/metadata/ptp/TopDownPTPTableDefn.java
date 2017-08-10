package com.globi.infa.metadata.ptp;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class TopDownPTPTableDefn {
	
	@Size(min = 3, max = 3)
	@NotNull
	@NotBlank
	private String source;
	
	@NotBlank
	@NotNull
	private String tableName;
	

}
