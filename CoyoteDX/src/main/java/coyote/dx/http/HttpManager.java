/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http;

import java.io.IOException;


/**
 * 
 */
public interface HttpManager {

  /**
   * @param socketReadTimeout
   * @param b
   */
  void start(int socketReadTimeout, boolean b) throws IOException;




  /**
   * @return the IP port on which the server is listening
   */
  public int getPort();




  /**
   * 
   */
  void stop();

}
