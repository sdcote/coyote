/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial API and implementation
 */
package coyote.commons.network;

import java.util.StringTokenizer;


/**
 * Class Ip6Address
 */
public class Ip6Address {

  /**
   * Tests a string to see if it represents a colon-separated hexadecimal
   * representation of a valid and complete IPv6 address. 
   * <p>
   * For instance, if <code>address</code> contains the value 
   * "1111:2222:3333:4444:5555:6666:7777:8888",
   *  <code>isValidIPv6(address)</code> will return 'true'.
   * </p>
   * 
   * @param address A string that may contain an IPv6 address in colon-separated
   *          notation.
   * @return true if address is a valid and complete IPv6 address.
   */
  public static boolean isValidIPv6( String address ) {
    StringTokenizer labels = new StringTokenizer( address, ":" );
    String thisLabel;
    int labelCount = 0;
    boolean nonHexCharFound = false;

    while ( labels.hasMoreTokens() && !nonHexCharFound ) {
      thisLabel = labels.nextToken();

      labelCount++;

      try {
        if ( !thisLabel.equals( null ) && ( thisLabel.length() > 0 ) ) {
          Integer.parseInt( thisLabel, 16 );
        }
      } catch ( NumberFormatException ex ) {
        nonHexCharFound = true;
      }
    }

    return ( labelCount == 8 ) && ( !nonHexCharFound );
  }




  /**
   * Take a colon-separated hexadecimal representation of an IPv6 address and
   * strip out any leading (padding) zeros from the elements of the address. 
   * <p>
   * For instance, if <code>address</code> contains the value
   * "0011:2222:0033:0444:0005:0066:0007:8888",
   * <code>stripIPv6LeadZeros(address)</code> will return
   * "11:2222:33:444:5:66:7:8888".
   * </p>
   * 
   * @param address A string that may contain an IPv6 address in colon-separated
   *          notation.
   * @return A copy of the original string with leading zeros stripped out.
   */
  public static String stripIPv6LeadZeros( String address ) {
    StringTokenizer labels = new StringTokenizer( address, ":" );
    String thisLabel;
    String retnValue = "";
    int value;

    while ( labels.hasMoreTokens() ) {
      thisLabel = labels.nextToken();
      value = Integer.parseInt( thisLabel, 16 );
      thisLabel = Integer.toHexString( value );

      if ( retnValue.equals( "" ) ) {
        retnValue = thisLabel;
      } else {
        retnValue = thisLabel + ":" + retnValue;
      }
    }

    return retnValue;
  }




  /**
   * Take a colon-separated hexadecimal representation of an IPv6 address and
   * pad each element so with leading zeros so that each element is four
   * hexadecimal characters wide.
   * <p>
   * For instance, if <code>address</code> contains the value 
   * "11:2222:33:444:5:66:7:8888", <code>addIPv6LeadZeros(address)</code> will 
   * return "0011:2222:0033:0444:0005:0066:0007:8888".
   * </p>
   * 
   * @param address A string that may contain an IPv6 address in colon-separated
   *          notation.
   * @return A copy of the original string with leading zeros added in.
   */
  public static String addIPv6LeadZeros( String address ) {
    StringTokenizer labels = new StringTokenizer( address, ":" );
    String thisLabel;
    String retnValue = "";
    int tokenLength;

    while ( labels.hasMoreTokens() ) {
      thisLabel = labels.nextToken();
      tokenLength = thisLabel.length();

      for ( int i = 0; i < 4 - tokenLength; i++ ) {
        thisLabel = "0" + thisLabel;
      }

      if ( retnValue.equals( "" ) ) {
        retnValue = thisLabel;
      } else {
        retnValue = thisLabel + ":" + retnValue;
      }
    }

    return retnValue;
  }




  /**
   * Create a query string that is appropriate for use as the target of a PTR
   * query for an IPv6 address. 
   * <p>
   * For instance if <code>address</code> contains the value 
   * "11:2222:33:444:5:66:7:8888", <code>IPv6ptrQueryString(addr)</code> will 
   * return '8888.7000.6600.5000.4440.3300.2222.1100.ip6.int'.
   * </p>
   * 
   * @param address
   * @return A string representing a reverse lookup of the address passed in.
   */
  public static String IPv6ptrQueryString( String address ) {
    if ( isValidIPv6( address ) ) {
      String fullAddress = addIPv6LeadZeros( address );
      StringTokenizer labels = new StringTokenizer( fullAddress, ":" );
      String thisLabel;
      String retnValue = "ip6.int";

      while ( labels.hasMoreTokens() ) {
        thisLabel = labels.nextToken();

        for ( int i = 0; i < 4; i++ ) {
          retnValue = thisLabel.charAt( i ) + "." + retnValue;
        }
      }

      return retnValue;
    } else {
      return "";
    }
  }
}