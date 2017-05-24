package com.globi.infa.datasource.gcrm;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
@Configuration
public class OracleGCRMConfig {


	 @Bean(name = "dsGCRM") 
	 @ConfigurationProperties("app.datasource.gcrm")
	    public DataSource dataSourceGCRM() { 
		  return DataSourceBuilder
			        .create()
			        .build();
	    } 

    
    @Bean(name = "jdbcOracleGCRM") 
    @Autowired
    public JdbcTemplate jdbcTemplate(@Qualifier("dsGCRM") DataSource dsGCRM) { 
        return new JdbcTemplate(dsGCRM); 
    } 
    
}