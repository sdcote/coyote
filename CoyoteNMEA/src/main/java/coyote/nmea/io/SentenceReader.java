package coyote.nmea.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import coyote.nmea.Sentence;
import coyote.nmea.SentenceEvent;
import coyote.nmea.SentenceId;
import coyote.nmea.SentenceListener;
import coyote.nmea.SentenceValidator;
import coyote.nmea.sentence.SentenceParser;


/**
 * This reads lines from an input stream and passes those lines to listeners as 
 * either valid sentences, raw data or exceptions.
 * 
 * <p>The {@link #read()} method is designed to be called repeatedly until the 
 * end of stream is reached when the {@link #read()} returns false, indicating 
 * EOF on the stream. This is designed to work well with reentrant designs
 * while still allowing separate threads to be used as background readers which 
 * can place valid sentences on a queue for other components to process when 
 * available.
 */
public class SentenceReader {
  //private static final String LISTENER_EXCPTION = "Exception caught from SentenceListener";

  // Map key for listeners that listen for all sentences
  private static final String ALL = "_ALL_";

  private ConcurrentMap<String, List<SentenceListener>> listeners = new ConcurrentHashMap<String, List<SentenceListener>>();

  private DataReader reader;

  private SentenceParser parser;

  // Non-NMEA data listener for capturing yet to be supported sentences or formats
  private DataListener dataListener;

  // Exception listener for handling our exceptions
  private ExceptionListener exceptionListener = null;




  public SentenceReader( InputStream stream ) {
    reader = new DefaultDataReader( stream, this );
    parser = SentenceParser.getInstance();
  }




  /**
   * Dispatch data to all listeners.
   *
   * @param sentence sentence string.
   */
  void fireSentenceEvent( Sentence sentence ) {

    String type = sentence.getSentenceId();
    Set<SentenceListener> targets = new HashSet<SentenceListener>();

    if ( listeners.containsKey( type ) ) {
      targets.addAll( listeners.get( type ) );
    }
    if ( listeners.containsKey( ALL ) ) {
      targets.addAll( listeners.get( ALL ) );
    }

    for ( SentenceListener listener : targets ) {
      try {
        SentenceEvent se = new SentenceEvent( this, sentence );
        listener.onRead( se );
      } catch ( Exception e ) {
        //Log.warn( LISTENER_EXCPTION, e );
      }
    }
  }




  /**
   * Pass data to DataListener.
   */
  void fireDataEvent( String data ) {
    try {
      if ( dataListener != null ) {
        dataListener.onRead( data );
      }
    } catch ( Exception e ) {

    }
  }




  /**
   * Returns all currently registered SentenceListeners.
   * 
   * @return List of SentenceListeners or empty list.
   */
  List<SentenceListener> getSentenceListeners() {
    Set<SentenceListener> all = new HashSet<SentenceListener>();
    for ( List<SentenceListener> sl : listeners.values() ) {
      all.addAll( sl );
    }
    return new ArrayList<SentenceListener>( all );
  }




  /**
   * Adds a {@link SentenceListener} that wants to receive all sentences read
   * by the reader.
   *
   * @param listener {@link SentenceListener} to be registered.
   * 
   * @see SentenceListener
   */
  public void addSentenceListener( SentenceListener listener ) {
    registerListener( listener, ALL );
  }




  /**
   * Adds a {@link SentenceListener} that is interested in receiving only
   * sentences of certain type.
   *
   * @param listener SentenceListener to add
   * @param type Sentence type for which the listener is registered.
   * 
   * @see SentenceListener
   */
  public void addSentenceListener( SentenceListener listener, SentenceId type ) {
    registerListener( listener, type.toString() );
  }




  /**
   * Adds a {@link SentenceListener} that is interested in receiving only
   * sentences of certain type.
   *
   * @param listener SentenceListener to add
   * @param type Sentence type for which the listener is registered.
   * 
   * @see SentenceListener
   */
  public void addSentenceListener( SentenceListener listener, String type ) {
    registerListener( listener, type );
  }




