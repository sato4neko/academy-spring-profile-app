package com.spring.springbootapplication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;

@Configuration
public class BaseLayOutConfig {

    @Bean
    public LayoutDialect layoutDialect() {
	    return new LayoutDialect();
	}
}
