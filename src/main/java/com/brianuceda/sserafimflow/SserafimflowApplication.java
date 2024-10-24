package com.brianuceda.sserafimflow;

import java.util.TimeZone;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
public class SserafimflowApplication {

  public static void main(String[] args) {
    SpringApplication.run(SserafimflowApplication.class, args);
  }

  @Bean
  public CommandLineRunner initData() {
    return args -> {
    };
  }

  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
  }
}
