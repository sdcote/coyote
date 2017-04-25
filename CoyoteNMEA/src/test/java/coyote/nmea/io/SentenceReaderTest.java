package coyote.nmea.io;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import coyote.nmea.Sentence;
import coyote.nmea.SentenceEvent;
import coyote.nmea.SentenceId;
import coyote.nmea.SentenceListener;
import coyote.nmea.UnsupportedSentenceException;


public class SentenceReaderTest {
  public final static String TEST_DATA = "src/test/resources/Navibe-GM720.txt";
  // public final static String TEST_DATA = "src/test/resources/Garmin-GPS15H.txt"; // GRME & GRMM
  // public final static String TEST_DATA = "src/test/resources/Garmin-GPS15.txt"; // GRME, GMRV & GRMM
  // public final static String TEST_DATA = "src/test/resources/Garmin-GPS76_route.txt"; // GRMZ, GRME & GRMM
  // public final static String TEST_DATA = "src/test/resources/Garmin-GPS76_goto.txt"; // GRMZ, GRME & GRMM
  // public final static String TEST_DATA = "src/test/resources/Garmin-GPS76_diff.txt"; // GRMZ, GRME & GRMM
  // public final static String TEST_DATA = "src/test/resources/Garmin-GPS76.txt"; // GRMZ, GRME & GRMM

  // AIS
  // public final static String TEST_DATA = "src/test/resources/sample1.txt";
  // public final static String TEST_DATA = "src/test/resources/AIS-VDM-VDO.txt";

  private Sentence sentence = null;
  private String dataline = null;
  private SentenceReader reader;
  private SentenceListener dummyListener; // we will remove this listener
  private SentenceListener testListener; // collects sentences for us
  private InputStream stream;




  @Before
  public void setUp() throws Exception {
    File file = new File( TEST_DATA );
    stream = new FileInputStream( file );
    reader = new SentenceReader( stream );

    dummyListener = new DummySentenceListener();
    testListener = new TestSentenceListener();
    reader.addSentenceListener( dummyListener );
    reader.addSentenceListener( testListener, SentenceId.GGA );
    reader.addSentenceListener( testListener, SentenceId.GSV );
    reader.addSentenceListener( testListener, SentenceId.RMC );
    reader.addSentenceListener( testListener, SentenceId.GSA );
    reader.setDataListener( new TestDataListener() );
    reader.setExceptionListener( new TestExceptionListener() );
  }




  @Test
  public void testRemoveSentenceListener() {
    List<SentenceListener> listeners = reader.getSentenceListeners();
    assertNotNull( listeners );
    int count = listeners.size();
    assertTrue( count > 0 );
    reader.removeSentenceListener( dummyListener );
    listeners = reader.getSentenceListeners();
    assertNotNull( listeners );
    assertTrue( count > listeners.size() );
  }




  @Test
  public void testRead() {

    while ( reader.read() ) {
      if ( sentence != null ) {
        System.out.println( sentence );
        sentence = null;
      }
    }
  }

  //

  // - 

  //

  class DummySentenceListener implements SentenceListener {
    @Override
    public void onRead( SentenceEvent event ) {}
  }

  class TestSentenceListener implements SentenceListener {
    @Override
    public void onRead( SentenceEvent event ) {
      sentence = event.getSentence();
    }
  }

  class TestDataListener implements DataListener {
    @Override
    public void onRead( String data ) {
      dataline = data;
      System.out.println( "DataListener: " + dataline );
    }
  }

  class TestExceptionListener implements ExceptionListener {
    @Override
    public void onException( Exception e ) {
      if ( e instanceof UnsupportedSentenceException ) {
        System.out.println( "ExceptionListener: Unsupported sentence: " + ( (UnsupportedSentenceException)e ).getSentence() );
      }
      System.out.println( "ExceptionListener: " + e );
    }
  }

}
