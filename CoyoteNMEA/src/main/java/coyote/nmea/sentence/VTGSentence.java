package coyote.nmea.sentence;

import coyote.nmea.DataNotAvailableException;
import coyote.nmea.FaaMode;
import coyote.nmea.ParseException;
import coyote.nmea.Sentence;


/**
 * Course and speed over the ground. 
 * 
 * <p>True and magnetic COG, speed provided in km/h and knots. Mode (the last 
 * "A" in example sentence) was added in NMEA 2.3 and may not always be 
 * available. Example:<pre>$GPVTG,46.96,T,,,16.89,N,31.28,K,A*43</pre>
 */
public interface VTGSentence extends Sentence {

  /** Char indicator for "true" */
  char TRUE = 'T';
  /** Char indicator for "magnetic" */
  char MAGNETIC = 'M';
  /** Units indicator for kilometers per hour */
  char KMPH = 'K';
  /** Units indicator for knots (nautical miles per hour) */
  char KNOT = 'N';
  /** Operating in manual mode (forced 2D or 3D). */
  char MODE_MANUAL = 'M';
  /** Operating in automatic mode (2D/3D). */
  char MODE_AUTOMATIC = 'A';




  /**
   * Get the magnetic course over ground.
   * 
   * @return Magnetic course
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getMagneticCourse();




  /**
   * Get the FAA operating mode of GPS receiver. The field may not be
   * available, depending on the NMEA version.
   * 
   * @since NMEA 2.3
   * 
   * @return {@link FaaMode} enum
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  FaaMode getMode();




  /**
   * Get current speed over ground, in kilometers per hour.
   * 
   * @return Speed in km/h
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getSpeedKmh();




  /**
   * Get speed over ground in knots.
   * 
   * @return Speed in knots
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getSpeedKnots();




  /**
   * Get the true course over ground.
   * 
   * @return True course, in degrees
   * 
   * @throws DataNotAvailableException If the data is not available.
   * @throws ParseException If the field contains unexpected or illegal value.
   */
  double getTrueCourse();




  /**
   * Set the magnetic course over ground.
   * 
   * @param mcog Course in degrees.
   */
  void setMagneticCourse( double mcog );




  /**
   * Set the FAA operating mode of GPS receiver.
   * 
   * @param mode Mode to set
   * 
   * @since NMEA 2.3
   */
  void setMode( FaaMode mode );




  /**
   * Set the current speed over ground.
   * 
   * @param kmh Speed in kilometers per hour (km/h).
   */
  void setSpeedKmh( double kmh );




  /**
   * Set the speed over ground, in knots.
   * 
   * @param knots Speed in knots
   */
  void setSpeedKnots( double knots );




  /**
   * Set the true course over ground.
   * 
   * @param tcog True course, in degrees
   * 
   * @throws IllegalArgumentException If specified course is out of bounds 
   * 0..360 degrees.
   */
  void setTrueCourse( double tcog );

}