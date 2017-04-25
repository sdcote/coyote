package coyote.nmea.sentence;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;
import coyote.nmea.Units;


/**
 * DBT sentence implementation.
 */
class DBTSentenceImpl extends AbstractSentence implements DBTSentence {

  private static final int DEPTH_FEET = 0;
  private static final int FEET = 1;
  private static final int DEPTH_METERS = 2;
  private static final int METERS = 3;
  private static final int DEPTH_FATHOMS = 4;
  private static final int FATHOMS = 5;




  /**
   * Creates a new instance of a DBT sentence.
   * 
   * @param nmea
   */
  public DBTSentenceImpl( String nmea ) {
    super( nmea, SentenceId.DBT );
  }




  /**
   * Creates a new instance of a DBT sentence with empty data fields.
   * 
   * @param talker TalkerId to set
   */
  public DBTSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.DBT, 6 );
    setCharValue( FEET, Units.FEET.toChar() );
    setCharValue( METERS, Units.METER.toChar() );
    setCharValue( FATHOMS, Units.FATHOMS.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.DepthSentence#getDepth()
   */
  @Override
  public double getDepth() {
    return getDoubleValue( DEPTH_METERS );
  }




  /**
   * @see coyote.nmea.sentence.DBTSentence#getFathoms()
   */
  @Override
  public double getFathoms() {
    return getDoubleValue( DEPTH_FATHOMS );
  }




  /**
   * @see coyote.nmea.sentence.DBTSentence#getFeet()
   */
  @Override
  public double getFeet() {
    return getDoubleValue( DEPTH_FEET );
  }




  /**
   * @see coyote.nmea.sentence.DepthSentence#setDepth(double)
   */
  @Override
  public void setDepth( double depth ) {
    setDoubleValue( DEPTH_METERS, depth, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.DBTSentence#setFathoms(double)
   */
  @Override
  public void setFathoms( double depth ) {
    setDoubleValue( DEPTH_FATHOMS, depth, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.DBTSentence#setFeet(double)
   */
  @Override
  public void setFeet( double depth ) {
    setDoubleValue( DEPTH_FEET, depth, 1, 1 );
  }
}
