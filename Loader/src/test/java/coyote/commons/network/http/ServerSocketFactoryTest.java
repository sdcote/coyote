package coyote.commons.network.http;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import org.junit.Assert;
import org.junit.Test;


public class ServerSocketFactoryTest extends HTTPD {

  private class TestFactory implements ServerSocketFactory {

    @Override
    public ServerSocket create() {
      try {
        return new ServerSocket();
      } catch ( final IOException e ) {
        e.printStackTrace();
      }
      return null;
    }
  }

  public static final int PORT = 8192;




  public ServerSocketFactoryTest() {
    super( PORT );

    setServerSocketFactory( new TestFactory() );
  }




  @Test
  public void isCustomServerSocketFactory() {
    Assert.assertTrue( getServerSocketFactory() instanceof TestFactory );
  }




  @Test
  public void testCreateServerSocket() {
    ServerSocket ss = null;
    try {
      ss = getServerSocketFactory().create();
    } catch ( final IOException e ) {}
    Assert.assertTrue( ss != null );
  }




  @Test
  public void testSSLServerSocketFail() {
    final String[] protocols = { "" };
    System.setProperty( "javax.net.ssl.trustStore", new File( "src/test/resources/keystore.jks" ).getAbsolutePath() );
    final ServerSocketFactory ssFactory = new SecureServerSocketFactory( null, protocols );
    ServerSocket ss = null;
    try {
      ss = ssFactory.create();
    } catch ( final Exception e ) {}
    Assert.assertTrue( ss == null );

  }
}
