/*
 * Copyright (c) 2004 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network;

/**
 * Exception thrown when there is a problem with IP Address operations.
 */
public final class IpAddressException extends Exception {

  private static final long serialVersionUID = 7815350628254830424L;




  /**
   * Constructor
   */
  public IpAddressException() {
    super();
  }




  /**
   * Constructor
   *
   * @param message Error message
   */
  public IpAddressException(final String message) {
    super(message);
  }




  /**
   * Constructor
   *
   * @param message Error message
   * @param excptn
   */
  public IpAddressException(final String message, final Throwable excptn) {
    super(message, excptn);
  }




  /**
   * Constructor
   *
   * @param excptn
   */
  public IpAddressException(final Throwable excptn) {
    super(excptn);
  }
}