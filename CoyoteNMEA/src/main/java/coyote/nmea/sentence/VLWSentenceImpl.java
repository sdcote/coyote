package coyote.nmea.sentence;

import coyote.nmea.TalkerId;


/**
 * VLW sentence implementation.
 */
class VLWSentenceImpl extends AbstractSentence implements VLWSentence {

  private static final int TOTAL = 0;
  private static final int TOTAL_UNITS = 1;
  private static final int TRIP = 2;
  private static final int TRIP_UNITS = 3;




  /**
   * Creates a new instance of VLW sentence with the given data.
   * 
   * @param nmea NMEA sentence String.
   */
  public VLWSentenceImpl( String nmea ) {
    super( nmea );
  }




  /**
   * Creates a new empty instance of a VLW sentence.
   * 
   * @param talker TalkerId to set.
   */
  public VLWSentenceImpl( TalkerId talker ) {
    super( talker, "VLW", 4 );
  }




  /**
   * @see coyote.nmea.sentence.VLWSentence#getTotal()
   */
  @Override
  public double getTotal() {
    return getDoubleValue( TOTAL );
  }




  /**
   * @see coyote.nmea.sentence.VLWSentence#getTotalUnits()
   */
  @Override
  public char getTotalUnits() {
    return getCharValue( TOTAL_UNITS );
  }




  /**
   * @see coyote.nmea.sentence.VLWSentence#getTrip()
   */
  @Override
  public double getTrip() {
    return getDoubleValue( TRIP );
  }




  /**
   * @see coyote.nmea.sentence.VLWSentence#getTripUnits()
   */
  @Override
  public char getTripUnits() {
    return getCharValue( TRIP_UNITS );
  }




  /**
   * @see coyote.nmea.sentence.VLWSentence#setTotal(double)
   */
  @Override
  public void setTotal( double distance ) {
    setDoubleValue( TOTAL, distance, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.VLWSentence#setTotalUnits(char)
   */
  @Override
  public void setTotalUnits( char unit ) {
    setUnit( TOTAL_UNITS, unit );
  }




  /**
   * @see coyote.nmea.sentence.VLWSentence#setTrip(double)
   */
  @Override
  public void setTrip( double distance ) {
    setDoubleValue( TRIP, distance, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.VLWSentence#setTripUnits(char)
   */
  @Override
  public void setTripUnits( char unit ) {
    setUnit( TRIP_UNITS, unit );
  }




  /**
   * Set and validate unit char.
   * 
   * @param index Field index
   * @param unit Unit char
   */
  private void setUnit( int index, char unit ) {
    if ( unit != VLWSentence.KM && unit != VLWSentence.NM ) {
      throw new IllegalArgumentException( "Invalid distance unit, expected 'N' or 'K'" );
    }
    setCharValue( index, unit );
  }

}
