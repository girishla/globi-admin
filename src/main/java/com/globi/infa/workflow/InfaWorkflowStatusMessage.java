package com.globi.infa.workflow;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.WhereJoinTable;

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
@Table(name = "M_INFA_WF_MSG")
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InfaWorkflowStatusMessage extends AbstractInfaWorkflowEntity{
		
	
	@Column(name="message",length=4000)
	private String statusMessage;
	

}


