package com.brianuceda.sserafimflow.interceptors;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
  // Se activa cuando un usuario no autenticado intenta acceder a un recurso protegido.

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
      throws IOException {

    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType("application/json");

    ResponseDTO responseDTO = new ResponseDTO("Recurso protegido");

    response.getWriter().write(objectMapper.writeValueAsString(responseDTO));
  }
}
