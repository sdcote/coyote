/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.mbus.network;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.StringTokenizer;

import coyote.commons.network.IpAddress;
import coyote.commons.network.IpAddressException;
import coyote.commons.network.IpNetwork;


/**
 * Holder for an Access Control list.
 */
public class IpAcl {
  /** The string representing the ALLOW state. */
  public static final String ALLOW_TAG = "ALLOW";

  /** The string representing the DENY state. */
  public static final String DENY_TAG = "DENY";

  /** The string representing the DEFAULT rule. */
  public static final String DEFAULT_TAG = "DEFAULT";

  /** The string ";" that delimits ACL rules. */
  private static final String RULE_DELIM = ";";

  /** The string ":" that delimits ACL rule tokens. */
  private static final String TOKEN_DELIM = ":";

  /** static to be used when adding an ALLOW rule to the ACL. */
  public static final boolean ALLOW = true;

  /** static to be used when adding a DENY rule to the ACL. */
  public static final boolean DENY = false;

  /** default setting for the default mode for a new ACL. */
  public static final boolean DEFAULT_MODE = IpAcl.ALLOW;

  /** The default mode of this ACL. */
  private boolean defaultAllow = IpAcl.DEFAULT_MODE;

  /** This is the friendly identifier of this component */
  private final String name = null;

  /** The ACL is stored in this ArrayList. */
  private final ArrayList<ACLRule> acl = new ArrayList<ACLRule>();




  /**
   * Construct a new Access Control List.
   *
   * <p>The default mode is to ALLOW anything that isn't explicitly blocked by
   * a rule.
   */
  public IpAcl() {
    this( IpAcl.DEFAULT_MODE );
  }




  /**
   * Construct a new Access Control List with a given default mode.
   *
   * <p>This mode specifies what should happen if a check does not match any
   * rules.
   *
   * @param defaultMode the default mode for non-matched checks
   */
  public IpAcl( final boolean defaultMode ) {
    setDefaultAllow( defaultMode );
  }




  /**
   * Add some new rules to the Access Control List in the form of a String.
   *
   * <p>The String should be of the following format:
   * <pre>
   *     network:allowed;network:allowed;network:allowed...
   * </pre>
   * Where "network" is a CIDR representation of a network against which a
   * match is to be made, and "allowed" is either 'ALLOW' or 'DENY'. There is a
   * special expression of 'DEFAULT' which represents the default rule (what
   * should  happen if no expression is matched when performing a check).
   *
   * <p>Examples include:
   * <pre>
   *     192.168/16:ALLOW;150.159/16:DENY;DEFAULT:DENY
   * </pre>
   *
   * Where everything coming from the 192.168.0.0/255.255.0.0 network is
   * allowed, 150.159.0.0/255.255.0.0 is denied and everything else is
   * denied.
   *
   * @param rules A semicolon delimited list of rules
   */
  public void parse( final String rules ) {
    if ( acl != null ) {
      // split the String into expression:rule parts
      final StringTokenizer st1 = new StringTokenizer( rules, IpAcl.RULE_DELIM );

      while ( st1.hasMoreTokens() ) {
        final String token1 = st1.nextToken();

        // if it doesn't have a :, it's not the correct format
        if ( token1.indexOf( IpAcl.TOKEN_DELIM ) != -1 ) {
          // split into expression and rule part
          final StringTokenizer st2 = new StringTokenizer( token1, IpAcl.TOKEN_DELIM );
          String network = "";
          String rule = "";

          if ( st2.hasMoreTokens() ) {
            network = st2.nextToken();
          } else {
            // mall-formed?
            continue;
          }

          if ( st2.hasMoreTokens() ) {
            rule = st2.nextToken();
          } else {
            // mall-formed?
            continue;
          }

          // check to see what sort of rule
          if ( rule.equalsIgnoreCase( IpAcl.ALLOW_TAG ) ) {
            // case for special 'DEFAULT' expression
            if ( network.equalsIgnoreCase( IpAcl.DEFAULT_TAG ) ) {
              setDefaultAllow( IpAcl.ALLOW );
            } else {
              try {
                add( network, IpAcl.ALLOW );
              } catch ( final Exception ipe ) {
                throw new IllegalArgumentException( "Malformed network specification (" + network + ")" );
              }
            }
          } else if ( rule.equalsIgnoreCase( IpAcl.DENY_TAG ) ) {
            // case for special 'DEFAULT' expression
            if ( network.equals( IpAcl.DEFAULT_TAG ) ) {
              setDefaultAllow( IpAcl.DENY );
            } else {
              try {
                add( network, IpAcl.DENY );
              } catch ( final Exception ipe ) {
                throw new IllegalArgumentException( "Malformed network specification (" + network + ")" );
              }
            }
          } else {
            // if it's not ALLOW or DENY, it's not a proper rule
            throw new IllegalArgumentException( "Invalid access specification (" + rule + ")" );
          }
        }
      }
    }
  }




