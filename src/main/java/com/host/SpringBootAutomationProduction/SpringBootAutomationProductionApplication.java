package com.host.SpringBootAutomationProduction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@SpringBootApplication
public class SpringBootAutomationProductionApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootAutomationProductionApplication.class, args);
	}

}
