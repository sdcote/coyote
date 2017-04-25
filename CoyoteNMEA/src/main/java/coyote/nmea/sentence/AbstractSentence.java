package coyote.nmea.sentence;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import coyote.nmea.Checksum;
import coyote.nmea.DataNotAvailableException;
import coyote.nmea.ParseException;
import coyote.nmea.Sentence;
import coyote.nmea.SentenceId;
import coyote.nmea.SentenceValidator;
import coyote.nmea.TalkerId;


/**
 * <p>Base class for all NMEA 0183 sentences. Contains generic methods such
 * as data field setters and getters, data formatting, validation etc.
 * 
 * <p>NMEA 0183 data is transmitted in form of ASCII Strings that are called
 * <em>sentences</em>. Each sentence starts with a '$', a two letter
 * <em>talker ID</em>, a three letter <em>sentence ID</em>, followed by a number
 * of comma separated <em>data fields</em>, <em>optional checksum</em> and a
 * carriage return/line feed terminator ({@code CR/LF}). Sentences may
 * contain up to 82 characters including the {@code CR/LF}. If data for
 * certain field is not available, the field value is simply omitted, but the
 * commas that would delimit it are still sent, with no space between them.
 * 
 * <p>Sentence structure:<pre>
 * $&lt;id&gt;,&lt;field #0&gt;,&lt;field #1&gt;,...,&lt;field #n&gt;*&lt;checksum&gt;(CR/LF)
 * </pre>
 * 
 * <p>For more details, see <a href="http://catb.org/gpsd/NMEA.html"
 * target="_blank">NMEA Revealed</a> by Eric S. Raymond.
 * 
 * <p>This class can also be used to implement and integrate parsers not 
 * provided by in the library. See {@link SentenceParser} for more instructions.
 */
public abstract class AbstractSentence implements Sentence {

  // The first character which will be '$' most of the times but could be '!'.
  private char beginChar;

  // The first two characters after '$'.
  private TalkerId talkerId;

  // The next three characters after talker id.
  private final String sentenceId;

  // actual data fields (sentence id and checksum omitted)
  private List<String> fields = new ArrayList<String>();




  /**
   * Creates a new empty sentence with specified begin char, talker id, 
   * sentence id and number of fields.
   *
   * @param begin Begin char, $ or !
   * @param tid TalkerId to set
   * @param sid SentenceId to set
   * @param size Number of sentence data fields
   */
  protected AbstractSentence( final char begin, final TalkerId tid, final SentenceId sid, final int size ) {
    this( begin, tid, sid.toString(), size );
  }




  /**
   * Creates a new empty sentence with specified begin char, talker id,
   * sentence id and number of fields.
   * 
   * @param begin The begin character, e.g. '$' or '!'
   * @param talker TalkerId to set
   * @param type Sentence id as String, e.g. "GGA or "GLL".
   * @param size Number of sentence data fields
   */
  protected AbstractSentence( final char begin, final TalkerId talker, final String type, final int size ) {
    if ( size < 1 ) {
      throw new IllegalArgumentException( "Minimum number of fields is 1" );
    }
    if ( talker == null ) {
      throw new IllegalArgumentException( "Talker ID must be specified" );
    }
    if ( ( type == null ) || "".equals( type ) ) {
      throw new IllegalArgumentException( "Sentence ID must be specified" );
    }
    beginChar = begin;
    talkerId = talker;
    sentenceId = type;
    final String[] values = new String[size];
    Arrays.fill( values, "" );
    fields.addAll( Arrays.asList( values ) );
  }




  /**
   * Creates a new instance of SentenceParser. Validates the input String and
   * resolves talker id and sentence type.
   * 
   * @param nmea A valid NMEA 0183 sentence
   * @throws IllegalArgumentException If the specified sentence is invalid or
   *             if sentence type is not supported.
   */
  public AbstractSentence( final String nmea ) {

    if ( !SentenceValidator.isValid( nmea ) ) {
      final String msg = String.format( "Invalid data [%s]", nmea );
      throw new IllegalArgumentException( msg );
    }

    beginChar = nmea.charAt( 0 );
    talkerId = TalkerId.parse( nmea );
    sentenceId = SentenceId.parseStr( nmea );

    final int begin = nmea.indexOf( Sentence.FIELD_DELIMITER ) + 1;
    final int end = Checksum.index( nmea );

    final String csv = nmea.substring( begin, end );
    final String[] values = csv.split( String.valueOf( FIELD_DELIMITER ), -1 );
    fields.addAll( Arrays.asList( values ) );
  }




