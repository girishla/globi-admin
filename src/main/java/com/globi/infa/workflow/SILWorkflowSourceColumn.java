package com.globi.infa.workflow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;

import com.globi.infa.AbstractEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@AllArgsConstructor
@Builder
@Table(name = "M_INFA_SIL_WF_COLS")
public class SILWorkflowSourceColumn extends AbstractEntity {

	@NonNull
	@NotBlank(message = "SIL column name cannot be empty!")
	@Column(name="COL_NAME")
	private String columnName;

	@NonNull
	@NotBlank(message = "SIL column type cannot be empty!")
	@Column(name="COL_TYPE")
	private String columnType;

	@Column(name="COL_D_TABLE")
	private String columnDimTable;

	
	@Column(name="COL_ORDER")
	private int columnOrder;

	
	@Builder.Default
	@Column(name="COL_STAGING_INDICATOR")
	private boolean stageTableColumn = false;
	
	@Builder.Default
	@Column(name="COL_TGT_INDICATOR")
	private boolean targetColumn = false;
	
	@Builder.Default
	@Column(name="COL_LEGCY_INDICATOR")
	private boolean legacyColumn = false;
	
	@Builder.Default
	@Column(name="COL_MINI_INDICATOR")
	private boolean miniDimColumn = false;
	
	@Builder.Default
	@Column(name="COL_DOM_LKP_INDICATOR")
	private boolean domainLookupColumn = false;
	
	@Column(name="COL_AUTO_INDICATOR")
	@Builder.Default
	private boolean autoColumn = false;

}
