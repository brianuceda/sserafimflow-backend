package com.brianuceda.sserafimflow.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.exceptions.GeneralExceptions.ConnectionFailed;
import com.brianuceda.sserafimflow.exceptions.SecurityExceptions.ProtectedResource;
import com.brianuceda.sserafimflow.exceptions.SecurityExceptions.SQLInjectionException;

@ControllerAdvice
public class GlobalExceptionHandler {
  // Security Exceptions
  @ExceptionHandler(ProtectedResource.class)
  public ResponseEntity<?> handleProtectedResource(ProtectedResource ex) {
    ResponseDTO response = new ResponseDTO(ex.getMessage(), 401);
    return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
  }

  @ExceptionHandler(SQLInjectionException.class)
  public ResponseEntity<?> handleSQLInjectionException(SQLInjectionException ex) {
    ResponseDTO response = new ResponseDTO(ex.getMessage(), 401);
    return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
  }

  // General Exceptions
  @ExceptionHandler(ConnectionFailed.class)
  public ResponseEntity<?> handleConnectionFailed(ConnectionFailed ex) {
    ResponseDTO response = new ResponseDTO(ex.getMessage(), 500);
    return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
  }
}
