/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


/**
 * The class DefaultTrapSender implements an interface for sending trap 
 * messages to a remote SNMP manager.
 * 
 * <p>The approach is that from version 1 of SNMP, using no encryption of data. 
 * Communication occurs via UDP, using port 162, the standard SNMP trap port, 
 * as the destination port.</p>
 */
public class DefaultTrapSender
{
  public static final int SNMP_TRAP_PORT = 162;

  /**
   * Largest size for datagram packet payload; based on RFC 1157, need to 
   * handle messages of at least 484 bytes.
   */
  public static final int MAXSIZE = 512;
  private DatagramSocket dSocket;

  


  /**
   * Construct a new trap sender object to send traps to remote SNMP hosts.
   *
   * @throws SocketException
   */
  public DefaultTrapSender() throws SocketException
  {
    dSocket = new DatagramSocket();
  }




  /**
   * Construct a new trap sender object to send traps to remote SNMP hosts, 
   * binding to the specified local port.
   *
   * @param localPort
   *
   * @throws SocketException
   */
  public DefaultTrapSender( int localPort ) throws SocketException
  {
    dSocket = new DatagramSocket( localPort );
  }




  /**
   * Send the supplied trap pdu to the specified host, using the supplied 
   * version number and community name. 
   * 
   * <p>Use version = 0 for SNMP version 1, or version = 1 for enhanced 
   * capabilities provided through RFC 1157.</p>
   *
   * @param version
   * @param hostAddress
   * @param community
   * @param pdu
   *
   * @throws IOException
   */
  public void sendTrap( int version, InetAddress hostAddress, String community, SnmpTrapPDU pdu ) throws IOException
  {
    SnmpMessage message = new SnmpMessage( version, community, pdu );

    byte[] messageEncoding = message.getBEREncoding();

    DatagramPacket outPacket = new DatagramPacket( messageEncoding, messageEncoding.length, hostAddress, SNMP_TRAP_PORT );

    dSocket.send( outPacket );

  }




  /**
   * Send the supplied trap pdu to the specified host, using the supplied 
   * community name and using 0 for the version field in the SNMP message 
   * (corresponding to SNMP version 1).
   *
   * @param hostAddress
   * @param community
   * @param pdu
   *
   * @throws IOException
   */
  public void sendTrap( InetAddress hostAddress, String community, SnmpTrapPDU pdu ) throws IOException
  {
    int version = 0;

    sendTrap( version, hostAddress, community, pdu );
  }




  /**
   * Method hexByte.
   *
   * @param b
   *
   * @return
   */
  private String hexByte( byte b )
  {
    int pos = b;
    if( pos < 0 )
    {
      pos += 256;
    }

    String returnString = new String();
    returnString += Integer.toHexString( pos / 16 );
    returnString += Integer.toHexString( pos % 16 );

    return returnString;
  }




  /**
   * Method getHex.
   *
   * @param theByte
   *
   * @return
   */
  private String getHex( byte theByte )
  {
    int b = theByte;

    if( b < 0 )
    {
      b += 256;
    }

    String returnString = new String( Integer.toHexString( b ) );

    // add leading 0 if needed
    if( returnString.length() % 2 == 1 )
    {
      returnString = "0" + returnString;
    }

    return returnString;
  }

}
