package com.globi.infa.metadata.pdl;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedStoredProcedureQueries;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureParameter;

@Entity
@NamedStoredProcedureQueries({
   @NamedStoredProcedureQuery(name = "generateDDL", 
                              procedureName = "DDL_DEPLOY_PDL",
                              parameters = {
                                 @StoredProcedureParameter(mode = ParameterMode.IN, name = "P_RELEASE", type = String.class),
                                 @StoredProcedureParameter(mode = ParameterMode.IN, name = "P_TABLE", type = String.class),
                                 @StoredProcedureParameter(mode = ParameterMode.IN, name = "P_REBUILD", type = String.class),
                                 @StoredProcedureParameter(mode = ParameterMode.IN, name = "P_BUILD_INDX", type = String.class),
                                 @StoredProcedureParameter(mode = ParameterMode.OUT, name = "P_MESSAGE", type = String.class)
                              }),

})
public class PuddleDDLGeneratorEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1454L;
	
    //stub to satisfy hibernate identifier requirement
    @Id @GeneratedValue
    private Long id;
}