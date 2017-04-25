package coyote.nmea.sentence;

import coyote.nmea.Sentence;


/**
 * Datum reference.
 * 
 * <p>Example:<pre>$GPDTM,W84,,0.000000,N,0.000000,E,0.0,W84*6F</pre>
 */
public interface DTMSentence extends Sentence {

  /**
   * Returns the altitude offset.
   * 
   * @return Altitude offset, in meters.
   */
  double getAltitudeOffset();




  /**
   * Returns the local datum code.
   * 
   * @return Datum code
   */
  String getDatumCode();




  /**
   * Returns the local datum subcode, may be blank.
   * 
   * @return Datum subcode
   */
  String getDatumSubCode();




  /**
   * Returns the latitude offset. Positive values depict northern offset,
   * negative for southern.
   * 
   * @return Offset value in minutes.
   */
  double getLatitudeOffset();




  /**
   * Returns the longitude offset. Positive values for east, negative west.
   * 
   * @return Longitude offset in minutes.
   */
  double getLongitudeOffset();




  /**
   * Returns the datum name, e.g. "W84" for WGS84 used by GPS.
   * 
   * @return Datum name
   */
  String getName();




  /**
   * Sets the local datum code.
   * 
   * @param code Code to set
   */
  void setDatumCode( String code );




  /**
   * Sets the local datum code, may be blank.
   * 
   * @param code Code to set
   */
  void setDatumSubCode( String code );




  /**
   * Sets the latitude offset. Positive values depict northern offset,
   * negative for southern.
   * 
   * @param offset Latitude offset in minutes.
   */
  void setLatitudeOffset( double offset );




  /**
   * Sets the longitude offset. Positive values for east, negative west.
   * 
   * @param offset Longitude offset in minutes.
   */
  void setLongitudeOffset( double offset );




  /**
   * Sets the datum name.
   * 
   * @param name Name to set.
   */
  void setName( String name );

}
