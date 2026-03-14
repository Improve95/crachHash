package ru.nsu.crackhash.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@ConfigurationPropertiesScan
@EnableConfigurationProperties
@SpringBootApplication
public class CrackHashApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrackHashApplication.class, args);
	}
}
