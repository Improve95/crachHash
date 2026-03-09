package ru.nsu.crackhash.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@ConfigurationPropertiesScan
@EnableConfigurationProperties
@SpringBootApplication
public class CrackHashApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrackHashApplication.class, args);
	}
}
