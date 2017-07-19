package com.globi.infa.metadata.pdl;

import org.springframework.data.repository.query.Param;

//Dummy Interface to autowire multiple data repos
public interface PuddleDDLRepository {

    String generateDDL (String release, String tableName, String rebuildFlag,String buildIndexFlag);

	
    
	
}
