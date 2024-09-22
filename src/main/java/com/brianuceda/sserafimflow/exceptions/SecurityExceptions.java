package com.brianuceda.sserafimflow.exceptions;

public class SecurityExceptions {
  public static class ProtectedResource extends RuntimeException {
    public ProtectedResource(String message) {
      super(message);
    }
  }
  
  public static class SQLInjectionException extends RuntimeException {
    public SQLInjectionException(String message) {
      super(message);
    }
  }
  
  public static class BlacklistedTokenException extends RuntimeException {
    public BlacklistedTokenException(String message) {
      super(message);
    }
  }
}
