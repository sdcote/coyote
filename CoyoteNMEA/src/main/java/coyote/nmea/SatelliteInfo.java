package coyote.nmea;

/**
 * SatelliteInfo represents the information about a single GPS satellite
 * vehicle.
 */
public class SatelliteInfo {

  private String id;
  private int elevation;
  private int azimuth;
  private int noise;




  /**
   * Creates a new instance of SatelliteInfo
   * 
   * @param id Satellite ID
   * @param elevation Current elevation of the satellite
   * @param azimuth Current azimuth of the satellite
   * @param noise Current noise ratio of the satellite signal
   */
  public SatelliteInfo( String id, int elevation, int azimuth, int noise ) {
    this.id = id;
    this.elevation = elevation;
    this.azimuth = azimuth;
    this.noise = noise;
  }




  /**
   * Get satellite azimuth, in degrees from true north (0..359&deg;).
   * 
   * @return azimuth value
   */
  public int getAzimuth() {
    return azimuth;
  }




  /**
   * Get satellite elevation, in degrees (max. 90&deg;).
   * 
   * @return elevation value
   */
  public int getElevation() {
    return elevation;
  }




  /**
   * Get the ID of satellite vehicle, for example "05".
   * 
   * @return ID String
   */
  public String getId() {
    return id;
  }




  /**
   * Get satellite the signal noise ratio, in dB (0-99 dB).
   * 
   * @return Noise ratio
   */
  public int getNoise() {
    return noise;
  }




  /**
   * Set satellite azimuth, in degrees from true north (0..359&deg;).
   * 
   * @param azimuth the azimuth to set
   * 
   * @throws IllegalArgumentException If value is out of bounds 0..360 deg.
   */
  public void setAzimuth( int azimuth ) {
    if ( azimuth < 0 || azimuth > 360 ) {
      throw new IllegalArgumentException( "Value out of bounds 0..360 deg" );
    }
    this.azimuth = azimuth;
  }




  /**
   * Set satellite elevation, in degrees (max. 90&deg;).
   * 
   * @param elevation the elevation to set
   * 
   * @throws IllegalArgumentException If value is out of bounds 0..90 deg.
   */
  public void setElevation( int elevation ) {
    if ( elevation < 0 || elevation > 90 ) {
      throw new IllegalArgumentException( "Value out of bounds 0..90 deg" );
    }
    this.elevation = elevation;
  }




  /**
   * Set the ID of satellite vehicle, for example "05".
   * 
   * @param id the id to set
   */
  public void setId( String id ) {
    this.id = id;
  }




  /**
   * Set the satellite signal noise ratio, in dB (0-99 dB).
   * 
   * @param noise the noise to set
   * 
   * @throws IllegalArgumentException If value is out of bounds 0..99 dB.
   */
  public void setNoise( int noise ) {
    if ( noise < 0 || noise > 99 ) {
      throw new IllegalArgumentException( "Value out of bounds 0..99 dB" );
    }
    this.noise = noise;
  }

}
