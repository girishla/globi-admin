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
@Table(name = "M_INFA_PTP_WF_COLS")
public class PTPWorkflowSourceColumn extends AbstractEntity {

	@NonNull
	@NotBlank(message = "PTP Table source column name cannot be empty!")
	@Column(name="COL_NAME")
	private String sourceColumnName;
	
	@Builder.Default
	@Column(name="INT_KEY_INDICATOR")
	private boolean integrationIdColumn = false;
	@Builder.Default
	@Column(name="CC_INDICATOR")
	private boolean changeCaptureColumn = false;
	@Builder.Default
	@Column(name="PGUID_INDICATOR")
	private boolean pguidColumn = false;
	@Builder.Default
	@Column(name="BUID_INDICATOR")
	private boolean buidColumn = false;
	
	@Column(name="COLUMNSEQUENCE")
	@Builder.Default
	private int columnSequence=0;

}
