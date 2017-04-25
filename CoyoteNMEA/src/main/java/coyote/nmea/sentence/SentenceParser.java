package coyote.nmea.sentence;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import coyote.nmea.Sentence;
import coyote.nmea.SentenceId;
import coyote.nmea.SentenceValidator;
import coyote.nmea.TalkerId;
import coyote.nmea.UnsupportedSentenceException;


/**
 * Parser for creating sentences from strings.
 * 
 * <p>This class parses strings into NMEA 0183 sentences and passes those 
 * sentences to listeners registered with this parser. 
 * 
 * <p>Custom sentences may be implemented and registered in the parser as 
 * follows:<ol>
 * <li>Define a sentence interface by extending the {@link Sentence} interface
 * (e.g. {@code com.acme.XYZSentence}).</li>
 * <li>Implement the interface in a class that extends {@link AbstractSentence},
 * (e.g. {@code com.acme.XYZSentenceImpl}).</li>
 * <li>Use the protected getters and setters in {@code AbstractSentence} to 
 * read and write sentence data.</li>
 * <li>Add a constructor in {@code XYZSentenceImpl} with {@code String} parameter, 
 * i.e. the sentence to be parsed. Pass this parameter to
 * {@link AbstractSentence#AbstractSentence(String, String)} with expected 
 * sentence type (e.g. {@code "XYZ"}).</li>
 * <li>Add another constructor with {@link TalkerId} parameter. Pass this 
 * parameter to {@link AbstractSentence#AbstractSentence(TalkerId, String, int)}
 * with sentence type and number of data fields.</li>
 * <li>Register {@code XYZSentenceImpl} in {@code SentenceParser} by using the 
 * {@link #registerSentence(String, Class)} method.</li>
 * <li>Use {@link SentenceParser#createSentence(String)} or
 * {@link SentenceParser#createSentence(TalkerId, String)} to obtain an 
 * instance of your sentence.</li></ol>
 */
public final class SentenceParser {

  // map that holds registered sentence types and sentence classes
  private static Map<String, Class<? extends AbstractSentence>> sentences;

  // singleton parser instance
  private static volatile SentenceParser instance;




  /**
   * Returns the singleton instance of {@code SentenceParser}.
   *
   * @return SentenceParser instance
   */
  public static SentenceParser getInstance() {
    if ( instance == null ) {
      instance = new SentenceParser();
    }
    return instance;
  }




  /**
   * Constructor.
   */
  private SentenceParser() {
    reset();
  }




  /**
   * Checks if a string is not null, empty ("") and not only whitespace.
   * 
   * <p>This is a convenience wrapper around isBlank(String) to make code 
   * slightly more readable.
   * 
   * @param str the String to check, may be null
   * 
   * @return {@code true} if the String is not empty and not null and not
   *         whitespace
   * 
   * @see #isBlank(String)
   */
  public static boolean isNotBlank( String str ) {
    return !SentenceParser.isBlank( str );
  }




  /**
   * Checks if a string is not null, empty ("") and not only whitespace.
   * 
   * @param str the String to check, may be null
   * 
   * @return {@code true} if the String is not empty and not null and not
   *         whitespace
   */
  public static boolean isBlank( String str ) {
    int strLen;
    if ( str == null || ( strLen = str.length() ) == 0 ) {
      return true;
    }
    for ( int i = 0; i < strLen; i++ ) {
      if ( ( Character.isWhitespace( str.charAt( i ) ) == false ) ) {
        return false;
      }
    }
    return true;
  }




  /**
   * Creates a sentence for specified NMEA 0183 sentence String. 
   * 
   * <p>The sentence implementation is selected from registered sentences 
   * according to sentence type. The returned instance must be cast in to 
   * correct sentence interface, for which the type should first be checked by 
   * using the {@link Sentence#getSentenceId()} method.
   *
   * @param nmea NMEA 0183 sentence String
   * 
   * @return Sentence sentence instance for specified sentence
   * 
   * @throws IllegalArgumentException If there is no sentence registered for 
   *         the given sentence type
   * @throws IllegalStateException If sentence is found, but it does not
   *         implement expected constructors or is otherwise unusable.
   */
  public Sentence createSentence( final String nmea ) {
    final String sid = SentenceId.parseStr( nmea );
    return createSentenceImpl( sid, nmea );
  }




