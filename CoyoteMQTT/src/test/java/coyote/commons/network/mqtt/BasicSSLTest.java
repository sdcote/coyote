package coyote.commons.network.mqtt;

import java.net.URI;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import coyote.commons.network.mqtt.Topic;
import coyote.commons.network.mqtt.client.TestClientFactory;
import coyote.commons.network.mqtt.properties.TestProperties;
import coyote.commons.network.mqtt.utilities.MqttV3Receiver;
import coyote.commons.network.mqtt.utilities.Utility;
import coyote.loader.log.Log;


/**
 * This test aims to run some basic SSL functionality tests of the MQTT client
 */

public class BasicSSLTest {

  private static URI serverURI;
  private static String serverHost;
  private static TestClientFactory clientFactory;
  private static int messageSize = 100000;




  /**
   * @throws Exception 
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    try {

      serverURI = TestProperties.getServerURI();
      serverHost = serverURI.getHost();
      clientFactory = new TestClientFactory();
      clientFactory.open();
    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
      throw exception;
    }
  }




  /**
   * @throws Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {

    try {
      if ( clientFactory != null ) {
        clientFactory.close();
        clientFactory.disconnect();
      }

    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
    }
  }




  /**
   * An ssl connection with server cert authentication, simple pub/sub
   * @throws Exception
   */
  @Test
  @Ignore
  public void testSSL() throws Exception {
    URI serverURI = new URI( "ssl://" + serverHost + ":" + TestProperties.getServerSSLPort() );
    String methodName = Utility.getMethodName();

    MqttBlockingClient mqttClient = null;
    try {
      mqttClient = clientFactory.createMqttClient( serverURI, methodName );
      MqttV3Receiver mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
      Log.info( "Assigning callback" );
      mqttClient.setCallback( mqttV3Receiver );

      Log.info( "Setting SSL properties..." );
      System.setProperty( "javax.net.ssl.keyStore", TestProperties.getClientKeyStore() );
      System.setProperty( "javax.net.ssl.keyStorePassword", TestProperties.getClientKeyStorePassword() );
      System.setProperty( "javax.net.ssl.trustStore", TestProperties.getClientTrustStore() );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:" + methodName );
      mqttClient.connect();

      String[] topicNames = new String[] { methodName + "/Topic" };
      int[] topicQos = { 2 };
      Log.info( "Subscribing to..." + topicNames[0] );
      mqttClient.subscribe( topicNames, topicQos );

      byte[] payload = ( "Message payload " + getClass().getName() + "." + methodName ).getBytes();
      Topic mqttTopic = mqttClient.getTopic( topicNames[0] );
      Log.info( "Publishing to..." + topicNames[0] );
      mqttTopic.publish( payload, 2, false );

      boolean ok = mqttV3Receiver.validateReceipt( topicNames[0], 2, payload );
      if ( !ok ) {
        Assert.fail( "Receive failed" );
      }
    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
      Assert.fail( "Failed to instantiate:" + methodName + " exception=" + exception );
    }
    finally {
      try {
        if ( ( mqttClient != null ) && mqttClient.isConnected() ) {
          Log.info( "Disconnecting..." );
          mqttClient.disconnect();
        }
        if ( mqttClient != null ) {
          Log.info( "Close..." );
          mqttClient.close();
        }
      } catch ( Exception exception ) {
        Log.error( "caught exception:", exception );
      }
    }

  }




