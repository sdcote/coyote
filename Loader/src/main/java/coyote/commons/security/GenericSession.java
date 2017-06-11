/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons.security;

/**
 * 
 */
public class GenericSession implements Session {
  private Login login = null;
  private String identifier = null;




  /**
   * @see coyote.commons.security.Session#setLogin(coyote.commons.security.Login)
   */
  @Override
  public void setLogin( Login login ) {
    this.login = login;
  }




  /**
   * @see coyote.commons.security.Session#getLogin()
   */
  @Override
  public Login getLogin() {
    return login;
  }




  @Override
  public String getId() {
    return identifier;
  }




  @Override
  public void setId( String id ) {
    identifier = id;
  }

}
