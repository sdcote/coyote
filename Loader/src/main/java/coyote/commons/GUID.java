/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;


/**
 * A GUID generator for those environments which do not have a UUID.
 * 
 * <p>Not all embedded JREs have a uniform UUID. This class provides one that 
 * offers spacial (via IP addresses) and temporal (via system clock) 
 * separation. This class also provides randomness by using a random number for 
 * each ID. The three components are then run through an MD5 hash to reduce the 
 * possibility of prediction through reverse-engineering.</p>
 *  
 * <p>In one test (586 class CPU), this class generates 100,000 secure GUIDs in 
 * 13.2 seconds (7692/sec) and the same number of non-secure GUIDS in 12.2 
 * seconds (8333/sec).</p>
 */

public class GUID {
  private static final byte[] EMPTY = new byte[0];
  private static Random myRand;
  private static SecureRandom mySecureRand;
  private static String s_id;

  String seed = "";

  public String identifier = "";

  public byte[] bytes = GUID.EMPTY;

  /**
   * Static block to take care of one time secureRandom seed.
   * 
   * <p>It takes a few seconds to initialize SecureRandom. You might want to 
   * consider removing this static block or replacing it with a "time since 
   * first loaded" seed to reduce this time.</p>
   * 
   * <p>This block will run only once per JVM instance.</p>
   */
  static {
    GUID.mySecureRand = new SecureRandom();
    final long secureInitializer = GUID.mySecureRand.nextLong();
    GUID.myRand = new Random( secureInitializer );
    try {
      GUID.s_id = InetAddress.getLocalHost().toString();
    } catch ( final UnknownHostException e ) {
      e.printStackTrace();
    }

  }




  /**
   * Default constructor.
   * 
   * <p>With no specification of security option, this constructor defaults to 
   * lower security, high performance.</p>
   */
  public GUID() {
    getRandomGUID( false );
  }




  /**
   * Constructor with security option.  
   * 
   * <p>Setting secure to 'true' enables each random number generated to be 
   * cryptographically strong. Secure 'false' defaults to the standard Random 
   * function seeded with a single cryptographically strong random number.</p>
   */
  public GUID( final boolean secure ) {
    getRandomGUID( secure );
  }




  /**
   * Method to generate the random GUID.
   */
  private void getRandomGUID( final boolean secure ) {
    MessageDigest md5 = null;
    final StringBuffer sbValueBeforeMD5 = new StringBuffer();

    try {
      md5 = MessageDigest.getInstance( "MD5" );
    } catch ( final NoSuchAlgorithmException e ) {
      System.out.println( "Error: " + e );
    }

    try {
      final long time = System.currentTimeMillis();
      long rand = 0;

      if ( secure ) {
        rand = GUID.mySecureRand.nextLong();
      } else {
        rand = GUID.myRand.nextLong();
      }

      sbValueBeforeMD5.append( GUID.s_id );
      sbValueBeforeMD5.append( ':' );
      sbValueBeforeMD5.append( time );
      sbValueBeforeMD5.append( ':' );
      sbValueBeforeMD5.append( rand );

      seed = sbValueBeforeMD5.toString();
      md5.update( seed.getBytes() );

      final byte[] array = md5.digest();
      final StringBuffer sb = new StringBuffer();
      for ( int j = 0; j < array.length; ++j ) {
        final int b = array[j] & 0xFF;
        if ( b < 0x10 ) {
          sb.append( '0' );
        }
        sb.append( Integer.toHexString( b ) );
      }

      identifier = sb.toString();
    } catch ( final Exception e ) {
      System.out.println( "Error:" + e );
    }
  }




  /**
   * Convert to the standard format for GUID (Useful for SQL Server Unique 
   * Identifiers, etc.) Example: AB338BE2-823F-93A6-2531-2EAF4A8E0C5D.
   */
  public String toString() {
    final String raw = identifier.toUpperCase();
    final StringBuffer sb = new StringBuffer();
    sb.append( raw.substring( 0, 8 ) );
    sb.append( "-" );
    sb.append( raw.substring( 8, 12 ) );
    sb.append( "-" );
    sb.append( raw.substring( 12, 16 ) );
    sb.append( "-" );
    sb.append( raw.substring( 16, 20 ) );
    sb.append( "-" );
    sb.append( raw.substring( 20 ) );

    return sb.toString();
  }




  /**
   * Create a new GUID.
   * 
   * <p>This is to match the new UUID class and its approach to generating 
   * random UIDs.</p>
   * 
   * @return a new GUID
   */
  public static Object randomGUID() {
    return new GUID();
  }




  /**
   * Create a new GUID which is more cryptographically strong.
   * 
   * <p>This is to match the new UUID class and its approach to generating 
   * random UIDs.</p>
   * 
   * @return a new, harder to predict GUID
   */
  public static Object randomSecureGUID() {
    return new GUID( true );
  }

}
