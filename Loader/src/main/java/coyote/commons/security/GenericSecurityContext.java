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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import coyote.commons.StringUtil;


/**
 * The GenericSecurityContext class models a named collection of roles, permissions and 
 * login memberships.
 * 
 * <p>This version of the context is a fully functional security context which
 * can be used in a variety of applications.</p>
 */
public class GenericSecurityContext implements SecurityContext {
  private String _name = null;

  private Map<String, Session> sessions = new Hashtable<String, Session>();
  private List<Login> logins = new ArrayList<Login>();
  private Map<String, Role> roles = new Hashtable<String, Role>();




  public GenericSecurityContext() {
    _name = UUID.randomUUID().toString();
  }




  public GenericSecurityContext( String name ) {
    _name = name;
  }




  /**
   * @see coyote.commons.security.SecurityContext#getLogin(java.lang.String, coyote.commons.security.CredentialSet)
   */
  @Override
  public Login getLogin( String name, CredentialSet creds ) {

    if ( name != null && creds != null ) {
      for ( Login login : logins ) {
        // if the name of the login principal matches the requested name
        if ( login.getPrincipal() != null && name.equals( login.getPrincipal().getName() ) ) {
          // check to see if all the credentials match...the all have to match
          if ( login.matchCredentials( creds ) ) {
            return login;
          }
        } else {
          // wrong credentials for the found security principal
          return null;
        }
      }
    }

    // no login found with the security principal with that name
    return null;
  }




  /**
   * @see coyote.commons.security.SecurityContext#add(coyote.commons.security.Login)
   */
  public void add( Login login ) {
    // if the login has a name and is not currently found in the context...
    if ( login != null && login.getPrincipal() != null && login.getPrincipal().getName() != null && getLoginByName( login.getPrincipal().getName() ) == null ) {
      logins.add( login ); // ...add the login
    }
  }




  /**
   * @see coyote.commons.security.SecurityContext#add(coyote.commons.security.Role)
   */
  public void add( Role role ) {
    if ( role != null && StringUtil.isNotBlank( role.getName() ) )
      roles.put( role.getName(), role );
  }




  /**
   * @see coyote.commons.security.SecurityContext#getLoginBySession(java.lang.String)
   */
  @Override
  public Login getLoginBySession( String sessionId ) {
    Session session = sessions.get( sessionId );
    if ( session != null ) {
      return session.getLogin();
    }
    return null;
  }




  /**
   * @see coyote.commons.security.SecurityContext#createSession(coyote.commons.security.Login)
   */
  @Override
  public Session createSession( Login login ) {
    return createSession( UUID.randomUUID().toString(), login );
  }




  /**
   * @see coyote.commons.security.SecurityContext#createSession(java.lang.String,coyote.commons.security.Login)
   */
  @Override
  public Session createSession( String id, Login login ) {
    Session retval = new GenericSession();
    retval.setLogin( login );
    retval.setId( id );
    sessions.put( retval.getId(), retval );
    return retval;
  }




  /**
   * @see coyote.commons.security.SecurityContext#getRole(java.lang.String)
   */
  @Override
  public Role getRole( String name ) {
    Role retval = null;
    if ( StringUtil.isNotBlank( name ) ) {
      retval = roles.get( name );
    }
    return retval;
  }




  /**
   * @see coyote.commons.security.SecurityContext#allows(coyote.commons.security.Login, long, java.lang.String)
   */
  @Override
  public boolean allows( Login login, long perms, String name ) {

    boolean retval = false;

    if ( login != null ) {
      // for each role in the login
      List<String> roles = login.getRoles();
      Role role = null;
      for ( String roleName : roles ) {
        role = getRole( roleName );
        if ( role != null && role.allows( name, perms ) ) {
          retval = true;
          break;
        }
      }

      // TODO: Check the login for specific permissions

      // TODO: Now check for revocations at the login level
    }
    return retval;
  }




  @Override
  public Session getSession( String sessionId ) {
    return sessions.get( sessionId );
  }




  @Override
  public Session getSession( Login login ) {
    if ( login != null ) {
      for ( Iterator<Map.Entry<String, Session>> it = sessions.entrySet().iterator(); it.hasNext(); ) {
        Entry<String, Session> entry = it.next();
        if ( login == entry.getValue().getLogin() ) {
          return entry.getValue();
        }
      } // for
    } // login ! null
    return null;
  }




  /**
   * @see coyote.commons.security.SecurityContext#getName()
   */
  @Override
  public String getName() {
    return _name;
  }




  /**
   * @see coyote.commons.security.SecurityContext#getLoginByName(java.lang.String)
   */
  @Override
  public Login getLoginByName( String name ) {
    if ( name != null ) {
      // for each login see if there is a login with the given name
      for ( Login login : logins ) {
        if ( login.getPrincipal() != null && name.equals( login.getPrincipal().getName() ) ) {
          return login;
        }
      }
    }
    return null;
  }

}
