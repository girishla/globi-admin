package com.globi.infa.metadata.sil;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;


@Repository
public class SilMetadataRepository {

	@Autowired
	@Qualifier("jdbcOracleMDT")
	protected JdbcTemplate jdbcOracleMDT;

	protected String columnSQL = "SELECT TBL_NAME,\r\n" + 
			"  STG_NAME,\r\n" + 
			"  TBL_TYPE,\r\n" + 
			"  COL_ORDER,\r\n" + 
			"  COL_NAME,\r\n" + 
			"  COL_TYPE,\r\n" + 
			"  D_TBL_NAME,\r\n" + 
			"  COL_DATATYPE,\r\n" + 
			"  COL_IND_TYPE,\r\n" + 
			"  COL_STG_FLG,\r\n" + 
			"  COL_TGT_FLG,\r\n" + 
			"  COL_LEGCY_FLG,\r\n" + 
			"  COL_MINI_FLG,\r\n" + 
			"  COL_DOM_LKP_FLG,\r\n" + 
			"  COL_AUTO_FLG\r\n" + 
			"FROM M_DDL_SIL_COLUMN_V \r\n" + 
			"WHERE TBL_NAME=?";
	

	public List<SilMetadata> getAll(String tableName) {
		return jdbcOracleMDT.query(columnSQL, tableColumnMapper,tableName);
	}
	
	
	
	protected final RowMapper<SilMetadata> tableColumnMapper = new RowMapper<SilMetadata>() {
		public SilMetadata mapRow(ResultSet rs, int rowNum) throws SQLException {

			SilMetadata col = SilMetadata.builder()//
					.autoColumnFlag(rs.getInt("COL_AUTO_FLG")==1?true:false)//
					.columnDataType(rs.getString("COL_DATATYPE"))//
					.columnName(rs.getString("COL_NAME"))//
					.columnOrder(Integer.toString(rs.getInt("COL_ORDER")))//
//					.columnPrecision(Integer.parseInt(rs.getString("COL_PRECISION")))//
					.columnType(rs.getString("COL_TYPE"))//
					.dimTableName(rs.getString("D_TBL_NAME"))//
					.domainLookupColumnFlag(rs.getInt("COL_DOM_LKP_FLG")==1?true:false)//
					.legacyColumnFlag(rs.getInt("COL_LEGCY_FLG")==1?true:false)//
					.miniDimColumnFlag(rs.getInt("COL_MINI_FLG")==1?true:false)//
					.stageColumnFlag(rs.getInt("COL_STG_FLG")==1?true:false)//
					.targetColumnFlag(rs.getInt("COL_TGT_FLG")==1?true:false)//
					.stageName(rs.getString("STG_NAME"))//
					.tableName(rs.getString("TBL_NAME"))//
					.build();
					
					
			return col;
		}
	};


}
