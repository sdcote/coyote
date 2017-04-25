package coyote.nmea;

import java.util.Date;


/**
 * Waypoint represents a named position.
 */
public class Waypoint extends Position {

  /**
   * Convenience method for creating a waypoint based on a position.
   * 
   * @param id waypoint ID or name
   * @param pos the Position from which to make the waypoint
   * 
   * @return the created waypoint
   */
  public static Waypoint toWaypoint( final String id, final Position pos ) {
    return new Waypoint( id, pos.getLatitude(), pos.getLongitude() );
  }

  private String id;
  private String description = "";

  private final Date timeStamp = new Date();




  /**
   * Creates a new instance of <code>Waypoint</code> with default WGS84 datum.
   * 
   * @param id waypoint identifier
   * @param lat Latitude degrees of the waypoint location
   * @param lon Longitude degrees of waypoint location
   */
  public Waypoint( final String id, final double lat, final double lon ) {
    super( lat, lon );
    this.id = id;
  }




  /**
   * Creates a new instance of waypoint with explicitly specified datum.
   * 
   * @param id waypoint identifier
   * @param lat Latitude degrees of the waypoint location
   * @param lon Longitude degrees of waypoint location
   * @param datum Position datum, i.e. the coordinate system.
   */
  public Waypoint( final String id, final double lat, final double lon, final Datum datum ) {
    super( lat, lon, datum );
    this.id = id;
  }




  /**
   * Creates a new instance of <code>Waypoint</code> with default WGS84 datum.
   * 
   * @param id waypoint identifier
   * @param lat Latitude degrees of the waypoint location
   * @param lon Longitude degrees of waypoint location
   * @param alt Altitude value, in meters above/below mean sea level
   */
  public Waypoint( final String id, final double lat, final double lon, final double alt ) {
    super( lat, lon, alt );
    this.id = id;
  }




  /**
   * Creates a new instance of <code>Waypoint</code> with explicitly specified
   * datum.
   * 
   * @param id waypoint identifier/name
   * @param lat Latitude degrees of the waypoint location
   * @param lon Longitude degrees of waypoint location
   * @param alt Altitude value, in meters above/below mean sea level
   * @param datum Position datum, i.e. the coordinate system.
   */
  public Waypoint( final String id, final double lat, final double lon, final double alt, final Datum datum ) {
    super( lat, lon, alt, datum );
    this.id = id;
  }




  /**
   * Gets the waypoint description/comment.
   * 
   * @return the description
   */
  public String getDescription() {
    return description;
  }




  /**
   * Get id of waypoint
   * 
   * @return id
   */
  public String getId() {
    return id;
  }




  /**
   * Returns the time stamp when <code>Waypoint</code> was created.
   * 
   * @return Date
   */
  public Date getTimeStamp() {
    return timeStamp;
  }




  /**
   * Sets the waypoint description.
   * 
   * @param description the description to set
   */
  public void setDescription( final String description ) {
    this.description = description;
  }




  /**
   * Set the id of waypoint
   * 
   * @param id the id to set
   */
  public void setId( final String id ) {
    this.id = id;
  }

}
