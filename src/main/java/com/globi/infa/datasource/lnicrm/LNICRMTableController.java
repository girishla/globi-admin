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

import com.globi.infa.DataSourceTable;

@RestController
public class LNICRMTableController {

	private final LNICRMTableRepository repository;

	@Autowired
	@Qualifier("dsLNICRM")
	private DataSource dataSource;

	@Autowired
	public LNICRMTableController(LNICRMTableRepository repo) {
		repository = repo;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/infagen/datasources/cuk/tables")
	public @ResponseBody ResponseEntity<?> getTables() {

		List<DataSourceTable> tables = repository.getAllTables();
		return ResponseEntity.ok(tables);
	}

}