  /**
   * Set listener for any data that is not recognized as NMEA 0183. 
   * 
   * <p>There are devices and environments that produce mixed content with both 
   * NMEA and non-NMEA data. This allows that data to be captured and processed
   * accordingly.
   * 
   * @param listener Listener to set, {@code null} to remove.
   */
  public void setDataListener( DataListener listener ) {
    dataListener = listener;
  }




  /**
   * Returns the data listener.
   * 
   * @return the currently set DataListener, or {@code null} if none.
   */
  public DataListener getDataListener() {
    return dataListener;
  }




  /**
   * Returns the exception call-back listener.
   * 
   * @return Currently set ExceptionListener, or {@code null} if none.
   */
  public ExceptionListener getExceptionListener() {
    return exceptionListener;
  }




  /**
   * Set exception call-back listener.
   * 
   * @param exceptionListener Listener to set, or {@code null} to reset.
   */
  public void setExceptionListener( ExceptionListener exceptionListener ) {
    this.exceptionListener = exceptionListener;
  }




  /**
    * Registers a SentenceListener with given key.
    * 
    * @param listener SentenceListener to register
    * @param type Sentence type to register for
    */
  private void registerListener( SentenceListener listener, String type ) {
    if ( listeners.containsKey( type ) ) {
      listeners.get( type ).add( listener );
    } else {
      List<SentenceListener> list = new Vector<SentenceListener>();
      list.add( listener );
      listeners.put( type, list );
    }
  }




  /**
   * Remove a listener from reader.
   *
   * @param listener {@link SentenceListener} to be removed.
   */
  public void removeSentenceListener( SentenceListener listener ) {
    for ( List<SentenceListener> list : listeners.values() ) {
      if ( list.contains( listener ) ) {
        list.remove( listener );
      }
    }
  }




  /**
   * Perform a read and dispatch any valid sentences.
   * 
   * <p>This may block depending on the nature of the reader. If blocking is 
   * not tolerable, use the {@link SentenceMonitor} which will run in a 
   * separate thread and just sent sentences as they are read.
   * 
   * @return false when the end of file/stream has been reached, true otherwise
   */
  public boolean read() {
    try {
      String line = reader.read();

      if ( line != null ) {
        try {
          if ( SentenceParser.isNotBlank( line ) ) {
            if ( SentenceValidator.isValid( line ) ) {
              Sentence sentence = parser.createSentence( line );
              fireSentenceEvent( sentence );
            } else if ( !SentenceValidator.isSentence( line ) ) {
              fireDataEvent( line );
            }
          }
        } catch ( Exception e ) {
          handleException( "Sentence parsing failed", e );
        }
        return true;
      } else {
        return false;
      }
    } catch ( Exception e ) {
      handleException( "Data read failed", e );
      return false;
    }
  }




  /**
   * Handles an exception by passing it to ExceptionHandler. 
   * 
   * <p>If no handler is present, the exception is silently ignored
   * 
   * @param msg Error message for logging
   * @param ex Exception to handle
   */
  void handleException( String msg, Exception ex ) {
    if ( exceptionListener != null ) {
      try {
        exceptionListener.onException( ex );
      } catch ( Exception e ) {
        //LOGGER.log( Level.WARNING, "Exception thrown by ExceptionListener", e );
      }
    } else {
      //LOGGER.log( Level.WARNING, msg, ex );
    }
  }




  /**
   * Determine if the reader has enough data in its buffer for a complete line 
   * to be read.
   * 
   * <p>This can be used to prevent the reader from blocking on call to {@link 
   * #read()}.
   * 
   * @return true if the next call to read will not block, false otherwise
   */
  public boolean isReady() {
    try {
      return reader.isReady();
    } catch ( IOException e ) {}
    return false;
  }

}
