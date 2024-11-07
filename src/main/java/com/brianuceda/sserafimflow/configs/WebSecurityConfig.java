package com.brianuceda.sserafimflow.configs;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.brianuceda.sserafimflow.interceptors.JwtAccessDeniedHandler;
import com.brianuceda.sserafimflow.interceptors.JwtAuthenticationEntryPoint;
import com.brianuceda.sserafimflow.interceptors.JwtAuthenticationFilter;

import lombok.extern.java.Log;

@Log
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {
  @Value("${IS_PRODUCTION}")
  private Boolean isProduction;

  private final String[] ALLOWED_ORIGINS = {
    "https://sserafimflow.vercel.app",
    "http://localhost:4200"
  };

  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final AuthenticationProvider authenticationProvider;

  public WebSecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
      JwtAccessDeniedHandler jwtAccessDeniedHandler, JwtAuthenticationFilter jwtAuthenticationFilter,
      AuthenticationProvider authenticationProvider) {
    this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.authenticationProvider = authenticationProvider;
  }

  @Bean
  private SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(request -> {
          CorsConfiguration configuration = new CorsConfiguration();
          
          configuration.setAllowedOrigins(Arrays.asList(ALLOWED_ORIGINS));
          configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
          configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
          configuration.setAllowCredentials(true);
          configuration.setMaxAge(3600L);

          return configuration;
        }))
        .authorizeHttpRequests(authRequest -> {
          authRequest.requestMatchers("/api/v1/auth/**").permitAll();
          authRequest.requestMatchers("/api/v1/logs/**").permitAll();
          authRequest.requestMatchers("/ws/**").permitAll();
          authRequest.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll();
          
          authRequest.anyRequest().authenticated();
        })
        .sessionManagement(sessionManagement -> {
          sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        })
        .exceptionHandling(exceptionHandling -> {
          exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint);
          exceptionHandling.accessDeniedHandler(jwtAccessDeniedHandler);
        })
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

}
