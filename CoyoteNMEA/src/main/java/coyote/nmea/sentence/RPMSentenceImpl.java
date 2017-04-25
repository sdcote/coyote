package coyote.nmea.sentence;

import coyote.nmea.DataStatus;
import coyote.nmea.TalkerId;


/**
 * RPM sentence implementation
 */
class RPMSentenceImpl extends AbstractSentence implements RPMSentence {

  private static final int SOURCE = 0;
  private static final int SOURCE_NUMBER = 1;
  private static final int REVOLUTIONS = 2;
  private static final int PITCH = 3;
  private static final int STATUS = 4;




  /**
   * Creates a new instance of a RPM sentence implementation.
   * 
   * @param nmea NMEA sentence String.
   */
  public RPMSentenceImpl( String nmea ) {
    super( nmea );
  }




  /**
   * Creates a new empty RPM sentence implementation.
   * 
   * @param talker TalkerId to set.
   */
  public RPMSentenceImpl( TalkerId talker ) {
    super( talker, "RPM", 5 );
  }




  /**
   * @see coyote.nmea.sentence.RPMSentence#getId()
   */
  @Override
  public int getId() {
    return getIntValue( SOURCE_NUMBER );
  }




  /**
   * @see coyote.nmea.sentence.RPMSentence#getPitch()
   */
  @Override
  public double getPitch() {
    return getDoubleValue( PITCH );
  }




  /**
   * @see coyote.nmea.sentence.RPMSentence#getRPM()
   */
  @Override
  public double getRPM() {
    return getDoubleValue( REVOLUTIONS );
  }




  /**
   * @see coyote.nmea.sentence.RPMSentence#getSource()
   */
  @Override
  public char getSource() {
    return getCharValue( SOURCE );
  }




  /**
   * @see coyote.nmea.sentence.RPMSentence#getStatus()
   */
  @Override
  public DataStatus getStatus() {
    return DataStatus.valueOf( getCharValue( STATUS ) );
  }




  /**
   * @see coyote.nmea.sentence.RPMSentence#isEngine()
   */
  @Override
  public boolean isEngine() {
    return getCharValue( SOURCE ) == RPMSentence.ENGINE;
  }




  /**
   * @see coyote.nmea.sentence.RPMSentence#isShaft()
   */
  @Override
  public boolean isShaft() {
    return getCharValue( SOURCE ) == RPMSentence.SHAFT;
  }




  /**
   * @see coyote.nmea.sentence.RPMSentence#setId(int)
   */
  @Override
  public void setId( int id ) {
    setIntValue( SOURCE_NUMBER, id );
  }




  /**
   * @see coyote.nmea.sentence.RPMSentence#setPitch(double)
   */
  @Override
  public void setPitch( double pitch ) {
    setDoubleValue( PITCH, pitch, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.RPMSentence#setSource(char)
   */
  @Override
  public void setSource( char source ) {
    if ( source != RPMSentence.ENGINE && source != RPMSentence.SHAFT ) {
      throw new IllegalArgumentException( "Invalid source indicator, expected 'E' or 'S'" );
    }
    setCharValue( SOURCE, source );
  }




  /**
   * @see coyote.nmea.sentence.RPMSentence#setStatus(coyote.nmea.DataStatus)
   */
  @Override
  public void setStatus( DataStatus status ) {
    setCharValue( STATUS, status.toChar() );
  }

}
