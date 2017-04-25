package coyote.nmea.sentence;

import coyote.nmea.DataStatus;
import coyote.nmea.Sentence;


/**
 * Revolutions, measured from shaft or engine.
 */
public interface RPMSentence extends Sentence {

  /** Source indicator for engine */
  public static final char ENGINE = 'E';

  /** Source indicator for shaft */
  public static final char SHAFT = 'S';




  /**
   * Returns the engine or shaft number/id.
   * 
   * @return Engine of shaft number
   */
  int getId();




  /**
   * Returns the propeller pitch, % of maximum.
   * 
   * @return Pitch value, negative values denote astern.
   */
  double getPitch();




  /**
   * Returns the revolutions value.
   * 
   * @return Speed, revolutions per minute.
   */
  double getRPM();




  /**
   * Returns the measurement source, engine or shaft.
   * 
   * @return 'E' for engine, 'S' for shaft.
   */
  char getSource();




  /**
   * Returns the data validity status.
   * 
   * @return DataStatus
   */
  DataStatus getStatus();




  /**
   * Tells if the data source is engine.
   * 
   * @return True if engine, otherwise false.
   */
  boolean isEngine();




  /**
   * Tells if the data source is shaft.
   * 
   * @return True for shaft, otherwise false.
   */
  boolean isShaft();




  /**
   * Sets the engine or shaft number/id.
   * 
   * @param id ID to set.
   */
  void setId( int id );




  /**
   * Sets the propeller pitch, % of maximum.
   * 
   * @param pitch Pitch value to set, negative values denote astern.
   */
  void setPitch( double pitch );




  /**
   * Sets the source indicator, engine or shaft.
   * 
   * @param source 'E' for engine or 'S' for shaft.
   * 
   * @throws IllegalArgumentException If specified char is not 'E' or 'S'.
   */
  void setSource( char source );




  /**
   * Sets the data validity status.
   * 
   * @param status DataStatus to set.
   */
  void setStatus( DataStatus status );

}
