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
 * The class SnmpClient defines methods for communicating 
 * with SNMP entities.
 * 
 * <p>The approach is that from version 1 of SNMP, using no encryption of data. 
 * Communication occurs via UDP, using port 161, the standard SNMP port.</p>
 */
public class SnmpClient
{

  public static final int SNMPPORT = 161;

  /**
   * Largest size for datagram packet payload; based on RFC 1157, need to 
   * handle messages of at least 484 bytes.
   */
  public static final int MAXSIZE = 512;

  private int version;
  private InetAddress hostAddress;
  private int hostPort = SNMPPORT;
  private String community;

  DatagramSocket dSocket;

  public int requestID = 1;

  


  /**
   * Construct a new communication object to communicate with the specified 
   * host using the given community name. 
   * 
   * <p>The version setting should be either 0 (version 1) or 1 (version 2, a 
   * la RFC 1157).</p>
   *
   * @param version
   * @param hostAddress
   * @param community
   *
   * @throws SocketException
   */
  public SnmpClient( int version, InetAddress hostAddress, String community ) throws SocketException
  {
    this.version = version;
    this.hostAddress = hostAddress;
    this.community = community;

    dSocket = new DatagramSocket();

    dSocket.setSoTimeout( 15000 );  // 15 seconds
  }




  /**
   * Construct a new communication object to communicate with the specified 
   * host using the given community name. 
   * 
   * <p>The version setting should be either 0 (version 1) or 1 (version 2, a 
   * la RFC 1157).</p>
   *
   * @param version
   * @param hostAddress
   * @param hostPort
   * @param community
   *
   * @throws SocketException
   */
  public SnmpClient( int version, InetAddress hostAddress, int hostPort, String community ) throws SocketException
  {
    this.version = version;
    this.hostAddress = hostAddress;
    this.hostPort = hostPort;
    this.community = community;

    dSocket = new DatagramSocket();

    dSocket.setSoTimeout( 15000 );  // 15 seconds
  }




  /**
   * Permits setting timeout value for underlying datagram socket (in 
   * milliseconds).
   *
   * @param socketTimeout
   *
   * @throws SocketException
   */
  public void setSocketTimeout( int socketTimeout ) throws SocketException
  {
    dSocket.setSoTimeout( socketTimeout );
  }




  /**
   * Close the "connection" with the devive.
   *
   * @throws SocketException
   */
  public void closeConnection() throws SocketException
  {
    dSocket.close();
  }




