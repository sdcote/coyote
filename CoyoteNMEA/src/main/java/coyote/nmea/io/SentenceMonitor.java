package coyote.nmea.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import coyote.loader.log.Log;


/**
 * This runs in its own thread and reads sentences with a sentence reader.
 * 
 * <p>Any observed NMEA sentences will be passed to whatever listeners are 
 * registered with the monitor/reader.
 */
public class SentenceMonitor implements Runnable {
  private List<MonitorListener> listeners = Collections.synchronizedList( new ArrayList<MonitorListener>() );
  private static final String LOG_MSG = "Exception caught from MonitorListener: ";

  // Default timeout value in milliseconds.
  private static final int DEFAULT_TIMEOUT = 5000;
  //timeout for "reading paused" in ms
  private volatile int pauseTimeout = DEFAULT_TIMEOUT;
  private long lastRead = -1;

  private SentenceReader reader = null;
  private Thread thread;

  // Sleep time between failed read attempts to prevent busy-looping
  private static final int SLEEP_TIME = 100;
  private volatile boolean running = false;




  /**
   * Create a SentenceMonitor for the given reader.
   * 
   * @param reader The reader the monitor uses
   */
  public SentenceMonitor( SentenceReader reader ) {
    this.reader = reader;
  }




  /**
   * Start this monitor running in its own thread.
   * 
   * <p>This will return immediately upon starting the monitoring thread.
   */
  public void start() {
    if ( thread != null && thread.isAlive() && reader != null && this.isRunning() ) {
      throw new IllegalStateException( "Monitor is already running" );
    }

    thread = new Thread( this );
    thread.start();
  }




  /**
   * Stop this monitor from reading.
   */
  public synchronized void stop() {
    if ( this.isRunning() ) {
      setRunning( false );
    }
  }




  /**
   * Returns the current reading paused timeout.
   *
   * @return Timeout limit in milliseconds.
   * 
   * @see #setPauseTimeout(int)
   */
  public int getPauseTimeout() {
    return pauseTimeout;
  }




  /**
   * Set timeout time for reading paused events.
   * 
   * <p>Default is 5000 ms.
   *
   * @param millis Timeout in milliseconds.
   */
  public void setPauseTimeout( int millis ) {
    this.pauseTimeout = millis;
  }




  /**
   * Returns all currently registered Monitor Listeners.
   * 
   * @return List of MonitorListener or empty list.
   */
  List<MonitorListener> getListeners() {
    List<MonitorListener> retval = new ArrayList<MonitorListener>( listeners.size() );
    for ( MonitorListener ml : listeners ) {
      retval.add( ml );
    }
    return retval;
  }




  /**
   * Notifies all listeners that reader has paused due to timeout.
   */
  void fireReadingPaused() {
    for ( MonitorListener listener : getListeners() ) {
      try {
        listener.readingPaused();
      } catch ( Exception e ) {
        Log.warn( LOG_MSG + e.getMessage() );
      }
    }
  }




  /**
   * Notifies all listeners that NMEA data has been detected in the stream and
   * events will be dispatched until stopped or timeout occurs.
   */
  void fireReadingStarted() {
    for ( MonitorListener listener : getListeners() ) {
      try {
        listener.readingStarted();
      } catch ( Exception e ) {
        Log.warn( LOG_MSG + e.getMessage() );
      }
    }
  }




  /**
   * Notifies all listeners that data reading has stopped.
   */
  void fireReadingStopped() {
    for ( MonitorListener listener : getListeners() ) {
      try {
        listener.readingStopped();
      } catch ( Exception e ) {
        Log.warn( LOG_MSG + e.getMessage() );
      }
    }
  }




  /**
   * Resets the last time a read occurred to its initial state.
   */
  public void reset() {
    lastRead = -1;
  }




  /**
   * Refreshes the monitor timestamp and fires reading started event if
   * currently paused.
   */
  public void refresh() {
    if ( lastRead < 0 ) {
      fireReadingStarted();
    }
    lastRead = System.currentTimeMillis();
  }




  /**
   * Perform an activity check.
   * 
   * <p>If the time interval between the last read and the current time exceeds 
   * the Pause Timeout, the {@code readingPaused} event is fired.
   * 
   * <p>Because the monitor may be blocked in a read, there is no reliable way 
   * for the SentenceMonitor to monitor itself. Therefore, the only way for the 
   * {@code readingPaused} event to be fired is for another thread to call this 
   * method periodically.  
   */
  public void checkPaused() {
    if ( getLastRead() > 0 ) {
      if ( ( System.currentTimeMillis() - getLastRead() ) >= getPauseTimeout() ) {
        fireReadingPaused();
        reset();
      }
    }

  }




  /**
   * Tells if the reader is running and actively scanning the data source for
   * new data.
   * 
   * @return <code>true</code> if running, otherwise <code>false</code>.
   */
  synchronized boolean isRunning() {
    return running;
  }




  /**
   * This is the main read loop.
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {

    setRunning( true );

    while ( isRunning() ) {
      if ( reader.isReady() ) {
        reader.read();// blocking read and event dispatch
        refresh(); // refresh the last read time
      }

      // pause a short time
      try {
        Thread.sleep( SLEEP_TIME );
      } catch ( InterruptedException interruptException ) {}
    }
    reset();
    fireReadingStopped();

  }




  private synchronized void setRunning( boolean flag ) {
    running = flag;
  }




  public void addListener( MonitorListener listener ) {
    if ( listener != null ) {
      listeners.add( listener );
    }
  }




  /**
   * @return the epoch time in milliseconds when the last read returned
   */
  public long getLastRead() {
    return lastRead;
  }

}
