/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.vault;

/**
 * Represents a processing exception in a {@code Vault} instance.
 */
public class VaultException extends Exception {

  private static final long serialVersionUID = 1697956075943361868L;

  /**
   * Constructs a VaultException with no detail message. A detail message is a String that describes this particular
   * exception.
   */
  public VaultException() {
    super();
  }

  /**
   * Constructs a VaultException with the specified detail message. A detail message is a String that describes this
   * particular exception.
   *
   * @param s the detail message.
   */
  public VaultException(String s) {
    super(s);
  }

  /**
   * Creates a VaultException with the specified detail message and cause.
   *
   * @param message the detail message (which is saved for later retrieval by the getMessage method).
   * @param cause the cause (which is saved for later retrieval by the getCause method). Null is permitted, and
   *              indicates that the cause is nonexistent or unknown.)
   */
  public VaultException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a VaultException with the specified cause and a detail message of (cause==null ? null : cause.toString())
   * (which typically contains the class and detail message of cause).
   *
   * @param cause the cause (which is saved for later retrieval by the getCause() method). Null is permitted, and
   *              indicates that the cause is nonexistent or unknown.)
   */
  public VaultException(Throwable cause) {
    super(cause);
  }

}
