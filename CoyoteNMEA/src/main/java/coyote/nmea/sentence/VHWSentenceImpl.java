package coyote.nmea.sentence;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * WHV sentence implementation.
 */
class VHWSentenceImpl extends AbstractSentence implements VHWSentence {

  private static final int TRUE_HEADING = 0;
  private static final int TRUE_INDICATOR = 1;
  private static final int MAGNETIC_HEADING = 2;
  private static final int MAGNETIC_INDICATOR = 3;
  private static final int SPEED_KNOTS = 4;
  private static final int KNOTS_INDICATOR = 5;
  private static final int SPEED_KMH = 6;
  private static final int KMH_INDICATOR = 7;




  /**
   * Creates a new instance of VHW sentence with given data.
   * 
   * @param nmea VHW sentence String
   */
  public VHWSentenceImpl( String nmea ) {
    super( nmea );
  }




  /**
   * Creates a new empty VHW parser instance.
   * 
   * @param talker Talker ID to set
   */
  public VHWSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.VHW, 8 );
    setCharValue( TRUE_INDICATOR, 'T' );
    setCharValue( MAGNETIC_INDICATOR, 'M' );
    setCharValue( KNOTS_INDICATOR, 'N' );
    setCharValue( KMH_INDICATOR, 'K' );
  }




  /**
   * @see coyote.nmea.sentence.HeadingSentence#getHeading()
   */
  @Override
  public double getHeading() {
    return getDoubleValue( TRUE_HEADING );
  }




  /**
   * @see coyote.nmea.sentence.VHWSentence#getMagneticHeading()
   */
  @Override
  public double getMagneticHeading() {
    return getDoubleValue( MAGNETIC_HEADING );
  }




  /**
   * @see coyote.nmea.sentence.VHWSentence#getSpeedKmh()
   */
  @Override
  public double getSpeedKmh() {
    return getDoubleValue( SPEED_KMH );
  }




  /**
   * @see coyote.nmea.sentence.VHWSentence#getSpeedKnots()
   */
  @Override
  public double getSpeedKnots() {
    return getDoubleValue( SPEED_KNOTS );
  }




  /**
   * @see coyote.nmea.sentence.HeadingSentence#isTrue()
   */
  @Override
  public boolean isTrue() {
    return true;
  }




  /**
   * @see coyote.nmea.sentence.HeadingSentence#setHeading(double)
   */
  @Override
  public void setHeading( double hdg ) {
    setDegreesValue( TRUE_HEADING, hdg );
  }




  /**
   * @see coyote.nmea.sentence.VHWSentence#setMagneticHeading(double)
   */
  @Override
  public void setMagneticHeading( double hdg ) {
    setDegreesValue( MAGNETIC_HEADING, hdg );
  }




  /**
   * @see coyote.nmea.sentence.VHWSentence#setSpeedKmh(double)
   */
  @Override
  public void setSpeedKmh( double kmh ) {
    setDoubleValue( SPEED_KMH, kmh, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.VHWSentence#setSpeedKnots(double)
   */
  @Override
  public void setSpeedKnots( double knots ) {
    setDoubleValue( SPEED_KNOTS, knots, 1, 1 );
  }

}