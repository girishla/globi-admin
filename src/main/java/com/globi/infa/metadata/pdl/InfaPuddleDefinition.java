package com.globi.infa.metadata.pdl;

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
@Table(name = "M_INFA_PDL_DEFN")
@AllArgsConstructor
@Builder
public class InfaPuddleDefinition extends AbstractEntity {

	@NonNull
	@NotBlank(message = "Source name cannot be empty!")
	@Column(unique=true)
	private String pdlTableName;

	@NotBlank(message = "Owner name cannot be empty!")
	private String ownerName;

	@OrderColumn //
	@Column(unique = true) //
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true) //
	@JoinColumn(name="pdl_defn_id",referencedColumnName="id")
	@Builder.Default
	private List<InfaPuddleColumnDefinition> columns = new ArrayList<>();
	
}
