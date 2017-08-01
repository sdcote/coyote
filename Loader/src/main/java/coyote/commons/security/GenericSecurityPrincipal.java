/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.security;

import java.util.UUID;


public class GenericSecurityPrincipal extends SecurityPrincipal {

  private String id = null;
  private String name = null;




  /**
   * Creates and "anonymous" principal with a random identifier.
   */
  public GenericSecurityPrincipal() {
    this(UUID.randomUUID().toString(), "ANONYMOUS");
  }




  /**
   * Create a security principal with the given name.
   *
   * @param name a name suitable for display to the principal this object represents.
   */
  public GenericSecurityPrincipal(final String name) {
    this(null, name);
  }




  /**
   * Create a security principal with the given identifier and name.
   *
   * @param id an identifier unique within the security context.
   * @param name a name suitable for display to the principal this object represents.
   */
  public GenericSecurityPrincipal(final String id, final String name) {
    setId(id);
    this.name = name;
  }




  @Override
  public String getId() {
    return id;
  }




  @Override
  public String getName() {
    return name;
  }




  @Override
  public void setId(final String id) {
    this.id = id;
  }




  @Override
  public void setName(final String name) {
    this.name = name;
  }

}
