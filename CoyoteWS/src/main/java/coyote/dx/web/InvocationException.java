package coyote.dx.web;

import java.io.IOException;


/**
 * Exception thrown when problems occurred on the remote system.
 */
public class InvocationException extends IOException {

  private static final long serialVersionUID = -3077030762221323062L;




  public InvocationException( final String message ) {
    super( message );
  }




  public InvocationException( final String message, final Exception cause ) {
    super( message, cause );
  }

}
