package com.arteisis;

import com.arteisis.config.ContactMailProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ContactMailProperties.class)
public class ArteIsisApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArteIsisApplication.class, args);
    }
}
