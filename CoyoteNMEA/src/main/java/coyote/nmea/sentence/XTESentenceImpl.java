package coyote.nmea.sentence;

import coyote.nmea.DataStatus;
import coyote.nmea.Direction;
import coyote.nmea.FaaMode;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * XTE sentence implementation.
 */
class XTESentenceImpl extends AbstractSentence implements XTESentence {

  private final static int SIGNAL_STATUS = 0;
  private final static int CYCLE_LOCK_STATUS = 1;
  private final static int DISTANCE = 2;
  private final static int DIRECTION = 3;
  private final static int DISTANCE_UNIT = 4;
  private final static int FAA_MODE = 5;




  /**
   * Creates new instance of XTE sentence.
   * 
   * @param nmea XTE sentence String
   */
  public XTESentenceImpl( String nmea ) {
    super( nmea );
    setFieldCount( 6 );
  }




  public XTESentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.XTE, 6 );
    setMode( FaaMode.NONE );
    setStatus( DataStatus.VOID );
    setCycleLockStatus( DataStatus.VOID );
    setCharValue( DISTANCE_UNIT, 'N' );
  }




  @Override
  public DataStatus getCycleLockStatus() {
    return DataStatus.valueOf( getCharValue( CYCLE_LOCK_STATUS ) );
  }




  @Override
  public double getMagnitude() {
    return getDoubleValue( DISTANCE );
  }




  @Override
  public FaaMode getMode() {
    return FaaMode.valueOf( getCharValue( FAA_MODE ) );
  }




  @Override
  public DataStatus getStatus() {
    return DataStatus.valueOf( getCharValue( SIGNAL_STATUS ) );
  }




  @Override
  public Direction getSteerTo() {
    return Direction.valueOf( getCharValue( DIRECTION ) );
  }




  @Override
  public void setCycleLockStatus( DataStatus status ) {
    setCharValue( CYCLE_LOCK_STATUS, status.toChar() );
  }




  @Override
  public void setMagnitude( double distance ) {
    setDoubleValue( DISTANCE, distance, 0, 2 );
  }




  @Override
  public void setMode( FaaMode mode ) {
    setCharValue( FAA_MODE, mode.toChar() );
  }




  @Override
  public void setStatus( DataStatus status ) {
    setCharValue( SIGNAL_STATUS, status.toChar() );
  }




  @Override
  public void setSteerTo( Direction direction ) {
    setCharValue( DIRECTION, direction.toChar() );
  }

}