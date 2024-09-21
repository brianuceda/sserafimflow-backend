package com.brianuceda.sserafimflow.exceptions;

public class DataExceptions {
  public static class InvalidDate extends RuntimeException {
    public InvalidDate(String message) {
      super(message);
    }
  }
}
