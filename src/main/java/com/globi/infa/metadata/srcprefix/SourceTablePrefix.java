package com.globi.infa.metadata.srcprefix;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Subselect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Subselect("SELECT * FROM MDT.SourceTablePrefix")
public class SourceTablePrefix  {

	@Column(name="ID")
	private @Id long id;
	
	@Column(name="SRC_NAME")
	private String sourceName;
	

	@Column(name="SRC_TBL")
	private String tableName;
	
	@Column(name="TBL_PREFIX")
	private String tablePrefix;
	
	@Column(name="UNIQUE_NAME")
	private String tableUniqueName;
	
}
