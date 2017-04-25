/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.Vector;


/**
 * The class DefaultAgent implements an interface for responding to
 * requests sent from a remote SNMP manager.
 *
 * <p>The agent simply listens for requests for information, and passes
 * requested OIDs on to concrete subclasses of IRequestListener. These are
 * expected to retrieve requested information from the system, and return this
 * to the agent interface for inclusion in a response to the manager.</p>
 *
 * <p>The approach is that from version 1 of SNMP, using no encryption of data.
 * Communication occurs via UDP, using port 162, the standard SNMP trap port,
 * as the destination port.</p>
 */
public class DefaultAgent implements Runnable
{
  public static final int SNMP_PORT = 161;
  public static final int SNMP_TRAP_PORT = 162;

  /**
   * Largest size for datagram packet payload; based on RFC 1157, need to
   * handle messages of at least 484 bytes
   */
  public static final int MAXSIZE = 512;

  /** Field version */
  int version = 0;

  private DatagramSocket dSocket;
  private Thread receiveThread;
  private Vector listenerVector;
  private int localPort = SNMP_PORT;




  /**
   * Construct a new agent object to listen for requests from remote SNMP 
   * managers. 
   * 
   * <p>The agent listens on the standard SNMP UDP port 161.</p>
   *
   * @param version
   *
   * @throws SocketException
   */
  public DefaultAgent( int version ) throws SocketException
  {
    this( version, SNMP_PORT );
  }




  /**
   * Construct a new agent object to listen for requests from remote SNMP 
   * managers. 
   * 
   * <p>The agent listens on the supplied port and starts a thread listening to
   * the datagram socket.</p>
   *
   * @param version
   * @param port
   *
   * @throws SocketException
   */
  public DefaultAgent( int version, int port ) throws SocketException
  {
    this.version = version;
    this.localPort = port;

    dSocket = new DatagramSocket( null );
    dSocket.setBroadcast( true );
    dSocket.setReuseAddress( true );
    dSocket.bind( new InetSocketAddress( this.localPort ) );

    listenerVector = new Vector();

    receiveThread = new Thread( this );
  }




  /**
   * Method addRequestListener.
   *
   * @param listener
   */
  public void addRequestListener( IRequestListener listener )
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
   * Method removeRequestListener.
   *
   * @param listener
   */
  public void removeRequestListener( IRequestListener listener )
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
   * Start listening for requests from remote managers.
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
   * Stop listening for requests from remote managers.
   *
   * @throws SocketException
   */
  public void stopReceiving() throws SocketException
  {
    // interrupt receive thread so it will die a natural death
    receiveThread.interrupt();
  }




