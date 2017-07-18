package com.globi.infa.metadata.pdl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import oracle.jdbc.OracleTypes;



//This is no longer used. Marked for deletion
public class PuddleDDLGenerator {

	@Autowired
	@Qualifier("jdbcOracleMDT")
	JdbcTemplate jdbcOracleMDT;

	PuddleDDLGenerator(JdbcTemplate jdbcOracleMDT) {

		this.jdbcOracleMDT = jdbcOracleMDT;
		jdbcOracleMDT.setResultsMapCaseInsensitive(true);
	}

	private SimpleJdbcCall sjc;

	public void generateRunDDL(String tableName) {

		sjc = new SimpleJdbcCall(jdbcOracleMDT)//
				.withCatalogName("MDT")//
				.withProcedureName("DDL_DEPLOY_PDL");

		sjc.useInParameterNames("P_RELEASE", "P_TABLE", "P_REBUILD", "P_BUILD_INDX")
				.withoutProcedureColumnMetaDataAccess()
				.declareParameters(
						new SqlParameter("P_RELEASE", OracleTypes.VARCHAR), //
						new SqlParameter("P_TABLE", OracleTypes.VARCHAR), //
						new SqlParameter("P_REBUILD", OracleTypes.VARCHAR), //
						new SqlParameter("P_BUILD_INDX", OracleTypes.VARCHAR));

		SqlParameterSource in = new MapSqlParameterSource()//
				.addValue("P_RELEASE", "NoRelease")//
				.addValue("P_TABLE", tableName)//
				.addValue("P_REBUILD", "Y")//
				.addValue("P_BUILD_INDX", "N");

		sjc.execute(in);

	}
}