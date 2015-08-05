/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch;

/**
 * 
 */
public class ConfigurationException extends Exception {

  private static final long serialVersionUID = 8859362730899865638L;




  public ConfigurationException( String msg ) {
    super( msg );
  }




  public ConfigurationException( String msg, Exception e ) {
    super( msg, e );
  }




  public ConfigurationException( Exception e ) {
    super( e );
  }

}
