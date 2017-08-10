package com.globi.infa.workflow.ptp;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.globi.infa.workflow.InfaWorkflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.Singular;
import lombok.ToString;

@Entity
@ToString(callSuper = true)
@NoArgsConstructor
@Getter
@Setter
@Table(name = "M_INFA_PTP_WF", uniqueConstraints = {
@UniqueConstraint(columnNames = { "src_name", "src_table_name" }) })
@AllArgsConstructor
@DiscriminatorValue("PTP")
//
public class PTPWorkflow extends InfaWorkflow {

	@NonNull
	@NotBlank(message = "PTP Workflow source name cannot be empty!")
	@Column(name = "src_name")
	private String sourceName;

	@NonNull
	@NotBlank(message = "PTP Workflow source table name cannot be empty!")
	@Column(name = "src_table_name")
	private String sourceTableName;

	@Column(name = "src_filter")
	private String sourceFilter = "";

	@Column(name = "tgt_table")
	@NonNull
	@NotBlank(message = "Target table name cannot be empty!")
	@Size(min=0,max = 24)
	private String targetTableName;

	
	@OrderColumn //
	@Column(unique = true) //
	@OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL, orphanRemoval = true) //
	@JoinColumn(name = "workflow_id", referencedColumnName = "id")
	@Singular
	@Valid
	private List<PTPWorkflowSourceColumn> columns = new ArrayList<>();


	@Builder
	public PTPWorkflow(String workflowName, String workflowUri, String workflowStatus, String sourceName,
			String sourceTableName, String sourceFilter, List<PTPWorkflowSourceColumn> columns,String targetTableName) {

		super(workflowName, workflowUri, workflowStatus);
		this.sourceFilter = sourceFilter;
		this.sourceTableName = sourceTableName;
		this.sourceName = sourceName;
		this.columns = columns;
		this.targetTableName=targetTableName;

	}



}
