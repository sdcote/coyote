package coyote.nmea.sentence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import coyote.nmea.Measurement;
import coyote.nmea.SentenceId;
import coyote.nmea.TalkerId;


/**
 * Transducer measurements.
 * 
 * <p>Format:<pre>
 *         1 2   3 4            n
 *         | |   | |            |
 *  $--XDR,a,x.x,a,c--c, ..... *hh<CR><LF>
 * </pre>Where:<ol>
 * <li>Transducer Type</li>
 * <li>Measurement Data</li>
 * <li>Units of measurement</li>
 * <li>Name of transducer</li></ol>
 * 
 * <p>There may be any number of quadruplets like this, each describing a 
 * sensor. The last field will be a checksum as usual.
 */
class XDRSentenceImpl extends AbstractSentence implements XDRSentence {

  // length of each data set is 4 fields
  private static int DATA_SET_LENGTH = 4;

  // data set field indices, relative to first field of each set
  private static int TYPE_INDEX = 0;
  private static int VALUE_INDEX = 1;
  private static int UNITS_INDEX = 2;
  private static int NAME_INDEX = 3;




  /**
   * Creates new instance of XDR sentence implementation.
   * 
   * @param nmea XDR sentence string
   */
  public XDRSentenceImpl( String nmea ) {
    super( nmea, SentenceId.XDR );
  }




  /**
   * Creates an empty XDR sentence implementation.
   * 
   * @param talker TalkerId to set
   */
  public XDRSentenceImpl( TalkerId talker ) {
    super( talker, SentenceId.XDR, DATA_SET_LENGTH );
  }




  /**
   * @see coyote.nmea.sentence.XDRSentence#addMeasurement(coyote.nmea.Measurement[])
   */
  @Override
  public void addMeasurement( Measurement... m ) {
    List<Measurement> ms = getMeasurements();
    ms.addAll( Arrays.asList( m ) );
    setMeasurements( ms );
  }




  /**
   * @see coyote.nmea.sentence.XDRSentence#getMeasurements()
   */
  @Override
  public List<Measurement> getMeasurements() {
    ArrayList<Measurement> result = new ArrayList<Measurement>();
    for ( int i = 0; i < getFieldCount(); i += DATA_SET_LENGTH ) {
      Measurement value = fetchValues( i );
      if ( !value.isEmpty() ) {
        result.add( value );
      }
    }
    return result;
  }




  /**
   * @see coyote.nmea.sentence.XDRSentence#setMeasurement(coyote.nmea.Measurement)
   */
  @Override
  public void setMeasurement( Measurement m ) {
    setFieldCount( DATA_SET_LENGTH );
    insertValues( TYPE_INDEX, m );
  }




  /**
   * @see coyote.nmea.sentence.XDRSentence#setMeasurements(java.util.List)
   */
  @Override
  public void setMeasurements( List<Measurement> measurements ) {

    setFieldCount( measurements.size() * DATA_SET_LENGTH );

    int i = 0;
    for ( Measurement m : measurements ) {
      insertValues( i, m );
      i += DATA_SET_LENGTH;
    }
  }




  /**
   * Fetch data set starting at given index.
   *  
   * @param i Start position of data set, i.e. index of first data field.
   * 
   * @return XDRValue object
   */
  private Measurement fetchValues( int i ) {

    Measurement m = new Measurement();

    if ( hasValue( i ) ) {
      m.setType( getStringValue( i ) );
    }

    if ( hasValue( i + VALUE_INDEX ) ) {
      m.setValue( getDoubleValue( i + VALUE_INDEX ) );
    }

    if ( hasValue( i + UNITS_INDEX ) ) {
      m.setUnits( getStringValue( i + UNITS_INDEX ) );
    }

    if ( hasValue( i + NAME_INDEX ) ) {
      m.setName( getStringValue( i + NAME_INDEX ) );
    }

    return m;
  }




  /**
   * Inserts the given data set beginning at given index. 
   * 
   * <p>Before inserting, make sure the sentence has enough fields for it.
   * 
   * @param i Start position of data set, i.e. index of first data field.
   * @param m XDR data set to insert
   */
  private void insertValues( int i, Measurement m ) {
    if ( m != null ) {
      setStringValue( ( i ), m.getType() );
      setDoubleValue( ( i + VALUE_INDEX ), m.getValue() );
      setStringValue( ( i + UNITS_INDEX ), m.getUnits() );
      setStringValue( ( i + NAME_INDEX ), m.getName() );
    }
  }

}