  /**
   * Changes the default allow mode of the ACL. <p>This is what the check will return if it does not find an explict rule to match against.
   * @param allow  The new default mode: True = allow by default, false = deny by default.
   */
  public synchronized void setDefaultAllow( final boolean allow ) {
    defaultAllow = allow;
  }




  /**
   * Add a network to the list with an allowance flag.
   *
   * @param network the network specification to add (e.g. "192.168/16","10/8")
   * @param allowed wether or not connections from the specified network will be
   *          accepted.
   *
   * @throws IpAddressException if the specified network is not valid.
   */
  public void add( final String network, final boolean allowed ) throws IpAddressException {
    // try and convert the expression into an IP network
    final IpNetwork net = new IpNetwork( network );

    add( net, allowed );
  }




  /**
   * Add a network to the list with an allowance flag.
   *
   * @param network the network specification to add
   * @param allowed wether or not connections from the specified network will be
   *          accepted.
   */
  public void add( final IpNetwork network, final boolean allowed ) {
    // create a new rule and add it to the list
    acl.add( new ACLRule( network, allowed ) );
  }




  /**
   * Test to see if this ACL allows the given InetAddress.
   *
   * @param addr The InetAdress to check
   *
   * @return whether the address was permitted by the ACL
   */
  public synchronized boolean allows( final InetAddress addr ) {
    try {
      return allows( new IpAddress( addr.getHostAddress() ) );
    } catch ( final Exception e ) {}

    return false;
  }




  /**
   * Test to see if this ACL allows the given IpAddress represented by the
   * string.
   *
   * @param addr The string representation of the adress to check
   *
   * @return whether the address was permitted by the ACL
   */
  public synchronized boolean allows( final String addr ) {
    try {
      return allows( new IpAddress( addr ) );
    } catch ( final Exception e ) {}

    return false;
  }




  /**
   * Test to see if this ACL allows the given IpAddress.
   *
   * @param addr the address to check
   *
   * @return whether the address was permitted by the ACL
   */
  public synchronized boolean allows( final IpAddress addr ) {
    for ( int i = 0; i < acl.size(); i++ ) {
      final ACLRule rule = (ACLRule)acl.get( i );

      if ( rule.network.contains( addr ) ) {
        return rule.allows;
      }
    }

    // We have gone through all the rules without a match, return the default
    return defaultAllow;
  }




  /**
   * Append the entries of the given ACL to the end of our own list.
   *
   * <p>The result is the ACLRules are shared between the two lists. A copy of
   * the ACLRule is <strong>NOT</strong> made.
   *
   * @param newacl The ACL to append to this list.
   */
  public synchronized void append( final IpAcl newacl ) {
    for ( int i = 0; i < newacl.acl.size(); acl.add( newacl.acl.get( i++ ) ) ) {
      ;
    }
  }




  /**
   * @return  the name
   */
  public String getName() {
    return name;
  }




  /**
   * Method toString
   *
   * @return TODO Complete Documentation
   */
  public String toString() {
    final StringBuffer buffer = new StringBuffer();

    buffer.append( IpAcl.DEFAULT_TAG );
    buffer.append( IpAcl.TOKEN_DELIM );

    if ( defaultAllow ) {
      buffer.append( IpAcl.ALLOW_TAG );
    } else {
      buffer.append( IpAcl.DENY_TAG );
    }

    if ( acl.size() > 0 ) {
      buffer.append( IpAcl.RULE_DELIM );

      for ( int i = 0; i < acl.size(); i++ ) {
        final ACLRule rule = (ACLRule)acl.get( i );
        buffer.append( rule.network.toString() );
        buffer.append( IpAcl.TOKEN_DELIM );

        if ( rule.allows ) {
          buffer.append( IpAcl.ALLOW_TAG );
        } else {
          buffer.append( IpAcl.DENY_TAG );
        }

        if ( i + 1 < acl.size() ) {
          buffer.append( IpAcl.RULE_DELIM );
        }
      }
    }

    return buffer.toString();
  }

  // 
  // --
  // 

  /**
   * Wrapper class for an ACL rule.
   */
  private class ACLRule {

    /** Field allows */
    boolean allows = true;

    /** Field network */
    IpNetwork network = null;




    /**
     * Constructor ACLRule
     *
     * @param net
     * @param allowed
     */
    private ACLRule( final IpNetwork net, final boolean allowed ) {
      network = net;
      allows = allowed;
    }
  }

}
