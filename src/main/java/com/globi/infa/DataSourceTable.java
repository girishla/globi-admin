package com.globi.infa;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

import org.springframework.data.domain.Persistable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("serial")
@NoArgsConstructor
@AllArgsConstructor
@Data
@RequiredArgsConstructor
public class DataSourceTable implements Persistable<Integer>  {

	@Id
	@JsonIgnore
	private Integer  id;
	
	@NonNull
	private String tableOwner;
	
	@NonNull
	private String tableName;
	
	@Override
	public Integer getId() {
		// TODO Auto-generated method stub
		return id;
	}
	@Override
	@JsonIgnore
	public boolean isNew() {
		// TODO Auto-generated method stub
		return id == null;
	}
	
	
}
