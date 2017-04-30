/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.ftp;

/**
 * Contract for classes responsible for determining if a Remote File matches a 
 * set of acceptance criteria. 
 */
public interface FileFilter {

  /**
   * Check to see if the given remote file matched the acceptance criteria.
   *  
   * @param file The file to check
   * 
   * @return true if the file matches all this filter acceptance criteria, 
   *         false otherwise.
   */
  public boolean accept( final RemoteFile file );

}
