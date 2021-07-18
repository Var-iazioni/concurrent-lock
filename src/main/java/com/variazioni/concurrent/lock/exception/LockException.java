package com.variazioni.concurrent.lock.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Lock Exception
 * 
 * @author Variazioni
 * @date 2021/07/18
 */
public class LockException extends Throwable {

  private static final long serialVersionUID = -6221360778156616780L;
  private String message;

  /**
   * Constructor
   * 
   * @param message
   */
  public LockException(String message) {
    super(message);
  }

  /**
   * Getter
   * 
   * @author Variazioni
   * @return String
   */
  public String getMsg() {
    return this.message;
  }

  /**
   * Get stack trace info
   * 
   * @author Variazioni
   * @return String
   */
  public String getStackTraceStr() {
    StringWriter sw = new StringWriter();
    printStackTrace(new PrintWriter(sw, true));
    return sw.toString();
  }
}
