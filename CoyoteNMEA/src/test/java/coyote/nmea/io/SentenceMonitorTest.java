package coyote.nmea.io;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import coyote.nmea.Sentence;
import coyote.nmea.SentenceEvent;
import coyote.nmea.SentenceListener;


public class SentenceMonitorTest {

  public final static String TEST_DATA = "src/test/resources/Navibe-GM720.txt";

  private Sentence sentence;
  private SentenceReader reader;
  private boolean paused;
  private boolean started;
  private boolean stopped;
  private InputStream stream;
  private SentenceMonitor monitor;




  @Before
  public void setUp() throws Exception {
    File file = new File( TEST_DATA );
    System.out.println( file.getAbsolutePath() );
    try {
      stream = new FileInputStream( file );
      reader = new SentenceReader( stream );

      // listen for all sentences
      reader.addSentenceListener( new TestReaderListener() );

      // create a monitor
      monitor = new SentenceMonitor( reader );
      monitor.addListener( new TestMonitorListener() );

      // listen for monitor events
    } catch ( FileNotFoundException e ) {
      fail( e.getMessage() );
    }

  }




  @Test
  @Ignore
  public void test() {
    fail( "Not yet implemented" );
  }




  @Test
  public void testStartAndStop() {
    try {
      assertNull( sentence );
      assertFalse( started );
      assertFalse( paused );
      assertFalse( stopped );

      monitor.start();
      Thread.sleep( 500 );

      assertNotNull( sentence );
      assertTrue( started );
      assertFalse( paused );

      monitor.stop();
      Thread.sleep( 100 );

      assertFalse( paused );
      assertTrue( stopped );
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }

  // 
  // - 
  // Listeners
  // - 
  //

  public class TestReaderListener implements SentenceListener {
    @Override
    public void onRead( SentenceEvent event ) {
      sentence = event.getSentence();
    }

  }

  public class TestMonitorListener implements MonitorListener {
    @Override
    public void readingPaused() {
      paused = true;
    }




    @Override
    public void readingStarted() {
      started = true;
    }




    @Override
    public void readingStopped() {
      stopped = true;
    }

  }

}
