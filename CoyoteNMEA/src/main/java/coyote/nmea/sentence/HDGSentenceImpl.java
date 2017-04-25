package coyote.nmea.sentence;

import coyote.nmea.CompassPoint;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * HDG sentence implementation.
 */
class HDGSentenceImpl extends AbstractSentence implements HDGSentence {

  private static final int HEADING = 0;
  private static final int DEVIATION = 1;
  private static final int DEV_DIRECTION = 2;
  private static final int VARIATION = 3;
  private static final int VAR_DIRECTION = 4;




  /**
   * Creates a new HDG sentence implementation.
   * 
   * @param nmea HDG sentence String
   */
  public HDGSentenceImpl( String nmea ) {
    super( nmea, SentenceId.HDG );
  }




  /**
   * Creates a new empty HDG sentence implementation.
   * 
   * @param talker Talker id to set
   */
  public HDGSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.HDG, 5 );
  }




  /**
   * @see coyote.nmea.sentence.HDGSentence#getDeviation()
   */
  @Override
  public double getDeviation() {
    double dev = getDoubleValue( DEVIATION );
    if ( dev == 0 ) {
      return dev;
    }
    CompassPoint dir = CompassPoint.valueOf( getCharValue( DEV_DIRECTION ) );
    return dir == CompassPoint.WEST ? -dev : dev;
  }




  /**
   * @see coyote.nmea.sentence.HeadingSentence#getHeading()
   */
  @Override
  public double getHeading() {
    return getDoubleValue( HEADING );
  }




  /**
   * @see coyote.nmea.sentence.HDGSentence#getVariation()
   */
  @Override
  public double getVariation() {
    double var = getDoubleValue( VARIATION );
    if ( var == 0 ) {
      return var;
    }
    CompassPoint dir = CompassPoint.valueOf( getCharValue( VAR_DIRECTION ) );
    return dir == CompassPoint.WEST ? -var : var;
  }




  /**
   * @see coyote.nmea.sentence.HeadingSentence#isTrue()
   */
  @Override
  public boolean isTrue() {
    return false;
  }




  /**
   * @see coyote.nmea.sentence.HDGSentence#setDeviation(double)
   */
  @Override
  public void setDeviation( double deviation ) {
    if ( deviation < -180 || deviation > 180 ) {
      throw new IllegalArgumentException( "Value out of range [-180..180]" );
    }
    if ( deviation > 0 ) {
      setCharValue( DEV_DIRECTION, CompassPoint.EAST.toChar() );
    } else if ( deviation < 0 ) {
      setCharValue( DEV_DIRECTION, CompassPoint.WEST.toChar() );
    } else {
      setStringValue( DEV_DIRECTION, "" );
    }
    setDoubleValue( DEVIATION, Math.abs( deviation ), 3, 1 );
  }




  /**
   * @see coyote.nmea.sentence.HeadingSentence#setHeading(double)
   */
  @Override
  public void setHeading( double heading ) {
    setDegreesValue( HEADING, heading );
  }




  /**
   * @see coyote.nmea.sentence.HDGSentence#setVariation(double)
   */
  @Override
  public void setVariation( double variation ) {
    if ( variation < -180 || variation > 180 ) {
      throw new IllegalArgumentException( "Value out of range [-180..180]" );
    }
    if ( variation > 0 ) {
      setCharValue( VAR_DIRECTION, CompassPoint.EAST.toChar() );
    } else if ( variation < 0 ) {
      setCharValue( VAR_DIRECTION, CompassPoint.WEST.toChar() );
    } else {
      setStringValue( VAR_DIRECTION, "" );
    }
    setDoubleValue( VARIATION, Math.abs( variation ), 3, 1 );
  }

}
