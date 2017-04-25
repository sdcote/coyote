package coyote.nmea.sentence;

import coyote.nmea.NMEADate;
import coyote.nmea.NMEATime;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * ZDA sentence implementation.
 */
class ZDASentenceImpl extends AbstractSentence implements ZDASentence {

  // field indices
  private static final int UTC_TIME = 0;
  private static final int DAY = 1;
  private static final int MONTH = 2;
  private static final int YEAR = 3;
  private static final int LOCAL_ZONE_HOURS = 4;
  private static final int LOCAL_ZONE_MINUTES = 5;




  /**
   * Creates a new instance of ZDA sentence implementation.
   *
   * @param nmea ZDA sentence String
   * 
   * @throws IllegalArgumentException If specified sentence is invalid.
   */
  public ZDASentenceImpl( String nmea ) {
    super( nmea, SentenceId.ZDA );
  }




  /**
   * Creates ZDA sentence implementation with empty data.
   *
   * @param talker TalkerId to set
   */
  public ZDASentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.ZDA, 6 );
  }




  /**
   * @see coyote.nmea.sentence.DateSentence#getDate()
   */
  @Override
  public NMEADate getDate() {
    int y = getIntValue( YEAR );
    int m = getIntValue( MONTH );
    int d = getIntValue( DAY );
    return new NMEADate( y, m, d );
  }




  /**
   * @see coyote.nmea.sentence.ZDASentence#getLocalZoneHours()
   */
  @Override
  public int getLocalZoneHours() {
    return getIntValue( LOCAL_ZONE_HOURS );
  }




  /**
   * @see coyote.nmea.sentence.ZDASentence#getLocalZoneMinutes()
   */
  @Override
  public int getLocalZoneMinutes() {
    return getIntValue( LOCAL_ZONE_MINUTES );
  }




  /**
   * @see coyote.nmea.sentence.TimeSentence#getTime()
   */
  public NMEATime getTime() {

    String str = getStringValue( UTC_TIME );
    int tzHrs = getLocalZoneHours();
    int tzMin = getLocalZoneMinutes();

    NMEATime t = new NMEATime( str );
    t.setOffsetHours( tzHrs );
    t.setOffsetMinutes( tzMin );

    return t;
  }




  /**
   * @see coyote.nmea.sentence.DateSentence#setDate(coyote.nmea.NMEADate)
   */
  @Override
  public void setDate( NMEADate date ) {
    setIntValue( YEAR, date.getYear() );
    setIntValue( MONTH, date.getMonth(), 2 );
    setIntValue( DAY, date.getDay(), 2 );
  }




  /**
   * @see coyote.nmea.sentence.ZDASentence#setLocalZoneHours(int)
   */
  @Override
  public void setLocalZoneHours( int hours ) {
    if ( hours < -13 || hours > 13 ) {
      throw new IllegalArgumentException( "Value must be within range -13..13" );
    }
    setIntValue( LOCAL_ZONE_HOURS, hours, 2 );
  }




  /**
   * @see coyote.nmea.sentence.ZDASentence#setLocalZoneMinutes(int)
   */
  @Override
  public void setLocalZoneMinutes( int minutes ) {
    if ( minutes < -59 || minutes > 59 ) {
      throw new IllegalArgumentException( "Value must be within range -59..59" );
    }
    setIntValue( LOCAL_ZONE_MINUTES, minutes, 2 );
  }




  /**
   * @see coyote.nmea.sentence.TimeSentence#setTime(coyote.nmea.NMEATime)
   */
  @Override
  public void setTime( NMEATime t ) {
    setStringValue( UTC_TIME, t.toString() );
  }




  /**
   * @see coyote.nmea.sentence.ZDASentence#setTimeAndLocalZone(coyote.nmea.NMEATime)
   */
  @Override
  public void setTimeAndLocalZone( NMEATime t ) {
    setTime( t );
    setLocalZoneHours( t.getOffsetHours() );
    setLocalZoneMinutes( t.getOffsetMinutes() );
  }




  /**
   * @see coyote.nmea.sentence.ZDASentence#toDate()
   */
  @Override
  public java.util.Date toDate() {
    return getTime().toDate( getDate().toDate() );
  }
}