  /**
   * Retrieve all MIB variable values subsequent to the starting object 
   * identifier given in startID (in dotted-integer notation). 
   * 
   * <p>Return as SnmpVarBindList object. Uses SnmpGetNextRequests to retrieve 
   * variable values in sequence.</p>
   *
   * @param startID
   *
   * @throws IOException Thrown when timeout experienced while waiting for 
   *         response to request.
   * @throws SnmpBadValueException
   */
  public SnmpVarBindList retrieveAllMIBInfo( String startID ) throws IOException, SnmpBadValueException
  {
    // send GetNextRequests until receive
    // an error message or a repeat of the object identifier we sent out
    SnmpVarBindList retrievedVars = new SnmpVarBindList();

    int errorStatus = 0;
    int errorIndex = 0;

    SnmpObjectIdentifier requestedObjectIdentifier = new SnmpObjectIdentifier( startID );
    SnmpVariablePair nextPair = new SnmpVariablePair( requestedObjectIdentifier, new SnmpInteger( 0 ) );
    SnmpSequence varList = new SnmpSequence();
    varList.addSnmpObject( nextPair );

    SnmpPdu pdu = new SnmpPdu( SnmpBerCodec.SNMPGETNEXTREQUEST, requestID, errorStatus, errorIndex, varList );
    SnmpMessage message = new SnmpMessage( version, community, pdu );
    byte[] messageEncoding = message.getBEREncoding();
    DatagramPacket outPacket = new DatagramPacket( messageEncoding, messageEncoding.length, hostAddress, hostPort );

    dSocket.send( outPacket );

    while( errorStatus == 0 )
    {

      DatagramPacket inPacket = new DatagramPacket( new byte[MAXSIZE], MAXSIZE );

      dSocket.receive( inPacket );

      byte[] encodedMessage = inPacket.getData();

      SnmpMessage receivedMessage = new SnmpMessage( SnmpBerCodec.extractNextTLV( encodedMessage, 0 ).value );
      // errorStatus = ((BigInteger)((SnmpInteger)((receivedMessage.getPDU()).getSNMPObjectAt(1))).getValue()).intValue();

      varList = ( receivedMessage.getPDU() ).getVarBindList();

      SnmpSequence newPair = (SnmpSequence)( varList.getSnmpObjectAt( 0 ) );

      SnmpObjectIdentifier newObjectIdentifier = (SnmpObjectIdentifier)( newPair.getSnmpObjectAt( 0 ) );
      SnmpObject newValue = newPair.getSnmpObjectAt( 1 );

      retrievedVars.addSnmpObject( newPair );

      if( requestedObjectIdentifier.equals( newObjectIdentifier ) )
      {
        break;
      }

      requestedObjectIdentifier = newObjectIdentifier;

      requestID++;

      pdu = new SnmpPdu( SnmpBerCodec.SNMPGETNEXTREQUEST, requestID, errorStatus, errorIndex, varList );
      message = new SnmpMessage( version, community, pdu );
      messageEncoding = message.getBEREncoding();
      outPacket = new DatagramPacket( messageEncoding, messageEncoding.length, hostAddress, hostPort );

      dSocket.send( outPacket );

    }

    return retrievedVars;

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
   * Retrieve the MIB variable value corresponding to the object identifier
   * given in itemID (in dotted-integer notation). 
   * 
   * <p>Return as SnmpVarBindList object; if no such variable (either due to 
   * device not supporting it, or community name having incorrect access 
   * privilege), SnmpGetException thrown</p>
   *
   * @param itemID
   *
   * @return the bind list
   * 
   * @throws IOException Thrown when timeout experienced while waiting for 
   *         response to request.
   * @throws SnmpBadValueException
   * @throws SnmpGetException Thrown if supplied OID has value that can't be 
   *         retrieved
   */
  public SnmpVarBindList getMIBEntry( String itemID ) throws IOException, SnmpBadValueException, SnmpGetException
  {
    // send GetRequest to specified host to retrieve specified object identifier

    SnmpVarBindList retrievedVars = new SnmpVarBindList();

    int errorStatus = 0;
    int errorIndex = 0;

    SnmpObjectIdentifier requestedObjectIdentifier = new SnmpObjectIdentifier( itemID );
    SnmpVariablePair nextPair = new SnmpVariablePair( requestedObjectIdentifier, new SnmpInteger( 0 ) );
    SnmpSequence varList = new SnmpSequence();
    varList.addSnmpObject( nextPair );

    SnmpPdu pdu = new SnmpPdu( SnmpBerCodec.SNMPGETREQUEST, requestID, errorStatus, errorIndex, varList );

    SnmpMessage message = new SnmpMessage( version, community, pdu );

    byte[] messageEncoding = message.getBEREncoding();

    /*
     * System.out.println("Request Message bytes:");
     *
     * for (int i = 0; i < messageEncoding.length; ++i)
     * System.out.print(hexByte(messageEncoding[i]) + " ");
     */
    DatagramPacket outPacket = new DatagramPacket( messageEncoding, messageEncoding.length, hostAddress, hostPort );

    dSocket.send( outPacket );

    while( true )  // wait until receive reply for requestID & OID (or error)
    {

      DatagramPacket inPacket = new DatagramPacket( new byte[MAXSIZE], MAXSIZE );

      dSocket.receive( inPacket );

      byte[] encodedMessage = inPacket.getData();

      /*
       * System.out.println("Message bytes:");
       *
       * for (int i = 0; i < encodedMessage.length; ++i)
       * {
       * System.out.print(hexByte(encodedMessage[i]) + " ");
       * }
       */
      SnmpMessage receivedMessage = new SnmpMessage( SnmpBerCodec.extractNextTLV( encodedMessage, 0 ).value );
      SnmpPdu receivedPDU = receivedMessage.getPDU();

      // check request identifier; if incorrect, just ignore packet and continue waiting
      if( receivedPDU.getRequestID() == requestID )
      {

        // check error status; if retrieval problem, throw SnmpGetException
        if( receivedPDU.getErrorStatus() != 0 )
        {
          throw new SnmpGetException( "OID " + itemID + " not available for retrieval", receivedPDU.getErrorIndex(), receivedPDU.getErrorStatus() );
        }

        varList = receivedPDU.getVarBindList();

        SnmpSequence newPair = (SnmpSequence)( varList.getSnmpObjectAt( 0 ) );

        SnmpObjectIdentifier newObjectIdentifier = (SnmpObjectIdentifier)( newPair.getSnmpObjectAt( 0 ) );
        SnmpObject newValue = newPair.getSnmpObjectAt( 1 );

        // check the object identifier to make sure the correct variable has been received;
        // if not, just continue waiting for receive
        if( newObjectIdentifier.toString().equals( itemID ) )
        {
          // got the right one; add it to retrieved var list and break!
          retrievedVars.addSnmpObject( newPair );

          break;
        }

      }

    }

    requestID++;

    return retrievedVars;

  }




  /**
   * Retrieve the MIB variable values corresponding to the object identifiers
   * given in the array itemID (in dotted-integer notation). 
   * 
   * <p>Return as SnmpVarBindList object; if no such variable (either due to 
   * device not supporting it, or community name having incorrect access 
   * privilege), SnmpGetException thrown.</p>
   *
   * @param itemID
   *
   * @return the bind list
   * 
   * @throws IOException Thrown when timeout experienced while waiting for 
   *         response to request.
   * @throws SnmpBadValueException
   * @throws SnmpGetException Thrown if one of supplied OIDs has value that 
   *         can't be retrieved
   */
  public SnmpVarBindList getMIBEntry( String[] itemID ) throws IOException, SnmpBadValueException, SnmpGetException
  {
    // send GetRequest to specified host to retrieve values of specified object identifiers

    SnmpVarBindList retrievedVars = new SnmpVarBindList();
    SnmpSequence varList = new SnmpSequence();

    int errorStatus = 0;
    int errorIndex = 0;

    for( int i = 0; i < itemID.length; i++ )
    {
      SnmpObjectIdentifier requestedObjectIdentifier = new SnmpObjectIdentifier( itemID[i] );
      SnmpVariablePair nextPair = new SnmpVariablePair( requestedObjectIdentifier, new SnmpInteger( 0 ) );
      varList.addSnmpObject( nextPair );
    }

    SnmpPdu pdu = new SnmpPdu( SnmpBerCodec.SNMPGETREQUEST, requestID, errorStatus, errorIndex, varList );

    SnmpMessage message = new SnmpMessage( version, community, pdu );

    byte[] messageEncoding = message.getBEREncoding();

    /*
     * System.out.println("Request Message bytes:");
     *
     * for (int i = 0; i < messageEncoding.length; ++i)
     * System.out.print(hexByte(messageEncoding[i]) + " ");
     */
    DatagramPacket outPacket = new DatagramPacket( messageEncoding, messageEncoding.length, hostAddress, hostPort );

    dSocket.send( outPacket );

    while( true )  // wait until receive reply for requestID & OID (or error)
    {

      DatagramPacket inPacket = new DatagramPacket( new byte[MAXSIZE], MAXSIZE );

      dSocket.receive( inPacket );

      byte[] encodedMessage = inPacket.getData();

      /*
       * System.out.println("Message bytes:");
       *
       * for (int i = 0; i < encodedMessage.length; ++i)
       * {
       * System.out.print(hexByte(encodedMessage[i]) + " ");
       * }
       */
      SnmpMessage receivedMessage = new SnmpMessage( SnmpBerCodec.extractNextTLV( encodedMessage, 0 ).value );
      SnmpPdu receivedPDU = receivedMessage.getPDU();

      // check request identifier; if incorrect, just ignore packet and continue waiting
      if( receivedPDU.getRequestID() == requestID )
      {

        // check error status; if retrieval problem, throw SnmpGetException
        if( receivedPDU.getErrorStatus() != 0 )
        {
          // determine error index
          errorIndex = receivedPDU.getErrorIndex();

          throw new SnmpGetException( "OID " + itemID[errorIndex - 1] + " not available for retrieval", errorIndex, receivedPDU.getErrorStatus() );
        }

        // copy info from retrieved sequence to var bind list
        varList = receivedPDU.getVarBindList();

        for( int i = 0; i < varList.size(); i++ )
        {
          SnmpSequence newPair = (SnmpSequence)( varList.getSnmpObjectAt( i ) );

          SnmpObjectIdentifier newObjectIdentifier = (SnmpObjectIdentifier)( newPair.getSnmpObjectAt( 0 ) );
          SnmpObject newValue = newPair.getSnmpObjectAt( 1 );

          if( newObjectIdentifier.toString().equals( itemID[i] ) )
          {
            retrievedVars.addSnmpObject( newPair );
          }
          else
          {
            // wrong OID; throw GetException
            throw new SnmpGetException( "OID " + itemID[i] + " expected at index " + i + ", OID " + newObjectIdentifier + " received", i + 1, SnmpRequestException.FAILED );
          }
        }

        break;

      }

    }

    requestID++;

    return retrievedVars;

  }




  /**
   * Retrieve the MIB variable value corresponding to the object identifier following that
   * given in itemID (in dotted-integer notation). Return as SnmpVarBindList object; if no
   * such variable (either due to device not supporting it, or community name having incorrect
   * access privilege), variable value will be SnmpNull object
   *
   * @param itemID
   *
   * @return the bind list
   * 
   * @throws IOException Thrown when timeout experienced while waiting for response to request.
   * @throws SnmpBadValueException
   * @throws SnmpGetException Thrown if one the OID following the supplied OID has value that can't be retrieved
   */
  public SnmpVarBindList getNextMIBEntry( String itemID ) throws IOException, SnmpBadValueException, SnmpGetException
  {
    // send GetRequest to specified host to retrieve specified object identifier

    SnmpVarBindList retrievedVars = new SnmpVarBindList();

    int errorStatus = 0;
    int errorIndex = 0;

    SnmpObjectIdentifier requestedObjectIdentifier = new SnmpObjectIdentifier( itemID );
    SnmpVariablePair nextPair = new SnmpVariablePair( requestedObjectIdentifier, new SnmpInteger( 0 ) );
    SnmpSequence varList = new SnmpSequence();
    varList.addSnmpObject( nextPair );

    SnmpPdu pdu = new SnmpPdu( SnmpBerCodec.SNMPGETNEXTREQUEST, requestID, errorStatus, errorIndex, varList );

    SnmpMessage message = new SnmpMessage( version, community, pdu );

    byte[] messageEncoding = message.getBEREncoding();

    /*
     * System.out.println("Request Message bytes:");
     *
     * for (int i = 0; i < messageEncoding.length; ++i)
     * System.out.print(hexByte(messageEncoding[i]) + " ");
     */
    DatagramPacket outPacket = new DatagramPacket( messageEncoding, messageEncoding.length, hostAddress, hostPort );

    dSocket.send( outPacket );

    while( true )  // wait until receive reply for requestID & OID (or error)
    {

      DatagramPacket inPacket = new DatagramPacket( new byte[MAXSIZE], MAXSIZE );

      dSocket.receive( inPacket );

      byte[] encodedMessage = inPacket.getData();

      /*
       * System.out.println("Message bytes:");
       *
       * for (int i = 0; i < encodedMessage.length; ++i)
       * {
       * System.out.print(hexByte(encodedMessage[i]) + " ");
       * }
       */
      SnmpMessage receivedMessage = new SnmpMessage( SnmpBerCodec.extractNextTLV( encodedMessage, 0 ).value );
      SnmpPdu receivedPDU = receivedMessage.getPDU();

      // check request identifier; if incorrect, just ignore packet and continue waiting
      if( receivedPDU.getRequestID() == requestID )
      {

        // check error status; if retrieval problem, throw SnmpGetException
        if( receivedPDU.getErrorStatus() != 0 )
        {
          throw new SnmpGetException( "OID " + itemID + " not available for retrieval", receivedPDU.getErrorIndex(), receivedPDU.getErrorStatus() );
        }

        varList = receivedPDU.getVarBindList();

        SnmpSequence newPair = (SnmpSequence)( varList.getSnmpObjectAt( 0 ) );

        SnmpObjectIdentifier newObjectIdentifier = (SnmpObjectIdentifier)( newPair.getSnmpObjectAt( 0 ) );
        SnmpObject newValue = newPair.getSnmpObjectAt( 1 );

        retrievedVars.addSnmpObject( newPair );

        break;

      }

    }

    requestID++;

    return retrievedVars;

  }




  /**
   * Retrieve the MIB variable value corresponding to the object identifiers following those
   * given in the itemID array (in dotted-integer notation). Return as SnmpVarBindList object;
   * if no such variable (either due to device not supporting it, or community name having
   * incorrect access privilege), SnmpGetException thrown
   *
   * @param itemID
   *
   * @return the bind list
   * 
   * @throws IOException Thrown when timeout experienced while waiting for response to request.
   * @throws SnmpBadValueException
   * @throws SnmpGetException Thrown if OID following one of supplied OIDs has value that can't be retrieved
   */
  public SnmpVarBindList getNextMIBEntry( String[] itemID ) throws IOException, SnmpBadValueException, SnmpGetException
  {
    // send GetRequest to specified host to retrieve values of specified object identifiers

    SnmpVarBindList retrievedVars = new SnmpVarBindList();
    SnmpSequence varList = new SnmpSequence();

    int errorStatus = 0;
    int errorIndex = 0;

    for( int i = 0; i < itemID.length; i++ )
    {
      SnmpObjectIdentifier requestedObjectIdentifier = new SnmpObjectIdentifier( itemID[i] );
      SnmpVariablePair nextPair = new SnmpVariablePair( requestedObjectIdentifier, new SnmpInteger( 0 ) );
      varList.addSnmpObject( nextPair );
    }

    SnmpPdu pdu = new SnmpPdu( SnmpBerCodec.SNMPGETNEXTREQUEST, requestID, errorStatus, errorIndex, varList );
    SnmpMessage message = new SnmpMessage( version, community, pdu );

    byte[] messageEncoding = message.getBEREncoding();

    /*
     * System.out.println("Request Message bytes:");
     *
     * for (int i = 0; i < messageEncoding.length; ++i)
     * System.out.print(hexByte(messageEncoding[i]) + " ");
     */
    DatagramPacket outPacket = new DatagramPacket( messageEncoding, messageEncoding.length, hostAddress, hostPort );

    dSocket.send( outPacket );

    while( true )  // wait until receive reply for requestID & OID (or error)
    {

      DatagramPacket inPacket = new DatagramPacket( new byte[MAXSIZE], MAXSIZE );

      dSocket.receive( inPacket );

      byte[] encodedMessage = inPacket.getData();

      /*
       * System.out.println("Message bytes:");
       *
       * for (int i = 0; i < encodedMessage.length; ++i)
       * {
       * System.out.print(hexByte(encodedMessage[i]) + " ");
       * }
       */
      SnmpMessage receivedMessage = new SnmpMessage( SnmpBerCodec.extractNextTLV( encodedMessage, 0 ).value );
      SnmpPdu receivedPDU = receivedMessage.getPDU();

      // check request identifier; if incorrect, just ignore packet and continue waiting
      if( receivedPDU.getRequestID() == requestID )
      {

        // check error status; if retrieval problem, throw SnmpGetException
        if( receivedPDU.getErrorStatus() != 0 )
        {
          // determine error index
          errorIndex = receivedPDU.getErrorIndex();

          throw new SnmpGetException( "OID following " + itemID[errorIndex - 1] + " not available for retrieval", errorIndex, receivedPDU.getErrorStatus() );
        }

        // copy info from retrieved sequence to var bind list
        varList = receivedPDU.getVarBindList();

        for( int i = 0; i < varList.size(); i++ )
        {
          SnmpSequence newPair = (SnmpSequence)( varList.getSnmpObjectAt( i ) );

          SnmpObjectIdentifier newObjectIdentifier = (SnmpObjectIdentifier)( newPair.getSnmpObjectAt( 0 ) );
          SnmpObject newValue = newPair.getSnmpObjectAt( 1 );

          retrievedVars.addSnmpObject( newPair );

        }

        break;

      }

    }

    requestID++;

    return retrievedVars;

  }




  /**
   * Set the MIB variable value of the object identifier
   * given in startID (in dotted-integer notation). Return SnmpVarBindList object returned
   * by device in its response; can be used to check that setting was successful.
   * Uses SNMPGetNextRequests to retrieve variable values in sequence.
   *
   * @param itemID
   * @param newValue
   *
   * @return the bind list
   * 
   * @throws IOException Thrown when timeout experienced while waiting for response to request.
   * @throws SnmpBadValueException
   * @throws SnmpSetException
   */
  public SnmpVarBindList setMIBEntry( String itemID, SnmpObject newValue ) throws IOException, SnmpBadValueException, SnmpSetException
  {
    // send SetRequest to specified host to set value of specified object identifier

    SnmpVarBindList retrievedVars = new SnmpVarBindList();

    int errorStatus = 0;
    int errorIndex = 0;

    SnmpObjectIdentifier requestedObjectIdentifier = new SnmpObjectIdentifier( itemID );
    SnmpVariablePair nextPair = new SnmpVariablePair( requestedObjectIdentifier, newValue );

    SnmpSequence varList = new SnmpSequence();
    varList.addSnmpObject( nextPair );

    SnmpPdu pdu = new SnmpPdu( SnmpBerCodec.SNMPSETREQUEST, requestID, errorStatus, errorIndex, varList );

    SnmpMessage message = new SnmpMessage( version, community, pdu );
    byte[] messageEncoding = message.getBEREncoding();

    /*
     * System.out.println("Message bytes:");
     *
     * for (int i = 0; i < messageEncoding.length; ++i)
     * {
     * System.out.print(getHex(messageEncoding[i]) + " ");
     * }
     */
    DatagramPacket outPacket = new DatagramPacket( messageEncoding, messageEncoding.length, hostAddress, hostPort );

    dSocket.send( outPacket );

    while( true )  // wait until receive reply for correct OID (or error)
    {

      DatagramPacket inPacket = new DatagramPacket( new byte[MAXSIZE], MAXSIZE );

      dSocket.receive( inPacket );

      byte[] encodedMessage = inPacket.getData();

      /*
       * System.out.println("Message bytes:");
       *
       * for (int i = 0; i < encodedMessage.length; ++i)
       * {
       * System.out.print((encodedMessage[i]) + " ");
       * }
       */
      SnmpMessage receivedMessage = new SnmpMessage( SnmpBerCodec.extractNextTLV( encodedMessage, 0 ).value );

      SnmpPdu receivedPDU = receivedMessage.getPDU();

      // check request identifier; if incorrect, just ignore packet and continue waiting
      if( receivedPDU.getRequestID() == requestID )
      {

        // check error status; if retrieval problem, throw SnmpGetException
        if( receivedPDU.getErrorStatus() != 0 )
        {
          switch( receivedPDU.getErrorStatus() )
          {

            case 1:
              throw new SnmpSetException( "Value supplied for OID " + itemID + " too big.", receivedPDU.getErrorIndex(), receivedPDU.getErrorStatus() );

            case 2:
              throw new SnmpSetException( "OID " + itemID + " not available for setting.", receivedPDU.getErrorIndex(), receivedPDU.getErrorStatus() );

            case 3:
              throw new SnmpSetException( "Bad value supplied for OID " + itemID + ".", receivedPDU.getErrorIndex(), receivedPDU.getErrorStatus() );

            case 4:
              throw new SnmpSetException( "OID " + itemID + " read-only.", receivedPDU.getErrorIndex(), receivedPDU.getErrorStatus() );

            default:
              throw new SnmpSetException( "Error setting OID " + itemID + ".", receivedPDU.getErrorIndex(), receivedPDU.getErrorStatus() );

          }
        }

        varList = receivedPDU.getVarBindList();

        SnmpSequence newPair = (SnmpSequence)( varList.getSnmpObjectAt( 0 ) );

        // check the object identifier to make sure the correct variable has been received;
        // if not, just continue waiting for receive
        if( ( (SnmpObjectIdentifier)newPair.getSnmpObjectAt( 0 ) ).toString().equals( itemID ) )
        {
          // got the right one; add it to retrieved var list and break!
          retrievedVars.addSnmpObject( newPair );

          break;
        }

      }

    }

    requestID++;

    return retrievedVars;

  }




  /**
   * Set the MIB variable values of the supplied object identifiers given in the
   * itemID array (in dotted-integer notation). Return SnmpVarBindList returned
   * by device in its response; can be used to check that setting was successful.
   * Uses SNMPGetNextRequests to retrieve variable values in sequence.
   *
   * @param itemID
   * @param newValue
   *
   * @return the bind list
   * 
   * @throws IOException Thrown when timeout experienced while waiting for response to request.
   * @throws SnmpBadValueException
   * @throws SnmpSetException
   */
  public SnmpVarBindList setMIBEntry( String[] itemID, SnmpObject[] newValue ) throws IOException, SnmpBadValueException, SnmpSetException
  {
    // check that OID and value arrays have same size
    if( itemID.length != newValue.length )
    {
      throw new SnmpSetException( "OID and value arrays must have same size", 0, SnmpRequestException.FAILED );
    }

    // send SetRequest to specified host to set values of specified object identifiers

    SnmpVarBindList retrievedVars = new SnmpVarBindList();
    SnmpSequence varList = new SnmpSequence();

    int errorStatus = 0;
    int errorIndex = 0;

    for( int i = 0; i < itemID.length; i++ )
    {
      SnmpObjectIdentifier requestedObjectIdentifier = new SnmpObjectIdentifier( itemID[i] );
      SnmpVariablePair nextPair = new SnmpVariablePair( requestedObjectIdentifier, newValue[i] );
      varList.addSnmpObject( nextPair );
    }

    SnmpPdu pdu = new SnmpPdu( SnmpBerCodec.SNMPSETREQUEST, requestID, errorStatus, errorIndex, varList );
    SnmpMessage message = new SnmpMessage( version, community, pdu );

    byte[] messageEncoding = message.getBEREncoding();

    /*
     * System.out.println("Message bytes:");
     *
     * for (int i = 0; i < messageEncoding.length; ++i)
     * {
     * System.out.print(getHex(messageEncoding[i]) + " ");
     * }
     */
    DatagramPacket outPacket = new DatagramPacket( messageEncoding, messageEncoding.length, hostAddress, hostPort );

    dSocket.send( outPacket );

    while( true )  // wait until receive reply for correct OID (or error)
    {

      DatagramPacket inPacket = new DatagramPacket( new byte[MAXSIZE], MAXSIZE );

      dSocket.receive( inPacket );

      byte[] encodedMessage = inPacket.getData();

      /*
       * System.out.println("Message bytes:");
       *
       * for (int i = 0; i < encodedMessage.length; ++i)
       * {
       * System.out.print((encodedMessage[i]) + " ");
       * }
       */
      SnmpMessage receivedMessage = new SnmpMessage( SnmpBerCodec.extractNextTLV( encodedMessage, 0 ).value );

      SnmpPdu receivedPDU = receivedMessage.getPDU();

      // check request identifier; if incorrect, just ignore packet and continue waiting
      if( receivedPDU.getRequestID() == requestID )
      {

        // check error status; if retrieval problem, throw SnmpGetException
        if( receivedPDU.getErrorStatus() != 0 )
        {
          errorIndex = receivedPDU.getErrorIndex();

          switch( receivedPDU.getErrorStatus() )
          {

            case 1:
              throw new SnmpSetException( "Value supplied for OID " + itemID[errorIndex - 1] + " too big.", receivedPDU.getErrorIndex(), receivedPDU.getErrorStatus() );

            case 2:
              throw new SnmpSetException( "OID " + itemID[errorIndex - 1] + " not available for setting.", receivedPDU.getErrorIndex(), receivedPDU.getErrorStatus() );

            case 3:
              throw new SnmpSetException( "Bad value supplied for OID " + itemID[errorIndex - 1] + ".", receivedPDU.getErrorIndex(), receivedPDU.getErrorStatus() );

            case 4:
              throw new SnmpSetException( "OID " + itemID[errorIndex - 1] + " read-only.", receivedPDU.getErrorIndex(), receivedPDU.getErrorStatus() );

            default:
              throw new SnmpSetException( "Error setting OID " + itemID[errorIndex - 1] + ".", receivedPDU.getErrorIndex(), receivedPDU.getErrorStatus() );

          }
        }

        // copy info from retrieved sequence to var bind list
        varList = receivedPDU.getVarBindList();

        for( int i = 0; i < varList.size(); i++ )
        {
          SnmpSequence newPair = (SnmpSequence)( varList.getSnmpObjectAt( i ) );

          SnmpObjectIdentifier newObjectIdentifier = (SnmpObjectIdentifier)( newPair.getSnmpObjectAt( 0 ) );
          // SnmpObject receivedValue = newPair.getSNMPObjectAt(1);

          if( newObjectIdentifier.toString().equals( itemID[i] ) )
          {
            retrievedVars.addSnmpObject( newPair );
          }
          else
          {
            // wrong OID; throw GetException
            throw new SnmpSetException( "OID " + itemID[i] + " expected at index " + i + ", OID " + newObjectIdentifier + " received", i + 1, SnmpRequestException.FAILED );
          }
        }

        break;

      }

    }

    requestID++;

    return retrievedVars;

  }




  /**
   * Retrieve all MIB variable values whose OIDs start with the supplied baseID. Since the entries of
   * an SNMP table have the form  <baseID>.<tableEntry>.<index>, this will retrieve all of the table
   * data as an SnmpVarBindList object consisting of sequence of SNMPVariablePairs.
   * Uses SNMPGetNextRequests to retrieve variable values in sequence.
   *
   * @param baseID
   *
   * @return the bind list
   * 
   * @throws IOException Thrown when timeout experienced while waiting for response to request.
   * @throws SnmpBadValueException
   * @throws SnmpGetException
   */
  public SnmpVarBindList retrieveMIBTable( String baseID ) throws IOException, SnmpBadValueException, SnmpGetException
  {
    // send GetNextRequests until receive
    // an error message or a repeat of the object identifier we sent out
    SnmpVarBindList retrievedVars = new SnmpVarBindList();

    int errorStatus = 0;
    int errorIndex = 0;

    String currentID = baseID;
    SnmpObjectIdentifier requestedObjectIdentifier = new SnmpObjectIdentifier( currentID );

    while( errorStatus == 0 )
    {

      SnmpVariablePair nextPair = new SnmpVariablePair( requestedObjectIdentifier, new SnmpInteger( 0 ) );
      SnmpSequence varList = new SnmpSequence();
      varList.addSnmpObject( nextPair );

      SnmpPdu pdu = new SnmpPdu( SnmpBerCodec.SNMPGETNEXTREQUEST, requestID, errorStatus, errorIndex, varList );
      SnmpMessage message = new SnmpMessage( version, community, pdu );
      byte[] messageEncoding = message.getBEREncoding();
      DatagramPacket outPacket = new DatagramPacket( messageEncoding, messageEncoding.length, hostAddress, hostPort );

      /*
       * System.out.println("Request bytes:");
       *
       * for (int i = 0; i < messageEncoding.length; ++i)
       * {
       * System.out.print(getHex(messageEncoding[i]) + " ");
       * }
       */
      dSocket.send( outPacket );

      DatagramPacket inPacket = new DatagramPacket( new byte[MAXSIZE], MAXSIZE );

      dSocket.receive( inPacket );

      byte[] encodedMessage = inPacket.getData();

      SnmpMessage receivedMessage = new SnmpMessage( SnmpBerCodec.extractNextTLV( encodedMessage, 0 ).value );
      SnmpPdu receivedPDU = receivedMessage.getPDU();

      // check request identifier; if incorrect, just ignore packet and continue waiting
      if( receivedPDU.getRequestID() == requestID )
      {

        // check error status; if retrieval problem, just break - could be there are no additional OIDs
        if( receivedPDU.getErrorStatus() != 0 )
        {
          break;
          // throw new SnmpGetException("OID following " + requestedObjectIdentifier + " not available for retrieval");
        }

        varList = receivedPDU.getVarBindList();

        SnmpSequence newPair = (SnmpSequence)( varList.getSnmpObjectAt( 0 ) );

        SnmpObjectIdentifier newObjectIdentifier = (SnmpObjectIdentifier)( newPair.getSnmpObjectAt( 0 ) );
        SnmpObject newValue = newPair.getSnmpObjectAt( 1 );

        // now see if retrieved ID starts with table base; if not, done with table - break
        String newOIDString = (String)newObjectIdentifier.toString();
        if( !newOIDString.startsWith( baseID ) )
        {
          break;
        }

        retrievedVars.addSnmpObject( newPair );

        requestedObjectIdentifier = newObjectIdentifier;

        requestID++;

      }

    }

    return retrievedVars;

  }




  /**
   * Retrieve all MIB variable values whose OIDs start with the supplied baseIDs. The normal way for
   * this to be used is for the base OID array to consist of the base OIDs of the columns of a table.
   * This method will then retrieve all of the entries of the table corresponding to these columns, one
   * row at a time (i.e., the entries for each row will be retrieved in a single SNMP request). This
   * will retrieve the table data as an SnmpVarBindList object consisting of sequence of SNMPVariablePairs,
   * with the entries for each row grouped together. This may provide a more convenient arrangement of
   * the table data than the simpler retrieveMIBTable method taking a single OID as argument; in addition,
   * it's more efficient, requiring one SNMP request per row rather than one request per entry.
   * Uses SNMPGetNextRequests to retrieve variable values for each row in sequence.
   *
   * @param baseID
   *
   * @return the bind list
   * 
   * @throws IOException Thrown when timeout experienced while waiting for response to request.
   * @throws SnmpBadValueException
   * @throws SnmpGetException Thrown if incomplete row retrieved
   */
  public SnmpVarBindList retrieveMIBTable( String[] baseID ) throws IOException, SnmpBadValueException, SnmpGetException
  {
    // send GetNextRequests until receive
    // an error message or a repeat of the object identifier we sent out
    SnmpVarBindList retrievedVars = new SnmpVarBindList();

    int errorStatus = 0;
    int errorIndex = 0;

    SnmpObjectIdentifier[] requestedObjectIdentifier = new SnmpObjectIdentifier[baseID.length];
    for( int i = 0; i < baseID.length; i++ )
    {
      requestedObjectIdentifier[i] = new SnmpObjectIdentifier( baseID[i] );
    }

    retrievalLoop:

    while( errorStatus == 0 )
    {

      SnmpSequence varList = new SnmpSequence();

      for( int i = 0; i < requestedObjectIdentifier.length; i++ )
      {
        SnmpVariablePair nextPair = new SnmpVariablePair( requestedObjectIdentifier[i], new SnmpInteger( 0 ) );
        varList.addSnmpObject( nextPair );
      }

      SnmpPdu pdu = new SnmpPdu( SnmpBerCodec.SNMPGETNEXTREQUEST, requestID, errorStatus, errorIndex, varList );
      SnmpMessage message = new SnmpMessage( version, community, pdu );

      byte[] messageEncoding = message.getBEREncoding();

      DatagramPacket outPacket = new DatagramPacket( messageEncoding, messageEncoding.length, hostAddress, hostPort );

      /*
       * System.out.println("Request bytes:");
       *
       * for (int i = 0; i < messageEncoding.length; ++i)
       * {
       * System.out.print(getHex(messageEncoding[i]) + " ");
       * }
       */
      dSocket.send( outPacket );

      DatagramPacket inPacket = new DatagramPacket( new byte[MAXSIZE], MAXSIZE );

      dSocket.receive( inPacket );

      byte[] encodedMessage = inPacket.getData();

      SnmpMessage receivedMessage = new SnmpMessage( SnmpBerCodec.extractNextTLV( encodedMessage, 0 ).value );
      SnmpPdu receivedPDU = receivedMessage.getPDU();

      // check request identifier; if incorrect, just ignore packet and continue waiting
      if( receivedPDU.getRequestID() == requestID )
      {

        // check error status; if retrieval problem for error index 1, just break - assume there are no additional OIDs
        // to retrieve. If index is other than 1, throw exception
        if( receivedPDU.getErrorStatus() != 0 )
        {
          int retrievedErrorIndex = receivedPDU.getErrorIndex();

          if( retrievedErrorIndex == 1 )
          {
            break retrievalLoop;
          }
          else
          {
            throw new SnmpGetException( "OID following " + requestedObjectIdentifier[retrievedErrorIndex - 1] + " not available for retrieval", retrievedErrorIndex, receivedPDU.getErrorStatus() );
          }
        }

        // copy info from retrieved sequence to var bind list
        varList = receivedPDU.getVarBindList();

        // make sure got the right number of vars in reply; if not, throw GetException
        if( varList.size() != requestedObjectIdentifier.length )
        {
          throw new SnmpGetException( "Incomplete row of table received", 0, SnmpRequestException.FAILED );
        }

        // copy the retrieved variable pairs into retrievedVars
        for( int i = 0; i < varList.size(); i++ )
        {
          SnmpSequence newPair = (SnmpSequence)( varList.getSnmpObjectAt( i ) );

          SnmpObjectIdentifier newObjectIdentifier = (SnmpObjectIdentifier)( newPair.getSnmpObjectAt( 0 ) );
          SnmpObject newValue = newPair.getSnmpObjectAt( 1 );

          // now see if retrieved ID starts with table base; if not, done with table - break
          String newOIDString = (String)newObjectIdentifier.toString();
          if( !newOIDString.startsWith( baseID[i] ) )
          {
            if( i == 0 )
            {
              // it's the first element of the row; just break
              break retrievalLoop;
            }
            else
            {
              // it's a subsequent row element; throw exception
              throw new SnmpGetException( "Incomplete row of table received", i + 1, SnmpRequestException.FAILED );
            }
          }

          retrievedVars.addSnmpObject( newPair );

          // set requested identifiers array to current identifiers to do get-next for next row
          requestedObjectIdentifier[i] = newObjectIdentifier;
        }

        requestID++;

      }

    }

    return retrievedVars;

  }

  
  
  
  /**
   * Send a discovery request to an network broadcast address.
   * 
   * @param ver
   * @param network
   * @param port
   * @param cmunty
   * @param itemID
   * 
   * @return the string
   * 
   * @throws IOException
   * @throws SnmpBadValueException
   */
  public static String discoverDevices( int ver, InetAddress network, int port, String cmunty, String itemID ) throws IOException, SnmpBadValueException
  {
    // send GetRequest to all hosts to retrieve specified object identifier

    int MAXSIZE = 512;

    StringBuffer retval = new StringBuffer();

    try
    {
      int errorStatus = 0;
      int errorIndex = 0;

      DatagramSocket sok = new DatagramSocket();
      sok.setSoTimeout( 15000 );  // 15 seconds
      
      int requestID = 0;
      SnmpObjectIdentifier requestedObjectIdentifier = new SnmpObjectIdentifier( itemID );
      SnmpVariablePair nextPair = new SnmpVariablePair( requestedObjectIdentifier, new SnmpInteger( 0 ) );
      SnmpSequence varList = new SnmpSequence();
      varList.addSnmpObject( nextPair );
      SnmpPdu pdu = new SnmpPdu( SnmpBerCodec.SNMPGETREQUEST, requestID, errorStatus, errorIndex, varList );
      SnmpMessage message = new SnmpMessage( 0, cmunty, pdu );
      byte[] messageEncoding = message.getBEREncoding();
      DatagramPacket outPacket = new DatagramPacket( messageEncoding, messageEncoding.length, network, port );

      sok.send( outPacket );

      DatagramPacket inPacket = new DatagramPacket( new byte[MAXSIZE], MAXSIZE );

      while( true )
      {
        sok.receive( inPacket );

        retval.append( "Discovered: ");
        retval.append( inPacket.getAddress().getHostName() );
        retval.append( "[");
        retval.append( inPacket.getAddress().getHostAddress() );
        retval.append( "]");

        byte[] encodedMessage = inPacket.getData();
        retval.append( " Bytes: ");
        for( int i = 0; i < encodedMessage.length; ++i )
        {
          retval.append( encodedMessage[i] );
          retval.append(' ');
        }

        SnmpMessage receivedMessage = new SnmpMessage( SnmpBerCodec.extractNextTLV( encodedMessage, 0 ).value );
        retval.append("\nContents: ");
        retval.append( receivedMessage.toString() );
        retval.append( "\n\n" );
      }
    }
    catch( Exception e )
    {
    }

    return retval.toString();
  }




  /**
   * Little-used utility for getting the hex value of a byte.
   * 
   * @param theByte the byte to encode.
   * 
   * @return The zero-padded hex representation of the byte.
   */
  private String getHex( byte theByte )
  {
    int b = theByte;

    if( b < 0 )
      b += 256;

    String returnString = new String( Integer.toHexString( b ) );

    // add leading 0 if needed
    if( returnString.length() % 2 == 1 )
      returnString = "0" + returnString;

    return returnString;
  }

}
