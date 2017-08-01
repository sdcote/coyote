/*
 * Copyright (c) 2002 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http;

/**
 * HTTP response status code and description
 */
public interface IStatus {

  /**
   * The description of the status.
   * 
   * @return human readable description of the status
   */
  String getDescription();




  /**
   * Status code.
   * 
   * @return numeric 3-digit code representing the status.
   */
  int getRequestStatus();
  
}