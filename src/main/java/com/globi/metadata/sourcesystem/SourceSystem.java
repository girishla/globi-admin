package com.globi.metadata.sourcesystem;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.validator.constraints.NotBlank;

import com.globi.infa.datasource.core.AbstractEntity;

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
//@Table(catalog="LAW",name = "S_LAW_SRC")
@Builder
@AllArgsConstructor
@Subselect("SELECT * FROM LAW.S_LAW_SRC")
public class SourceSystem  {

	@Column(name="SRC_ID")
	private @Id long id;
	
	@NonNull
	@NotBlank(message = "Name cannot be empty!")
	@Column(name="SRC_NAME",unique = true)
	@Size(max = 50)
	private String name;
	
	@Size(max = 255)
	@Column(name="SRC_DESC")
	private String description;
	
	@Size(max = 50)
	@Column(name="SRC_DB_NAME")
	@NotBlank(message = "Database name cannot be empty!")
	private String dbName;
	
	@Size(max = 50)
	@Column(name="SRC_DB_TYPE")
	@NotBlank(message = "Database type cannot be empty!")
	private String dbType;
	
	@Size(max = 50)
	@Column(name="SRC_TBL_OWNER_NAME")
	@NotBlank(message = "Owner name cannot be empty!")
	private String ownerName;
	
	@NotBlank(message = "Source number cannot be empty!")
	@Column(name="SRC_NUM")
	private int sourceNum;
	

	
}
