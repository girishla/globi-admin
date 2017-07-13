package com.globi.infa.metadata.src;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class InfaSourceDefinitionPullDTO {

	
	@NotEmpty
	@NonNull
	@Size(min=3,max=3)
	String sourceName;
	
	@NotEmpty
	@NonNull
	String tableName;
	
	
}
