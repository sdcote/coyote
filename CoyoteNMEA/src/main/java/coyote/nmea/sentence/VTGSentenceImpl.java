package coyote.nmea.sentence;

import coyote.nmea.FaaMode;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * VTG sentence implementation.
 */
class VTGSentenceImpl extends AbstractSentence implements VTGSentence {

  private static final int TRUE_COURSE = 0;
  private static final int TRUE_INDICATOR = 1;
  private static final int MAGNETIC_COURSE = 2;
  private static final int MAGNETIC_INDICATOR = 3;
  private static final int SPEED_KNOTS = 4;
  private static final int KNOTS_INDICATOR = 5;
  private static final int SPEED_KMPH = 6;
  private static final int KMPH_INDICATOR = 7;
  private static final int MODE = 8;




  /**
   * Creates a new instance of VTGParser.
   * 
   * @param nmea VTG sentence String
   * 
   * @throws IllegalArgumentException If specified sentence is invalid
   */
  public VTGSentenceImpl( String nmea ) {
    super( nmea, SentenceId.VTG );
  }




  /**
   * Creates VTG parser with empty sentence.
   * 
   * @param talker TalkerId to set
   */
  public VTGSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.VTG, 9 );
    setCharValue( TRUE_INDICATOR, VTGSentence.TRUE );
    setCharValue( MAGNETIC_INDICATOR, VTGSentence.MAGNETIC );
    setCharValue( KNOTS_INDICATOR, VTGSentence.KNOT );
    setCharValue( KMPH_INDICATOR, VTGSentence.KMPH );
  }




  /**
   * @see coyote.nmea.sentence.VTGSentence#getMagneticCourse()
   */
  @Override
  public double getMagneticCourse() {
    return getDoubleValue( MAGNETIC_COURSE );
  }




  /**
   * @see coyote.nmea.sentence.VTGSentence#getMode()
   */
  @Override
  public FaaMode getMode() {
    return FaaMode.valueOf( getCharValue( MODE ) );
  }




  /**
   * @see coyote.nmea.sentence.VTGSentence#getSpeedKmh()
   */
  @Override
  public double getSpeedKmh() {
    return getDoubleValue( SPEED_KMPH );
  }




  /**
   * @see coyote.nmea.sentence.VTGSentence#getSpeedKnots()
   */
  @Override
  public double getSpeedKnots() {
    return getDoubleValue( SPEED_KNOTS );
  }




  /**
   * @see coyote.nmea.sentence.VTGSentence#getTrueCourse()
   */
  @Override
  public double getTrueCourse() {
    return getDoubleValue( TRUE_COURSE );
  }




  /**
   * @see coyote.nmea.sentence.VTGSentence#setMagneticCourse(double)
   */
  @Override
  public void setMagneticCourse( double mcog ) {
    setDegreesValue( MAGNETIC_COURSE, mcog );
  }




  /**
   * @see coyote.nmea.sentence.VTGSentence#setMode(coyote.nmea.FaaMode)
   */
  @Override
  public void setMode( FaaMode mode ) {
    setFieldCount( 9 );
    setCharValue( MODE, mode.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.VTGSentence#setSpeedKmh(double)
   */
  @Override
  public void setSpeedKmh( double kmh ) {
    if ( kmh < 0 ) {
      throw new IllegalArgumentException( "Speed cannot be negative" );
    }
    setDoubleValue( SPEED_KMPH, kmh, 1, 2 );
  }




  /**
   * @see coyote.nmea.sentence.VTGSentence#setSpeedKnots(double)
   */
  @Override
  public void setSpeedKnots( double knots ) {
    if ( knots < 0 ) {
      throw new IllegalArgumentException( "Speed cannot be negative" );
    }
    setDoubleValue( SPEED_KNOTS, knots, 1, 2 );
  }




  /**
   * @see coyote.nmea.sentence.VTGSentence#setTrueCourse(double)
   */
  @Override
  public void setTrueCourse( double tcog ) {
    setDegreesValue( TRUE_COURSE, tcog );
  }

}
