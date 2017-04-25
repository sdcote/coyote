package coyote.nmea.sentence;

import coyote.nmea.DataStatus;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * VBW sentence implementation.
 */
class VBWSentenceImpl extends AbstractSentence implements VBWSentence {

  public static final int LONG_WATERSPEED = 0;
  public static final int TRAV_WATERSPEED = 1;
  public static final int WATER_SPEED_STATUS = 2;
  public static final int LONG_GROUNDSPEED = 3;
  public static final int TRAV_GROUNDSPEED = 4;
  public static final int GROUND_SPEED_STATUS = 5;
  public static final int STERN_WATERSPEED = 6;
  public static final int STERN_SPEED_STATUS = 7;
  public static final int STERN_GROUNDSPEED = 8;
  public static final int STERN_GROUNDSPEED_STATUS = 9;




  /**
   * Create a new instance of VBW sentence.
   * 
   * @param nmea VBW sentence String.
   * 
   * @throws IllegalArgumentException If specified sentence is invalid.
   */
  public VBWSentenceImpl( String nmea ) {
    super( nmea, SentenceId.VBW );
  }




  /**
   * Create a VBW implementation with an empty sentence.
   * 
   * @param talker TalkerId to set
   */
  public VBWSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.VBW, 10 );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#getLongWaterSpeed()
   */
  @Override
  public double getLongWaterSpeed() {
    return getDoubleValue( LONG_WATERSPEED );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#getWaterSpeedStatus()
   */
  @Override
  public DataStatus getWaterSpeedStatus() {
    return DataStatus.valueOf( getCharValue( WATER_SPEED_STATUS ) );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#getGroundSpeedStatus()
   */
  @Override
  public DataStatus getGroundSpeedStatus() {
    return DataStatus.valueOf( getCharValue( GROUND_SPEED_STATUS ) );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#getLongGroundSpeed()
   */
  @Override
  public double getLongGroundSpeed() {
    return getDoubleValue( LONG_GROUNDSPEED );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#getTravWaterSpeed()
   */
  @Override
  public double getTravWaterSpeed() {
    return getDoubleValue( TRAV_WATERSPEED );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#getTravGroundSpeed()
   */
  @Override
  public double getTravGroundSpeed() {
    return getDoubleValue( TRAV_GROUNDSPEED );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#getSternWaterSpeed()
   */
  @Override
  public double getSternWaterSpeed() {
    return getDoubleValue( STERN_WATERSPEED );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#getSternWaterSpeedStatus()
   */
  @Override
  public DataStatus getSternWaterSpeedStatus() {
    return DataStatus.valueOf( getCharValue( STERN_SPEED_STATUS ) );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#getSternGroundSpeed()
   */
  @Override
  public double getSternGroundSpeed() {
    return getDoubleValue( STERN_GROUNDSPEED );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#getSternGroundSpeedStatus()
   */
  @Override
  public DataStatus getSternGroundSpeedStatus() {
    return DataStatus.valueOf( getCharValue( STERN_GROUNDSPEED_STATUS ) );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#setLongWaterSpeed(double)
   */
  @Override
  public void setLongWaterSpeed( double speed ) {
    setDoubleValue( LONG_WATERSPEED, speed, 2, 1 );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#setLongGroundSpeed(double)
   */
  @Override
  public void setLongGroundSpeed( double speed ) {
    setDoubleValue( LONG_GROUNDSPEED, speed, 2, 1 );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#setTravWaterSpeed(double)
   */
  @Override
  public void setTravWaterSpeed( double speed ) {
    setDoubleValue( TRAV_WATERSPEED, speed, 2, 1 );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#setTravGroundSpeed(double)
   */
  @Override
  public void setTravGroundSpeed( double speed ) {
    setDoubleValue( TRAV_GROUNDSPEED, speed, 2, 1 );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#setWaterSpeedStatus(coyote.nmea.DataStatus)
   */
  @Override
  public void setWaterSpeedStatus( DataStatus status ) {
    setCharValue( WATER_SPEED_STATUS, status.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#setGroundSpeedStatus(coyote.nmea.DataStatus)
   */
  @Override
  public void setGroundSpeedStatus( DataStatus status ) {
    setCharValue( GROUND_SPEED_STATUS, status.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#setSternWaterSpeed(double)
   */
  @Override
  public void setSternWaterSpeed( double speed ) {
    setDoubleValue( STERN_WATERSPEED, speed, 2, 1 );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#setSternWaterSpeedStatus(coyote.nmea.DataStatus)
   */
  @Override
  public void setSternWaterSpeedStatus( DataStatus status ) {
    setCharValue( STERN_SPEED_STATUS, status.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#setSternGroundSpeed(double)
   */
  @Override
  public void setSternGroundSpeed( double speed ) {
    setDoubleValue( STERN_GROUNDSPEED, speed, 2, 1 );
  }




  /**
   * @see coyote.nmea.sentence.VBWSentence#setSternGroundSpeedStatus(coyote.nmea.DataStatus)
   */
  @Override
  public void setSternGroundSpeedStatus( DataStatus status ) {
    setCharValue( STERN_GROUNDSPEED_STATUS, status.toChar() );
  }

}
