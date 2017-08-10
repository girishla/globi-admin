package com.globi.infa.metadata.sil;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopDownSILTableDefn {
	
	@NotBlank
	@NotNull
	private String loadType;
	
	@NotBlank
	@NotNull
	private String tableName;
	

}
