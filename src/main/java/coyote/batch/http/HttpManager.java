/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.http;

import java.io.IOException;


/**
 * 
 */
public interface HttpManager {

  /**
   * @param socketReadTimeout
   * @param b
   */
  void start( int socketReadTimeout, boolean b ) throws IOException;




  /**
   * @return
   */
  public int getPort();




  /**
   * 
   */
  void stop();

}
