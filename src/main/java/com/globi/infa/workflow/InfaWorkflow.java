package com.globi.infa.workflow;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;

import com.globi.infa.AbstractEntity;
import com.globi.infa.AbstractInfaWorkflowEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;


@Entity
@ToString(callSuper = true)
//@RequiredArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Table(name = "M_INFA_WF")
@AllArgsConstructor
@Builder
public class InfaWorkflow extends AbstractInfaWorkflowEntity{
		
	@NonNull
	@NotBlank(message = "Workflow name cannot be empty!")
	@Column(name="wf_name",unique=true)
	private String workflowName;
	
	@Column(name="wf_uri")
	private String workflowUri;
	
	@NonNull
	@NotBlank(message = "Workflow type cannot be empty!")
	@Column(name="TYPE")
	private String workflowType;
	
	
	private String workflowStatus;
	
}
