package com.globi.infa.workflow;

import javax.persistence.Entity;
import javax.persistence.Table;

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
//@RequiredArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "COLS")
public class PTPWorkflowSourceColumn extends AbstractEntity{
		
	@NonNull
	@NotBlank(message = "PTP Table source column name cannot be empty!")
	private String sourceColumnName;
	@Builder.Default
	private boolean integrationIdColumn=false;
	@Builder.Default
	private boolean changeCaptureColumn=false;
	
	
}
