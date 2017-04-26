package coyote.commons.network.mqtt;

import java.net.URI;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.network.mqtt.ClientListener;
import coyote.commons.network.mqtt.MqttDeliveryToken;
import coyote.commons.network.mqtt.MqttMessage;
import coyote.commons.network.mqtt.properties.TestProperties;
import coyote.loader.log.Log;


/**
 * 
 */
public class PahoBugTest {
  private static URI serverURI;




  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    try {
      serverURI = TestProperties.getServerURI();
    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
      throw exception;
    }
  }




  @Test
  public void testBug443142() throws Exception {
    CountDownLatch stopLatch = new CountDownLatch( 1 );
    MqttBlockingClientImpl client1 = new MqttBlockingClientImpl( serverURI.toString(), "foo" );
    client1.connect();
    MqttBlockingClientImpl client2 = new MqttBlockingClientImpl( serverURI.toString(), "bar" );
    client2.setCallback( new MyMqttCallback( stopLatch ) );
    client2.connect();
    client2.subscribe( "bar" );

    // publish messages until the queue is full > 10
    for ( int i = 0; i < 16; i++ ) {
      MqttMessage message = new MqttMessage( ( "foo-" + i ).getBytes() );
      client1.publish( "bar", message );
      Log.info( "client1 publish: " + message );
    }

    // wait until the exception is thrown
    stopLatch.await();

    // wait some time let client2 to shutdown because of the exception thrown from the callback
    Thread.sleep( 5000 );

    // client2 should be disconnected
    Assert.assertTrue( "client1 should connected", client1.isConnected() );
    Assert.assertFalse( "client2 should disconnected", client2.isConnected() );

    // close client1
    client1.disconnect();
    client1.close();
    Assert.assertFalse( "client1 should disconnected", client1.isConnected() );
  }

  private static class MyMqttCallback implements ClientListener {
    private final CountDownLatch stopLatch;




    public MyMqttCallback( CountDownLatch stopLatch ) {
      this.stopLatch = stopLatch;
    }




    @Override
    public void connectionLost( Throwable cause ) {}




    @Override
    public void messageArrived( String topic, MqttMessage message ) throws Exception {
      System.out.println( new String( message.getPayload() ) );
      Thread.sleep( 5000 );
      stopLatch.countDown();
      throw new RuntimeException( "deadlock" );
    }




    @Override
    public void deliveryComplete( MqttDeliveryToken token ) {}




    @Override
    public void connectComplete( boolean reconnect, String serverURI ) {
    }
  }

}
