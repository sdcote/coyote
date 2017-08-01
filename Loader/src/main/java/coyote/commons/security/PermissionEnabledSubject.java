/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * This class represents a security subject to which permissions can be
 * assigned.
 *
 * <p>This is intended to be the base class for both the Role class which
 * represents a group of security subjects and the Login, which represents an
 * individual security subject. This allows permissions to be assigned at
 * both the group and individual levels.
 */
public class PermissionEnabledSubject {
  private final HashMap<String, Permission> permissions = new HashMap<String, Permission>();




  public void addPermission(final Permission perm) {
    if (perm != null) {
      final Permission p = permissions.get(perm.getTarget());
      if (p != null) {
        p.addAction(perm.getAction());
      } else {
        permissions.put(perm.getTarget(), perm);
      }
    }
  }




  public void addPermission(final String target, final long action) {
    addPermission(new Permission(target, action));
  }




  public void addPermissions(final List<Permission> permissions) {
    if (permissions != null) {
      for (final Permission permission : permissions) {
        addPermission(permission);
      }
    }
  }




  /**
   * Check to see if the given target allows the given action in this role.
   *
   * @param target The target of the permission.
   * @param action The action being checked.
   *
   * @return True if the role allows the action on the target, false otherwise.
   */
  public boolean allows(final String target, final long action) {
    final Permission perm = permissions.get(target);
    if ((perm != null) && perm.allows(action)) {
      return true;
    }

    return false;
  }




  /**
   * @return list of permissions in this subject.
   */
  public List<Permission> getPermissions() {
    return new ArrayList<Permission>(permissions.values());
  }




  public boolean hasPermissions() {
    return permissions.size() > 0;
  }




  public void revokePermission(final Permission perm) {
    if (perm != null) {
      final Permission p = permissions.get(perm.getTarget());
      if (p != null) {
        p.revokeAction(perm.getAction());
      }
    }
  }




  public void revokePermission(final String target, final long action) {
    revokePermission(new Permission(target, action));
  }




  public void revokePermissions(final List<Permission> permissions) {
    if (permissions != null) {
      for (final Permission permission : permissions) {
        revokePermission(permission);
      }
    }
  }

}
