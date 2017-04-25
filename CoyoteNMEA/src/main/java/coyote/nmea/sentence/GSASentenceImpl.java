package coyote.nmea.sentence;

import java.util.ArrayList;
import java.util.List;

import coyote.nmea.FaaMode;
import coyote.nmea.GpsFixStatus;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * GSA sentence implementation.
 */
class GSASentenceImpl extends AbstractSentence implements GSASentence {

  private static final int GPS_MODE = 0;
  private static final int FIX_MODE = 1;
  private static final int FIRST_SV = 2;
  private static final int LAST_SV = 13;
  private static final int POSITION_DOP = 14;
  private static final int HORIZONTAL_DOP = 15;
  private static final int VERTICAL_DOP = 16;




  /**
   * Creates a new instance of a GSA sentence.
   * 
   * @param nmea GSA sentence String
   * 
   * @throws IllegalArgumentException If specified sentence is invalid.
   */
  public GSASentenceImpl( String nmea ) {
    super( nmea, SentenceId.GSA );
  }




  /**
   * Creates an empty GSA sentence.
   * 
   * @param talker TalkerId to set
   */
  public GSASentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.GSA, 17 );
  }




  /**
   * @see coyote.nmea.sentence.GSASentence#getFixStatus()
   */
  @Override
  public GpsFixStatus getFixStatus() {
    return GpsFixStatus.valueOf( getIntValue( FIX_MODE ) );
  }




  /**
   * @see coyote.nmea.sentence.GSASentence#getHorizontalDOP()
   */
  @Override
  public double getHorizontalDOP() {
    return getDoubleValue( HORIZONTAL_DOP );
  }




  /**
   * @see coyote.nmea.sentence.GSASentence#getMode()
   */
  @Override
  public FaaMode getMode() {
    return FaaMode.valueOf( getCharValue( GPS_MODE ) );
  }




  /**
   * @see coyote.nmea.sentence.GSASentence#getPositionDOP()
   */
  @Override
  public double getPositionDOP() {
    return getDoubleValue( POSITION_DOP );
  }




  /**
   * @see coyote.nmea.sentence.GSASentence#getSatelliteIds()
   */
  @Override
  public String[] getSatelliteIds() {
    List<String> result = new ArrayList<String>();
    for ( int i = FIRST_SV; i <= LAST_SV; i++ ) {
      if ( hasValue( i ) ) {
        result.add( getStringValue( i ) );
      }
    }
    return result.toArray( new String[result.size()] );
  }




  /**
   * @see coyote.nmea.sentence.GSASentence#getVerticalDOP()
   */
  @Override
  public double getVerticalDOP() {
    return getDoubleValue( VERTICAL_DOP );
  }




  /**
   * @see coyote.nmea.sentence.GSASentence#setFixStatus(coyote.nmea.GpsFixStatus)
   */
  @Override
  public void setFixStatus( GpsFixStatus status ) {
    setIntValue( FIX_MODE, status.toInt() );
  }




  /**
   * @see coyote.nmea.sentence.GSASentence#setHorizontalDOP(double)
   */
  @Override
  public void setHorizontalDOP( double hdop ) {
    setDoubleValue( HORIZONTAL_DOP, hdop, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.GSASentence#setMode(coyote.nmea.FaaMode)
   */
  @Override
  public void setMode( FaaMode mode ) {
    setCharValue( GPS_MODE, mode.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.GSASentence#setPositionDOP(double)
   */
  @Override
  public void setPositionDOP( double pdop ) {
    setDoubleValue( POSITION_DOP, pdop, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.GSASentence#setSatelliteIds(java.lang.String[])
   */
  @Override
  public void setSatelliteIds( String[] ids ) {
    if ( ids.length > ( LAST_SV - FIRST_SV + 1 ) ) {
      throw new IllegalArgumentException( "List length exceeded (12)" );
    }
    int j = 0;
    for ( int i = FIRST_SV; i <= LAST_SV; i++ ) {
      String id = ( j < ids.length ) ? ids[j++] : "";
      setStringValue( i, id );
    }
  }




  /**
   * @see coyote.nmea.sentence.GSASentence#setVerticalDOP(double)
   */
  @Override
  public void setVerticalDOP( double vdop ) {
    setDoubleValue( VERTICAL_DOP, vdop, 1, 1 );
  }

}
