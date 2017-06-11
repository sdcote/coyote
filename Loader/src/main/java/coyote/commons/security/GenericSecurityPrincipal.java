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

import java.util.UUID;


public class GenericSecurityPrincipal extends SecurityPrincipal {

  private String name = null;
  private String id = null;




  /**
   * Creates and "anonymous" principal with a random identifier.
   */
  public GenericSecurityPrincipal() {
    this( UUID.randomUUID().toString(), "ANONYMOUS" );
  }




  /**
   * Create a security principal with the given name.
   * 
   * @param name a name suitable for display to the principal this object represents.
   */
  public GenericSecurityPrincipal( String name ) {
    this( null, name );
  }




  /**
   * Create a security principal with the given identifier and name.
   * 
   * @param id an identifier unique within the security context.
   * @param name a name suitable for display to the principal this object represents.
   */
  public GenericSecurityPrincipal( String id, String name ) {
    this.setId( id );
    this.name = name;
  }




  @Override
  public String getName() {
    return name;
  }




  @Override
  public void setName( String name ) {
    this.name = name;
  }




  @Override
  public String getId() {
    return id;
  }




  @Override
  public void setId( String id ) {
    this.id = id;
  }

}
