package coyote.nmea.sentence;

import coyote.nmea.CompassPoint;
import coyote.nmea.DataStatus;
import coyote.nmea.FaaMode;
import coyote.nmea.NMEADate;
import coyote.nmea.NMEATime;
import coyote.nmea.Position;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * RMC sentence implementation.
 */
class RMCSentenceImpl extends AbstractPositionSentence implements RMCSentence {

  private static final int UTC_TIME = 0;
  private static final int DATA_STATUS = 1;
  private static final int LATITUDE = 2;
  private static final int LAT_HEMISPHERE = 3;
  private static final int LONGITUDE = 4;
  private static final int LON_HEMISPHERE = 5;
  private static final int SPEED = 6;
  private static final int COURSE = 7;
  private static final int UTC_DATE = 8;
  private static final int MAG_VARIATION = 9;
  private static final int VAR_HEMISPHERE = 10;
  private static final int MODE = 11;




  /**
   * Creates a new instance of RMC sentence implementation.
   *
   * @param nmea RMC sentence String.
   * @throws IllegalArgumentException If specified sentence is invalid.
   */
  public RMCSentenceImpl( String nmea ) {
    super( nmea, SentenceId.RMC );
  }




  /**
   * Creates an empty RMC sentence.
   *
   * @param talker TalkerId to set
   */
  public RMCSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.RMC, 12 );
  }




  /**
   * @see coyote.nmea.sentence.RMCSentence#getCorrectedCourse()
   */
  @Override
  public double getCorrectedCourse() {
    return getCourse() + getVariation();
  }




  /**
   * @see coyote.nmea.sentence.RMCSentence#getCourse()
   */
  @Override
  public double getCourse() {
    return getDoubleValue( COURSE );
  }




  /**
   * @see coyote.nmea.sentence.DateSentence#getDate()
   */
  @Override
  public NMEADate getDate() {
    return new NMEADate( getStringValue( UTC_DATE ) );
  }




  /**
   * @see coyote.nmea.sentence.RMCSentence#getDirectionOfVariation()
   */
  @Override
  public CompassPoint getDirectionOfVariation() {
    return CompassPoint.valueOf( getCharValue( VAR_HEMISPHERE ) );
  }




  /**
   * @see coyote.nmea.sentence.RMCSentence#getMode()
   */
  @Override
  public FaaMode getMode() {
    return FaaMode.valueOf( getCharValue( MODE ) );
  }




  /**
   * @see coyote.nmea.PositionSentence#getPosition()
   */
  @Override
  public Position getPosition() {
    return parsePosition( LATITUDE, LAT_HEMISPHERE, LONGITUDE, LON_HEMISPHERE );
  }




  /**
   * @see coyote.nmea.sentence.RMCSentence#getSpeed()
   */
  @Override
  public double getSpeed() {
    return getDoubleValue( SPEED );
  }




  /**
   * @see coyote.nmea.sentence.RMCSentence#getStatus()
   */
  @Override
  public DataStatus getStatus() {
    return DataStatus.valueOf( getCharValue( DATA_STATUS ) );
  }




  /**
   * @see coyote.nmea.sentence.TimeSentence#getTime()
   */
  @Override
  public NMEATime getTime() {
    String str = getStringValue( UTC_TIME );
    return new NMEATime( str );
  }




  /**
   * @see coyote.nmea.sentence.RMCSentence#getVariation()
   */
  @Override
  public double getVariation() {
    double variation = getDoubleValue( MAG_VARIATION );
    if ( CompassPoint.EAST == getDirectionOfVariation() && variation > 0 ) {
      variation = -( variation );
    }
    return variation;
  }




  /**
   * @see coyote.nmea.sentence.RMCSentence#setCourse(double)
   */
  @Override
  public void setCourse( double cog ) {
    setDegreesValue( COURSE, cog );
  }




  /**
   * @see coyote.nmea.sentence.DateSentence#setDate(coyote.nmea.NMEADate)
   */
  @Override
  public void setDate( NMEADate date ) {
    setStringValue( UTC_DATE, date.toString() );
  }




  /**
   * @see coyote.nmea.sentence.RMCSentence#setDirectionOfVariation(coyote.nmea.CompassPoint)
   */
  @Override
  public void setDirectionOfVariation( CompassPoint dir ) {
    if ( dir != CompassPoint.EAST && dir != CompassPoint.WEST ) {
      throw new IllegalArgumentException( "Invalid variation direction, expected EAST or WEST." );
    }
    setCharValue( VAR_HEMISPHERE, dir.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.RMCSentence#setMode(coyote.nmea.FaaMode)
   */
  @Override
  public void setMode( FaaMode mode ) {
    setFieldCount( 12 );
    setCharValue( MODE, mode.toChar() );
  }




  /**
   * @see coyote.nmea.PositionSentence#setPosition(coyote.nmea.Position)
   */
  @Override
  public void setPosition( Position pos ) {
    setPositionValues( pos, LATITUDE, LAT_HEMISPHERE, LONGITUDE, LON_HEMISPHERE );
  }




  /**
   * @see coyote.nmea.sentence.RMCSentence#setSpeed(double)
   */
  @Override
  public void setSpeed( double sog ) {
    setDoubleValue( SPEED, sog, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.RMCSentence#setStatus(coyote.nmea.DataStatus)
   */
  @Override
  public void setStatus( DataStatus status ) {
    setCharValue( DATA_STATUS, status.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.TimeSentence#setTime(coyote.nmea.NMEATime)
   */
  @Override
  public void setTime( NMEATime t ) {
    setStringValue( UTC_TIME, t.toString() );
  }




  /**
   * @see coyote.nmea.sentence.RMCSentence#setVariation(double)
   */
  @Override
  public void setVariation( double var ) {
    setDegreesValue( MAG_VARIATION, var );
  }

}
