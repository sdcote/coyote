/*
 * $Id:$
 *
 * Copyright Stephan D. Cote' 2008 - All rights reserved.
 */
package coyote.loader.cfg;

/**
 * The exception thrown when there are problems with configuration parameters
 */
public class ConfigurationException extends Exception {

  /**
   *
   */
  private static final long serialVersionUID = 229675499440617423L;




  /**
   *
   */
  public ConfigurationException() {
    super();
  }




  /**
   * @param message
   */
  public ConfigurationException( final String message ) {
    super( message );
  }




  /**
   * @param message
   * @param newNested
   */
  public ConfigurationException( final String message, final Throwable newNested ) {
    super( message, newNested );
  }




  /**
   * @param newNested
   */
  public ConfigurationException( final Throwable newNested ) {
    super( newNested );
  }

}
