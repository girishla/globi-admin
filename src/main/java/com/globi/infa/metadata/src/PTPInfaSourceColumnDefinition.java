package com.globi.infa.metadata.src;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

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
@Table(name = "M_INFA_SRC_PTP_COLS")

public class PTPInfaSourceColumnDefinition extends InfaSourceColumnDefinition {

	
	@Builder.Default
	private Boolean integrationIdFlag=false;
	@Builder.Default
	private Boolean pguidFlag=false;
	@Builder.Default
	private Boolean buidFlag=false;
	@Builder.Default
	private Boolean ccFlag=false;

	@Builder
	public PTPInfaSourceColumnDefinition(Boolean integrationIdFlag,
			Boolean pguidFlag,Boolean buidFlag,Boolean ccFlag){
		
		
	}
	
	
}
