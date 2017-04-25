package coyote.nmea.sentence;

import coyote.nmea.DataStatus;
import coyote.nmea.Direction;
import coyote.nmea.Position;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;
import coyote.nmea.Waypoint;


/**
 * RMB sentence implementation.
 */
class RMBSentenceImpl extends AbstractPositionSentence implements RMBSentence {

  // field indexes
  private static final int STATUS = 0;
  private static final int CROSS_TRACK_ERROR = 1;
  private static final int STEER_TO = 2;
  private static final int ORIGIN_WPT = 3;
  private static final int DEST_WPT = 4;
  private static final int DEST_LAT = 5;
  private static final int DEST_LAT_HEM = 6;
  private static final int DEST_LON = 7;
  private static final int DEST_LON_HEM = 8;
  private static final int RANGE_TO_DEST = 9;
  private static final int BEARING_TO_DEST = 10;
  private static final int VELOCITY = 11;
  private static final int ARRIVAL_STATUS = 12;




  /**
   * Constructor.
   * 
   * @param nmea RMB sentence string
   */
  public RMBSentenceImpl( String nmea ) {
    super( nmea, SentenceId.RMB );
  }




  /**
   * Creates RMB sentence implementation with empty sentence.
   * 
   * @param talker TalkerId to set
   */
  public RMBSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.RMB, 13 );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#getArrivalStatus()
   */
  @Override
  public DataStatus getArrivalStatus() {
    return DataStatus.valueOf( getCharValue( ARRIVAL_STATUS ) );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#getBearing()
   */
  @Override
  public double getBearing() {
    return getDoubleValue( BEARING_TO_DEST );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#getCrossTrackError()
   */
  @Override
  public double getCrossTrackError() {
    return getDoubleValue( CROSS_TRACK_ERROR );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#getDestination()
   */
  @Override
  public Waypoint getDestination() {
    String id = getStringValue( DEST_WPT );
    Position p = parsePosition( DEST_LAT, DEST_LAT_HEM, DEST_LON, DEST_LON_HEM );
    return new Waypoint( id, p.getLatitude(), p.getLongitude() );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#getOriginId()
   */
  @Override
  public String getOriginId() {
    return getStringValue( ORIGIN_WPT );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#getRange()
   */
  @Override
  public double getRange() {
    return getDoubleValue( RANGE_TO_DEST );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#getStatus()
   */
  @Override
  public DataStatus getStatus() {
    return DataStatus.valueOf( getCharValue( STATUS ) );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#getSteerTo()
   */
  @Override
  public Direction getSteerTo() {
    return Direction.valueOf( getCharValue( STEER_TO ) );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#getVelocity()
   */
  @Override
  public double getVelocity() {
    return getDoubleValue( VELOCITY );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#hasArrived()
   */
  @Override
  public boolean hasArrived() {
    return DataStatus.ACTIVE.equals( getArrivalStatus() );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#setArrivalStatus(coyote.nmea.DataStatus)
   */
  @Override
  public void setArrivalStatus( DataStatus status ) {
    setCharValue( ARRIVAL_STATUS, status.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#setBearing(double)
   */
  @Override
  public void setBearing( double bearing ) {
    setDegreesValue( BEARING_TO_DEST, bearing );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#setCrossTrackError(double)
   */
  @Override
  public void setCrossTrackError( double xte ) {
    setDoubleValue( CROSS_TRACK_ERROR, xte, 1, 2 );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#setDestination(coyote.nmea.Waypoint)
   */
  @Override
  public void setDestination( Waypoint dest ) {
    setStringValue( DEST_WPT, dest.getId() );
    setPositionValues( dest, DEST_LAT, DEST_LAT_HEM, DEST_LON, DEST_LON_HEM );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#setOriginId(java.lang.String)
   */
  @Override
  public void setOriginId( String id ) {
    setStringValue( ORIGIN_WPT, id );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#setRange(double)
   */
  @Override
  public void setRange( double range ) {
    setDoubleValue( RANGE_TO_DEST, range, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#setStatus(coyote.nmea.DataStatus)
   */
  @Override
  public void setStatus( DataStatus status ) {
    setCharValue( STATUS, status.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#setSteerTo(coyote.nmea.Direction)
   */
  @Override
  public void setSteerTo( Direction steer ) {
    if ( steer != Direction.LEFT && steer != Direction.RIGHT ) {
      throw new IllegalArgumentException( "Expected steer-to is LEFT or RIGHT." );
    }
    setCharValue( STEER_TO, steer.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.RMBSentence#setVelocity(double)
   */
  @Override
  public void setVelocity( double velocity ) {
    setDoubleValue( VELOCITY, velocity, 1, 1 );
  }

}
