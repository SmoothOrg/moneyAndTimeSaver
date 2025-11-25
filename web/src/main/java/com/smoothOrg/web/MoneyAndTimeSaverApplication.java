package com.smoothOrg.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.smoothOrg")
@EnableJpaRepositories(basePackages = "com.smoothOrg.domain.repository")
@EntityScan(basePackages = "com.smoothOrg.domain.entity")
public class MoneyAndTimeSaverApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyAndTimeSaverApplication.class, args);
	}

}
