package com.globi.infa.datasource.gen;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.globi.AbstractIntegrationTest;
import com.globi.infa.DataSourceTableDTO;
import com.globi.infa.datasource.core.OracleTableMetadataVisitor;
import com.globi.infa.datasource.gen.GENTableRepository;

import lombok.extern.slf4j.Slf4j;



@Slf4j
public class GENTableIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	GENTableRepository genRepository;
	
	@Autowired
	private OracleTableMetadataVisitor tblQueryVisitor;
	
	@Test
	public void canQueryAllTables(){
		log.info("Starting to test all tables.");
		 List<DataSourceTableDTO> results;
		 log.info("Result of all tables query.{}", genRepository.accept(tblQueryVisitor).toString());
	}
	
}