  /**
   * The run() method for the agent interface's listener.
   * 
   * <p>This method is called immediately upon the constructor being called and 
   * a new datagram socket being created.</p>
   *
   * <p>Just waits for SNMP request messages to come in on port 161 (or the
   * port supplied in the constructor), then dispatches the retrieved SnmpPdu
   * and community name to each of the registered SnmpRequestListeners by
   * calling their processRequest() methods.</p>
   */
  public void run()
  {
    try
    {
      if( dSocket == null )
      {
        dSocket.setBroadcast( true );
        dSocket.setReuseAddress( true );
        dSocket.bind( new InetSocketAddress( localPort ) );
      }

      while( !receiveThread.isInterrupted() )
      {
        DatagramPacket inPacket = new DatagramPacket( new byte[MAXSIZE], MAXSIZE );

        dSocket.receive( inPacket );

        InetAddress requesterAddress = inPacket.getAddress();
        int requesterPort = inPacket.getPort();

        byte[] encodedMessage = inPacket.getData();

        SnmpMessage receivedMessage = new SnmpMessage( SnmpBerCodec.extractNextTLV( encodedMessage, 0 ).value );

        String communityName = receivedMessage.getCommunityName();
        SnmpPdu receivedPDU = receivedMessage.getPDU();
        byte requestPDUType = receivedPDU.getPDUType();

        // System.out.println("Received message; community = " + communityName + ", pdu type = " + Byte.toString(requestPDUType));
        // System.out.println("  read community = " + readCommunityName + ", write community = " + writeCommunityName);

        SnmpSequence requestedVarList = receivedPDU.getVarBindList();

        Hashtable variablePairHashtable = new Hashtable();
        SnmpSequence responseVarList = new SnmpSequence();
        int errorIndex = 0;
        int errorStatus = SnmpRequestException.NO_ERROR;
        int requestID = receivedPDU.getRequestID();

        try
        {
          // pass the received PDU and community name to the processRequest method of any listeners;
          // handle differently depending on whether the request is a get-next, or a get or set
          if( ( requestPDUType == SnmpBerCodec.SNMPGETREQUEST ) || ( requestPDUType == SnmpBerCodec.SNMPSETREQUEST ) )
          {
            // pass the received PDU and community name to any registered listeners
            for( int i = 0; i < listenerVector.size(); i++ )
            {
              IRequestListener listener = (IRequestListener)listenerVector.elementAt( i );

              // return value is sequence of variable pairs for those OIDs handled by the listener
              SnmpSequence handledVarList = listener.processRequest( receivedPDU, communityName, requesterAddress );

              if( handledVarList != null )
              {
              // add to Hashtable of handled OIDs, if not already there
              for( int j = 0; j < handledVarList.size(); j++ )
              {
                SnmpSequence handledPair = (SnmpSequence)handledVarList.getSnmpObjectAt( j );
                SnmpObjectIdentifier snmpOID = (SnmpObjectIdentifier)handledPair.getSnmpObjectAt( 0 );
                SnmpObject snmpObject = (SnmpObject)handledPair.getSnmpObjectAt( 1 );

                if( !variablePairHashtable.containsKey( snmpOID ) )
                {
                  variablePairHashtable.put( snmpOID, snmpObject );
                }
              }
              }
            }

            // construct response containing the handled OIDs; if any OID not handled, throw exception
            for( int j = 0; j < requestedVarList.size(); j++ )
            {
              SnmpSequence requestPair = (SnmpSequence)requestedVarList.getSnmpObjectAt( j );
              SnmpObjectIdentifier snmpOID = (SnmpObjectIdentifier)requestPair.getSnmpObjectAt( 0 );

              // find corresponding SNMP object in hashtable
              if( !variablePairHashtable.containsKey( snmpOID ) )
              {
                errorIndex = j + 1;
                errorStatus = SnmpRequestException.VALUE_NOT_AVAILABLE;

                if( requestPDUType == SnmpBerCodec.SNMPGETREQUEST )
                {
                  throw new SnmpGetException( "OID " + snmpOID + " not handled", errorIndex, errorStatus );
                }
                else
                {
                  throw new SnmpSetException( "OID " + snmpOID + " not handled", errorIndex, errorStatus );
                }
              }
              SnmpObject snmpObject = (SnmpObject)variablePairHashtable.get( snmpOID );
              SnmpVariablePair responsePair = new SnmpVariablePair( snmpOID, snmpObject );

              responseVarList.addSnmpObject( responsePair );
            }
          }
          else if( requestPDUType == SnmpBerCodec.SNMPGETNEXTREQUEST )
          {
            // pass the received PDU and community name to any registered listeners
            for( int i = 0; i < listenerVector.size(); i++ )
            {
              IRequestListener listener = (IRequestListener)listenerVector.elementAt( i );

              // return value is sequence of nested variable pairs for those OIDs handled by the listener:
              // consists of (supplied OID, (following OID, value)) nested variable pairs
              SnmpSequence handledVarList = listener.processGetNextRequest( receivedPDU, communityName, requesterAddress );

              // add variable pair to Hashtable of handled OIDs, if not already there
              for( int j = 0; j < handledVarList.size(); j++ )
              {
                SnmpSequence handledPair = (SnmpSequence)handledVarList.getSnmpObjectAt( j );
                SnmpObjectIdentifier snmpOID = (SnmpObjectIdentifier)handledPair.getSnmpObjectAt( 0 );
                SnmpObject snmpObject = (SnmpObject)handledPair.getSnmpObjectAt( 1 );

                if( !variablePairHashtable.containsKey( snmpOID ) )
                {
                  variablePairHashtable.put( snmpOID, snmpObject );
                }
              }
            }

            // construct response containing the handled OIDs; if any OID not handled, throw exception
            for( int j = 0; j < requestedVarList.size(); j++ )
            {
              SnmpSequence requestPair = (SnmpSequence)requestedVarList.getSnmpObjectAt( j );
              SnmpObjectIdentifier snmpOID = (SnmpObjectIdentifier)requestPair.getSnmpObjectAt( 0 );

              // find corresponding SNMP object in hashtable
              if( !variablePairHashtable.containsKey( snmpOID ) )
              {
                errorIndex = j + 1;
                errorStatus = SnmpRequestException.VALUE_NOT_AVAILABLE;
                throw new SnmpGetException( "OID " + snmpOID + " not handled", errorIndex, errorStatus );
              }

              // value in hashtable is complete variable pair
              SnmpVariablePair responsePair = (SnmpVariablePair)variablePairHashtable.get( snmpOID );

              responseVarList.addSnmpObject( responsePair );
            }
          }
          else
          {
            // some other PDU type; silently ignore
            continue;
          }

        }
        catch( SnmpRequestException e )
        {
          // exception should contain the index and cause of error; return this in message
          errorIndex = e.errorIndex;
          errorStatus = e.errorStatus;

          // just return request variable list as response variable list
          responseVarList = requestedVarList;
        }
        catch( Exception e )
        {
          // don't have a specific index and cause of error; return message as general error, index 0
          errorIndex = 0;
          errorStatus = SnmpRequestException.FAILED;

          // just return request variable list as response variable list
          responseVarList = requestedVarList;

          // also report the exception locally
          System.out.println( "Exception while processing request: " + e.toString() );
        }

        SnmpPdu pdu = new SnmpPdu( SnmpBerCodec.SNMPGETRESPONSE, requestID, errorStatus, errorIndex, responseVarList );
        SnmpMessage message = new SnmpMessage( version, communityName, pdu );
        byte[] messageEncoding = message.getBEREncoding();
        DatagramPacket outPacket = new DatagramPacket( messageEncoding, messageEncoding.length, requesterAddress, requesterPort );

        dSocket.send( outPacket );
      }
    }
    catch( IOException e )
    {
      // just report the problem
      System.out.println( "IOException during request processing: " + e.toString() );
    }
    catch( SnmpBadValueException e )
    {
      // just report the problem
      System.out.println( "SnmpBadValueException during request processing: " + e.toString() );
    }
    catch( Exception e )
    {
      // just report the problem
      System.out.println( "Exception during request processing: " + e.toString() );
    }
    finally
    {
      dSocket.close();
      dSocket = null;
    }
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
  public void sendTrap( int version, InetAddress hostAddress, int hostPort, String community, SnmpTrapPDU pdu ) throws IOException
  {
    // Create a new message
    SnmpMessage message = new SnmpMessage( version, community, pdu );

    // encode it
    byte[] messageEncoding = message.getBEREncoding();

    // create a UDP packet
    DatagramPacket outPacket = new DatagramPacket( messageEncoding, messageEncoding.length, hostAddress, hostPort );

    // send it over the network
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
    sendTrap( version, hostAddress, SNMP_TRAP_PORT, community, pdu );
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

}
