package com.globi.infa.datasource.law;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.datasource.type.oracle.OracleNonOwnerTableMetadataVisitor;

@RestController
public class LAWTableController {

	@Autowired
	private final LAWTableRepository repository;
	
	@Autowired
	private OracleNonOwnerTableMetadataVisitor tableQueryVisitor;

	@Autowired
	@Qualifier("dsLAW")
	private DataSource dataSource;

	@Autowired
	public LAWTableController(LAWTableRepository repo) {
		repository = repo;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/datasources/law/tables")
	public @ResponseBody ResponseEntity<?> getTables() {

		List<DataSourceTableDTO> tables = repository.accept(tableQueryVisitor);
		return ResponseEntity.ok(tables);
	}

}