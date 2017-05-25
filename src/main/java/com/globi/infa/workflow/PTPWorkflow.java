package com.globi.infa.workflow;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.WhereJoinTable;
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
//@RequiredArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Table(name = "M_INFA_PTP_WF")
@AllArgsConstructor
@Builder
public class PTPWorkflow extends AbstractEntity implements Workflow{
	
	@NonNull
	@NotBlank(message = "PTP Workflow source name cannot be empty!")
	private String sourceName;
	
	@NonNull
	@NotBlank(message = "PTP Workflow source table name cannot be empty!")
	private String sourceTableName;
	
	@OrderColumn //
	@Column(unique = true) //
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true) //
	private final List<PTPWorkflowSourceColumn> columns = new ArrayList<>();
	
	@OneToOne(fetch = FetchType.EAGER,cascade = {CascadeType.ALL})
	@MapsId 
    @JoinColumn(name = "ID")
    @WhereJoinTable(clause = "TYPE = 'PTP'")
	private InfaWorkflow workflow;
 
	
	
	
}
