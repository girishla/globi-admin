package com.globi.infa.datasource.vpt;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.globi.AbstractIntegrationTest;
import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.datasource.vpt.VPTTableRepository;
import com.globi.infa.datasource.type.oracle.OracleTableMetadataVisitor;

import lombok.extern.slf4j.Slf4j;



@Slf4j
public class VPTTableIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	VPTTableRepository vptRepository;
	
	@Autowired
	private OracleTableMetadataVisitor tblQueryVisitor;
	
	@Test
	public void canQueryAllTablesOnGenesis(){
		log.info("Starting to test all tables.");
		log.info("Result of all tables query.{}", vptRepository.accept(tblQueryVisitor).toString());
	}
	
}
