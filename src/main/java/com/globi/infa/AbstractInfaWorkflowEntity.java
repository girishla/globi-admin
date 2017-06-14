package com.globi.infa;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.hateoas.Identifiable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base class for entity implementations. Uses a {@link Long} id.
 * 
 * @author Girish lakshmanan
 */
@MappedSuperclass
@Getter
@ToString
@EqualsAndHashCode
@EntityListeners(AuditingEntityListener.class)
public class AbstractInfaWorkflowEntity implements Identifiable<Long> {

	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	final private Long id;
	private @Version Long version;

	protected AbstractInfaWorkflowEntity() {
		this.id = null;
	}
	

	@Override
	public Long getId() {
		return this.id;
	}


	@Column(name = "created_date", updatable = false,columnDefinition = "DATE")
	@CreatedDate
	private Date  createdDate;


	@Column(name = "updated_date",columnDefinition = "DATE")
	@LastModifiedDate
	private Date  modifiedDate;

}
