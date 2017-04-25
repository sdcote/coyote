/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import coyote.commons.network.snmp.SnmpBerCodec;
import coyote.commons.network.snmp.SnmpCounter32;
import coyote.commons.network.snmp.SnmpCounter64;
import coyote.commons.network.snmp.SnmpGauge32;
import coyote.commons.network.snmp.SnmpInteger;
import coyote.commons.network.snmp.SnmpIpAddress;
import coyote.commons.network.snmp.SnmpMessage;
import coyote.commons.network.snmp.SnmpNsapAddress;
import coyote.commons.network.snmp.SnmpObject;
import coyote.commons.network.snmp.SnmpObjectIdentifier;
import coyote.commons.network.snmp.SnmpOctetString;
import coyote.commons.network.snmp.SnmpPdu;
import coyote.commons.network.snmp.SnmpSequence;
import coyote.commons.network.snmp.SnmpTimeTicks;
import coyote.commons.network.snmp.SnmpUInteger32;

/**
 * Class TestSNMP.
 *
 * @version $Revision:$
 */
public class TestSNMP
{

  /**
   * Method main.
   *
   * @param args
   */
  public static void main( String args[] )
  {

    try
    {

      byte[] encoding;

      // instantiate and check out BER encodings of various types
      SnmpObject snmpObject;

      snmpObject = new SnmpCounter32( 127 );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );

      snmpObject = SnmpBerCodec.extractEncoding( SnmpBerCodec.extractNextTLV( snmpObject.getBEREncoding(), 0 ) );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );
      printBEREncoding( snmpObject );

