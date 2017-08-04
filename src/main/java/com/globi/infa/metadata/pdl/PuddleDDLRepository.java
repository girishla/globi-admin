package com.globi.infa.metadata.pdl;

//Dummy Interface to autowire multiple data repos
public interface PuddleDDLRepository {

    String generateDDL (String release, String tableName, String rebuildFlag,String buildIndexFlag);

	
    
	
}
