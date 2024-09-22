package com.brianuceda.sserafimflow.utils;

import java.util.List;

import org.springframework.stereotype.Component;

import com.brianuceda.sserafimflow.exceptions.SecurityExceptions.ProtectedResource;
import com.brianuceda.sserafimflow.exceptions.SecurityExceptions.SQLInjectionException;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class DataUtils {
  public static void verifyAllowedOrigin(HttpServletRequest request, List<String> allowedOrigins) {
    String origin = request.getHeader("Origin");

    if (origin == null || !allowedOrigins.contains(origin)) {
      throw new ProtectedResource("Acceso no autorizado");
    }
  }

  public static void verifySQLInjection(String str) {
    if (str.matches(".*(--|[;+*^$|?{}\\[\\]()'\"\\']).*") || str.contains("SELECT") || str.contains("DELETE")
        || str.contains("UPDATE") || str.contains("INSERT") || str.contains("DROP") || str.contains(" OR ")) {
      throw new SQLInjectionException("Esas cosas son del diablo");
    }
  }
}
