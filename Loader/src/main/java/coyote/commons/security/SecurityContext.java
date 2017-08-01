/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 *
 */

package coyote.commons.security;

/**
 * The SecurityContext class models a logical grouping of roles and logins
 * each with a set of permissions.
 *
 * <p>Roles are named group of permissions.
 *
 * <p>Logins are a list of roles and additional permissions for that login.
 *
 * <p>Security contexts allow an application to obtain Logins by querying for
 * CredentialSet. Those Logins contain Permissions and links to Roles against
 * which actions against which targets can be checked.
 *
 * <p>It is possible for each service to have it's own security context. This
 * means each service can have its own unique combination of roles and
 * permissions.
 *
 * <p>All a component need do is to use a credential set to locate a login in
 * its context. Once the login is retrieved, the principal associated to that
 * login can be ascertained. The login also contains the set of associated
 * roles and permissions can be checked for authorization.
 */
public interface SecurityContext {

  /**
   * Add the given login to this context.
   *
   * @param login The login to add.
   */
  public void add(Login login);




  /**
   * Add the given role to the context.
   *
   * <p>All roles have a name, and if the given role has a name which already
   * exists in the context, the existing context will be over written.
   *
   * @param role The role to add.
   */
  public void add(Role role);




  /**
   * Check to see if the given login has the given named permissions.
   *
   * <p><strong>Side Effect:</strong> If the login passed is a shallow object
   * (no Role or Permission references populated) it will have its values
   * populated with the appropriate security references for this security
   * context. This implies that a performance hit is taken on first calling
   * this method with a shallow object but subsequent calls will check
   * permissions based on the populated values.
   *
   * <p>This is a basic authorization check. The underlying implementation can
   * choose to perform the check using a variety of strategies including, but
   * not limited to, Role Based Access Control (RBAC) or individualized
   * permissions. The implementation may also support the concept of revocation
   * where the role grants permissions, but the login contains permissions
   * which revoke specific permissions.
   *
   * <p>HINT: Permissions can be AND'ed to create more specific checks and
   * OR'ed to create more broad checks as permissions are essentially bit
   * flags.
   *
   * @param login The login to check
   * @param perms The permission flags to check
   * @param name The name of the permission
   *
   * @return True if the given login has the given permissions, false otherwise.
   *
   * @see #getLogin(String, CredentialSet)
   * @see #getLoginByName(String)
   * @see #getLoginBySession(String)
   */
  public boolean allows(Login login, long perms, String name);




  public Session createSession(Login login);




  /**
   * Create a new session with the given identifier for the given login.
   *
   * @param id The identifier of this session
   * @param login THe login to be associated to this session
   *
   * @return the session created for this login.
   */
  public Session createSession(String id, Login login);




  /**
   * Retrieve a shallow login using the given login name and credential set for
   * authentication - authorization references are NOT populated.
   *
   * <p>Shallow logins are objects without all of its references to other
   * objects populated. This keeps retrieval fast and light. In this case, the
   * login retrieved only contains a security principal and credential set.
   * Roles and Permissions are not retrieved as these may not be needed and
   * their retrieval and population can slow the system down unnecessarily
   * since many components only want authentication and do not need
   * authorization capabilities.
   *
   * <p>This method centralizes all credential matching login in one location
   * to assure uniform credential matching throughout the context. If all the
   * given credentials do not match the login contained in the context, no
   * login is returned. Partial matches do not count.
   *
   * <p>The given name is assumed to be unique in the context. The login name
   * is normally an email address but can be any string convenient for the
   * user. Because the Context itself is named, it is possible to have many
   * security contexts in a system. This allows for a multi-tenant security
   * context with a composite key of context and login name.
   *
   * <p>This method uses the given credentials to find the associated login.
   * All the credentials much match, so in cases of multi-factor authentication
   * several credentials may be passed to this method to return the appropriate
   * login.
   *
   * <p>It is recommended that context implementations reject null or empty
   * credentials as a security measure.
   *
   * @param name the login name to retrieve
   * @param credentialSet The set of credentials which must match.
   *
   * @return The named login which has a match to the all the given credentials
   * or null if not found or a partial match is found.
   */
  public Login getLogin(String name, CredentialSet credentialSet);




  /**
   * Retrieve a shallow login using the given login name - authorization
   * references are NOT populated.
   *
   * <p>Shallow logins are objects without all of its references to other
   * objects populated. This keeps retrieval fast and light. In this case, the
   * login retrieved only contains a security principal and credential set.
   * Roles and Permissions are not retrieved as these may not be needed and
   * their retrieval and population can slow the system down unnecessarily
   * since many components only want authentication and do not need
   * authorization capabilities.
   *
   * <p>The given name is assumed to be unique in the context. The login name
   * is normally an email address but can be any string convenient for the
   * user. Because the Context itself is named, it is possible to have many
   * security contexts in a system. This allows for a multi-tenant security
   * context with a composite key of context and login name.
   *
   * @param name the login name to retrieve
   *
   * @return The login which has a match to the given credentials
   */
  public Login getLoginByName(String name);




  /**
   * Retrieve a login using the given session identifier.
   *
   * <p>Not all contexts are required to support sessions. Even if they do, the
   * identifier of the session may not be static; they may change frequently as
   * in the case of a session nonce.
   *
   * <p>The login returned may be a shallow reference; and not contain any
   * Roles or Permissions as this would require additional resources to
   * retrieve which is oftentimes unnecessary. The values are only populated
   * when a call to check authorization is made and roles and permissions are
   * needed for that operation. Authorization references (roles and
   * permissions) are usually only retrieve on authorization checks (see the
   * {@code allows} method).
   *
   * @param sessionId the identifier of the session to retrieve
   *
   * @return The session in this context with the given identifier or null if
   * no session could be found.
   *
   * @see #allows(Login, long, String)
   */
  public Login getLoginBySession(String sessionId);




  /**
   * Retrieve the name of this security context.
   *
   * <p>This name is assigned through the context's constructor and is used to
   * address this context in the system. It is therefore assumed that the name
   * is unique to the system.
   *
   * @return The name assigned to this security context.
   */
  public String getName();




  /**
   * Retrieve the role with the given name
   *
   * @param name Name of the role to retrieve
   *
   * @return The role in the context with the given name or null if there is no
   * role in the context with the given name.
   */
  public Role getRole(String name);




  /**
   * Retrieve a session based on its associated login.
   *
   * @param login the login to which the session is associated.
   *
   * @return the session for the given login or null if the login does not
   * have a session in this context.
   */
  public Session getSession(Login login);




  /**
   * Retrieve a session by it's identifier.
   *
   * @param sessionId the identifier of the session to retrieve.
   *
   * @return the session with the given identifier or null if the context
   * does not have a session with that identifier.
   */
  public Session getSession(String sessionId);

}
