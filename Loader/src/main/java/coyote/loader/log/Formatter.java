/*
 * Copyright (c) 2007 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.loader.log;

/**
 * The Formatter class models object which creates strings suitable for entries
 * in logs.
 */
public interface Formatter {

  /**
   * Format the given object into a string based upon the given category.
   *
   * @param event The object to format into a string.
   * @param category The category of the event to be used in optional condition
   *        formatting.
   * @param cause The exception that caused the log entry. Can be null.
   *
   * @return String representation of the event as it will be written to the 
   *         log stream.
   */
  public String format( Object event, String category, Throwable cause );




  /**
   * Setup the formatter.
   *
   * <p>This method will return a series of bytes that should be placed ath the 
   * beginning of a log stream. For example, an XML formatter may return a 
   * header and a root node to the caller to be placed at the beginning of the 
   * log stream to ensure a valid XML document.</p>
   *  
   * @return bytes to be placed in the beginning of a log stream
   */
  public byte[] initialize();




  /**
   * Terminate the formatter.
   *
   * <p>This method will return a series of bytes that should be placed at the 
   * end of a log stream. For example, a HTML formatter may return a footer and 
   * closing tags at the end of the log stream to ensure a complete and valid
   * document.</p>
   *  
   * @return bytes to be placed in the end of a log stream
   */
  public byte[] terminate();

}