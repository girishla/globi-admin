package com.globi.infa.datasource.vpt;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.globi.AbstractIntegrationTest;
import com.globi.infa.datasource.type.sqlserver.SQLServerTableMetadataVisitor;

import lombok.extern.slf4j.Slf4j;



@Slf4j
public class VPTTableIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	VPTTableRepository vptRepository;
	
	@Autowired
	private SQLServerTableMetadataVisitor tblQueryVisitor;
	
	@Test
	@Ignore //ignore until firewall is opened
	public void canQueryAllTablesOnViewpount(){
		log.info("Starting to test all tables.");
		log.info("Result of all tables query.{}", vptRepository.accept(tblQueryVisitor).toString());
	}
	
}