  /**
   * Creates a sentence for specified talker and sentence type. 
   * 
   * <p>The returned instance needs to be cast to corresponding sentence 
   * interface.
   *
   * @param talker Sentence talker id
   * @param type Sentence type
   * 
   * @return Sentence sentence of requested type.
   * 
   * @throws IllegalArgumentException If talker id is null or if there is no
   *         sentence registered for given sentence type.
   * @throws IllegalStateException If sentence instantiation fails.
   */
  public Sentence createSentence( final TalkerId talker, final SentenceId type ) {
    return createSentence( talker, type.toString() );
  }




  /**
   * Creates a sentence for specified talker and sentence type. 
   * 
   * <p>This method is mainly intended to be used when custom sentences have 
   * been registered in the parser. The returned instance needs to be cast to 
   * corresponding sentence interface.
   *
   * @param talker Talker ID to use in sentence
   * @param type Type of the sentence to create
   * 
   * @return Sentence sentence for requested type
   * 
   * @throws IllegalArgumentException If talker id is null or if there is no
   *         sentence registered for given sentence type.
   * @throws IllegalStateException If sentence is found, but it does not
   *         implement expected constructors or is otherwise unusable.
   */
  public Sentence createSentence( final TalkerId talker, final String type ) {
    if ( talker == null ) {
      throw new IllegalArgumentException( "TalkerId cannot be null" );
    }
    return createSentenceImpl( type, talker );
  }




  /**
   * Creates a new sentence instance with specified parameters.
   *
   * @param sid Sentence/sentence type ID, e.g. "GGA" or "GLL"
   * @param param Object to pass as parameter to sentence constructor
   * 
   * @return Sentence sentence
   */
  private Sentence createSentenceImpl( final String sid, final Object param ) {

    Sentence sentence = null;

    if ( !hasSentence( sid ) ) {
      final String msg = String.format( "Sentence for type '%s' not found", sid );
      if ( SentenceValidator.isSentence( (String)param ) ) {
        sentence = new UnknownSentence( (String)param );
      }
      throw new UnsupportedSentenceException( msg, sentence );
    }

    final Class<?> klass = param.getClass();

    try {
      final Class<? extends AbstractSentence> c = sentences.get( sid );
      final Constructor<? extends AbstractSentence> co = c.getConstructor( klass );
      sentence = co.newInstance( param );

    } catch ( final NoSuchMethodException e ) {
      final String name = klass.getName();
      final String msg = "Constructor with %s parameter not found";
      throw new IllegalStateException( String.format( msg, name ), e );
    } catch ( final InstantiationException e ) {
      throw new IllegalStateException( "Unable to instantiate sentence", e );
    } catch ( final IllegalAccessException e ) {
      throw new IllegalStateException( "Unable to access sentence", e );
    } catch ( final InvocationTargetException e ) {
      throw new IllegalStateException( "Unable to invoke sentence constructor", e );
    }

    return sentence;
  }




  /**
   * Tells if the parser is able to create sentence for specified sentence
   * type. 
   * 
   * <p>All {@link SentenceId} enum values should result returning {@code true} 
   * at all times.
   *
   * @param type Sentence type id, e.g. "GLL" or "GGA".
   * 
   * @return true if type is supported, otherwise false.
   */
  public boolean hasSentence( final String type ) {
    return sentences.containsKey( type );
  }




  /**
   * Returns a list of currently parseable sentence types.
   *
   * @return List of sentence IDs
   */
  public List<String> listSentences() {
    final Set<String> keys = sentences.keySet();
    return Arrays.asList( keys.toArray( new String[sentences.size()] ) );
  }




