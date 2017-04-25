package coyote.nmea.sentence;

import coyote.nmea.Position;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;
import coyote.nmea.Waypoint;


/**
 * WPL sentence implementation.
 */
class WPLSentenceImpl extends AbstractPositionSentence implements WPLSentence {

  private static final int LATITUDE = 0;
  private static final int LAT_HEMISPHERE = 1;
  private static final int LONGITUDE = 2;
  private static final int LON_HEMISPHERE = 3;
  private static final int WAYPOINT_ID = 4;




  /**
   * Creates a new instance of a WPL sentence.
   * 
   * @param nmea WPL sentence String.
   * 
   * @throws IllegalArgumentException If specified sentence is invalid.
   */
  public WPLSentenceImpl( String nmea ) {
    super( nmea, SentenceId.WPL );
  }




  /**
   * Creates an empty WPL sentence.
   * 
   * @param talker TalkerId to set
   */
  public WPLSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.WPL, 5 );
  }




  /**
   * @see coyote.nmea.sentence.WPLSentence#getWaypoint()
   */
  @Override
  public Waypoint getWaypoint() {
    String id = getStringValue( WAYPOINT_ID );
    Position p = parsePosition( LATITUDE, LAT_HEMISPHERE, LONGITUDE, LON_HEMISPHERE );

    return new Waypoint( id, p.getLatitude(), p.getLongitude() );
  }




  /**
   * @see coyote.nmea.sentence.WPLSentence#setWaypoint(coyote.nmea.Waypoint)
   */
  @Override
  public void setWaypoint( Waypoint wp ) {
    setStringValue( WAYPOINT_ID, wp.getId() );
    setPositionValues( wp, LATITUDE, LAT_HEMISPHERE, LONGITUDE, LON_HEMISPHERE );
  }
}