  /**
   * Creates a new instance of SentenceParser with specified sentence data.
   * Type of the sentence is checked against the specified expected sentence
   * type id.
   * 
   * @param nmea Sentence String
   * @param type Sentence type enum
   */
  AbstractSentence( final String nmea, final SentenceId type ) {
    this( nmea, type.toString() );
  }




  /**
   * Creates a new instance of SentenceParser. 
   * 
   * <p>Sentence may be constructed only if parameter {@code nmea} contains a 
   * valid NMEA 0183 sentence of the specified {@code type}.
   * 
   * <p>For example, GGA sentence implementation should specify "GGA" as the 
   * type.
   *
   * @param nmea NMEA 0183 sentence String
   * @param type Expected type of the sentence in {@code nmea} parameter
   * 
   * @throws IllegalArgumentException If the specified sentence is not a valid
   *         or is not of expected type.
   */
  protected AbstractSentence( final String nmea, final String type ) {
    this( nmea );
    if ( ( type == null ) || "".equals( type ) ) {
      throw new IllegalArgumentException( "Sentence type must be specified." );
    }
    final String sid = getSentenceId();
    if ( !sid.equals( type ) ) {
      final String ptrn = "Sentence id mismatch; expected [%s], found [%s].";
      final String msg = String.format( ptrn, type, sid );
      throw new IllegalArgumentException( msg );
    }
  }




  /**
   * Creates a new instance of SentenceParser without any data.
   * 
   * @param tid Talker id to set in sentence
   * @param sid Sentence id to set in sentence
   * @param size Number of data fields following the sentence id field
   */
  AbstractSentence( final TalkerId tid, final SentenceId sid, final int size ) {
    this( tid, sid.toString(), size );
  }




