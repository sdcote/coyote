/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.mc.snow;

import java.io.IOException;


/**
 * Exception thrown when there is problem decoding a web service response data.
 */
public class SnowException extends IOException {

  private static final long serialVersionUID = 9061573802816807817L;

  public SnowException(final Exception cause, final String response) {
    super(response , cause);
  }

  public SnowException(final String message, final String response) {
    super(response == null ? message  : message +"\n" + response);
  }

}
