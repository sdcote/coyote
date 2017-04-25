/*
 * $Id:$
 */
package coyote.commons.network.snmp;

/**
 * Utility class holding components of an ASN.1 (type, length, value) triple.
 * 
 * TODO - Support tags properly
 */
class SnmpTlv
{

  /** The first byte of the field*/
  byte tag;

  /** 
   * The BER spec indicates the length can be 2^1008 - 1 octets, but that is 
   * _very_ unlikely. 4 GB is sufficient. 
   */
  int totalLength;

  /** Field value */
  byte[] value;

  
  

  // - -
  
  // check bits 7 and 8 (first two from left)
  /** Not Implemented */
  boolean isUniversal()
  {// 00
    return false;
  }




  /** Not Implemented */
  boolean isApplication()
  {// 01
    return false;
  }




  /** Not Implemented */
  boolean isContext()
  {// 10
    return false;
  }




  /** Not Implemented */
  boolean isPrivate()
  {// 11
    return false;
  }

  


  //Check bit 6 (3rd from left), if 0 then primitive, else constructed
  /** Not Implemented */
  boolean isPrimitive()
  {
    return false;
  }




  /** Not Implemented */
  boolean isConstructed()
  {
    return false;
  }

  
  
  
  // bits 1-5 (last 5 from left) for the tag number
  /** Not Implemented */
  short getTagNumber()
  {
    return -1;
  }
  
}
