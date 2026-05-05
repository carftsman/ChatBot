package com.dhatvibs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ChatBotServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatBotServiceApplication.class, args);
	}

}
