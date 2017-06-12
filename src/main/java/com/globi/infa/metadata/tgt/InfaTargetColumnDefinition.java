package com.globi.infa.metadata.tgt;

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
@RequiredArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Table(name = "M_INFA_TGT_DEFN_COLS")
@AllArgsConstructor
@Builder
public class InfaTargetColumnDefinition extends AbstractEntity {

	@NonNull
	private String columnName;
	@NonNull
	private String columnDataType;

	private int columnNumber;

	private String nullable;
	

	private int precision;
	
	private int scale;
	
	
}
