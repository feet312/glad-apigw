package com.kt.icis.sa.icisapigw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan("com.kt")
@EnableFeignClients
public class IcisApigwApplication {

	public static void main(String[] args) {
		SpringApplication.run(IcisApigwApplication.class, args);
	}

}
