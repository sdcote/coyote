package coyote.nmea.sentence;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;
import coyote.nmea.Units;


/**
 * VDR sentence implementation.
 */
class VDRSentenceImpl extends AbstractSentence implements VDRSentence {

  private static final int TRUE_DIRECTION = 0;
  private static final int TRUE_INDICATOR = 1;
  private static final int MAGN_DIRECTION = 2;
  private static final int MAGN_INDICATOR = 3;
  private static final int SPEED = 4;
  private static final int SPEED_UNITS = 5;




  /**
   * Creates a new instance of aVDR sentence implementation.
   * 
   * @param nmea VDR sentence String
   */
  public VDRSentenceImpl( String nmea ) {
    super( nmea );
  }




  /**
   * Creates a new empty instance of VDR sentence.
   * 
   * @param tid TalkerId to set
   */
  public VDRSentenceImpl( TalkerId tid ) {
    super( tid, SentenceId.VDR, 6 );
    setCharValue( TRUE_INDICATOR, 'T' );
    setCharValue( MAGN_INDICATOR, 'M' );
    setCharValue( SPEED_UNITS, Units.KNOT.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.VDRSentence#getMagneticDirection()
   */
  @Override
  public double getMagneticDirection() {
    return getDoubleValue( MAGN_DIRECTION );
  }




  /**
   * @see coyote.nmea.sentence.VDRSentence#getSpeed()
   */
  @Override
  public double getSpeed() {
    return getDoubleValue( SPEED );
  }




  /**
   * @see coyote.nmea.sentence.VDRSentence#getTrueDirection()
   */
  @Override
  public double getTrueDirection() {
    return getDoubleValue( TRUE_DIRECTION );
  }




  /**
   * @see coyote.nmea.sentence.VDRSentence#setMagneticDirection(double)
   */
  @Override
  public void setMagneticDirection( double direction ) {
    setDegreesValue( MAGN_DIRECTION, direction );
  }




  /**
   * @see coyote.nmea.sentence.VDRSentence#setSpeed(double)
   */
  @Override
  public void setSpeed( double speed ) {
    setDoubleValue( SPEED, speed, 0, 1 );
  }




  /**
   * @see coyote.nmea.sentence.VDRSentence#setTrueDirection(double)
   */
  @Override
  public void setTrueDirection( double direction ) {
    setDegreesValue( TRUE_DIRECTION, direction );
  }

}
