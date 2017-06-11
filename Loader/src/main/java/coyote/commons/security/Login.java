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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 * This class models a list of roles and their associated permissions with a 
 * security principal and a set of credentials.
 * 
 * <p>A single security principal can have many different logins, each with a 
 * different set of credentials and a different set of roles. These 
 * associations of principal to roles are maintained by the application and 
 * modeled by this class.
 * 
 * <p>Each login has one or more credentials to perform authentication by the
 * application. The set of roles in this class assists the application perform 
 * role based authorization. The identifier of this class allows the 
 * application to perform auditing by tagging this identifier to all 
 * operational records and transactions. Authorization, Authentication and 
 * Auditing are the foundation of all secure applications. 
 */
public class Login extends PermissionEnabledSubject {

  /** This logins identifier */
  String id;

  /** The credentials used to authenticate this login */
  CredentialSet credentials;

  /** The principal (entity) of this login. */
  SecurityPrincipal principal;

  /** A map of role names this login assumes. */
  Hashtable<String, Role> roles = new Hashtable<String, Role>();




  /**
   * Constructs a Login with a security principal with the given name and a 
   * credential set with the given password.
   * 
   * <p>The password is saved as a single round MD5 hash of its UTF8 encoding. 
   * This is to help ensure that the password is not stored in an easily 
   * retrievable format. This implies that the clear text password is not used 
   * in the system for authentication and that if the password is exposed by 
   * the system, the viewer of the password value will not have the original 
   * password provided by the user.  
   * 
   * @param name name of the security principal (i.e. username)
   * @param password authentication credential
   */
  public Login( String name, String password ) {
    principal = new GenericSecurityPrincipal( name );
    credentials = new CredentialSet( CredentialSet.PASSWORD, password, 1 );
  }




  /**
   * Construct a login with the given security principal and credential set.
   * 
   * @param principal The principal (entity) of this login.
   * @param creds The credentials used to authenticate this login
   */
  public Login( SecurityPrincipal principal, CredentialSet creds ) {
    this.principal = principal;
    credentials = creds;
  }




  /**
   * Constructor Login
   */
  public Login() {}




  /**
   * Add a role name to this login.
   * 
   * @param role The name of the role to add.
   */
  public void addRole( Role role ) {
    if ( role != null ) {
      roles.put( role.getName(), role );
    }
  }




  /**
   * Add the given list of roles to this login.
   * 
   * <p>Duplicates are ignored.
   * 
   * @param roles The list of roles to add
   */
  public void addRoles( List<Role> roles ) {
    if ( roles != null ) {
      for ( Role role : roles ) {
        addRole( role );
      }
    }
  }




  /**
   * @return true if there is at least one role assigned to this login, false 
   * if there are no roles assigned.
   */
  public boolean hasRoles() {
    return roles.size() > 0;
  }




  /**
   * Remove the named role from the login.
   * 
   * @param name The name of the role to remove
   * 
   * @return the role removed, null if no role was found with that name.
   */
  public Role removeRole( String name ) {
    try {
      return roles.remove( name );
    } catch ( Exception e ) {
      return null;
    }
  }




  /**
  * @see java.lang.Object#toString()
  */
  @Override
  public String toString() {
    StringBuilder b = new StringBuilder( "Login: Principal=" );
    if ( principal != null ) {
      if ( principal.getName() != null )
        b.append( principal.getName() );
      else
        b.append( "null" );
    } else {
      b.append( "NULL" );
    }

    b.append( " Creds:" );
    b.append( credentials.size() );

    return b.toString();
  }




  /**
   * Test to see if all the given credentials match what is recorded in in 
   * this login.
   * 
   * <p>The most common scenario is two credentials being passed to this method
   * for matching; username and password. It is therefore important to match 
   * both credentials.
   * 
   * <p>Other scenarios involve multi-factor authentication with password and 
   * some other credential passed such as a biometric digest, or challenge 
   * response. The more credentials passed and matched, the higher the 
   * confidence of the authentication operation.
   * 
   * <p>While this login may have dozens of credentials, the given credentials 
   * are expected to be a subset, maybe even one credential. If all the given 
   * credentials match, return true. If even one of the given credentials fail 
   * to match, then return false.
   * 
   * @param creds The set of credentials to match.
   * 
   * @return True if all the given credentials match, false otherwise.
   */
  public boolean matchCredentials( CredentialSet creds ) {
    if ( creds != null ) {
      return credentials.matchAll( creds );
    } else {
      return false;
    }
  }




  /**
   * @return a list of role names to which this login belongs.
   */
  public List<String> getRoles() {
    return new ArrayList<String>( roles.keySet() );
  }




  /**
   * @return the identifier for this login
   */
  public String getId() {
    return id;
  }




  /**
   * Set the identifier for this login
   * 
   * @param id the identifier unique to the security context
   */
  public void setId( String id ) {
    this.id = id;
  }




  /**
   * @return The principal associated to this login
   */
  public SecurityPrincipal getPrincipal() {
    return principal;
  }




  /**
   * Set the security principal associated to this login.
   * 
   * @param principal The principal associated with this login.
   */
  public void setPrincipal( SecurityPrincipal principal ) {
    this.principal = principal;
  }




  /**
   * @return the credentials for this login
   */
  public CredentialSet getCredentials() {
    return credentials;
  }




  /**
   * Set (replace) the credentials with this set of credentials.
   * 
   * @param credentialset the credentials to set
   */
  public void setCredentials( CredentialSet credentialset ) {
    this.credentials = credentialset;
  }

}