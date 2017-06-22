package com.globi.infa.metadata.src;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;

import com.globi.infa.AbstractEntity;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.ToString;

@Entity
@ToString(callSuper = true)
@RequiredArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Table(name = "M_INFA_SRC_DEFN")
@AllArgsConstructor
@Builder
public class InfaSourceDefinition extends AbstractEntity {

	@NotBlank(message = "Database name cannot be empty!")
	private String databaseName;

	@NotBlank(message = "Database type cannot be empty!")
	private String databaseType;

	@NonNull
	@NotBlank(message = "Source name cannot be empty!")
	private String sourceTableName;

	@NotBlank(message = "Owner name cannot be empty!")
	private String ownerName;
	
	
	private String sourceTableUniqueName;


	@OrderColumn //
	@Column(unique = true) //
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true) //
	@JoinColumn(name="source_id",referencedColumnName="id")
	@Builder.Default
	private List<InfaSourceColumnDefinition> columns = new ArrayList<>();
	
}
