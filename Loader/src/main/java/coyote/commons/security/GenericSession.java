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
public class GenericSession implements Session {
  private String identifier = null;
  private Login login = null;




  @Override
  public String getId() {
    return identifier;
  }




  /**
   * @see coyote.commons.security.Session#getLogin()
   */
  @Override
  public Login getLogin() {
    return login;
  }




  @Override
  public void setId(final String id) {
    identifier = id;
  }




  /**
   * @see coyote.commons.security.Session#setLogin(coyote.commons.security.Login)
   */
  @Override
  public void setLogin(final Login login) {
    this.login = login;
  }

}
