/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.security;

/**
 *
 */
public interface Session {

  /**
   * @return return the unique identifier for this session.
   */
  public String getId();




  /**
   * @return the Login associated with this session.
   */
  public Login getLogin();




  /**
   * Set the identifier on the session.
   *
   * @param id The identifier to set.
   */
  public void setId(String id);




  /**
   * Associate the given login with this session.
   *
   * @param login The login associated to this session.
   */
  public void setLogin(Login login);

}
