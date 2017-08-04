package com.globi.infa.datasource.law;

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
@ConfigurationProperties("app.datasource.law")
public class OracleLAWConfig  extends HikariConfig  {


	 @Bean(name = "dsLAW") 
	    public DataSource dataSourceLAW() { 
			return new HikariDataSource(this);
	    } 

    
    @Bean(name = "jdbcOracleLAW") 
    @Autowired
    public JdbcTemplate jdbcTemplate(@Qualifier("dsLAW") DataSource dsLAW) { 
        return new JdbcTemplate(dsLAW); 
    } 
    
}