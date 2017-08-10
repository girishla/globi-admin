package com.globi.infa.workflow.sil;

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
@Table(name = "M_INFA_SIL_WF", uniqueConstraints = {
@UniqueConstraint(columnNames = {"TBL_BASE_NAME", "STG_NAME","LOAD_TYPE" }) })
@AllArgsConstructor
@DiscriminatorValue("SIL")

public class SILWorkflow extends InfaWorkflow {
	
	@NonNull
	@NotBlank(message = "table name cannot be empty!")
	@Column(name = "TBL_BASE_NAME")
	private String tableBaseName;

	
	@NonNull
	@NotBlank(message = "load Type cannot be empty!")
	@Column(name = "LOAD_TYPE")
	private String loadType;
	
	@NonNull
	@NotBlank(message = "Staging table name cannot be empty!")
	@Column(name = "STG_NAME")
	private String stageName;
	
	@OrderColumn //
	@Column(unique = true) //
	@OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL, orphanRemoval = true) //
	@JoinColumn(name = "workflow_id", referencedColumnName = "id")
	@Singular
	@Valid
	private List<SILWorkflowSourceColumn> columns = new ArrayList<>();

	
	
	@Builder
	public SILWorkflow(String workflowName, String workflowUri, String workflowStatus,List<SILWorkflowSourceColumn> columns, String stageName,
			String tableName,String loadType) {

		super(workflowName, workflowUri, workflowStatus);
		this.tableBaseName = tableName;
		this.stageName = stageName;
		this.loadType=loadType;
		this.columns=columns;

	}



}
