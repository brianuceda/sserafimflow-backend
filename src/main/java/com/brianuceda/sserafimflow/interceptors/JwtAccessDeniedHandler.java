package com.brianuceda.sserafimflow.interceptors;

import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
  // Se activa cuando un usuario autenticado intenta acceder a un recurso protegido sin los permisos necesarios.

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      org.springframework.security.access.AccessDeniedException accessDeniedException) throws IOException {

    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType("application/json");

    ResponseDTO responseDTO = new ResponseDTO("Acceso denegado");

    response.getWriter().write(objectMapper.writeValueAsString(responseDTO));
  }
}
