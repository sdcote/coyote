package coyote.nmea.sentence;

import coyote.nmea.DataStatus;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;
import coyote.nmea.Units;


/**
 * MWV sentence implementation.
 */
class MWVSentenceImpl extends AbstractSentence implements MWVSentence {

  private static final int WIND_ANGLE = 0;
  private static final int REFERENCE = 1;
  private static final int WIND_SPEED = 2;
  private static final int SPEED_UNITS = 3;
  private static final int DATA_STATUS = 4;




  /**
   * Creates a new instance of a MWV sentence implementation.
   * 
   * @param nmea MWV sentence String
   */
  public MWVSentenceImpl( String nmea ) {
    super( nmea, SentenceId.MWV );
  }




  /**
   * Creates a new empty instance of a MWV sentence implementation.
   * 
   * @param talker Talker id to set
   */
  public MWVSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.MWV, 5 );
    setCharValue( DATA_STATUS, DataStatus.VOID.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.MWVSentence#getAngle()
   */
  @Override
  public double getAngle() {
    return getDoubleValue( WIND_ANGLE );
  }




  /**
   * @see coyote.nmea.sentence.MWVSentence#getSpeed()
   */
  @Override
  public double getSpeed() {
    return getDoubleValue( WIND_SPEED );
  }




  /**
   * @see coyote.nmea.sentence.MWVSentence#getSpeedUnit()
   */
  @Override
  public Units getSpeedUnit() {
    return Units.valueOf( getCharValue( SPEED_UNITS ) );
  }




  /**
   * @see coyote.nmea.sentence.MWVSentence#getStatus()
   */
  @Override
  public DataStatus getStatus() {
    return DataStatus.valueOf( getCharValue( DATA_STATUS ) );
  }




  /**
   * @see coyote.nmea.sentence.MWVSentence#isTrue()
   */
  @Override
  public boolean isTrue() {
    char ch = getCharValue( REFERENCE );
    return ch == 'T';
  }




  /**
   * @see coyote.nmea.sentence.MWVSentence#setAngle(double)
   */
  @Override
  public void setAngle( double angle ) {
    setDegreesValue( WIND_ANGLE, angle );
  }




  /**
   * @see coyote.nmea.sentence.MWVSentence#setSpeed(double)
   */
  @Override
  public void setSpeed( double speed ) {
    if ( speed < 0 ) {
      throw new IllegalArgumentException( "Speed must be positive" );
    }
    setDoubleValue( WIND_SPEED, speed, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.MWVSentence#setSpeedUnit(coyote.nmea.Units)
   */
  @Override
  public void setSpeedUnit( Units unit ) {
    if ( unit == Units.METER || unit == Units.KMH || unit == Units.KNOT ) {
      setCharValue( SPEED_UNITS, unit.toChar() );
      return;
    }
    throw new IllegalArgumentException( "Invalid unit for speed" );
  }




  /**
   * @see coyote.nmea.sentence.MWVSentence#setStatus(coyote.nmea.DataStatus)
   */
  @Override
  public void setStatus( DataStatus status ) {
    setCharValue( DATA_STATUS, status.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.MWVSentence#setTrue(boolean)
   */
  @Override
  public void setTrue( boolean isTrue ) {
    if ( isTrue ) {
      setCharValue( REFERENCE, 'T' );
    } else {
      setCharValue( REFERENCE, 'R' );
    }
  }

}
