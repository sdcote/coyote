/*
 * $Id:$
 */
package coyote.commons.network.snmp;

import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * SNMP datatype used to represent time value. Just extension of SnmpInteger.
 * 
 * <p>From RFC1155 section 3.2.3.5: This application-wide type represents a 
 * non-negative integer which counts the time in hundredths of a second since 
 * some epoch. When object types are defined in the MIB which use this ASN.1 
 * type, the description of the object type identifies the reference epoch.</p>
 */
public class SnmpTimeTicks extends SnmpInteger
{

  /** The calendar object we use to calculate the UTC timestamp for each frame */
  private static final Calendar cal = GregorianCalendar.getInstance();

  static
  {
    cal.set( Calendar.HOUR, 0 );
    cal.set( Calendar.MINUTE, 0 );
    cal.set( Calendar.SECOND, 0 );
    cal.add( Calendar.HOUR, 0 );
  }



  /**
   * A convenience method to generate a timestamp value.
   * 
   * @return the current number of hundreths of seconds past midnight.
   */
  public static int currentTimeStamp()
  {
    return (int)( System.currentTimeMillis() - cal.getTime().getTime() ) / 10;
  }




  /**
   * Constructor SnmpTimeTicks.
   */
  public SnmpTimeTicks()
  {
    this( 0 );  // initialize value to 0
  }




  /**
   * Number of tickes (in hundreths of a second) since the beginning of some 
   * epoch.
   * 
   * <p>Do not use System.currentTimeMillis() as this will result in a value 
   * that is too large to be encoded in an integer vlaue.</p>
   *
   * @param value
   */
  public SnmpTimeTicks( int value )
  {
    super( value );
    tag = SnmpBerCodec.SNMPTIMETICKS;
  }




  /**
   * Constructor SnmpTimeTicks.
   *
   * @param enc
   *
   * @throws SnmpBadValueException
   */
  protected SnmpTimeTicks( byte[] enc ) throws SnmpBadValueException
  {
    super( enc );
    tag = SnmpBerCodec.SNMPTIMETICKS;
  }

}
