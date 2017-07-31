package com.globi.infa.datasource.vpt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

@Configuration
@ConfigurationProperties("app.datasource.vpt")
public class SQLServerVPTConfig  extends HikariConfig {
	
	
	@Bean(name = "dsVPT")
	public DataSource dataSourceVPT() {
		return new HikariDataSource(this);
	}

	@Bean(name = "jdbcSQLServerVPT")
	@Autowired
	public JdbcTemplate jdbcTemplate(@Qualifier("dsVPT") DataSource dsVPT) {
		return new JdbcTemplate(dsVPT);
	}

}