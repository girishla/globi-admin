package com.globi.infa.datasource.gen;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.globi.AbstractIntegrationTest;
import com.globi.infa.DataSourceTable;
import com.globi.infa.datasource.gen.GENTableRepository;

import lombok.extern.slf4j.Slf4j;



@Slf4j
public class GENTableIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	GENTableRepository genRepository;
	
	@Test
	public void canQueryAllTables(){
		log.info("Starting to test all tables.");
		 List<DataSourceTable> results;
		 log.info("Result of all tables query.{}", genRepository.getAllTables().toString());
	}
	
}
