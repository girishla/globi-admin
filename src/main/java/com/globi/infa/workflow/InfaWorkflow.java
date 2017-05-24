package com.globi.infa.workflow;


import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;

import com.globi.infa.datasource.core.AbstractEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity
@ToString(callSuper = true)
@RequiredArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Table(name = "M_INFA_WORKFLOW")
@AllArgsConstructor
@Builder
public class InfaWorkflow extends AbstractEntity{
		
	@NonNull
	@NotBlank(message = "Workflow name cannot be empty!")
	private String workflowName;
	
	private String workflowScmUri;
	
	
}
