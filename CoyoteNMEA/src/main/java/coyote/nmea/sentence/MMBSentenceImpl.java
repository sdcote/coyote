package coyote.nmea.sentence;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * MMBParser - Barometer.
 *
 * $--MMB,x.x,I,x.x,B*hh<CR><LF>
 */
class MMBSentenceImpl extends AbstractSentence implements MMBSentence {

  private static final int PRESSURE_INHG = 0;
  private static final int UNIT_INHG = 1;
  private static final int PRESSURE_BARS = 2;
  private static final int UNIT_BARS = 3;




  /**
   * Constructor for parsing MMB.
   *
   * @param nmea MMB sentence String.
   */
  public MMBSentenceImpl( String nmea ) {
    super( nmea, SentenceId.MMB );
  }




  /**
   * Constructs a fresh MMB parser.
   *
   * @param tid TalkerId to use in sentence.
   */
  public MMBSentenceImpl( TalkerId tid ) {
    super( tid, SentenceId.MMB, 4 );
    setCharValue( UNIT_INHG, 'I' );
    setCharValue( UNIT_BARS, 'B' );
  }



/**
 * @see coyote.nmea.sentence.MMBSentence#getInchesOfMercury()
 */
  @Override
  public double getInchesOfMercury() {
    return getDoubleValue( PRESSURE_INHG );
  }



/**
 * @see coyote.nmea.sentence.MMBSentence#getBars()
 */
  @Override
  public double getBars() {
    return getDoubleValue( PRESSURE_BARS );
  }



/**
 * @see coyote.nmea.sentence.MMBSentence#setInchesOfMercury(double)
 */
  @Override
  public void setInchesOfMercury( double inHg ) {
    setDoubleValue( PRESSURE_INHG, inHg );
  }



/**
 * @see coyote.nmea.sentence.MMBSentence#setBars(double)
 */
  @Override
  public void setBars( double bars ) {
    setDoubleValue( PRESSURE_BARS, bars );
  }

}