/*
 * Copyright (c) 2004 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package examples.snmp.network;

import java.net.InetAddress;

import coyote.commons.network.snmp.SnmpClient;

/**
 * The SimpleDiscover class models...
 * 
 * @author Stephan D. Cote' - Enterprise Architecture
 * @version $Revision:$
 */
public class SimpleDiscover
{

  public static void main( String[] args )
  {
    try
    {
      // create a communications interface to a remote SNMP-capable device;
      // need to provide the remote host's InetAddress and the community
      // name for the device; in addition, need to  supply the version number
      // for the SNMP messages to be sent (the value 0 corresponding to SNMP
      // version 1)

      // broadcast address for my workstation (today anyway)
      InetAddress hostAddress = InetAddress.getByName( "10.8.103.255" );

      // The port on which the SNMP agent is listening
      int hostPort = 161;

      String community = "public";
      int version = 0; // SNMPv1

      // now send an SNMP GET request to retrieve the value of the SNMP 
      // variable corresponding to OID 1.3.6.1.2.1.2.1.0; this is the OID 
      // corresponding to the device identifying string, and the type is thus 
      // SnmpOctetString
      String itemID = "1.3.6.1.2.1.1.1.0";

      System.out.println( "Discovering via OID " + itemID );

      System.out.println( SnmpClient.discoverDevices( version, hostAddress, hostPort, community, itemID ) );

    }
    catch( Exception e )
    {
      System.out.println( "Exception during SNMP operation:  " + e + "\n" );
    }

  }

}