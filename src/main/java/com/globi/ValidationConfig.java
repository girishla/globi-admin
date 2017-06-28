package com.globi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;


@Configuration
public class ValidationConfig extends RepositoryRestConfigurerAdapter {
 
    @Bean
    Validator validatorLocal() {
        return new LocalValidatorFactoryBean();
    }
 
    
    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
        validatingListener.addValidator("beforeCreate",  validatorLocal());
        validatingListener.addValidator("beforeSave", validatorLocal());

    }
}