      snmpObject = new SnmpCounter64( 128 * 128 );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );

      snmpObject = SnmpBerCodec.extractEncoding( SnmpBerCodec.extractNextTLV( snmpObject.getBEREncoding(), 0 ) );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );
      printBEREncoding( snmpObject );

      snmpObject = new SnmpGauge32( 1024 * 1024 * 1024 * 3 );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );

      snmpObject = SnmpBerCodec.extractEncoding( SnmpBerCodec.extractNextTLV( snmpObject.getBEREncoding(), 0 ) );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );
      printBEREncoding( snmpObject );

      snmpObject = new SnmpInteger( 128 );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );

      snmpObject = SnmpBerCodec.extractEncoding( SnmpBerCodec.extractNextTLV( snmpObject.getBEREncoding(), 0 ) );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );
      printBEREncoding( snmpObject );

      snmpObject = new SnmpIpAddress( "128.20.255.13" );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );

      snmpObject = SnmpBerCodec.extractEncoding( SnmpBerCodec.extractNextTLV( snmpObject.getBEREncoding(), 0 ) );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );
      printBEREncoding( snmpObject );

      snmpObject = new SnmpNsapAddress( "12.34.56.78.90.AB" );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );

      snmpObject = SnmpBerCodec.extractEncoding( SnmpBerCodec.extractNextTLV( snmpObject.getBEREncoding(), 0 ) );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );
      printBEREncoding( snmpObject );

      snmpObject = new SnmpObjectIdentifier( "1.3.2.4.8.16.32.64.128.256.512.1024" );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );

      snmpObject = SnmpBerCodec.extractEncoding( SnmpBerCodec.extractNextTLV( snmpObject.getBEREncoding(), 0 ) );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );
      printBEREncoding( snmpObject );

      snmpObject = new SnmpOctetString( "Howdy doody!" );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );

      snmpObject = SnmpBerCodec.extractEncoding( SnmpBerCodec.extractNextTLV( snmpObject.getBEREncoding(), 0 ) );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );
      printBEREncoding( snmpObject );

      snmpObject = new SnmpTimeTicks( 12345 );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );

      snmpObject = SnmpBerCodec.extractEncoding( SnmpBerCodec.extractNextTLV( snmpObject.getBEREncoding(), 0 ) );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );
      printBEREncoding( snmpObject );

      snmpObject = new SnmpUInteger32( 12345 );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );

      snmpObject = SnmpBerCodec.extractEncoding( SnmpBerCodec.extractNextTLV( snmpObject.getBEREncoding(), 0 ) );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );
      printBEREncoding( snmpObject );

      // now play with a sequence
      SnmpSequence snmpSequence = new SnmpSequence();
      snmpSequence.addSnmpObject( new SnmpCounter32( 127 ) );
      snmpSequence.addSnmpObject( new SnmpOctetString( "abc" ) );
      snmpSequence.addSnmpObject( new SnmpNsapAddress( "12.34.56.78.90.AB" ) );

      snmpObject = snmpSequence;

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );

      snmpObject = SnmpBerCodec.extractEncoding( SnmpBerCodec.extractNextTLV( snmpObject.getBEREncoding(), 0 ) );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );
      printBEREncoding( snmpObject );

      // create a PDU
      SnmpPdu pdu = new SnmpPdu( SnmpBerCodec.SNMPGETREQUEST, 64, 0, 0, snmpSequence );
      snmpObject = pdu;

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );

      snmpObject = SnmpBerCodec.extractEncoding( SnmpBerCodec.extractNextTLV( snmpObject.getBEREncoding(), 0 ) );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );
      printBEREncoding( snmpObject );

      // create a message
      SnmpMessage message = new SnmpMessage( 0, "community", pdu );
      snmpObject = message;

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );

      snmpObject = SnmpBerCodec.extractEncoding( SnmpBerCodec.extractNextTLV( snmpObject.getBEREncoding(), 0 ) );

      System.out.println( "Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString() );
      printBEREncoding( snmpObject );

      /*
       *
       * // create a communications interface to a remote SNMP-capable device;
       * // need to provide the remote host's InetAddress and the community
       * // name for the device; in addition, need to  supply the version number
       * // for the SNMP messages to be sent (the value 0 corresponding to SNMP
       * // version 1)
       * InetAddress hostAddress = InetAddress.getByName("10.0.1.1");
       * String community = "public";
       * int version = 0;        // SNMPv1
       *
       * SimpleSnmpClient comInterface = new SimpleSnmpClient(version, hostAddress, community);
       *
       *
       *
       * // now send an SNMP GET request to retrieve the value of the SNMP variable
       * // corresponding to OID 1.3.6.1.2.1.2.1.0; this is the OID corresponding to
       * // the device identifying string, and the type is thus SnmpOctetString
       * String itemID = "1.3.6.1.2.1.1.1.0";
       *
       * System.out.println("Retrieving value corresponding to OID " + itemID);
       *
       * // the getMIBEntry method of the communications interface returns an SnmpVarBindList
       * // object; this is essentially a Vector of SNMP (OID,value) pairs. In this case, the
       * // returned Vector has just one pair inside it.
       * SnmpVarBindList newVars = comInterface.getMIBEntry(itemID);
       *
       * // extract the (OID,value) pair from the SnmpVarBindList; the pair is just a two-element
       * // SnmpSequence
       * SnmpSequence pair = (SnmpSequence)(newVars.getSNMPObjectAt(0));
       *
       * // extract the object identifier from the pair; it's the first element in the sequence
       * SnmpObjectIdentifier snmpOID = (SnmpObjectIdentifier)pair.getSNMPObjectAt(0);
       *
       * // extract the corresponding value from the pair; it's the second element in the sequence
       * SnmpObject snmpValue = pair.getSNMPObjectAt(1);
       *
       * // print out the String representation of the retrieved value
       * System.out.println("Retrieved value: type " + snmpValue.getClass().getName() + ", value " + snmpValue.toString());
       *
       * // the retrieved value can be obtained from the SnmpObject using the getValue method;
       * // the return type of the method is the generic base class Object, and must be cast to
       * // the appropriate actual Java type; in this case, for an SnmpOctetString, the underlying
       * // Java type is a byte array[]
       * byte[] javaByteArrayValue = (byte[])snmpValue.getValue();
       *
       *
       *
       * // now send an SNMP GET request to retrieve the value of the SNMP variable
       * // corresponding to OID 1.3.6.1.2.1.1.3.0; this is the OID corresponding to
       * // the uptime of the device, and the return type is thus SnmpTimeTicks
       * itemID = "1.3.6.1.2.1.1.3.0";
       *
       * System.out.println("Retrieving value corresponding to OID " + itemID);
       *
       * // the getMIBEntry method of the communications interface returns an SnmpVarBindList
       * // object; this is essentially a Vector of SNMP (OID,value) pairs. In this case, the
       * // returned Vector has just one pair inside it.
       * newVars = comInterface.getMIBEntry(itemID);
       *
       * // extract the (OID,value) pair from the SnmpVarBindList; the pair is just a two-element
       * // SnmpSequence
       * pair = (SnmpSequence)(newVars.getSNMPObjectAt(0));
       *
       * // extract the object identifier from the pair; it's the first element in the sequence
       * snmpOID = (SnmpObjectIdentifier)pair.getSNMPObjectAt(0);
       *
       * // extract the corresponding value from the pair; it's the second element in the sequence
       * snmpValue = pair.getSNMPObjectAt(1);
       *
       * // print out the String representation of the retrieved value
       * System.out.println("Retrieved value: type " + snmpValue.getClass().getName() + ", value " + snmpValue.toString());
       *
       * // the retrieved value can be obtained from the SnmpObject using the getValue method;
       * // the return type of the method is the generic base class Object, and must be cast to
       * // the appropriate actual Java type; in this case, for SnmpTimeTicks, which is a subclass
       * // of SnmpInteger, the actual type is BigInteger (which permits arbitrarily large values to
       * // be represented).
       * BigInteger javaIntegerValue = (BigInteger)snmpValue.getValue();
       *
       *
       *
       * // now send an SNMP SET request to set the value of the SNMP variable
       * // corresponding to OID 1.3.6.1.2.1.1.1.0; this is the OID corresponding to
       * // the device identifying string, and the type is thus SnmpOctetString;
       * // to set a new value, a string is supplied
       * itemID = "1.3.6.1.2.1.1.1.0";
       *
       * SnmpOctetString newValue = new SnmpOctetString("New device name");
       *
       * System.out.println("Setting value corresponding to OID " + itemID);
       * System.out.println("New value: " + newValue.toString());
       *
       * // the setMIBEntry method of the communications interface returns the SnmpVarBindList
       * // corresponding to the supplied OID and value
       * // This call will probably cause an SnmpSetException to be thrown, since the
       * // community name "public" is probably not the read/write password of the device
       * newVars = comInterface.setMIBEntry(itemID, newValue);
       *
       */
    }
    catch( Exception e )
    {
      System.out.println( "Exception during SNMP operation:  " + e + "\n" );
    }

  }




  /**
   * Method printBEREncoding.
   *
   * @param object
   */
  private static void printBEREncoding( SnmpObject object )
  {
    byte[] encoding = object.getBEREncoding();

    System.out.println( "BER encoding:" );

    for( int i = 0; i < encoding.length; ++i )
    {
      System.out.print( hexByte( encoding[i] ) + " " );
    }

    System.out.println( "\n" );
  }




  /**
   * Method hexByte.
   *
   * @param b
   *
   * @return
   */
  private static String hexByte( byte b )
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
