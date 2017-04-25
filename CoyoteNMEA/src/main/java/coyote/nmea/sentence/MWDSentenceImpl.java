package coyote.nmea.sentence;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * Wind speed and direction.
 */
class MWDSentenceImpl extends AbstractSentence implements MWDSentence {

  /**
   * Wind direction, degrees True, to the nearest 0,1 degree.
   */
  private static int WIND_DIRECTION_TRUE = 0;

  /**
   * T = true
   */
  private static int WIND_DIRECTION_TRUE_UNIT = 1;

  /**
   * Wind direction, degrees Magnetic, to the nearest 0,1 degree.
   */
  private static int WIND_DIRECTION_MAGNETIC = 2;

  /**
   * M = magnetic.
   */
  private static int WIND_DIRECTION_MAGNETIC_UNIT = 3;

  /**
   * Wind speed, knots, to the nearest 0,1 knot.
   */
  private static int WIND_SPEED_KNOTS = 4;

  /**
   * N = knots.
   */
  private static int WIND_SPEED_KNOTS_UNIT = 5;

  /**
   * Wind speed, meters per second, to the nearest 0,1 m/s.
   */
  private static int WIND_SPEED_METERS = 6;

  /**
   * M = meters per second
   */
  private static int WIND_SPEED_METERS_UNIT = 7;




  /**
   * Creates a new instance of a MWD sentence implementation.
   * 
   * @param nmea MWV sentence String
   */
  public MWDSentenceImpl( String nmea ) {
    super( nmea, SentenceId.MWD );
  }




  /**
   * Creates a new empty instance of a MWD sentence implementation.
   * 
   * @param talker Talker id to set
   */
  public MWDSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.MWD, 20 );
    setCharValue( WIND_DIRECTION_TRUE_UNIT, 'T' );
    setCharValue( WIND_DIRECTION_MAGNETIC_UNIT, 'M' );
    setCharValue( WIND_SPEED_METERS_UNIT, 'M' );
    setCharValue( WIND_SPEED_KNOTS_UNIT, 'K' );
  }




  /**
   * @see coyote.nmea.sentence.MWDSentence#getMagneticWindDirection()
   */
  @Override
  public double getMagneticWindDirection() {
    if ( hasValue( WIND_DIRECTION_MAGNETIC ) && hasValue( WIND_DIRECTION_MAGNETIC_UNIT ) && getStringValue( WIND_DIRECTION_MAGNETIC_UNIT ).equalsIgnoreCase( "M" ) ) {
      return getDoubleValue( WIND_DIRECTION_MAGNETIC );
    } else {
      return Double.NaN;
    }
  }




  /**
   * @see coyote.nmea.sentence.MWDSentence#getTrueWindDirection()
   */
  @Override
  public double getTrueWindDirection() {
    if ( hasValue( WIND_DIRECTION_TRUE ) && hasValue( WIND_DIRECTION_TRUE_UNIT ) && getStringValue( WIND_DIRECTION_TRUE_UNIT ).equalsIgnoreCase( "T" ) ) {
      return getDoubleValue( WIND_DIRECTION_TRUE );
    } else {
      return Double.NaN;
    }
  }




  /**
   * @see coyote.nmea.sentence.MWDSentence#getWindSpeed()
   */
  @Override
  public double getWindSpeed() {
    if ( hasValue( WIND_SPEED_METERS ) && hasValue( WIND_SPEED_METERS_UNIT ) && getStringValue( WIND_SPEED_METERS_UNIT ).equalsIgnoreCase( "M" ) ) {
      return getDoubleValue( WIND_SPEED_METERS );
    } else {
      return Double.NaN;
    }
  }




  /**
   * @see coyote.nmea.sentence.MWDSentence#getWindSpeedKnots()
   */
  @Override
  public double getWindSpeedKnots() {
    if ( hasValue( WIND_SPEED_KNOTS ) && hasValue( WIND_SPEED_KNOTS_UNIT ) && getStringValue( WIND_SPEED_KNOTS_UNIT ).equalsIgnoreCase( "N" ) ) {
      return getDoubleValue( WIND_SPEED_KNOTS );
    } else {
      return Double.NaN;
    }
  }




  /**
   * @see coyote.nmea.sentence.MWDSentence#setMagneticWindDirection(double)
   */
  @Override
  public void setMagneticWindDirection( double direction ) {
    setDegreesValue( WIND_DIRECTION_MAGNETIC, direction );
  }




  /**
   * @see coyote.nmea.sentence.MWDSentence#setTrueWindDirection(double)
   */
  @Override
  public void setTrueWindDirection( double direction ) {
    setDegreesValue( WIND_DIRECTION_TRUE, direction );
  }




  /**
   * @see coyote.nmea.sentence.MWDSentence#setWindSpeed(double)
   */
  @Override
  public void setWindSpeed( double speed ) {
    setDoubleValue( WIND_SPEED_METERS, speed );
  }




  /**
   * @see coyote.nmea.sentence.MWDSentence#setWindSpeedKnots(double)
   */
  @Override
  public void setWindSpeedKnots( double speed ) {
    setDoubleValue( WIND_SPEED_KNOTS, speed );
  }

}
