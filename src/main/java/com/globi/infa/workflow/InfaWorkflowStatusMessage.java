package com.globi.infa.workflow;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "M_INFA_PTP_WF_MSG")
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InfaWorkflowStatusMessage extends AbstractInfaWorkflowEntity{
		
	
	@Column(name="message",length=4000)
	private String statusMessage;
	
	
	public void setStatusMessage(String msg) {

		msg = msg.substring(0, msg.length() > 3999 ? 3999 : msg.length());
		
		if (this.statusMessage != null) {
			this.setStatusMessage(this.getStatusMessage() + "\n" + msg);
		} else {
			this.setStatusMessage(msg);
		}

		

	}
	
	
//    @OneToOne(mappedBy = "messageObject")
//    private PTPWorkflow workflow;
//	

}


