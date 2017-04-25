package coyote.nmea.sentence;

import coyote.nmea.Direction;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * VWT sentence parser.
 */
class VWTSentenceImpl extends AbstractSentence implements VWTSentence {

  private static final int WIND_ANGLE_DEGREES = 0;
  private static final int WIND_DIRECTION_LEFT_RIGHT_OF_BOW = 1;
  private static final int SPEED_KNOTS = 2;
  private static final int KNOTS_INDICATOR = 3;
  private static final int SPEED_MPS = 4;
  private static final int MPS_INDICATOR = 5;
  private static final int SPEED_KMPH = 6;
  private static final int KMPH_INDICATOR = 7;




  /**
   * Creates a new instance of VWT sentence.
   *
   * @param nmea VWT sentence String
   * 
   * @throws IllegalArgumentException If specified sentence is invalid
   */
  public VWTSentenceImpl( String nmea ) {
    super( nmea, SentenceId.VWT );
  }




  /**
   * Creates empty VWT implementation.
   *
   * @param talker TalkerId to set
   */
  public VWTSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.VWT, 9 );
    setCharValue( KNOTS_INDICATOR, VWTSentence.KNOT );
    setCharValue( MPS_INDICATOR, VWTSentence.MPS );
    setCharValue( KMPH_INDICATOR, VWTSentence.KMPH );
  }




  /**
   * @see coyote.nmea.sentence.VWTSentence#getWindAngle()
   */
  @Override
  public double getWindAngle() {
    return getDoubleValue( WIND_ANGLE_DEGREES );
  }




  /**
   * @see coyote.nmea.sentence.VWTSentence#getDirectionLeftRight()
   */
  @Override
  public Direction getDirectionLeftRight() {
    return Direction.valueOf( getCharValue( WIND_DIRECTION_LEFT_RIGHT_OF_BOW ) );
  }




  /**
   * @see coyote.nmea.sentence.VWTSentence#getSpeedKmh()
   */
  @Override
  public double getSpeedKmh() {
    return getDoubleValue( SPEED_KMPH );
  }




  /**
   * @see coyote.nmea.sentence.VWTSentence#getSpeedKnots()
   */
  @Override
  public double getSpeedKnots() {
    return getDoubleValue( SPEED_KNOTS );
  }




  /**
   * @see coyote.nmea.sentence.VWTSentence#setSpeedKmh(double)
   */
  @Override
  public void setSpeedKmh( double kmh ) {
    if ( kmh < 0 ) {
      throw new IllegalArgumentException( "Speed cannot be negative" );
    }
    setDoubleValue( SPEED_KMPH, kmh, 1, 2 );
  }




  /**
   * @see coyote.nmea.sentence.VWTSentence#setSpeedKnots(double)
   */
  @Override
  public void setSpeedKnots( double knots ) {
    if ( knots < 0 ) {
      throw new IllegalArgumentException( "Speed cannot be negative" );
    }
    setDoubleValue( SPEED_KNOTS, knots, 1, 2 );
  }




  /**
   * @see coyote.nmea.sentence.VWTSentence#setWindAngle(double)
   */
  @Override
  public void setWindAngle( double mWindAngle ) {
    setDegreesValue( WIND_ANGLE_DEGREES, mWindAngle );
  }




  /**
   * @see coyote.nmea.sentence.VWTSentence#setDirectionLeftRight(coyote.nmea.Direction)
   */
  @Override
  public void setDirectionLeftRight( Direction directionLeftRight ) {
    setCharValue( WIND_DIRECTION_LEFT_RIGHT_OF_BOW, directionLeftRight.toChar() );
  }

}