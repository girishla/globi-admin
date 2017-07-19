package com.globi.infa.metadata.pdl;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.Subselect;

@Entity
@Subselect("SELECT 'DUMMY' FROM DUAL")
public class DummyEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1454L;
	
    //stub to satisfy hibernate identifier requirement
    @Id @GeneratedValue
    private Long id;
	
}
