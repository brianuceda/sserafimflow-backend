package com.brianuceda.sserafimflow.exceptions;

public class GeneralExceptions {
  public static class ConnectionFailed extends RuntimeException {
    public ConnectionFailed(String message) {
      super(message);
    }
  }
}
