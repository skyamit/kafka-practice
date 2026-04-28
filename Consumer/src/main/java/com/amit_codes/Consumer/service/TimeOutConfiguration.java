package com.amit_codes.Consumer.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class TimeOutConfiguration {

    @Bean
    public DefaultErrorHandler errorHandler() {
        return new DefaultErrorHandler(
                new FixedBackOff(2000L, 3) // retry 3 times with 2s delay
        );
    }

}
