package com.globi.infa.metadata.pdl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional("uncommittedRead")
public interface PuddleDDLRepository extends JpaRepository<PuddleDDLGeneratorEntity, Long> {
    @Procedure(name = "generateDDL")
    void generateDDL(@Param("P_RELEASE") String release,@Param("P_TABLE") String tableName,@Param("P_REBUILD") String rebuildFlag,@Param("P_BUILD_INDX") String buildIndexFlag);

 }