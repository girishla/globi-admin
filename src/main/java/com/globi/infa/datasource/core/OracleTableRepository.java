package com.globi.infa.datasource.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.globi.infa.DataSourceTableDTO;
import com.globi.metadata.sourcesystem.SourceSystem;
import com.globi.metadata.sourcesystem.SourceSystemRepository;
import com.nurkiewicz.jdbcrepository.JdbcRepository;
import com.nurkiewicz.jdbcrepository.RowUnmapper;

@Component
public class OracleTableRepository {


	@Autowired
	protected SourceSystemRepository sourceSystemRepo;
	
	protected String columnSQL="SELECT OWNER,TABLE_NAME FROM ALL_TABLES WHERE ROWNUM<10 AND OWNER=?";

	protected static final RowMapper<DataSourceTableDTO> tableMapper = new RowMapper<DataSourceTableDTO>() {
		public DataSourceTableDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
			DataSourceTableDTO table = new DataSourceTableDTO(rowNum, rs.getString("OWNER"), rs.getString("TABLE_NAME"));
			return table;
		}
	};
	
	public List<DataSourceTableDTO> getAllTables(JdbcTemplate jdbcT,String ownerName) {

		return jdbcT.query(columnSQL, tableMapper,ownerName);
	}


}








