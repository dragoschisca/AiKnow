package com.aiknow;

import com.aiknow.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(AppProperties.class)
public class AiKnowApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiKnowApplication.class, args);
    }
}
