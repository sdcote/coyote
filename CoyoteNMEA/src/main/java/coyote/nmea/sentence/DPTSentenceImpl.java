package coyote.nmea.sentence;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * DPT sentence implementation.
 */
class DPTSentenceImpl extends AbstractSentence implements DPTSentence {

  private static final int DEPTH = 0;
  private static final int OFFSET = 1;
  private static final int MAXIMUM = 2;




  /**
   * Creates a new instance of a DPT sentence.
   * 
   * @param nmea DPT sentence String
   */
  public DPTSentenceImpl( String nmea ) {
    super( nmea, SentenceId.DPT );
  }




  /**
   * Creates a new instance of a DPT sentence with empty data fields.
   * 
   * @param talker TalkerId to set
   */
  public DPTSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.DPT, 3 );
  }




  /**
   * @see coyote.nmea.sentence.DepthSentence#getDepth()
   */
  @Override
  public double getDepth() {
    return getDoubleValue( DEPTH );
  }




  /**
   * @see coyote.nmea.sentence.DPTSentence#getOffset()
   */
  @Override
  public double getOffset() {
    return getDoubleValue( OFFSET );
  }




  /**
   * @see coyote.nmea.sentence.DepthSentence#setDepth(double)
   */
  @Override
  public void setDepth( double depth ) {
    setDoubleValue( DEPTH, depth, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.DPTSentence#setOffset(double)
   */
  @Override
  public void setOffset( double offset ) {
    setDoubleValue( OFFSET, offset, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.DPTSentence#getMaximum()
   */
  @Override
  public double getMaximum() {
    return getDoubleValue( MAXIMUM );
  }




  /**
   * @see coyote.nmea.sentence.DPTSentence#setMaximum(double)
   */
  @Override
  public void setMaximum( double max ) {
    setDoubleValue( MAXIMUM, max );
  }

}
