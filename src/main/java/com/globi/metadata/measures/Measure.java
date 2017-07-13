package com.globi.metadata.measures;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
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
@Builder
@AllArgsConstructor
@Subselect("SELECT ROWNUM ID, DYN_DIM_MEASURES.* FROM DYN_DIM_MEASURES")
public class Measure  {

	@Column(name="ID")
	private @Id long id;
	
	@Column(name="DIMENSION")
	private String dimension;
	
	@Column(name="MEASURETYPE")
	private String type;
	
	@Column(name="MEASURE")
	private double  measure;
	
	
}
