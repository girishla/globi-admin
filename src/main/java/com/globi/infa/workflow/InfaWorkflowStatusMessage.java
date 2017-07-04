package com.globi.infa.workflow;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.globi.infa.AbstractInfaWorkflowEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@Table(name = "M_INFA_WF_MSG")
@AllArgsConstructor
@Builder
public class InfaWorkflowStatusMessage extends AbstractInfaWorkflowEntity{
		
	
	@Column(name="message",length=4000)
	private String statusMessage;
	

	
}


