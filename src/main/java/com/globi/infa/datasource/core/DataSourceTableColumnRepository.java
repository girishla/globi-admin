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
public class DataSourceTableColumnRepository {

	@Autowired
	@Qualifier("jdbcOracleMDT")
	protected JdbcTemplate jdbcOracleMDT;

	protected String columnSQL = "SELECT DISTINCT\r\n" + 
			"               R.SRC_NAME SRC_NAME\r\n" + 
			",               NVL(S.SRC_TBL,A.AUX_TBL_NAME) SRC_TBL\r\n" + 
			",               M.SRC_TBL_COL SRC_TBL_COL\r\n" + 
			"FROM\r\n" + 
			"                LAW.S_LAW_COL C\r\n" + 
			"                INNER JOIN LAW.S_LAW_TBL T ON C.TBL_ID=T.TBL_ID\r\n" + 
			"                INNER JOIN LAW.S_LAW_STG_SRC S ON S.STG_NAME=T.STG_NAME\r\n" + 
			"                LEFT OUTER JOIN LAW.S_LAW_SRC_AUX A ON S.STG_SRC_ID=A.STG_SRC_ID\r\n" + 
			"                INNER JOIN LAW.S_LAW_MAP M ON C.COL_ID=M.COL_ID AND M.SRC_TBL_ALIS IN (S.SRC_TBL,A.AUX_TBL_ALIS)\r\n" + 
			"                INNER JOIN LAW.S_LAW_SRC R ON R.SRC_ID=S.SRC_ID     \r\n" + 
			"WHERE R.SRC_NAME IN ('CUK','CGL','FBM','GEN') AND  M.SRC_TBL_COL IS NOT NULL\r\n" + 
			"";
	

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
					.integrationId(false)//
					.changeCaptureCol(false).build();

					
			return col;
		}
	};


}
