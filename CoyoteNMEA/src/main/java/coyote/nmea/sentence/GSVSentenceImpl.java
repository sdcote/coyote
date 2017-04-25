package coyote.nmea.sentence;

import java.util.ArrayList;
import java.util.List;

import coyote.nmea.DataNotAvailableException;
import coyote.nmea.SatelliteInfo;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * GSV sentence implementation.
 */
class GSVSentenceImpl extends AbstractSentence implements GSVSentence {

  private static final int NUMBER_OF_SENTENCES = 0;
  private static final int SENTENCE_NUMBER = 1;
  private static final int SATELLITES_IN_VIEW = 2;

  // satellite id fields
  private static final int[] ID_FIELDS = { 3, 7, 11, 15 };

  // satellite data fields, relative to each id field
  private static final int ELEVATION = 1;
  private static final int AZIMUTH = 2;
  private static final int NOISE = 3;




  /**
   * Constructor.
   * 
   * @param nmea GSV Sentence
   */
  public GSVSentenceImpl( String nmea ) {
    super( nmea, SentenceId.GSV );
  }




  /**
   * Creates an empty GSV sentence.
   * 
   * @param talker TalkerId to set
   */
  public GSVSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.GSV, 19 );
  }




  /**
   * @see coyote.nmea.sentence.GSVSentence#getSatelliteCount()
   */
  @Override
  public int getSatelliteCount() {
    return getIntValue( SATELLITES_IN_VIEW );
  }




  /**
   * @see coyote.nmea.sentence.GSVSentence#getSatelliteInfo()
   */
  @Override
  public List<SatelliteInfo> getSatelliteInfo() {

    List<SatelliteInfo> satellites = new ArrayList<SatelliteInfo>( 4 );

    for ( int idf : ID_FIELDS ) {
      try {
        String id = getStringValue( idf );
        int elev = getIntValue( idf + ELEVATION );
        int azm = getIntValue( idf + AZIMUTH );
        int snr = getIntValue( idf + NOISE );
        satellites.add( new SatelliteInfo( id, elev, azm, snr ) );
      } catch ( DataNotAvailableException e ) {
        // nevermind missing satellite info
      } catch ( IndexOutOfBoundsException e ) {
        // less than four satellites, give up
        break;
      }
    }

    return satellites;
  }




  /**
   * @see coyote.nmea.sentence.GSVSentence#getSentenceCount()
   */
  @Override
  public int getSentenceCount() {
    return getIntValue( NUMBER_OF_SENTENCES );
  }




  /**
   * @see coyote.nmea.sentence.GSVSentence#getSentenceIndex()
   */
  @Override
  public int getSentenceIndex() {
    return getIntValue( SENTENCE_NUMBER );
  }




  /**
   * @see coyote.nmea.sentence.GSVSentence#isFirst()
   */
  @Override
  public boolean isFirst() {
    return ( getSentenceIndex() == 1 );
  }




  /**
   * @see coyote.nmea.sentence.GSVSentence#isLast()
   */
  @Override
  public boolean isLast() {
    return ( getSentenceIndex() == getSentenceCount() );
  }




  /**
   * @see coyote.nmea.sentence.GSVSentence#setSatelliteCount(int)
   */
  @Override
  public void setSatelliteCount( int count ) {
    if ( count < 0 ) {
      throw new IllegalArgumentException( "Satellite count cannot be negative" );
    }
    setIntValue( SATELLITES_IN_VIEW, count );
  }




  /**
   * @see coyote.nmea.sentence.GSVSentence#setSatelliteInfo(java.util.List)
   */
  @Override
  public void setSatelliteInfo( List<SatelliteInfo> info ) {
    if ( info.size() > 4 ) {
      throw new IllegalArgumentException( "Maximum list size is 4" );
    }
    int i = 0;
    for ( int id : ID_FIELDS ) {
      if ( i < info.size() ) {
        SatelliteInfo si = info.get( i++ );
        setStringValue( id, si.getId() );
        setIntValue( id + ELEVATION, si.getElevation() );
        setIntValue( id + AZIMUTH, si.getAzimuth(), 3 );
        setIntValue( id + NOISE, si.getNoise() );
      } else {
        setStringValue( id, "" );
        setStringValue( id + ELEVATION, "" );
        setStringValue( id + AZIMUTH, "" );
        setStringValue( id + NOISE, "" );
      }
    }
  }




  /**
   * @see coyote.nmea.sentence.GSVSentence#setSentenceCount(int)
   */
  @Override
  public void setSentenceCount( int count ) {
    if ( count < 1 ) {
      throw new IllegalArgumentException( "Number of sentences cannot be negative" );
    }
    setIntValue( NUMBER_OF_SENTENCES, count );
  }




  /**
   * @see coyote.nmea.sentence.GSVSentence#setSentenceIndex(int)
   */
  @Override
  public void setSentenceIndex( int index ) {
    if ( index < 0 ) {
      throw new IllegalArgumentException( "Sentence index cannot be negative" );
    }
    setIntValue( SENTENCE_NUMBER, index );
  }

}
