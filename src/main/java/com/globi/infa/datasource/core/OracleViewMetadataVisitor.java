package com.globi.infa.datasource.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.globi.infa.DataSourceTableDTO;


@Component
public class OracleViewMetadataVisitor implements DBMetadataVisitor {

	protected String columnSQL="SELECT OWNER,VIEW_NAME FROM ALL_VIEWS WHERE OWNER=?";
	
	
	protected static final RowMapper<DataSourceTableDTO> tableMapper = new RowMapper<DataSourceTableDTO>() {
		public DataSourceTableDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
			DataSourceTableDTO table = new DataSourceTableDTO(rowNum, rs.getString("OWNER"), rs.getString("VIEW_NAME"));
			return table;
		}
	};
	
	@Override
	public List<DataSourceTableDTO> getAllTables(JdbcTemplate jdbcT,String ownerName) {

			return  jdbcT.query(columnSQL, tableMapper, ownerName);
	}

}
