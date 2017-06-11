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
  private HashMap<String, Permission> permissions = new HashMap<String, Permission>();




  public void addPermission( String target, long action ) {
    addPermission( new Permission( target, action ) );
  }




  public void addPermission( Permission perm ) {
    if ( perm != null ) {
      Permission p = permissions.get( perm.getTarget() );
      if ( p != null ) {
        p.addAction( perm.getAction() );
      } else {
        permissions.put( perm.getTarget(), perm );
      }
    }
  }




  public void addPermissions( List<Permission> permissions ) {
    if ( permissions != null ) {
      for ( Permission permission : permissions ) {
        addPermission( permission );
      }
    }
  }




  public void revokePermissions( List<Permission> permissions ) {
    if ( permissions != null ) {
      for ( Permission permission : permissions ) {
        revokePermission( permission );
      }
    }
  }




  public void revokePermission( String target, long action ) {
    revokePermission( new Permission( target, action ) );
  }




  public void revokePermission( Permission perm ) {
    if ( perm != null ) {
      Permission p = permissions.get( perm.getTarget() );
      if ( p != null ) {
        p.revokeAction( perm.getAction() );
      }
    }
  }




  public boolean hasPermissions() {
    return permissions.size() > 0;
  }




  /**
   * Check to see if the given target allows the given action in this role.
   * 
   * @param target The target of the permission.
   * @param action The action being checked.
   * 
   * @return True if the role allows the action on the target, false otherwise.
   */
  public boolean allows( String target, long action ) {
    Permission perm = permissions.get( target );
    if ( perm != null && perm.allows( action ) )
      return true;

    return false;
  }




  /**
   * @return list of permissions in this subject.
   */
  public List<Permission> getPermissions() {
    return new ArrayList<Permission>( permissions.values() );
  }
  
}
