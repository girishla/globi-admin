package com.globi.infa.workflow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.validator.constraints.NotBlank;

import com.globi.infa.AbstractEntity;

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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "M_INFA_PTP_WF_COLS")
public class PTPWorkflowSourceColumn extends AbstractEntity {

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


}