  /**
   * Register a sentence sentence with the parser. 
   * 
   * <p>After registration, the {@link #createSentence(String)} method can be 
   * used to obtain instances of the registered sentence. 
   * 
   * <p>Sentences supported by the library are registered automatically, but 
   * they can be overridden simply be registering a new sentence implementation 
   * for chosen sentence type. That is, each sentence type can have only one
   * sentence registered at a time.
   *
   * @param type Sentence type id, e.g. "GGA" or "GLL".
   * @param sentence Class of sentence implementation for given {@code type}.
   */
  public void registerSentence( final String type, final Class<? extends AbstractSentence> sentence ) {

    try {
      sentence.getConstructor( new Class[] { String.class } );
      sentence.getConstructor( new Class[] { TalkerId.class } );
      sentences.put( type, sentence );
    } catch ( final SecurityException e ) {
      final String msg = "Unable to register sentence due security violation";
      throw new IllegalArgumentException( msg, e );
    } catch ( final NoSuchMethodException e ) {
      final String msg = "Required constructors not found; AbstractSentence(String), AbstractSentence(TalkerId)";
      throw new IllegalArgumentException( msg, e );
    }
  }




  /**
   * Unregisters a sentence class, regardless of sentence type(s) it is
   * registered for.
   *
   * @param sentence Sentence implementation class for {@code type}.
   * 
   * @see #registerSentence(String, Class)
   */
  public void unregisterSentence( final Class<? extends AbstractSentence> sentence ) {
    for ( final String key : sentences.keySet() ) {
      if ( sentences.get( key ) == sentence ) {
        sentences.remove( key );
        break;
      }
    }
  }




  /**
   * Resets the parser to its initial state.
   */
  public void reset() {
    sentences = new HashMap<String, Class<? extends AbstractSentence>>();
    registerSentence( "APB", APBSentenceImpl.class );
    registerSentence( "BOD", BODSentenceImpl.class );
    registerSentence( "DBT", DBTSentenceImpl.class );
    registerSentence( "DPT", DPTSentenceImpl.class );
    registerSentence( "DTM", DTMSentenceImpl.class );
    registerSentence( "GGA", GGASentenceImpl.class );
    registerSentence( "GLL", GLLSentenceImpl.class );
    registerSentence( "GSA", GSASentenceImpl.class );
    registerSentence( "GSV", GSVSentenceImpl.class );
    registerSentence( "HDG", HDGSentenceImpl.class );
    registerSentence( "HDM", HDMSentenceImpl.class );
    registerSentence( "HDT", HDTSentenceImpl.class );
    registerSentence( "MDA", MDASentenceImpl.class );
    registerSentence( "MHU", MHUSentenceImpl.class );
    registerSentence( "MMB", MMBSentenceImpl.class );
    registerSentence( "MTA", MTASentenceImpl.class );
    registerSentence( "MTW", MTWSentenceImpl.class );
    registerSentence( "MWD", MWDSentenceImpl.class );
    registerSentence( "MWV", MWVSentenceImpl.class );
    registerSentence( "RMB", RMBSentenceImpl.class );
    registerSentence( "RMC", RMCSentenceImpl.class );
    registerSentence( "ROT", ROTSentenceImpl.class );
    registerSentence( "RPM", RPMSentenceImpl.class );
    registerSentence( "RSA", RSASentenceImpl.class );
    registerSentence( "RTE", RTESentenceImpl.class );
    registerSentence( "TTM", TTMSentenceImpl.class );
    registerSentence( "VBW", VBWSentenceImpl.class );
    registerSentence( "VDR", VDRSentenceImpl.class );
    registerSentence( "VHW", VHWSentenceImpl.class );
    registerSentence( "VLW", VLWSentenceImpl.class );
    registerSentence( "VTG", VTGSentenceImpl.class );
    registerSentence( "WPL", WPLSentenceImpl.class );
    registerSentence( "XDR", XDRSentenceImpl.class );
    registerSentence( "XTE", XTESentenceImpl.class );
    registerSentence( "ZDA", ZDASentenceImpl.class );

    //
    // Other sentences in the NMEA 0183 Specification yet to be implemented
    //
    registerSentence( "CUR", CURSentenceImpl.class ); // still in DEV
    registerSentence( "VWR", VWRSentenceImpl.class ); // still in DEV
    registerSentence( "VWT", VWTSentenceImpl.class ); // still in DEV

    //
    // AIS Messages - Not actually NMEA, but uses NMEA 0183 formatting
    //
    registerSentence( "VDM", VDMSentenceImpl.class );
    registerSentence( "VDO", VDOSentenceImpl.class );

    //
    // Garmin Proprietary Messages
    //
    //    registerSentence( "GRME", GRMESentenceImpl.class );
    //    registerSentence( "GRMM", GRMMSentenceImpl.class );

  }

}