  /**
   * Creates a new empty sentence with specified talker and sentence IDs.
   * 
   * @param talker Talker type Id, e.g. "GP" or "LC".
   * @param type Sentence type Id, e.g. "GGA or "GLL".
   * @param size Number of data fields
   */
  protected AbstractSentence( final TalkerId talker, final String type, final int size ) {
    this( Sentence.BEGIN_CHAR, talker, type, size );
  }




  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj ) {
    if ( obj == this ) {
      return true;
    }
    if ( obj instanceof AbstractSentence ) {
      final AbstractSentence sp = (AbstractSentence)obj;
      return sp.toString().equals( toString() );
    }
    return false;
  }




  /**
   * @see coyote.nmea.Sentence#getBeginChar()
   */
  @Override
  public final char getBeginChar() {
    return beginChar;
  }




  /**
   * Parse a single character from the specified sentence field.
   * 
   * @param index Data field index in sentence
   * 
   * @return Character contained in the field
   * 
   * @throws ParseException If field contains more than one character
   */
  protected final char getCharValue( final int index ) {
    final String val = getStringValue( index );
    if ( val.length() > 1 ) {
      final String msg = String.format( "Expected char, found String [%s]", val );
      throw new ParseException( msg );
    }
    return val.charAt( 0 );
  }




  /**
   * Parse double value from the specified sentence field.
   * 
   * @param index Data field index in sentence
   * 
   * @return Field as parsed by {@link java.lang.Double#parseDouble(String)}
   */
  protected final double getDoubleValue( final int index ) {
    double value;
    try {
      value = Double.parseDouble( getStringValue( index ) );
    } catch ( final NumberFormatException ex ) {
      throw new ParseException( "Field does not contain double value", ex );
    }
    return value;
  }




  /**
   * @see coyote.nmea.Sentence#getFieldCount()
   */
  @Override
  public final int getFieldCount() {
    if ( fields == null ) {
      return 0;
    }
    return fields.size();
  }




  /**
   * Parse integer value from the specified sentence field.
   * 
   * @param index Field index in sentence
   * 
   * @return Field parsed by {@link java.lang.Integer#parseInt(String)}
   */
  protected final int getIntValue( final int index ) {
    int value;
    try {
      value = Integer.parseInt( getStringValue( index ) );
    } catch ( final NumberFormatException ex ) {
      throw new ParseException( "Field does not contain integer value", ex );
    }
    return value;
  }




  /**
   * @see coyote.nmea.Sentence#getSentenceId()
   */
  @Override
  public final String getSentenceId() {
    return sentenceId;
  }




  /**
   * Get contents of a data field as a String. Field indexing is zero-based.
   * 
   * <p>The address field (e.g. {@code $GPGGA}) and checksum at the end 
   * are not considered as a data fields and cannot therefore be fetched with 
   * this method.
   * 
   * <p>Field indexing, let i = 1:<pre>
   * $&lt;id&gt;,&lt;i&gt;,&lt;i+1&gt;,&lt;i+2&gt;,...,&lt;i+n&gt;*&lt;checksum&gt;</pre>
   * 
   * @param index Field index
   * 
   * @return Field value as String
   * 
   * @throws DataNotAvailableException If the field is empty
   */
  protected final String getStringValue( final int index ) {
    final String value = fields.get( index );
    if ( ( value == null ) || "".equals( value ) ) {
      throw new DataNotAvailableException( "Data not available" );
    }
    return value;
  }




  /**
   * @see coyote.nmea.Sentence#getTalkerId()
   */
  @Override
  public final TalkerId getTalkerId() {
    return talkerId;
  }




  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }




  /**
   * Tells is if the field specified by the given index contains a value.
   * 
   * @param index Field index
   * 
   * @return True if field contains value, otherwise false.
   */
  protected final boolean hasValue( final int index ) {
    return ( fields.size() > index ) && ( fields.get( index ) != null ) && !fields.get( index ).isEmpty();
  }




  /**
   * @see coyote.nmea.Sentence#isAISSentence()
   */
  @Override
  public boolean isAISSentence() {
    final String[] types = { "VDO", "VDM" };
    return Arrays.asList( types ).contains( getSentenceId() );
  }




  @Override
  public boolean isProprietary() {
    return TalkerId.P.equals( getTalkerId() );
  }




  @Override
  public boolean isValid() {
    return SentenceValidator.isValid( toString() );
  }




  @Override
  public final void reset() {
    for ( int i = 0; i < fields.size(); i++ ) {
      fields.set( i, "" );
    }
  }




  @Override
  public void setBeginChar( final char ch ) {
    if ( ( ch != BEGIN_CHAR ) && ( ch != ALTERNATIVE_BEGIN_CHAR ) ) {
      final String msg = "Invalid begin char; expected '$' or '!'";
      throw new IllegalArgumentException( msg );
    }
    beginChar = ch;
  }




  /**
   * Set a character in specified field.
   * 
   * @param index Field index
   * @param value Value to set
   */
  protected final void setCharValue( final int index, final char value ) {
    setStringValue( index, String.valueOf( value ) );
  }




  /**
   * Set degrees value, e.g. course or heading.
   * 
   * @param index Field index where to insert value
   * @param deg The degrees value to set
   * 
   * @throws IllegalArgumentException If degrees value out of range [0..360]
   */
  protected final void setDegreesValue( final int index, final double deg ) {
    if ( ( deg < 0 ) || ( deg > 360 ) ) {
      throw new IllegalArgumentException( "Value out of bounds [0..360]" );
    }
    setDoubleValue( index, deg, 3, 1 );
  }




  /**
   * Set double value in specified field. Value is set "as-is" without any
   * formatting or rounding.
   * 
   * @param index Field index
   * @param value Value to set
   * 
   * @see #setDoubleValue(int, double, int, int)
   */
  protected final void setDoubleValue( final int index, final double value ) {
    setStringValue( index, String.valueOf( value ) );
  }




  /**
   * Set double value in specified field, with given number of digits before
   * and after the decimal separator ('.'). When necessary, the value is
   * padded with leading zeros and/or rounded to meet the requested number of
   * digits.
   * 
   * @param index Field index
   * @param value Value to set
   * @param leading Number of digits before decimal separator
   * @param decimals Maximum number of digits after decimal separator
   * 
   * @see #setDoubleValue(int, double)
   */
  protected final void setDoubleValue( final int index, final double value, final int leading, final int decimals ) {

    final StringBuilder pattern = new StringBuilder();
    for ( int i = 0; i < leading; i++ ) {
      pattern.append( '0' );
    }
    if ( decimals > 0 ) {
      pattern.append( '.' );
      for ( int i = 0; i < decimals; i++ ) {
        pattern.append( '0' );
      }
    }
    if ( pattern.length() == 0 ) {
      pattern.append( '0' );
    }

    final DecimalFormat nf = new DecimalFormat( pattern.toString() );
    final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setDecimalSeparator( '.' );
    nf.setDecimalFormatSymbols( dfs );

    setStringValue( index, nf.format( value ) );
  }




  /**
   * Sets the number of sentence data fields. Increases or decreases the
   * fields array, values in fields not affected by the change remain
   * unchanged. Does nothing if specified new size is equal to count returned
   * by {@link #getFieldCount()}.
   * 
   * @param size Number of data fields, must be greater than zero.
   */
  protected final void setFieldCount( final int size ) {
    if ( size < 1 ) {
      throw new IllegalArgumentException( "Number of fields must be greater than zero." );
    }

    if ( size < fields.size() ) {
      fields = fields.subList( 0, size );
    } else if ( size > fields.size() ) {
      for ( int i = fields.size(); i < size; i++ ) {
        fields.add( "" );
      }
    }
  }




  /**
   * Set integer value in specified field.
   * 
   * @param index Field index
   * @param value Value to set
   */
  protected final void setIntValue( final int index, final int value ) {
    setStringValue( index, String.valueOf( value ) );
  }




  /**
   * Set integer value in specified field, with specified minimum number of
   * digits. Leading zeros are added to value if when necessary.
   * 
   * @param index Field index
   * @param value Value to set
   * @param leading Number of digits to use.
   */
  protected final void setIntValue( final int index, final int value, final int leading ) {
    String pattern = "%d";
    if ( leading > 0 ) {
      pattern = "%0" + leading + "d";
    }
    setStringValue( index, String.format( pattern, value ) );
  }




  /**
   * Set String value in specified data field.
   * 
   * @param index Field index
   * @param value String to set, {@code null} converts to empty String.
   */
  protected final void setStringValue( final int index, final String value ) {
    fields.set( index, value == null ? "" : value );
  }




  /**
   * Replace multiple fields with given String array, starting at the specified 
   * index. 
   * 
   * <p>If parameter {@code first} is zero, all sentence fields are 
   * replaced.
   * 
   * <p>If the length of {@code newFields} does not fit in the sentence field 
   * count or it contains less values, fields are removed or added accordingly. 
   * As the result, total number of fields may increase or decrease. Thus, if 
   * the sentence field count must not change, you may need to add empty 
   * Strings to {@code newFields} in order to preserve the original number of 
   * fields. Also, all existing values after {@code first} are lost.
   * 
   * @param first Index of first field to set
   * @param newFields Array of Strings to set
   */
  protected final void setStringValues( final int first, final String[] newFields ) {

    final List<String> temp = new ArrayList<String>();
    temp.addAll( fields.subList( 0, first ) );

    for ( final String field : newFields ) {
      temp.add( field == null ? "" : field );
    }
    fields.clear();
    fields = temp;
  }




  /**
   * @see coyote.nmea.Sentence#setTalkerId(coyote.nmea.TalkerId)
   */
  @Override
  public final void setTalkerId( final TalkerId id ) {
    talkerId = id;
  }




  /**
   * @see coyote.nmea.Sentence#toSentence()
   */
  @Override
  public final String toSentence() {
    final String s = toString();
    if ( !SentenceValidator.isValid( s ) ) {
      final String msg = String.format( "Validation failed [%s]", toString() );
      throw new IllegalStateException( msg );
    }
    return s;
  }




  /**
   * @see coyote.nmea.Sentence#toSentence(int)
   */
  @Override
  public final String toSentence( final int maxLength ) {
    final String s = toSentence();
    if ( s.length() > maxLength ) {
      final String msg = "Sentence max length exceeded " + maxLength;
      throw new IllegalStateException( msg );
    }
    return s;
  }




  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    final StringBuilder sb = new StringBuilder( MAX_LENGTH );
    sb.append( talkerId.toString() );
    sb.append( sentenceId );

    for ( final String field : fields ) {
      sb.append( FIELD_DELIMITER );
      sb.append( field == null ? "" : field );
    }

    final String checksum = Checksum.xor( sb.toString() );
    sb.append( CHECKSUM_DELIMITER );
    sb.append( checksum );
    sb.insert( 0, beginChar );

    return sb.toString();
  }

}
