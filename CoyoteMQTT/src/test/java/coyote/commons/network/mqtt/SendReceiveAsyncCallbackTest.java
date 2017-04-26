package coyote.commons.network.mqtt;

import java.net.URI;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.network.mqtt.AsyncActionListener;
import coyote.commons.network.mqtt.MessageListener;
import coyote.commons.network.mqtt.MqttClient;
import coyote.commons.network.mqtt.MqttMessage;
import coyote.commons.network.mqtt.MqttToken;
import coyote.commons.network.mqtt.client.TestClientFactory;
import coyote.commons.network.mqtt.properties.TestProperties;
import coyote.commons.network.mqtt.utilities.Utility;
import coyote.loader.log.Log;


/**
 *
 */
public class SendReceiveAsyncCallbackTest {

  private final int messageCount = 5;
  private static URI serverURI;
  private static TestClientFactory clientFactory;
  private boolean testFinished = false;
  private String topicFilter = "SendReceiveAsyncCallback/topic";
  private listener myListener = new listener();
  private onPublish myOnPublish = new onPublish( 1 );




  /**
   * @throws Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    try {
      String methodName = Utility.getMethodName();

      serverURI = TestProperties.getServerURI();
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
    String methodName = Utility.getMethodName();

    try {
      if ( clientFactory != null ) {
        clientFactory.close();
        clientFactory.disconnect();
      }
    } catch ( Exception exception ) {
      Log.error( "caught exception:", exception );
    }
  }

  class onDisconnect implements AsyncActionListener {

    private int testno;




    onDisconnect( int testno ) {
      this.testno = testno;
    }




    @Override
    public void onSuccess( MqttToken token ) {
      Log.info( "onDisconnect: test no " + testno + " onSuccess" );

      if ( testno == 1 ) {
        testFinished = true;
      } else {
        Assert.fail( "Wrong test numnber: onSuccess" );
        testFinished = true;
      }

    }




    @Override
    public void onFailure( MqttToken asyncActionToken, Throwable exception ) {
      Log.info( "onDisconnect: test no " + testno + " onFailure" );
      testFinished = true;
    }

  }

  class listener implements MessageListener {

    ArrayList<MqttMessage> messages;




    public listener() {
      messages = new ArrayList<MqttMessage>();
    }




    public MqttMessage getNextMessage() {
      synchronized( messages ) {
        if ( messages.size() == 0 ) {
          try {
            messages.wait( 1000 );
          } catch ( InterruptedException e ) {
            // empty
          }
        }

        if ( messages.size() == 0 ) {
          return null;
        }
        return messages.remove( 0 );
      }
    }




    public void messageArrived( String topic, MqttMessage message ) throws Exception {

      Log.info( "message arrived: '" + new String( message.getPayload() ) + "' " + this.hashCode() + " " + ( message.isDuplicate() ? "duplicate" : "" ) );

      if ( !message.isDuplicate() ) {
        synchronized( messages ) {
          messages.add( message );
          messages.notifyAll();
        }
      }
    }
  }

  class onPublish implements AsyncActionListener {

    private int testno;
    private int count;




    onPublish( int testno ) {
      this.testno = testno;
      count = 0;
    }




    @Override
    public void onSuccess( MqttToken token ) {

      if ( testno == 1 ) {
        try {
          if ( ++count < messageCount ) {
            token.getClient().publish( topicFilter, "my data".getBytes(), 2, false, null, myOnPublish );
          } else {
            Log.info( "all messages published" );
            testFinished = true;
          }
        } catch ( Exception exception ) {
          Log.error( "caught exception:", exception );
          Assert.fail( "Failed: exception=" + exception );
        }
      } else {
        Assert.fail( "Wrong test numnber: onSuccess" );
        testFinished = true;
      }

    }




    @Override
    public void onFailure( MqttToken asyncActionToken, Throwable exception ) {
      Log.info( "onPublish failure, test no " + testno + " onFailure" );
      testFinished = true;
    }

  }

  class onSubscribe implements AsyncActionListener {

    private int testno;




    onSubscribe( int testno ) {
      this.testno = testno;
    }




    @Override
    public void onSuccess( MqttToken token ) {
      Log.info( ": onSubscribe" );

      if ( testno == 1 ) {
        try {
          token.getClient().publish( topicFilter, "my data".getBytes(), 2, false, myListener, myOnPublish );
        } catch ( Exception exception ) {
          Log.error( "caught exception:", exception );
          Assert.fail( "Failed: exception=" + exception );
        }
      } else {
        Assert.fail( "Wrong test numnber: " );
        testFinished = true;
      }

    }




    @Override
    public void onFailure( MqttToken asyncActionToken, Throwable exception ) {
      Log.info( "Subscribe failure, test no " + testno );
      testFinished = true;
    }

  }

  class onConnect implements AsyncActionListener {

    private int testno;




    onConnect( int testno ) {
      this.testno = testno;
    }




    @Override
    public void onSuccess( MqttToken token ) {
      Log.info( "onSuccess: onConnect" );

      try {
        if ( testno == 1 ) {
          token.getClient().subscribe( topicFilter, 2, null, new onSubscribe( 1 ), myListener );
        } else {
          Assert.fail( "Wrong test numnber:onSuccess" );
          testFinished = true;
        }
      } catch ( Exception exception ) {
        Log.error( "caught exception:", exception );
        Assert.fail( "Failed:onSuccess exception=" + exception );
        testFinished = true;
      }

    }




    @Override
    public void onFailure( MqttToken asyncActionToken, Throwable exception ) {
      Log.error( "connect failure:", exception );
      Assert.fail( "onConnect:onSuccess exception=" + exception );
      testFinished = true;
    }

  }




  /**
   * Connect, subscribe and publish
   * 
   * @throws Exception
   */
  @Test
  public void test1() throws Exception {

    MqttClient mqttClient = null;
    try {
      testFinished = false;

      mqttClient = clientFactory.createMqttAsyncClient( serverURI, "test1" );

      mqttClient.connect( null, new onConnect( 1 ) );
      Log.info( "Connecting...(serverURI:" + serverURI + ", ClientId:" + mqttClient.getClientId() + ")" );

      int count = 0;
      while ( !testFinished && ++count < 80 ) {
        Thread.sleep( 500 );
      }
      Log.info( "test1: all Messages published " + testFinished );
      Assert.assertTrue( "Callbacks not called", testFinished );

      count = 0;
      while ( myListener.messages.size() < messageCount && ++count < 10 ) {
        Thread.sleep( 500 );
      }
      Log.info( "test1: all messages received " + ( myListener.messages.size() == messageCount ) );
      Assert.assertTrue( "All messages received", myListener.messages.size() == messageCount );

      testFinished = false;

      Log.info( "Disconnecting...(serverURI:" + serverURI + ", ClientId:" + mqttClient.getClientId() + ")" );
      mqttClient.disconnect( 30000, null, new onDisconnect( 1 ) );

      count = 0;
      while ( !testFinished && ++count < 80 ) {
        Thread.sleep( 500 );
      }
      Assert.assertTrue( "Callbacks not called", testFinished );

    } catch ( Exception exception ) {
      Log.info( "Exception thrown" + exception );
      Log.error( "caught exception:", exception );
      Assert.fail( "Failed:test1 exception=" + exception );
    }
    finally {
      if ( mqttClient != null ) {
        Log.info( "Close..." );
        mqttClient.close();
      }
    }

  }

}
