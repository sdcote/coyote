package coyote.nmea.sentence;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import coyote.nmea.AcquisitionType;
import coyote.nmea.NMEATime;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;
import coyote.nmea.TargetStatus;
import coyote.nmea.Units;


/**
 * TTM sentence implementation.
 */
class TTMSentenceImpl extends AbstractSentence implements TTMSentence {

  private static final int NUMBER = 0;
  private static final int DISTANCE = 1;
  private static final int BEARING = 2;
  private static final int BEARING_TRUE_REL = 3;
  private static final int SPEED = 4;
  private static final int COURSE = 5;
  private static final int COURSE_TRUE_REL = 6;
  private static final int DISTANCE_CPA = 7;
  private static final int TIME_CPA = 8;
  private static final int UNITS = 9;
  private static final int NAME = 10;
  private static final int STATUS = 11;
  private static final int REFERENCE = 12;
  private static final int UTC_TIME = 13;
  private static final int ACQUISITON_TYPE = 14;




  /**
   * Create a new instance of TTM sentence.
   *
   * @param nmea TTM sentence String.
   * @throws IllegalArgumentException If specified sentence is invalid.
   */
  public TTMSentenceImpl( String nmea ) {
    super( nmea, SentenceId.TTM );
  }




  /**
   * Create a TTM parser with an empty sentence.
   *
   * @param talker TalkerId to set
   */
  public TTMSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.TTM, 15 );
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
   * @see coyote.nmea.sentence.TimeSentence#setTime(coyote.nmea.NMEATime)
   */
  @Override
  public void setTime( NMEATime t ) {
    // The TTM specification calls for seconds with TWO decimals, not the usual 
    // three implemented by the Time.toString(). So we create our own string.
    String str = String.format( "%02d%02d", t.getHour(), t.getMinutes() );

    DecimalFormat nf = new DecimalFormat( "00.00" );
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setDecimalSeparator( '.' );
    nf.setDecimalFormatSymbols( dfs );

    str += nf.format( t.getSeconds() );
    setStringValue( UTC_TIME, str );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#getNumber()
   */
  @Override
  public int getNumber() {
    return getIntValue( NUMBER );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#getDistance()
   */
  @Override
  public double getDistance() {
    return getDoubleValue( DISTANCE );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#getBearing()
   */
  @Override
  public double getBearing() {
    return getDoubleValue( BEARING );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#getSpeed()
   */
  @Override
  public double getSpeed() {
    return getDoubleValue( SPEED );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#getCourse()
   */
  @Override
  public double getCourse() {
    return getDoubleValue( COURSE );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#getDistanceOfCPA()
   */
  @Override
  public double getDistanceOfCPA() {
    return getDoubleValue( DISTANCE_CPA );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#getTimeToCPA()
   */
  @Override
  public double getTimeToCPA() {
    return getDoubleValue( TIME_CPA );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#getUnits()
   */
  @Override
  public Units getUnits() {
    return Units.valueOf( getCharValue( UNITS ) );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#getName()
   */
  @Override
  public String getName() {
    return getStringValue( NAME );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#getStatus()
   */
  @Override
  public TargetStatus getStatus() {
    return TargetStatus.valueOf( getCharValue( STATUS ) );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#getAcquisitionType()
   */
  @Override
  public AcquisitionType getAcquisitionType() {
    return AcquisitionType.valueOf( getCharValue( ACQUISITON_TYPE ) );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#getReference()
   */
  @Override
  public boolean getReference() {
    return getCharValue( REFERENCE ) == 'R';
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#setNumber(int)
   */
  @Override
  public void setNumber( int number ) {
    setIntValue( NUMBER, number, 2 );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#setDistance(double)
   */
  @Override
  public void setDistance( double distance ) {
    setDoubleValue( DISTANCE, distance, 1, 1 );
    setCharValue( UNITS, 'N' );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#setBearing(double)
   */
  @Override
  public void setBearing( double bearing ) {
    setDoubleValue( BEARING, bearing, 1, 1 );
    setCharValue( BEARING_TRUE_REL, 'T' );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#setSpeed(double)
   */
  @Override
  public void setSpeed( double speed ) {
    setDoubleValue( SPEED, speed, 1, 1 );
    setCharValue( UNITS, 'N' );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#setCourse(double)
   */
  @Override
  public void setCourse( double course ) {
    setDoubleValue( COURSE, course, 1, 1 );
    setCharValue( COURSE_TRUE_REL, 'T' );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#setDistanceOfCPA(double)
   */
  @Override
  public void setDistanceOfCPA( double distance ) {
    setDoubleValue( DISTANCE_CPA, distance, 1, 1 );
    setCharValue( UNITS, 'N' );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#setTimeToCPA(double)
   */
  @Override
  public void setTimeToCPA( double minutes ) {
    setDoubleValue( TIME_CPA, minutes, 1, 1 );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#setName(java.lang.String)
   */
  @Override
  public void setName( String name ) {
    setStringValue( NAME, name );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#setStatus(coyote.nmea.TargetStatus)
   */
  @Override
  public void setStatus( TargetStatus status ) {
    setCharValue( STATUS, status.toChar() );
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#setReference(boolean)
   */
  @Override
  public void setReference( boolean isReference ) {
    if ( isReference ) {
      setCharValue( REFERENCE, 'R' );
    }
  }




  /**
   * @see coyote.nmea.sentence.TTMSentence#setAcquisitionType(coyote.nmea.AcquisitionType)
   */
  @Override
  public void setAcquisitionType( AcquisitionType acquisitionType ) {
    setCharValue( ACQUISITON_TYPE, acquisitionType.toChar() );
  }
}
