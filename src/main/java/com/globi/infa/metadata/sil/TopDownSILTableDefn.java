package com.globi.infa.metadata.sil;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class TopDownSILTableDefn {
	
	@NotBlank
	@NotNull
	private String tableName;
	

}
