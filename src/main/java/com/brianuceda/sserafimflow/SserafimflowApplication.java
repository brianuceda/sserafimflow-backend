package com.brianuceda.sserafimflow;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.brianuceda.sserafimflow.respositories.ExchangeRateRepository;

@SpringBootApplication
@EnableScheduling
public class SserafimflowApplication {

  public static void main(String[] args) {
  	SpringApplication.run(SserafimflowApplication.class, args);
  }

  @Bean
  public CommandLineRunner initData(
  	ExchangeRateRepository exchangeRateRepository
  ) {
  	return args -> {
  		
  	};
  }
}
