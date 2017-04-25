package coyote.nmea.sentence;

import coyote.nmea.DataStatus;
import coyote.nmea.SentenceId;
import coyote.nmea.Side;
import coyote.nmea.TalkerId;


/**
 * RSA sentence implementation.
 */
class RSASentenceImpl extends AbstractSentence implements RSASentence {

  private static final int STARBOARD_SENSOR = 0;
  private static final int STARBOARD_STATUS = 1;
  private static final int PORT_SENSOR = 2;
  private static final int PORT_STATUS = 3;




  /**
   * Creates a new instance of an RSA sentence implementation.
   * 
   * @param nmea RSA sentence to parse
   */
  public RSASentenceImpl( String nmea ) {
    super( nmea, SentenceId.RSA );
  }




  /**
   * Creates a new instance of RSA sentence with empty data fields.
   * 
   * @param talker TalkerId to set
   */
  public RSASentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.RSA, 4 );
    setStatus( Side.STARBOARD, DataStatus.VOID );
    setStatus( Side.PORT, DataStatus.VOID );
  }




  /**
   * @see coyote.nmea.sentence.RSASentence#getRudderAngle(coyote.nmea.Side)
   */
  @Override
  public double getRudderAngle( Side side ) {
    if ( Side.STARBOARD.equals( side ) ) {
      return getDoubleValue( STARBOARD_SENSOR );
    }
    return getDoubleValue( PORT_SENSOR );
  }




  /**
   * @see coyote.nmea.sentence.RSASentence#setRudderAngle(coyote.nmea.Side, double)
   */
  @Override
  public void setRudderAngle( Side side, double angle ) {
    if ( Side.STARBOARD.equals( side ) ) {
      setDoubleValue( STARBOARD_SENSOR, angle );
    } else {
      setDoubleValue( PORT_SENSOR, angle );
    }
  }




  /**
   * @see coyote.nmea.sentence.RSASentence#getStatus(coyote.nmea.Side)
   */
  @Override
  public DataStatus getStatus( Side side ) {
    if ( Side.STARBOARD.equals( side ) ) {
      return DataStatus.valueOf( getCharValue( STARBOARD_STATUS ) );
    }
    return DataStatus.valueOf( getCharValue( PORT_STATUS ) );
  }




  /**
   * @see coyote.nmea.sentence.RSASentence#setStatus(coyote.nmea.Side, coyote.nmea.DataStatus)
   */
  @Override
  public void setStatus( Side side, DataStatus status ) {
    if ( Side.STARBOARD.equals( side ) ) {
      setCharValue( STARBOARD_STATUS, status.toChar() );
    } else {
      setCharValue( PORT_STATUS, status.toChar() );
    }
  }
}
