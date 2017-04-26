package coyote.commons.network.mqtt.network.websocket;

import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;


public class Base64 {

  public class Base64Encoder extends AbstractPreferences {

    private String base64String = null;




    public Base64Encoder() {
      super( null, "" );
    }




    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
      return null;
    }




    @Override
    protected AbstractPreferences childSpi( final String name ) {
      return null;
    }




    @Override
    protected void flushSpi() throws BackingStoreException {

    }




    public String getBase64String() {
      return base64String;
    }




    @Override
    protected String getSpi( final String key ) {
      return null;
    }




    @Override
    protected String[] keysSpi() throws BackingStoreException {
      return null;
    }




    @Override
    protected void putSpi( final String key, final String value ) {
      base64String = value;
    }




    @Override
    protected void removeNodeSpi() throws BackingStoreException {

    }




    @Override
    protected void removeSpi( final String key ) {}




    @Override
    protected void syncSpi() throws BackingStoreException {

    }

  }

  private static final Base64 instance = new Base64();

  private static final Base64Encoder encoder = instance.new Base64Encoder();




  public static String encode( final String s ) {
    encoder.putByteArray( "akey", s.getBytes() );
    final String result = encoder.getBase64String();
    return result;
  }




  public static String encodeBytes( final byte[] b ) {
    encoder.putByteArray( "aKey", b );
    final String result = encoder.getBase64String();
    return result;

  }

}
