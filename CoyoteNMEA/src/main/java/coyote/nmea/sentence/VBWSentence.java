package coyote.nmea.sentence;

import coyote.nmea.DataStatus;
import coyote.nmea.Sentence;


/**
 * VBW Dual Ground/Water Speed Longitudinal, Transverse and Stern Ground/Water
 * Speed with Status. Example:<pre>
 * $IIVBW,11.0,02.0,A,06.0,03.0,A,05.3,A,01.0,A*43</pre>
 */
public interface VBWSentence extends Sentence {

  /**
   * Returns the Longitudinal Water Speed.
   * 
   * @return Longitudinal Water Speed
   */
  double getLongWaterSpeed();




  /**
   * Returns the Longitudinal Ground Speed.
   * 
   * @return Longitudinal Ground Speed
   */
  double getLongGroundSpeed();




  /**
   * Returns the Transverse Water Speed.
   * 
   * @return Transverse Water Speed
   */
  double getTravWaterSpeed();




  /**
   * Returns the Transverse Ground Speed.
   * 
   * @return Transverse Ground Speed
   */
  double getTravGroundSpeed();




  /**
   * Returns the Water Speed Status.
   * 
   * @return DataStatus Water Speed Status
   */
  DataStatus getWaterSpeedStatus();




  /**
   * Returns the Ground Speed Status.
   * 
   * @return DataStatus Ground Speed Status
   */
  DataStatus getGroundSpeedStatus();




  /**
   * Returns the Stern Water Speed.
   * 
   * @return Stern Water Speed
   */
  double getSternWaterSpeed();




  /**
   * Returns the Stern Water Speed Status.
   * 
   * @return DataStatus Stern Water Speed Status
   */
  DataStatus getSternWaterSpeedStatus();




  /**
   * Returns the Stern Ground Speed.
   * 
   * @return Stern Ground Speed
   * @see #setSternGroundSpeed(double)
   */
  double getSternGroundSpeed();




  /**
   * Returns the Stern Ground Speed Status.
   * 
   * @return DataStatus Stern Ground Speed Status
   */
  DataStatus getSternGroundSpeedStatus();




  /**
   * Sets Longitudinal Water Speed.
   * 
   * @param speed Longitudinal Water Speed.
   */
  void setLongWaterSpeed( double speed );




  /**
   * Sets Longitudinal Ground Speed.
   * 
   * @param speed Longitudinal Ground Speed.
   */
  void setLongGroundSpeed( double speed );




  /**
   * Sets Transverse Water Speed.
   * 
   * @param speed Transverse Water Speed.
   */
  void setTravWaterSpeed( double speed );




  /**
   * Sets Transverse Ground Speed.
   * 
   * @param speed Transverse Ground Speed.
   */
  void setTravGroundSpeed( double speed );




  /**
   * Sets Water Speed Status.
   * 
   * @param status Water Speed Status
   */
  void setWaterSpeedStatus( DataStatus status );




  /**
   * Sets Ground Speed Status.
   * 
   * @param status Ground Speed Status
   */
  void setGroundSpeedStatus( DataStatus status );




  /**
   * Sets Stern Water Speed.
   * 
   * @param speed Stern Water Speed.
   */
  void setSternWaterSpeed( double speed );




  /**
   * Sets Stern Water Speed Status.
   * 
   * @param status Stern Water Speed Status.
   */
  void setSternWaterSpeedStatus( DataStatus status );




  /**
   * Sets Stern Ground Speed.
   * 
   * @param speed Stern Ground Speed.
   */
  void setSternGroundSpeed( double speed );




  /**
   * Sets Stern Ground Speed Status.
   * 
   * @param status Stern Ground Speed Status.
   */
  void setSternGroundSpeedStatus( DataStatus status );

}
