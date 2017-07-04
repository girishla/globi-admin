package com.globi.infa.workflow;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.WhereJoinTable;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.globi.infa.AbstractInfaWorkflowEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Entity
@ToString(callSuper = true)
// @RequiredArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Table(name = "M_INFA_WF")
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "TYPE")
public class InfaWorkflow extends AbstractInfaWorkflowEntity implements GeneratedWorkflow {

	@NonNull
	@NotBlank(message = "Workflow name cannot be empty!")
	@Column(name = "wf_name", unique = true)
	private String workflowName;

	@Column(name = "wf_uri")
	private String workflowUri;

	@NonNull
	@Column(name = "TYPE", insertable = false, updatable = false)
	private String workflowType = "";

	private String workflowStatus;

	private String workflowRunStatus;

	@OneToOne(targetEntity = InfaWorkflowStatusMessage.class, fetch = FetchType.LAZY, optional = true, cascade = {
			CascadeType.ALL })
	@JoinColumn(name = "MESSAGE_ID", referencedColumnName = "ID")
	private InfaWorkflowStatusMessage messageObject;

	@Override
	@JsonIgnore
	public InfaWorkflow getWorkflow() {
		// TODO Auto-generated method stub
		return this;
	}

	public void setStatusMessage(String msg) {

		this.setMessageObject(InfaWorkflowStatusMessage.builder().statusMessage(msg).build());

	}

	public InfaWorkflow(String workflowName, String workflowUri, String workflowStatus) {
		this.workflowName = workflowName;
		this.workflowStatus = workflowStatus;
		this.workflowUri = workflowUri;

	}

}
