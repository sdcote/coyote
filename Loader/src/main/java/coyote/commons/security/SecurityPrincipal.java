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

import java.security.Principal;


public abstract class SecurityPrincipal implements Principal {

  public SecurityPrincipal() {
  }




  /**
   * @param name a name suitable for display to the principal this object represents.
   */
  public abstract void setName( String name );




  /**
   * @return the identifier for this principal
   */
  public abstract String getId();




  /**
   * @param id an identifier unique within the security context
   */
  public abstract void setId( String id );

}
