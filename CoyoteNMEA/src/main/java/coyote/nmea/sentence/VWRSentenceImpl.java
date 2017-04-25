package coyote.nmea.sentence;

import coyote.nmea.Direction;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * VWR sentence implementation.
 */
class VWRSentenceImpl extends AbstractSentence implements VWRSentence {

  private static final int WIND_ANGLE_DEGREES = 0;
  private static final int WIND_DIRECTION_LEFT_RIGHT_OF_BOW = 1;
  private static final int SPEED_KNOTS = 2;
  private static final int KNOTS_INDICATOR = 3;
  private static final int SPEED_MPS = 4;
  private static final int MPS_INDICATOR = 5;
  private static final int SPEED_KMPH = 6;
  private static final int KMPH_INDICATOR = 7;




  /**
   * Creates a new instance of VWRParser.
   *
   * @param nmea VWR sentence String
   * @throws IllegalArgumentException If specified sentence is invalid
   */
  public VWRSentenceImpl( String nmea ) {
    super( nmea, SentenceId.VWR );
  }




  /**
   * Creates VWR parser with empty sentence.
   *
   * @param talker TalkerId to set
   */
  public VWRSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.VWR, 9 );
    setCharValue( KNOTS_INDICATOR, VWRSentence.KNOT );
    setCharValue( MPS_INDICATOR, VWRSentence.MPS );
    setCharValue( KMPH_INDICATOR, VWRSentence.KMPH );
  }




  /**
   * @see coyote.nmea.sentence.VWRSentence#getWindAngle()
   */
  @Override
  public double getWindAngle() {
    return getDoubleValue( WIND_ANGLE_DEGREES );
  }




  /**
   * @see coyote.nmea.sentence.VWRSentence#getDirectionLeftRight()
   */
  @Override
  public Direction getDirectionLeftRight() {
    return Direction.valueOf( getCharValue( WIND_DIRECTION_LEFT_RIGHT_OF_BOW ) );
  }




  /**
   * @see coyote.nmea.sentence.VWRSentence#getSpeedKmh()
   */
  @Override
  public double getSpeedKmh() {
    return getDoubleValue( SPEED_KMPH );
  }




  /**
   * @see coyote.nmea.sentence.VWRSentence#getSpeedKnots()
   */
  @Override
  public double getSpeedKnots() {
    return getDoubleValue( SPEED_KNOTS );
  }




  /**
   * @see coyote.nmea.sentence.VWRSentence#setSpeedKmh(double)
   */
  @Override
  public void setSpeedKmh( double kmh ) {
    if ( kmh < 0 ) {
      throw new IllegalArgumentException( "Speed cannot be negative" );
    }
    setDoubleValue( SPEED_KMPH, kmh, 1, 2 );
  }




  /**
   * @see coyote.nmea.sentence.VWRSentence#setSpeedKnots(double)
   */
  @Override
  public void setSpeedKnots( double knots ) {
    if ( knots < 0 ) {
      throw new IllegalArgumentException( "Speed cannot be negative" );
    }
    setDoubleValue( SPEED_KNOTS, knots, 1, 2 );
  }




  /**
   * @see coyote.nmea.sentence.VWRSentence#setWindAngle(double)
   */
  @Override
  public void setWindAngle( double mWindAngle ) {
    setDegreesValue( WIND_ANGLE_DEGREES, mWindAngle );
  }




  /**
   * @see coyote.nmea.sentence.VWRSentence#setDirectionLeftRight(coyote.nmea.Direction)
   */
  @Override
  public void setDirectionLeftRight( Direction directionLeftRight ) {
    setCharValue( WIND_DIRECTION_LEFT_RIGHT_OF_BOW, directionLeftRight.toChar() );
  }
}
