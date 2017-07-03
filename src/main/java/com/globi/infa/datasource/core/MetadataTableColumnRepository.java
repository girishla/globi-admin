package com.globi.infa.datasource.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;


@Repository
public class MetadataTableColumnRepository {

	@Autowired
	@Qualifier("jdbcOracleMDT")
	protected JdbcTemplate jdbcOracleMDT;

	protected String columnSQL = "SELECT SRC_NAME, \r\n" + 
			"       SRC_TBL, \r\n" + 
			"       SRC_TBL_COL, \r\n" + 
			"       MAX(SRC_COL_ORDER) SRC_COL_ORDER, \r\n" + 
			"       MAX(INT_ID_INDICATOR) INT_ID_INDICATOR, \r\n" + 
			"       MAX(BU_PGUID_INDICATOR) BU_PGUID_INDICATOR, \r\n" + 
			"       MAX(PGUID_INDICATOR)  PGUID_INDICATOR \r\n" + 
			"FROM   MDT_SOURCE_TBL_COLS \r\n" + 
			"WHERE (SRC_TBL IS NOT NULL) AND (SRC_TBL_COL IS NOT NULL) AND SRC_NAME IN ('GEN','CUK','CGL')\r\n" + 
			"GROUP BY SRC_NAME, \r\n" + 
			"          SRC_TBL, \r\n" + 
			"          SRC_TBL_COL";
	

	public List<DataSourceTableColumnDTO> getAll() {
		return jdbcOracleMDT.query(columnSQL, tableColumnMapper);
	}
	
	
	protected final RowMapper<DataSourceTableColumnDTO> tableColumnMapper = new RowMapper<DataSourceTableColumnDTO>() {
		public DataSourceTableColumnDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

			DataSourceTableColumnDTO col = DataSourceTableColumnDTO.builder()//
					.colName(rs.getString("SRC_TBL_COL"))//
					.tableName(rs.getString("SRC_TBL"))//
					.sourceName(rs.getString("SRC_NAME"))
					.tableOwner("")
					.dataType("")
					.id(rowNum)
					.integrationId(rs.getInt("INT_ID_INDICATOR")==1?true:false)//
					.changeCaptureCol(false)
					.pguidCol(rs.getInt("PGUID_INDICATOR")==1?true:false)//
					.buidCol(rs.getInt("BU_PGUID_INDICATOR")==1?true:false)//
					.colOrder(rs.getInt("SRC_COL_ORDER"))
					.build();

					
			return col;
		}
	};


}
