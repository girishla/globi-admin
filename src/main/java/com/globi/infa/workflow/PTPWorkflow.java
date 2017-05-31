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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.WhereJoinTable;
import org.hibernate.validator.constraints.NotBlank;

import com.globi.infa.AbstractEntity;

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
//@RequiredArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Table(name = "M_INFA_PTP_WF",uniqueConstraints={@UniqueConstraint(columnNames = {"src_name" , "src_table_name"})})
@AllArgsConstructor
@Builder
public class PTPWorkflow extends AbstractEntity implements GeneratedWorkflow{
	
	@NonNull
	@NotBlank(message = "PTP Workflow source name cannot be empty!")
	@Column(name="src_name")
	private String sourceName;
	
	@NonNull
	@NotBlank(message = "PTP Workflow source table name cannot be empty!")
	@Column(name="src_table_name")
	private String sourceTableName;
	
	@OrderColumn //
	@Column(unique = true) //
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true) //
	@JoinColumn(name="workflow_id",referencedColumnName="id")
	@Singular
	private List<PTPWorkflowSourceColumn> columns = new ArrayList<>();
	
	@OneToOne(fetch = FetchType.EAGER,cascade = {CascadeType.ALL})
	@MapsId 
    @JoinColumn(name = "ID")
    @WhereJoinTable(clause = "TYPE = 'PTP'")
	private InfaWorkflow workflow;
 
	

	
	
}
