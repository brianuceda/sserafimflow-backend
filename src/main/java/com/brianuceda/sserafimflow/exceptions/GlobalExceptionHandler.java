package com.brianuceda.sserafimflow.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.exceptions.GeneralExceptions.*;
import com.brianuceda.sserafimflow.exceptions.SecurityExceptions.*;

@ControllerAdvice
public class GlobalExceptionHandler {
  // Security Exceptions
  @ExceptionHandler(ProtectedResource.class)
  public ResponseEntity<?> handleProtectedResource(ProtectedResource ex) {
    ResponseDTO response = new ResponseDTO(ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(SQLInjectionException.class)
  public ResponseEntity<?> handleSQLInjectionException(SQLInjectionException ex) {
    ResponseDTO response = new ResponseDTO(ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(BlacklistedTokenException.class)
  public ResponseEntity<?> handleBlacklistedTokenException(BlacklistedTokenException ex) {
    ResponseDTO response = new ResponseDTO(ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  // General Exceptions
  @ExceptionHandler(ConnectionFailed.class)
  public ResponseEntity<?> handleConnectionFailed(ConnectionFailed ex) {
    ResponseDTO response = new ResponseDTO(ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
