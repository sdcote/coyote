package coyote.nmea.sentence;

import coyote.nmea.DataStatus;
import coyote.nmea.Sentence;
import coyote.nmea.Side;


/**
 * Rudder angle, measured in degrees. 
 * <p>Negative value represents port side, positive starboard side turn. May 
 * contain value for both port and starboard rudder. {@link Side#PORT} is used 
 * for vessels with single rudder. Example:<pre>$IIRSA,9,A,,*38</pre>
 */
public interface RSASentence extends Sentence {

  /**
   * Returns the rudder angle for specified side.
   * 
   * @return Rudder angle in degrees.
   */
  double getRudderAngle( Side side );




  /**
   * Sets the rudder's angle for specified side.
   * 
   * @param side Rudder side
   * @param angle Rudder angle in degrees
   */
  void setRudderAngle( Side side, double angle );




  /**
   * Returns the data status (valid/invalid) for specified side.
   * 
   * @param side Rudder side
   * @return Data status
   */
  DataStatus getStatus( Side side );




  /**
   * Set data status for specified side.
   * @param side Rudder side
   * @param status Data status to set
   */
  void setStatus( Side side, DataStatus status );

}
