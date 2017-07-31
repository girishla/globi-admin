package com.globi.infa.datasource.lnicrm;

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
@ConfigurationProperties("app.datasource.lnicrm")
public class OracleLNICRMConfig  extends HikariConfig {


	 @Bean(name = "dsLNICRM") 

	    public DataSource dataSourceLNICRM() { 
			return new HikariDataSource(this);
	    } 

    
    @Bean(name = "jdbcOracleLNICRM") 
    @Autowired
    public JdbcTemplate jdbcTemplate(@Qualifier("dsLNICRM") DataSource dsLNICRM) { 
        return new JdbcTemplate(dsLNICRM); 
    } 
    
}