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
@Table(name = "M_INFA_PTPP_WF_COLS")
public class PTPPrimaryWorkflowSourceColumn extends AbstractEntity {

	@NonNull
	@NotBlank(message = "PTP Table source column name cannot be empty!")
	@Column(name="col_name")
	private String sourceColumnName;
	
	@Builder.Default
	@Column(name="int_key_indicator")
	private boolean integrationIdColumn = false;
	@Builder.Default
	@Column(name="cc_indicator")
	private boolean changeCaptureColumn = false;
	@Builder.Default
	@Column(name="pguid_indicator")
	private boolean pguidColumn = false;
	@Builder.Default
	@Column(name="buid_indicator")
	private boolean buidColumn = false;

}