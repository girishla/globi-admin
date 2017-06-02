package com.globi.infa.datasource.core;

import javax.persistence.Entity;
import javax.persistence.Table;
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

//@Entity
@ToString(callSuper = true)
@RequiredArgsConstructor
@Getter
@Setter
@NoArgsConstructor
//@Table(name = "COLS")
@AllArgsConstructor
@Builder
public class InfaSourceColumnDefinition extends AbstractEntity {

	@NonNull
	private String columnName;
	@NonNull
	private String columnDataType;

	private int columnNumber;

	private String nullable;

	private int columnLength;
	private int offset;
	private int physicalLength;
	private int physicalOffset;
	private int precision;
	private int scale;
	@Size(min = 1, max = 1)
	private Boolean integrationIdFlag;
	
	private Boolean pguidFlag;

	public String getColumnName() {

		return columnName.equals("INTEGRATION_ID") ? "SRC_INTEGRATION_ID" : columnName;

	}

}
