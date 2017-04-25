/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Vector;


/**
 * The class DefaultTrapListener implements a server which listens for 
 * trap messages sent from remote SNMP entities. 
 * 
 * <p>The approach is that from version 1 of SNMP, using no encryption of data.
 * Communication occurs via UDP, using port 162, the standard SNMP trap 
 * port.</p>
 *
 * <p>Applications utilize this class with classes which implement the 
 * ITrapListener interface. These must provide a processTrap() method, and 
 * are registered/deregistered with this class through its addTrapListener() 
 * and removeTrapListener methods.</p>
 */
public class DefaultTrapListener implements Runnable
{
  public static final int SNMP_TRAP_PORT = 162;

  /**
   * Largest size for datagram packet payload; based on RFC 1157, need to 
   * handle messages of at least 484 bytes.
   */

  public static final int MAXSIZE = 512;
  private DatagramSocket dSocket;
  private Thread receiveThread;

  private Vector listenerVector;




  /**
   * Construct a new trap receiver object to receive traps from remote SNMP 
   * hosts.
   * 
   * <p>This version will accept messages from all hosts using any community 
   * name.</p>
   *
   * @throws SocketException
   */
  public DefaultTrapListener() throws SocketException
  {
    dSocket = new DatagramSocket( SNMP_TRAP_PORT );

    listenerVector = new Vector();

    receiveThread = new Thread( this );

  }




  /**
   * Method addTrapListener.
   *
   * @param listener
   */
  public void addTrapListener( ITrapListener listener )
  {
    // see if listener already added; if so, ignore
    for( int i = 0; i < listenerVector.size(); i++ )
    {
      if( listener == listenerVector.elementAt( i ) )
      {
        return;
      }
    }

    // if got here, it's not in the list; add it
    listenerVector.add( listener );
  }




  /**
   * Method removeTrapListener.
   *
   * @param listener
   */
  public void removeTrapListener( ITrapListener listener )
  {
    // see if listener in list; if so, remove, if not, ignore
    for( int i = 0; i < listenerVector.size(); i++ )
    {
      if( listener == listenerVector.elementAt( i ) )
      {
        listenerVector.removeElementAt( i );

        break;
      }
    }

  }




  /**
   * Start listening for trap messages.
   */
  public void startReceiving()
  {
    // if receiveThread not already running, start it
    if( !receiveThread.isAlive() )
    {
      receiveThread = new Thread( this );

      receiveThread.start();
    }
  }




  /**
   * Stop listening for trap messages.
   *
   * @throws SocketException
   */
  public void stopReceiving() throws SocketException
  {
    // interrupt receive thread so it will die a natural death
    receiveThread.interrupt();
  }




  /**
   * The run() method for the trap interface's listener. 
   * 
   * <p>Just waits for trap messages to come in on port 162 (or the port 
   * supplied in the constructor), then dispatches the retrieved SnmpTrapPDU to 
   * each of the registered SnmpTrapListeners by calling their processTrap() 
   * methods.</p>
   */
  public void run()
  {
    int errorStatus = 0;
    int errorIndex = 0;

    try
    {

      while( !receiveThread.isInterrupted() )
      {

        DatagramPacket inPacket = new DatagramPacket( new byte[MAXSIZE], MAXSIZE );

        dSocket.receive( inPacket );

        byte[] encodedMessage = inPacket.getData();

        /*
         * System.out.println("Message bytes length (in): " + inPacket.getLength());
         *
         * System.out.println("Message bytes (in):");
         * for (int i = 0; i < encodedMessage.length; ++i)
         * {
         * System.out.print(hexByte(encodedMessage[i]) + " ");
         * }
         * System.out.println("\n");
         */
        SnmpMessage receivedMessage = new SnmpMessage( SnmpBerCodec.extractNextTLV( encodedMessage, 0 ).value );

        SnmpTrapPDU receivedPDU = receivedMessage.getTrapPDU();

        // pass the received trap PDU to the processTrap method of any listeners
        for( int i = 0; i < listenerVector.size(); i++ )
        {
          ITrapListener listener = (ITrapListener)listenerVector.elementAt( i );

          listener.processTrap( receivedPDU );
        }

      }

    }
    catch( IOException e )
    {
      // do nothing for now...
    }
    catch( SnmpBadValueException e )
    {
      // do nothing for now...
    }

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
