/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.security;

import java.security.Principal;


/**
 * The Role class models a named collection of permissions.
 *
 * <p>Permissions are assigned to roles and permission checks are performed
 * against roles as opposed to individuals to keep the number of permission
 * collections to a minimum. This makes permission management easier.
 *
 * <p>In some simplified security schemes, components only check for the role
 * to grant access to resources. For example, if the 'admin' role is presented,
 * the component may allow access to all its resources. Just the use of
 * resources can implement a RBAC security model.
 *
 * <p>In more complex security models, roles are not as important as are the
 * permissions assigned to them. In these cases, components aggregate all the
 * permissions from all the roles presented to determine access to its
 * resources. This approach then uses roles as collections of permissions and
 * use roles as a way to better manage large collections of permissions.
 */
public final class Role extends PermissionEnabledSubject implements Principal {
  private String _desc = null;
  private String _name = null;




  public Role(final String name) {
    _name = name;
  }




  public Role(final String name, final String description) {
    _name = name;
    _desc = description;
  }




  /**
   * @return the description of the role.
   */
  public String getDescription() {
    return _desc;
  }




  @Override
  public String getName() {
    return _name;
  }




  /**
   * Set the description of the role.
   *
   * @param _desc the description to set
   */
  public void setDescription(final String _desc) {
    this._desc = _desc;
  }

}
