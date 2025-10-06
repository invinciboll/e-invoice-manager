package com.invinciboll.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.sf.saxon.s9api.Processor;

@Configuration
public class SaxonConfig {

    @Bean
    public Processor saxonProcessor() {
        // false = non-schema-aware processor
        return new Processor(false);
    }
}
