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

/**
 * The Permission class models a set of allowable actions to be performed on a 
 * named target.
 * 
 * <p>Target names are meaningful only to an application and can represent any
 * object instance or logical construct. This design allows a generic 
 * permission object to be applied to anything and not require specialized 
 * permission classes like FilePermission or SocketPermission.
 * 
 * <p>The action of a permission is a set of standard actions that can be 
 * performed on a variety of objects. Each action is represented by a single 
 * bit in a field of bits and checking an action or set of actions is extremely 
 * fast as action masks are ORed as 64-bit fields. This far out-performs many
 * permission checking implementations that rely on if-else logic or create
 * collections of permissions against which to check.
 * 
 * <p>Extending this class, it is possible to create additional permissions by 
 * assigning additional 
 */
public final class Permission {
  public static final long ALL = -1;
  public static final long NONE = 0;
  public static final long CREATE = 1 << 0;
  public static final long READ = 1 << 1;
  public static final long UPDATE = 1 << 2;
  public static final long DELETE = 1 << 3;
  public static final long EXECUTE = 1 << 4;
  public static final long ACCEPT = 1 << 5;
  public static final long CONNECT = 1 << 6;
  public static final long LISTEN = 1 << 7;
  public static final long ASSIGN = 1 << 8;
  public static final long RESOLVE = 1 << 9;
  public static final long CLOSE = 1 << 10;
  public static final long OPEN = 1 << 12;
  public static final long OWN = 1 << 13;
  public static final long SEE = 1 << 14; // you know it exists, but cannot read its details
  public static final long GRANT = 1 << 15;
  public static final long REVOKE = 1 << 16;

  // There is room for lots more custom permissions which can be represented thusly
  // public static final long FOO = 1 << 23;
  // public static final long BAR = 1 << 24;
  // public static final long BAZ = 1 << 25;
  // ... all the way to 1 << 63... 1<<64 is  -1 and used for ALL above 

  private String _target = null;
  private long _action = NONE;




  /**
   * Construct a permission with the given target and action mask.
   * 
   * @param target The name of the target of the permission.
   * @param action A mask of bit flags indicating the actions to allow.
   */
  public Permission( String target, long action ) {
    if ( target == null || target.length() == 0 )
      throw new IllegalArgumentException( "Null or empty target passed to constructor" );

    _target = target;
    _action = action;
  }




  /**
   * Add the given action mask to the permission.
   * 
   * <p>Has package access so other security objects may perform this function 
   * but outside objects must go through the security package.
   * 
   * @param action the action(s) to add to this mask.
   */
  void addAction( long action ) {
    _action |= action;
  }




  /**
   * Remove the given action from the permission.
   * 
   * <p>Has package access so other security objects may perform this function 
   * but outside objects must go through the security package.
   * 
   * @param action the action(s) to remove from this mask.
   */
  void revokeAction( long action ) {
    _action = ~( ~( _action ) | action );
  }




  /**
   * @return The name of the target of this permission.
   */
  public String getTarget() {
    return _target;
  }




  /**
   * @return a mask of all the permissions allowed.
   */
  public long getAction() {
    return _action;
  }




  /**
   * Check to see if this permission allows the given action(s).
   * 
   * <p>The given action may represent a single or a mask of several actions.
   * For example passing the value of 3 (binary 11) represents both the CREATE 
   * and READ actions. If this permission allows both actions, then the result 
   * of true will be returned. If either CREATE(01) or READ (10) are not set in 
   * this permission, then a value of false will be returned.
   * 
   * <p>Suppose a request to write to a TCP socket is requested. Calling this 
   * method once with a check on the permissions of OPEN, WRITE and CLOSE may 
   * be performed before the entire operation is started, as even if the OPEN 
   * and WRITE permissions are allowed, the logic would disallow the closing of 
   * the TCP socket causing problems later. Also, checking once saves the other 
   * two calls later.
   * 
   * @param action The mask of actions to check this permission against.
   * 
   * @return True if <em>all</em> the actions represented in the argument are 
   *         allowed, false is one or more actions are not allowed.
   */
  public boolean allows( long action ) {
    return ( ( _action & action ) != (long)0 );
  }




  public String toString() {
    return _target + ":" + Long.toBinaryString( _action );
  }

}
