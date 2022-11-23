package com.google.idea.blaze.scala.fastbuild;

public class ScalaFastCompilationException extends RuntimeException {

  public ScalaFastCompilationException(String message) {
    super(message);
  }

  public ScalaFastCompilationException(Exception e) {
    super(e);
  }

}
