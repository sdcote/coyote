package coyote.nmea.sentence;

import java.util.ArrayList;
import java.util.List;

import coyote.nmea.DataNotAvailableException;
import coyote.nmea.RouteType;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * RTE sentence implementation.
 */
class RTESentenceImpl extends AbstractSentence implements RTESentence {

  private static final int NUMBER_OF_SENTENCES = 0;
  private static final int SENTENCE_NUMBER = 1;
  private static final int STATUS = 2;
  private static final int ROUTE_ID = 3;
  private static final int FIRST_WPT = 4;




  /**
   * Creates a new instance of RTE sentence.
   * 
   * @param nmea RTE sentence string.
   */
  public RTESentenceImpl( String nmea ) {
    super( nmea, SentenceId.RTE );
  }




  /**
   * Creates RTE sentence with no data. The created RTE sentence contains no 
   * waypoint ID fields.
   * 
   * @param talker TalkerId to set
   */
  public RTESentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.RTE, 4 );
  }




  /**
   * @see coyote.nmea.sentence.RTESentence#addWaypointId(java.lang.String)
   */
  @Override
  public int addWaypointId( String id ) {

    String[] ids = getWaypointIds();
    String[] newIds = new String[ids.length + 1];

    System.arraycopy( ids, 0, newIds, 0, ids.length );
    newIds[newIds.length - 1] = id;

    setStringValues( FIRST_WPT, newIds );
    return newIds.length;
  }




  /**
   * @see coyote.nmea.sentence.RTESentence#getRouteId()
   */
  @Override
  public String getRouteId() {
    return getStringValue( ROUTE_ID );
  }




  /**
   * @see coyote.nmea.sentence.RTESentence#getSentenceCount()
   */
  @Override
  public int getSentenceCount() {
    return getIntValue( NUMBER_OF_SENTENCES );
  }




  /**
   * @see coyote.nmea.sentence.RTESentence#getSentenceIndex()
   */
  @Override
  public int getSentenceIndex() {
    return getIntValue( SENTENCE_NUMBER );
  }




  /**
   * @see coyote.nmea.sentence.RTESentence#getWaypointCount()
   */
  @Override
  public int getWaypointCount() {
    return getWaypointIds().length;
  }




  /**
   * @see coyote.nmea.sentence.RTESentence#getWaypointIds()
   */
  @Override
  public String[] getWaypointIds() {

    List<String> temp = new ArrayList<String>();

    for ( int i = FIRST_WPT; i < getFieldCount(); i++ ) {
      try {
        temp.add( getStringValue( i ) );
      } catch ( DataNotAvailableException e ) {
        // probably empty fields
      }
    }

    return temp.toArray( new String[temp.size()] );
  }




  /**
   * @see coyote.nmea.sentence.RTESentence#isActiveRoute()
   */
  @Override
  public boolean isActiveRoute() {
    return getCharValue( STATUS ) == RouteType.ACTIVE.toChar();
  }




  /**
   * @see coyote.nmea.sentence.RTESentence#isFirst()
   */
  @Override
  public boolean isFirst() {
    return ( getSentenceIndex() == 1 );
  }




  /**
   * @see coyote.nmea.sentence.RTESentence#isLast()
   */
  @Override
  public boolean isLast() {
    return ( getSentenceIndex() == getSentenceCount() );
  }




  /**
   * @see coyote.nmea.sentence.RTESentence#isWorkingRoute()
   */
  @Override
  public boolean isWorkingRoute() {
    return getCharValue( STATUS ) == RouteType.WORKING.toChar();
  }




  /**
   * @see coyote.nmea.sentence.RTESentence#setRouteId(java.lang.String)
   */
  @Override
  public void setRouteId( String id ) {
    setStringValue( ROUTE_ID, id );
  }




  /**
   * @see coyote.nmea.sentence.RTESentence#setRouteType(coyote.nmea.RouteType)
   */
  @Override
  public void setRouteType( RouteType type ) {
    setCharValue( STATUS, type.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.RTESentence#setSentenceCount(int)
   */
  @Override
  public void setSentenceCount( int count ) {
    if ( count < 0 ) {
      throw new IllegalArgumentException( "Count cannot be negative" );
    }
    setIntValue( NUMBER_OF_SENTENCES, count );
  }




  /**
   * @see coyote.nmea.sentence.RTESentence#setSentenceIndex(int)
   */
  @Override
  public void setSentenceIndex( int index ) {
    if ( index < 0 ) {
      throw new IllegalArgumentException( "Index cannot be negative" );
    }
    setIntValue( SENTENCE_NUMBER, index );
  }




  /**
   * @see coyote.nmea.sentence.RTESentence#setWaypointIds(java.lang.String[])
   */
  @Override
  public void setWaypointIds( String[] ids ) {
    setStringValues( FIRST_WPT, ids );
  }

}