  /**
   * An ssl connection with server cert authentication, small workload with multiple clients
   * @throws Exception
   */
  @Test
  @Ignore
  public void testSSLWorkload() throws Exception {
    URI serverURI = new URI( "ssl://" + serverHost + ":" + TestProperties.getServerSSLPort() );
    String methodName = Utility.getMethodName();

    MqttBlockingClient[] mqttPublisher = new MqttBlockingClient[4];
    MqttBlockingClient[] mqttSubscriber = new MqttBlockingClient[20];
    try {
      String[] topicNames = new String[] { methodName + "/Topic" };
      int[] topicQos = { 0 };

      Topic[] mqttTopic = new Topic[mqttPublisher.length];
      for ( int i = 0; i < mqttPublisher.length; i++ ) {
        mqttPublisher[i] = clientFactory.createMqttClient( serverURI, "MultiPub" + i );

        Log.info( "Setting SSL properties...ClientId: MultiPub" + i );
        System.setProperty( "javax.net.ssl.keyStore", TestProperties.getClientKeyStore() );
        System.setProperty( "javax.net.ssl.keyStorePassword", TestProperties.getClientKeyStorePassword() );
        System.setProperty( "javax.net.ssl.trustStore", TestProperties.getClientKeyStore() );
        System.setProperty( "javax.net.ssl.trustStorePassword", TestProperties.getClientKeyStorePassword() );
        Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId: MultiPub" + i );
        mqttPublisher[i].connect();
        mqttTopic[i] = mqttPublisher[i].getTopic( topicNames[0] );

      } // for...

      MqttV3Receiver[] mqttV3Receiver = new MqttV3Receiver[mqttSubscriber.length];
      for ( int i = 0; i < mqttSubscriber.length; i++ ) {
        mqttSubscriber[i] = clientFactory.createMqttClient( serverURI, "MultiSubscriber" + i );
        mqttV3Receiver[i] = new MqttV3Receiver( mqttSubscriber[i], System.out );
        Log.info( "Assigning callback..." );
        mqttSubscriber[i].setCallback( mqttV3Receiver[i] );

        Log.info( "Setting SSL properties...ClientId: MultiSubscriber" + i );
        System.setProperty( "javax.net.ssl.keyStore", TestProperties.getClientKeyStore() );
        System.setProperty( "javax.net.ssl.keyStorePassword", TestProperties.getClientKeyStorePassword() );
        System.setProperty( "javax.net.ssl.trustStore", TestProperties.getClientKeyStore() );
        System.setProperty( "javax.net.ssl.trustStorePassword", TestProperties.getClientKeyStorePassword() );
        Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId: MultiSubscriber" + i );
        mqttSubscriber[i].connect();
        Log.info( "Subcribing to..." + topicNames[0] );
        mqttSubscriber[i].subscribe( topicNames, topicQos );
      } // for...

      for ( int iMessage = 0; iMessage < 10; iMessage++ ) {
        byte[] payload = ( "Message " + iMessage ).getBytes();
        for ( int i = 0; i < mqttPublisher.length; i++ ) {
          Log.info( "Publishing to..." + topicNames[0] );
          mqttTopic[i].publish( payload, 0, false );
        }

        for ( int i = 0; i < mqttSubscriber.length; i++ ) {
          for ( int ii = 0; ii < mqttPublisher.length; ii++ ) {
            boolean ok = mqttV3Receiver[i].validateReceipt( topicNames[0], 0, payload );
            if ( !ok ) {
              Assert.fail( "Receive failed" );
            }
          } // for publishers...
        } // for subscribers...
      } // for messages...
    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
      throw exception;
    }
    finally {
      try {
        for ( int i = 0; i < mqttPublisher.length; i++ ) {
          Log.info( "Disconnecting...MultiPub" + i );
          mqttPublisher[i].disconnect();
          Log.info( "Close..." );
          mqttPublisher[i].close();
        }
        for ( int i = 0; i < mqttSubscriber.length; i++ ) {
          Log.info( "Disconnecting...MultiSubscriber" + i );
          mqttSubscriber[i].disconnect();
          Log.info( "Close..." );
          mqttSubscriber[i].close();
        }

      } catch ( Exception exception ) {
        Log.error( "caught exception:", exception );
      }
    }

  }




