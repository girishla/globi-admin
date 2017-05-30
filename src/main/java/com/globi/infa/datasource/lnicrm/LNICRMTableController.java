package com.globi.infa.datasource.lnicrm;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.datasource.core.OracleTableMetadataVisitor;

@RestController
public class LNICRMTableController {

	@Autowired
	private final LNICRMTableRepository repository;
	
	@Autowired
	private OracleTableMetadataVisitor tblQueryVisitor;

	@Autowired
	@Qualifier("dsLNICRM")
	private DataSource dataSource;

	@Autowired
	public LNICRMTableController(LNICRMTableRepository repo) {
		repository = repo;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/datasources/cuk/tables")
	public @ResponseBody ResponseEntity<?> getTables() {

		List<DataSourceTableDTO> tables = repository.accept(tblQueryVisitor);
		return ResponseEntity.ok(tables);
	}

}