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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Represents a set of data for authentication operations.
 * 
 * <p>Modern systems use more than just username and password for 
 * authentication. In order to support these system and the security operations 
 * of more sophisticated systems, this class supports a set of credential data 
 * to be used in various ways.
 * 
 * <p>Data in this class is stored as bytes so future systems can store a 
 * variety of data (e.g. biometric) in this class. This class can even help 
 * with some public-private key management schemes.
 * 
 * <p>Credentials should not be stored in memory in their raw state to help
 * prevent accidental exposure should a memory dump occurs or memory become
 * accessible as might be the case in an overflow attack. This class supports
 * the ability to only store the hashed or digested representation of the 
 * credential data by setting the number of rounds of digest calculations are 
 * performed on credentials before they are stored and checked. 
 */
public class CredentialSet {

  private Hashtable<String, byte[]> _credentials = new Hashtable<String, byte[]>();

  private int _rounds = 0;

  private static final String MD5 = "MD5";
  private static final String UTF8 = "UTF8";

  // Identifying key
  public static final String KEY = "key";

  // Password key
  public static final String PASSWORD = "password";

  // Public key for encryption
  public static final String PUBLICKEY = "publickey";

  // Private key for encryption
  public static final String PRIVATEKEY = "privatekey";

  // An activation token
  public static final String ACTIVATION = "activation";

  static {
    try {
      @SuppressWarnings("unused")
      MessageDigest md = MessageDigest.getInstance( MD5 );
    } catch ( NoSuchAlgorithmException e ) {
      e.printStackTrace();
    }
    try {
      UTF8.getBytes( UTF8 );
    } catch ( UnsupportedEncodingException e ) {
      e.printStackTrace();
    }
  }




  /**
   * Constructor CredentialSet
   */
  public CredentialSet() {}




  /**
   * Create a credential set using the given number of rounds.
   * 
   * <p>In order to prevent keeping a copy of the credential data in memory in 
   * an accessible format, the credential set can pass the credential data 
   * through a MD5 digest and store the digest value instead of the actual 
   * credential data. Setting the number of rounds to 1 (or more) will result 
   * in this class using the MD5 has of the credential.
   * 
   * @param rounds Number of rounds of MD5 digests to perform.  0 equals using 
   * the raw (unsecure) credentials, 1=single round of digest (more secure).
   */
  public CredentialSet( int rounds ) {
    if ( rounds < 0 )
      throw new IllegalArgumentException( "Number of rounds cannot be negative" );

    _rounds = rounds;
  }




  /**
   * Retrieve the number of times credential values are to be passed through 
   * digest calculations. 
   *
   * @return the number of rounds of MD5 to perform on credentials
   */
  public int getRounds() {
    return _rounds;
  }




  /**
   * Add the named data to these credentials
   * 
   * <p>If rounds are set to 1 or more, the credential data will be stored as 
   * an MD5 digest. 
   * 
   * @param name The name of the credential
   * @param bytes the bytes representing the credential
   */
  public void add( String name, byte[] bytes ) {
    byte[] val = bytes;

    if ( _rounds > 0 ) {
      MessageDigest md = null;
      try {
        md = MessageDigest.getInstance( MD5 );
      } catch ( NoSuchAlgorithmException e ) {}

      if ( md != null ) {
        // make sure all the credentials are hashed even if added after digest
        for ( int x = 0; x < _rounds; x++ ) {
          val = md.digest( val );
        }
      }
    }

    // add the data
    _credentials.put( name, val );
  }




  /**
   * Constructor which populates a named credential with the UTF-8 byte encoded 
   * given value.
   * 
   * <p>This stores credentials as cleartext in memory and can represent a 
   * security risk. Consider using {@link #CredentialSet(String, String, int)} 
   * to store credentials as MD5 digested values.
   * 
   * <p>This is a convenience method for one of the most common use case 
   * scenarios for this class.
   * 
   * <p>The value is stored in UTF8 encoding, again addressing the most common 
   * character set use case.
   *
   * @param name The name of the credential being set
   * @param value The authenticating data
   */
  public CredentialSet( String name, String value ) {
    try {
      add( name, value.getBytes( UTF8 ) );
    } catch ( UnsupportedEncodingException e ) {}
  }




  /**
   * Constructor which populates a named credential with the UTF-8 byte encoded 
   * given value after passing it through the given number of rounds of MD5.
   * 
   * <p>This is a convenience method for one of the most common secure use case 
   * scenarios for this class.
   * 
   * <p>The value is converted to UTF8 encoding and stored as an MD5 hash to 
   * help reduce the exposure of the clear test credential value.
   *
   * @param name The name of the credential being set
   * @param value The authenticating data
   * @param rounds The number of times the value is digested. Negative values will default to 1 round.
   */
  public CredentialSet( String name, String value, int rounds ) {
    if ( rounds >= 0 ) {
      this._rounds = rounds;
    } else {
      // negative numbers will default to 1
      _rounds = 1;
    }

    try {
      add( name, value.getBytes( UTF8 ) );
    } catch ( UnsupportedEncodingException e ) {}
  }




  public boolean contains( String name ) {
    return _credentials.containsKey( name );
  }




  /**
   * @return the number of credentials in the set
   */
  public int size() {
    return _credentials.size();
  }




  /**
   * Test to see if all the given credentials match what is recorded in in 
   * this credential set.
   * 
   * <p>This exits early on the first failure to match.
   * 
   * @param creds The set of credentials to match.
   * 
   * @return True if all the given credentials match, false otherwise.
   */
  public boolean matchAll( CredentialSet creds ) {
    boolean retval = true;
    if ( creds == null ) {
      return false;
    } else {

      // iterate through the given credentials
      for ( Iterator<Map.Entry<String, byte[]>> it = creds._credentials.entrySet().iterator(); it.hasNext(); ) {
        Entry<String, byte[]> entry = it.next();

        // If we have a credential with the given name
        if ( _credentials.containsKey( entry.getKey() ) ) {
          byte[] data = entry.getValue(); // given credential value
          byte[] ourdata = _credentials.get( entry.getKey() ); // our credential value

          if ( !Arrays.equals( data, ourdata ) ) {
            return false; // values do not match
          }
        } else {
          return false; // we do not contain a credential with the given name
        }
      } // for
    } // not null

    return retval;
  }




  /**
   * @return a list of credential names in this credential set.
   */
  public List<String> getNames() {
    return new ArrayList<String>( _credentials.keySet() );
  }




  /**
   * Return the value of the credential with the given name.
   * 
   * @param name The name of the credential to return.
   * 
   * @return the value of the named credential of null if the name is null or the credential value was not found.
   */
  public byte[] getValue( String name ) {
    if ( name != null ) {
      return _credentials.get( name );
    }
    return null;
  }
}