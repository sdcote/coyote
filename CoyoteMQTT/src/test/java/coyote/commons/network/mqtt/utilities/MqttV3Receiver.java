package coyote.commons.network.mqtt.utilities;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import coyote.commons.network.mqtt.ClientListener;
import coyote.commons.network.mqtt.MqttBlockingClient;
import coyote.commons.network.mqtt.MqttClient;
import coyote.commons.network.mqtt.MqttDeliveryToken;
import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.MqttMessage;


/**
 * Listen for inbound messages and connection loss.
 */
public class MqttV3Receiver implements ClientListener {

  static final String className = MqttV3Receiver.class.getName();
  static final Logger log = Logger.getLogger( className );

  final static String TRACE_GROUP = "Test";

  private final PrintStream reportStream;
  private boolean reportConnectionLoss = true;
  private boolean connected = false;
  private String clientId;

  /**
   * For the in bound message.
   */
  public class ReceivedMessage {

    /** */
    public String topic;
    /** */
    public MqttMessage message;




    ReceivedMessage( String topic, MqttMessage message ) {
      this.topic = topic;
      this.message = message;
    }
  }

  List<ReceivedMessage> receivedMessages = new ArrayList<ReceivedMessage>();




  /**
   * @param mqttClient
   * @param reportStream
   */
  public MqttV3Receiver( MqttBlockingClient mqttClient, PrintStream reportStream ) {
    String methodName = Utility.getMethodName();
    log.entering( className, methodName );

    this.reportStream = reportStream;
    connected = true;

    clientId = mqttClient.getClientId();

    log.exiting( className, methodName );
  }




  /**
   * @param mqttClient
   * @param reportStream
   */
  public MqttV3Receiver( MqttClient mqttClient, PrintStream reportStream ) {
    String methodName = Utility.getMethodName();
    log.entering( className, methodName );

    this.reportStream = reportStream;
    connected = true;

    clientId = mqttClient.getClientId();

    log.exiting( className, methodName );
  }




  /**
   * @return flag
   */
  public final boolean isReportConnectionLoss() {
    return reportConnectionLoss;
  }




  /**
   * @param reportConnectionLoss
   */
  public final void setReportConnectionLoss( boolean reportConnectionLoss ) {
    this.reportConnectionLoss = reportConnectionLoss;
  }




  /**
   * @param waitMilliseconds
   * @return message
   * @throws InterruptedException
   */
  public synchronized ReceivedMessage receiveNext( long waitMilliseconds ) throws InterruptedException {
    ReceivedMessage receivedMessage = null;
    if ( receivedMessages.isEmpty() ) {
      wait( waitMilliseconds );
    }
    if ( !receivedMessages.isEmpty() ) {
      receivedMessage = receivedMessages.remove( 0 );
    }

    return receivedMessage;
  }




  /**
   * @param sendTopic
   * @param expectedQos
   * @param sentBytes
   * @return flag
   * @throws MqttException
   * @throws InterruptedException
   */
  public boolean validateReceipt( String sendTopic, int expectedQos, byte[] sentBytes ) throws MqttException, InterruptedException {

    long waitMilliseconds = 40 * 30000;
    ReceivedMessage receivedMessage = receiveNext( waitMilliseconds );
    if ( receivedMessage == null ) {
      report( " No message received in waitMilliseconds=" + waitMilliseconds );
      return false;
    }

    if ( !sendTopic.equals( receivedMessage.topic ) ) {
      report( " Received invalid topic sent=" + sendTopic + " received topic=" + receivedMessage.topic );
      return false;
    }

    if ( !java.util.Arrays.equals( sentBytes, receivedMessage.message.getPayload() ) ) {
      report( "Received invalid payload=" + Arrays.toString( receivedMessage.message.getPayload() ) + "\nSent:" + new String( sentBytes ) + "\nReceived:" + new String( receivedMessage.message.getPayload() ) );
      return false;
    }

    if ( expectedQos != receivedMessage.message.getQos() ) {
      report( "expectedQos=" + expectedQos + " != Received Qos=" + receivedMessage.message.getQos() );
      return false;
    }

    return true;
  }




