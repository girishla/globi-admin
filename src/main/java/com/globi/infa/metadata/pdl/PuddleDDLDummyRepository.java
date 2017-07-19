package com.globi.infa.metadata.pdl;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile("test")
@Transactional(propagation=Propagation.SUPPORTS,transactionManager="coreTransactionManager")
//Expect a transaction to exist
public interface PuddleDDLDummyRepository extends JpaRepository<DummyEntity, Long>,PuddleDDLRepository {
    

	@Query("SELECT 'DUMMY' FROM DummyEntity D where ''=:release AND ''=:tableName AND ''=:rebuildFlag AND ''=:buildIndexFlag")
	String generateDDL (@Param("release")  String release, @Param("tableName")  String tableName, @Param("rebuildFlag")  String rebuildFlag,@Param("buildIndexFlag")  String buildIndexFlag);
    
    
}