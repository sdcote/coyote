package coyote.nmea.sentence;

import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * BOD sentence implementation.
 */
class BODSentenceImpl extends AbstractSentence implements BODSentence {

  private static final int BEARING_TRUE = 0;
  private static final int TRUE_INDICATOR = 1;
  private static final int BEARING_MAGN = 2;
  private static final int MAGN_INDICATOR = 3;
  private static final int DESTINATION = 4;
  private static final int ORIGIN = 5;




  /**
   * Creates a new instance of BOD sentence implementation.
   * 
   * @param nmea BOD sentence String
   * 
   * @throws IllegalArgumentException If specified String is invalid or does
   *         not contain a BOD sentence.
   */
  public BODSentenceImpl( String nmea ) {
    super( nmea, SentenceId.BOD );
  }




  /**
   * Creates an empty BOD sentence implementation.
   * 
   * @param talker TalkerId to set
   */
  public BODSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.BOD, 6 );
    setCharValue( TRUE_INDICATOR, 'T' );
    setCharValue( MAGN_INDICATOR, 'M' );
  }




  /**
   * @see coyote.nmea.sentence.BODSentence#getDestinationWaypointId()
   */
  @Override
  public String getDestinationWaypointId() {
    return getStringValue( DESTINATION );
  }




  /**
   * @see coyote.nmea.sentence.BODSentence#getMagneticBearing()
   */
  @Override
  public double getMagneticBearing() {
    return getDoubleValue( BEARING_MAGN );
  }




  /**
   * @see coyote.nmea.sentence.BODSentence#getOriginWaypointId()
   */
  @Override
  public String getOriginWaypointId() {
    return getStringValue( ORIGIN );
  }




  /**
   * @see coyote.nmea.sentence.BODSentence#getTrueBearing()
   */
  @Override
  public double getTrueBearing() {
    return getDoubleValue( BEARING_TRUE );
  }




  /**
   * @see coyote.nmea.sentence.BODSentence#setDestinationWaypointId(java.lang.String)
   */
  @Override
  public void setDestinationWaypointId( String id ) {
    setStringValue( DESTINATION, id );
  }




  /**
   * @see coyote.nmea.sentence.BODSentence#setMagneticBearing(double)
   */
  @Override
  public void setMagneticBearing( double bearing ) {
    setDegreesValue( BEARING_MAGN, bearing );
  }




  /**
   * @see coyote.nmea.sentence.BODSentence#setOriginWaypointId(java.lang.String)
   */
  @Override
  public void setOriginWaypointId( String id ) {
    setStringValue( ORIGIN, id );
  }




  /**
   * @see coyote.nmea.sentence.BODSentence#setTrueBearing(double)
   */
  @Override
  public void setTrueBearing( double bearing ) {
    setDegreesValue( BEARING_TRUE, bearing );
  }

}