  /**
   * Validate receipt of a batch of messages sent to a topic by a number of
   * publishers 
   * 
   * <p>The message payloads are expected to have the format</br><b>"Batch Message 
   * payload :&lt;batch&gt;:&lt;publisher&gt;:&lt;messageNumber&gt;:&lt;any 
   * additional payload&gt;"</b></p>
   * 
   * <p>We want to detect excess messages, so we don't just handle a certain
   * number. Instead we wait for a timeout period, and exit if no message is
   * received in that period.<b> The timeout period can make this test long
   * running, so we attempt to dynamically adjust, allowing 10 seconds for the
   * first message and then averaging the time taken to receive messages and
   * applying some swag factor.</p>
   * 
  * @param sendTopics
   * @param expectedQosList
   * @param nPublishers
   * @param expectedBatchNumber
   * @param sentBytes
   * @param expectOrdered
   * @return flag
   * @throws MqttException
   * @throws InterruptedException
   */
  public boolean validateReceipt( List<String> sendTopics, List<Integer> expectedQosList, int expectedBatchNumber, int nPublishers, List<byte[]> sentBytes, boolean expectOrdered ) throws MqttException, InterruptedException {

    int expectedMessageNumbers[] = new int[nPublishers];
    for ( int i = 0; i < nPublishers; i++ ) {
      expectedMessageNumbers[i] = 0;
    }
    long waitMilliseconds = 10000;

    // track time taken to receive messages
    long totWait = 0;
    int messageNo = 0;
    while ( true ) {
      long startWait = System.currentTimeMillis();
      ReceivedMessage receivedMessage = receiveNext( waitMilliseconds );
      if ( receivedMessage == null ) {
        break;
      }
      messageNo++;
      totWait += ( System.currentTimeMillis() - startWait );

      // Calculate new wait time based on experience, but not allowing it
      // to get too small
      waitMilliseconds = Math.max( totWait / messageNo, 500 );

      byte[] payload = receivedMessage.message.getPayload();
      String payloadString = new String( payload );
      if ( !payloadString.startsWith( "Batch Message payload :" ) ) {
        report( "Received invalid payload\nReceived:" + payloadString );
        report( "Payload did not start with {Batch Message payload :}" );
        return false;
      }

      String[] payloadParts = payloadString.split( ":" );
      if ( payloadParts.length != 5 ) {
        report( "Received invalid payload\nReceived:" + payloadString );
        report( "Payload was not of expected format" );
        log.finer( "Return false: " + receivedMessage );
        return false;
      }

      try {
        int batchNumber = Integer.parseInt( payloadParts[1] );
        if ( batchNumber != expectedBatchNumber ) {
          report( "Received invalid payload\nReceived:" + payloadString );
          report( "batchnumber" + batchNumber + " was not the expected value " + expectedBatchNumber );
          return false;
        }
      } catch ( NumberFormatException e ) {
        report( "Received invalid payload\nReceived:" + payloadString );
        report( "batchnumber was not a numeric value" );
        return false;
      }

      int publisher = -1;
      try {
        publisher = Integer.parseInt( payloadParts[2] );
        if ( ( publisher < 0 ) || ( publisher >= nPublishers ) ) {
          report( "Received invalid payload\nReceived:" + payloadString );
          report( "publisher " + publisher + " was not in the range 0 - " + ( nPublishers - 1 ) );
          return false;
        }
      } catch ( NumberFormatException e ) {
        report( "Received invalid payload\nReceived:" + payloadString );
        report( "publisher was not a numeric value" );
        return false;
      }

      if ( expectOrdered ) {
        try {
          int messageNumber = Integer.parseInt( payloadParts[3] );
          if ( messageNumber == expectedMessageNumbers[publisher] ) {
            expectedMessageNumbers[publisher] += 1;
          } else {
            report( "Received invalid payload\nReceived:" + payloadString );
            report( "messageNumber " + messageNumber + " was received out of sequence - expected value was " + expectedMessageNumbers[publisher] );
            return false;
          }
        } catch ( NumberFormatException e ) {
          report( "Received invalid payload\nReceived:" + payloadString );
          report( "messageNumber was not a numeric value" );
          return false;
        }
      }

      int location;
      for ( location = 0; location < sentBytes.size(); location++ ) {
        if ( Arrays.equals( payload, sentBytes.get( location ) ) ) {
          break;
        }
      }

      String sendTopic = null;
      int expectedQos = -1;
      if ( location < sentBytes.size() ) {
        sentBytes.remove( location );
        sendTopic = sendTopics.remove( location );
        expectedQos = expectedQosList.remove( location );
      } else {
        report( "Received invalid payload\nReceived:" + payloadString );
        for ( byte[] expectedPayload : sentBytes ) {
          report( "\texpected message :" + new String( expectedPayload ) );
        }
        return false;
      }

      if ( !sendTopic.equals( receivedMessage.topic ) ) {
        report( " Received invalid topic sent=" + sendTopic + " received topic=" + receivedMessage.topic );
        return false;
      }

      if ( expectedQos != receivedMessage.message.getQos() ) {
        report( "expectedQos=" + expectedQos + " != Received Qos=" + receivedMessage.message.getQos() );
        return false;
      }

    }

    if ( !sentBytes.isEmpty() ) {
      for ( byte[] missedPayload : sentBytes ) {
        report( "Did not receive message \n" + new String( missedPayload ) );
      }
      return false;
    }

    return true;
  }




  /**
   * @param waitMilliseconds
   * @return flag
   * @throws InterruptedException
   */
  public synchronized boolean waitForConnectionLost( long waitMilliseconds ) throws InterruptedException {
    if ( connected ) {
      wait( waitMilliseconds );
    }

    return connected;
  }




  /**
   * @param cause
   */
  public void connectionLost( Throwable cause ) {

    if ( reportConnectionLoss ) {
      report( "ConnectionLost: clientId=" + clientId + " cause=" + cause );
    }

    synchronized( this ) {
      connected = false;
      notifyAll();
    }
  }




  /**
   * @param arg0
   */
  public void deliveryComplete( MqttDeliveryToken arg0 ) {
    // Auto-generated method stub
  }




  /**
   * @param arg0
   * @param arg1
   */
  public void deliveryFailed( MqttDeliveryToken arg0, MqttException arg1 ) {
    // Auto-generated method stub
  }




  /**
   * @param topic
   * @param message
   * @throws Exception
   */
  public synchronized void messageArrived( String topic, MqttMessage message ) throws Exception {
    receivedMessages.add( new ReceivedMessage( topic, message ) );
    notify();
  }




  public synchronized List<ReceivedMessage> getReceivedMessagesInCopy() {
    return new ArrayList<ReceivedMessage>( receivedMessages );
  }




  /**
   * @param text
   */
  public void report( String text ) {
    StackTraceElement[] stack = ( new Throwable() ).getStackTrace();
    reportStream.println( stack[1].getClassName() + ":" + stack[1].getLineNumber() + " " + text );
  }




  public int receivedMessageCount() {
    return receivedMessages.size();
  }




  @Override
  public void connectComplete( boolean reconnect, String serverURI ) {
  }
}
