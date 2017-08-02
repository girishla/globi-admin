package com.globi.infa.workflow;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.Table;

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

	@Lob
	@Column(name = "message")
	private String message;

	@Override
	@JsonIgnore
	public InfaWorkflow getWorkflow() {
		// TODO Auto-generated method stub
		return this;
	}

	public InfaWorkflow(String workflowName, String workflowUri, String workflowStatus) {
		this.workflowName = workflowName;
		this.workflowStatus = workflowStatus;
		this.workflowUri = workflowUri;
	}

	public void setStatusMessage(String msg) {

		String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH.mm.ss").format(new Date());

		if (this.message != null) {
			this.message += "\n" + timeStamp + "  " + msg;
		} else {
			this.message = timeStamp + "  " + msg;
		}



	}

}