  /**
   * An ssl connection with server cert authentication, simple pub/sub of a large message
   * 'messageSize' defined at start of test, change it to meet your requirements
   * @throws Exception
   */
  @Test
  @Ignore
  public void testSSLLargeMessage() throws Exception {
    URI serverURI = new URI( "ssl://" + serverHost + ":" + TestProperties.getServerSSLPort() );
    String methodName = Utility.getMethodName();

    MqttBlockingClient mqttClient = null;
    try {
      mqttClient = clientFactory.createMqttClient( serverURI, methodName );
      MqttV3Receiver mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
      Log.info( "Assigning callback..." );
      mqttClient.setCallback( mqttV3Receiver );

      Log.info( "Setting SSL properties..." );
      System.setProperty( "javax.net.ssl.keyStore", TestProperties.getClientKeyStore() );
      System.setProperty( "javax.net.ssl.keyStorePassword", TestProperties.getClientKeyStorePassword() );
      System.setProperty( "javax.net.ssl.trustStore", TestProperties.getClientKeyStore() );
      System.setProperty( "javax.net.ssl.trustStorePassword", TestProperties.getClientKeyStorePassword() );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:" + methodName );
      mqttClient.connect();

      String[] topicNames = new String[] { methodName + "/Topic" };
      int[] topicQos = { 2 };
      Log.info( "Subscribing to..." + topicNames[0] );
      mqttClient.subscribe( topicNames, topicQos );

      // Create message of size 'messageSize'
      byte[] message = new byte[messageSize];
      java.util.Arrays.fill( message, (byte)'s' );

      Topic mqttTopic = mqttClient.getTopic( topicNames[0] );
      Log.info( "Publishing to..." + topicNames[0] );
      mqttTopic.publish( message, 2, false );
      boolean ok = mqttV3Receiver.validateReceipt( topicNames[0], 2, message );
      if ( !ok ) {
        Assert.fail( "Receive failed" );
      }
    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
      Assert.fail( "Failed:" + methodName + " exception=" + exception );
    }
    finally {
      try {
        if ( ( mqttClient != null ) && mqttClient.isConnected() ) {
          Log.info( "Disconnecting..." );
          mqttClient.disconnect();
        }
        if ( mqttClient != null ) {
          Log.info( "Close..." );
          mqttClient.close();
        }
      } catch ( Exception exception ) {
        Log.error( "caught exception:", exception );
        throw exception;
      }
    }
  }




  /**
   * A non ssl connection to an ssl channel
   * @throws Exception
   */
  @Test
  public void testNonSSLtoSSLChannel() throws Exception {
    String methodName = Utility.getMethodName();

    MqttBlockingClient mqttClient = null;
    try {
      mqttClient = clientFactory.createMqttClient( new URI( "tcp://" + serverHost + ":" + TestProperties.getServerSSLPort() ), methodName );
      MqttV3Receiver mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
      mqttClient.setCallback( mqttV3Receiver );
      Log.info( "Assigning callback..." );
      try {
        Log.info( "Connecting...Expect to fail" );
        mqttClient.connect();
        Assert.fail( "Non SSL Connection was allowed to SSL channel with Client Authentication" );
      } catch ( Exception e ) {
        // Expected exception
      }

    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
      Assert.fail( "Failed:" + methodName + " exception=" + exception );
    }
    finally {
      try {
        if ( ( mqttClient != null ) && mqttClient.isConnected() ) {
          Log.info( "Disconnecting..." );
          mqttClient.disconnect();
        }
        if ( mqttClient != null ) {
          Log.info( "Close..." );
          mqttClient.close();
        }

      } catch ( Exception exception ) {
        Log.error( "caught exception:", exception );
        throw exception;
      }
    }
  }




  /**
   * Try ssl connection to channel without ssl
   * @throws Exception
   */
  @Test
  public void testSSLtoNonSSLChannel() throws Exception {
    String methodName = Utility.getMethodName();

    MqttBlockingClient mqttClient = null;
    try {
      mqttClient = clientFactory.createMqttClient( new URI( "ssl://" + serverHost + ":18883" ), methodName );
      MqttV3Receiver mqttV3Receiver = new MqttV3Receiver( mqttClient, System.out );
      Log.info( "Assigning callback..." );
      mqttClient.setCallback( mqttV3Receiver );

      Log.info( "Setting SSL properties..." );
      System.setProperty( "javax.net.ssl.keyStore", TestProperties.getClientKeyStore() );
      System.setProperty( "javax.net.ssl.keyStorePassword", TestProperties.getClientKeyStorePassword() );
      System.setProperty( "javax.net.ssl.trustStore", TestProperties.getClientKeyStore() );
      System.setProperty( "javax.net.ssl.trustStorePassword", TestProperties.getClientKeyStorePassword() );
      try {
        Log.info( "Connecting...Expect to fail" );
        mqttClient.connect();
        Assert.fail( "SSL Connection was allowed to a channel without SSL" );
      } catch ( Exception e ) {
        // Expected exception
      }

    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
      Assert.fail( "Failed:" + methodName + " exception=" + exception );
    }
    finally {
      try {
        if ( ( mqttClient != null ) && mqttClient.isConnected() ) {
          Log.info( "Disconnecting..." );
          mqttClient.disconnect();
        }
        if ( mqttClient != null ) {
          Log.info( "Close..." );
          mqttClient.close();
        }

      } catch ( Exception exception ) {
        Log.error( "caught exception:", exception );
        throw exception;
      }
    }
  }
}
