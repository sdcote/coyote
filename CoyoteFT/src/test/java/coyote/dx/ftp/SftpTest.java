package coyote.dx.ftp;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import coyote.dx.TransformEngine;
import coyote.loader.log.Log;


/**
 * 
 */
public class SftpTest extends AbstractEngineTest {

  //@Test
  public void test() {

    // load the configuration from the class path
    TransformEngine engine = loadEngine( "sftptest" );
    assertNotNull( engine );

    try {
      engine.run();
    } catch ( Exception e ) {
      Log.error( e.getMessage() );
      e.printStackTrace();
    }

    try {
      engine.close();
    } catch ( IOException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
