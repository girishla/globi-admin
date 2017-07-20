package com.globi.infa.datasource.chb;

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
import com.globi.infa.datasource.type.oracle.OracleViewMetadataVisitor;

@RestController
public class CHBTableController {

	@Autowired
	private final CHBTableRepository repository;
	
	@Autowired
	private OracleViewMetadataVisitor viewQueryVisitor;

	@Autowired
	@Qualifier("dsCHB")
	private DataSource dataSource;

	@Autowired
	public CHBTableController(CHBTableRepository repo) {
		repository = repo;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/datasources/chb/tables")
	public @ResponseBody ResponseEntity<?> getTables() {

		List<DataSourceTableDTO> tables = repository.accept(viewQueryVisitor);
		return ResponseEntity.ok(tables);
	}

}