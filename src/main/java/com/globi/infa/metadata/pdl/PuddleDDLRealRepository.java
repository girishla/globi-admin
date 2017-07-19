package com.globi.infa.metadata.pdl;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile("prod")
@Transactional(propagation=Propagation.SUPPORTS,transactionManager="coreTransactionManager")
//Expect a transaction to exist
public interface PuddleDDLRealRepository extends JpaRepository<PuddleDDLGeneratorEntity, Long>,PuddleDDLRepository {
    @Procedure(name = "generateDDL")
    String generateDDL(@Param("P_RELEASE") String release,@Param("P_TABLE") String tableName,@Param("P_REBUILD") String rebuildFlag,@Param("P_BUILD_INDX") String buildIndexFlag);


    
    
    
    
}