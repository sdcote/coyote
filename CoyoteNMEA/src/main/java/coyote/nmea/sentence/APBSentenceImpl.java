package coyote.nmea.sentence;

import coyote.nmea.DataStatus;
import coyote.nmea.Direction;
import coyote.nmea.TalkerId;


/**
 * 
 */
class APBSentenceImpl extends AbstractSentence implements APBSentence {

  private static final int SIGNAL_STATUS = 0;
  private static final int CYCLE_LOCK_STATUS = 1;
  private static final int XTE_DISTANCE = 2;
  private static final int XTE_STEER_TO = 3;
  private static final int XTE_UNITS = 4;
  private static final int CIRCLE_STATUS = 5;
  private static final int PERPENDICULAR_STATUS = 6;
  private static final int BEARING_ORIGIN_DEST = 7;
  private static final int BEARING_ORIGIN_DEST_TYPE = 8;
  private static final int DEST_WAYPOINT_ID = 9;
  private static final int BEARING_POS_DEST = 10;
  private static final int BEARING_POS_DEST_TYPE = 11;
  private static final int HEADING_TO_DEST = 12;
  private static final int HEADING_TO_DEST_TYPE = 13;




  /**
   * Creates a new instance of an APB sentence.
   * 
   * @param nmea NMEA sentence String.
   */
  public APBSentenceImpl( String nmea ) {
    super( nmea );
  }




  /**
   * Creates a new empty APB sentence.
   * 
   * @param talker TalkerId to set
   */
  public APBSentenceImpl( TalkerId talker ) {
    super( talker, "APB", 14 );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#getBearingPositionToDestination()
   */
  @Override
  public double getBearingPositionToDestination() {
    return getDoubleValue( BEARING_POS_DEST );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#getBearingOriginToDestination()
   */
  @Override
  public double getBearingOriginToDestination() {
    return getDoubleValue( BEARING_ORIGIN_DEST );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#getCrossTrackError()
   */
  @Override
  public double getCrossTrackError() {
    return getDoubleValue( XTE_DISTANCE );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#getCrossTrackUnits()
   */
  @Override
  public char getCrossTrackUnits() {
    return getCharValue( XTE_UNITS );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#getCycleLockStatus()
   */
  @Override
  public DataStatus getCycleLockStatus() {
    return DataStatus.valueOf( getCharValue( CYCLE_LOCK_STATUS ) );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#getDestionationWaypointId()
   */
  @Override
  public String getDestionationWaypointId() {
    return getStringValue( DEST_WAYPOINT_ID );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#getHeadingToDestionation()
   */
  @Override
  public double getHeadingToDestionation() {
    return getDoubleValue( HEADING_TO_DEST );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#getStatus()
   */
  @Override
  public DataStatus getStatus() {
    return DataStatus.valueOf( getCharValue( SIGNAL_STATUS ) );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#getSteerTo()
   */
  @Override
  public Direction getSteerTo() {
    return Direction.valueOf( getCharValue( XTE_STEER_TO ) );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#isArrivalCircleEntered()
   */
  @Override
  public boolean isArrivalCircleEntered() {
    return getCharValue( CIRCLE_STATUS ) == 'A';
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#isBearingOriginToDestionationTrue()
   */
  @Override
  public boolean isBearingOriginToDestionationTrue() {
    return getCharValue( BEARING_ORIGIN_DEST_TYPE ) == 'T';
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#isBearingPositionToDestinationTrue()
   */
  @Override
  public boolean isBearingPositionToDestinationTrue() {
    return getCharValue( BEARING_POS_DEST_TYPE ) == 'T';

  }




  /**
   * @see coyote.nmea.sentence.APBSentence#isHeadingToDestinationTrue()
   */
  @Override
  public boolean isHeadingToDestinationTrue() {
    return getCharValue( HEADING_TO_DEST_TYPE ) == 'T';
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#isPerpendicularPassed()
   */
  @Override
  public boolean isPerpendicularPassed() {
    return getCharValue( PERPENDICULAR_STATUS ) == 'A';
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#setArrivalCircleEntered(boolean)
   */
  @Override
  public void setArrivalCircleEntered( boolean isEntered ) {
    DataStatus s = isEntered ? DataStatus.ACTIVE : DataStatus.VOID;
    setCharValue( CIRCLE_STATUS, s.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#setBearingOriginToDestination(double)
   */
  @Override
  public void setBearingOriginToDestination( double bearing ) {
    setDegreesValue( BEARING_ORIGIN_DEST, bearing );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#setBearingOriginToDestionationTrue(boolean)
   */
  @Override
  public void setBearingOriginToDestionationTrue( boolean isTrue ) {
    char c = isTrue ? 'T' : 'M';
    setCharValue( BEARING_ORIGIN_DEST_TYPE, c );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#setBearingPositionToDestination(double)
   */
  @Override
  public void setBearingPositionToDestination( double bearing ) {
    setDegreesValue( BEARING_POS_DEST, bearing );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#setBearingPositionToDestinationTrue(boolean)
   */
  @Override
  public void setBearingPositionToDestinationTrue( boolean isTrue ) {
    char c = isTrue ? 'T' : 'M';
    setCharValue( BEARING_POS_DEST_TYPE, c );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#setCrossTrackError(double)
   */
  @Override
  public void setCrossTrackError( double distance ) {
    setDoubleValue( XTE_DISTANCE, distance, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#setCrossTrackUnits(char)
   */
  @Override
  public void setCrossTrackUnits( char unit ) {
    if ( unit != APBSentence.KM && unit != APBSentence.NM ) {
      throw new IllegalAccessError( "Invalid distance unit char, expected 'K' or 'N'" );
    }
    setCharValue( XTE_UNITS, unit );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#setCycleLockStatus(coyote.nmea.DataStatus)
   */
  @Override
  public void setCycleLockStatus( DataStatus status ) {
    setCharValue( CYCLE_LOCK_STATUS, status.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#setDestinationWaypointId(java.lang.String)
   */
  @Override
  public void setDestinationWaypointId( String id ) {
    setStringValue( DEST_WAYPOINT_ID, id );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#setHeadingToDestination(double)
   */
  @Override
  public void setHeadingToDestination( double heading ) {
    setDoubleValue( HEADING_TO_DEST, heading );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#setHeadingToDestinationTrue(boolean)
   */
  @Override
  public void setHeadingToDestinationTrue( boolean isTrue ) {
    char c = isTrue ? 'T' : 'M';
    setCharValue( HEADING_TO_DEST_TYPE, c );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#setPerpendicularPassed(boolean)
   */
  @Override
  public void setPerpendicularPassed( boolean isPassed ) {
    DataStatus s = isPassed ? DataStatus.ACTIVE : DataStatus.VOID;
    setCharValue( PERPENDICULAR_STATUS, s.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#setStatus(coyote.nmea.DataStatus)
   */
  @Override
  public void setStatus( DataStatus status ) {
    setCharValue( SIGNAL_STATUS, status.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.APBSentence#setSteerTo(coyote.nmea.Direction)
   */
  @Override
  public void setSteerTo( Direction direction ) {
    setCharValue( XTE_STEER_TO, direction.toChar() );
  }

}
