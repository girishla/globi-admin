package com.globi.infa.datasource.gcrm;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ConfigurationProperties("app.datasource.gcrm")
public class OracleGCRMConfig extends HikariConfig {

	@Bean(name = "dsGCRM")

	public DataSource dataSourceGCRM() {
		return new HikariDataSource(this);
	}

	@Bean(name = "jdbcOracleGCRM")
	@Autowired
	public JdbcTemplate jdbcTemplate(@Qualifier("dsGCRM") DataSource dsGCRM) {
		return new JdbcTemplate(dsGCRM);
	}

}