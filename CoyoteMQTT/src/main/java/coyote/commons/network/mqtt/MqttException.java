package coyote.commons.network.mqtt;

/**
 * Thrown if an error occurs communicating with the server.
 */
public class MqttException extends Exception {
  private static final long serialVersionUID = 300L;

  /** 
   * Client encountered an exception. Use the {@link #getCause()}
   * method to get the underlying reason.
   */
  public static final short CLIENT_EXCEPTION = 0x00;

  // CONNACK return codes
  /** The protocol version requested is not supported by the server. */
  public static final short INVALID_PROTOCOL_VERSION = 0x01;
  /** The server has rejected the supplied client ID */
  public static final short INVALID_CLIENT_ID = 0x02;
  /** The broker was not available to handle the request. */
  public static final short BROKER_UNAVAILABLE = 0x03;
  /** Authentication with the server has failed, due to a bad user name or password. */
  public static final short FAILED_AUTHENTICATION = 0x04;
  /** Not authorized to perform the requested operation */
  public static final short NOT_AUTHORIZED = 0x05;

  /** An unexpected error has occurred. */
  public static final short UNEXPECTED_ERROR = 0x06;

  /** Error from subscribe - returned from the server. */
  public static final short SUBSCRIBE_FAILED = 0x10;

  /** 
   * Client timed out while waiting for a response from the server.
   * The server is no longer responding to keep-alive messages.
   */
  public static final short CLIENT_TIMEOUT = 100;

  /**
   * Internal error, caused by no new message IDs being available.
   */
  public static final short NO_MESSAGE_IDS_AVAILABLE = 101;

  /** 
   * Client timed out while waiting to write messages to the server.
   */
  public static final short WRITE_TIMEOUT = 102;

  /** The client is already connected. */
  public static final short CLIENT_CONNECTED = 200;

  /** The client is already disconnected. */
  public static final short CLIENT_ALREADY_DISCONNECTED = 201;

  /** 
   * The client is currently disconnecting and cannot accept any new work. This 
   * can occur when waiting on a token, and then disconnecting the client. If 
   * the message delivery does not complete within the quiesce timeout period, 
   * then the waiting token will be notified with an exception.
   */
  public static final short CLIENT_DISCONNECTING = 202;

  /** Unable to connect to server */
  public static final short SERVER_CONNECT_ERROR = 203;

  /** 
   * The client is not connected to the broker. The {@link MqttClient#connect()}
   * or {@link MqttClient#connect(MqttConnectOptions)} method must be called 
   * first. It is also possible that the connection was lost - see 
   * {@link MqttClient#setCallback(ClientListener)} for a way to track lost 
   * connections. 
   */
  public static final short CLIENT_NOT_CONNECTED = 204;

  /** 
   * Server URI and supplied {@code SocketFactory} do not match.
   * URIs beginning {@code tcp://} must use a {@code javax.net.SocketFactory},
   * and URIs beginning {@code ssl://} must use a {@code javax.net.ssl.SSLSocketFactory}.
   */
  public static final short SOCKET_FACTORY_MISMATCH = 205;

  /** SSL configuration error. */
  public static final short SSL_CONFIG_ERROR = 206;

  /** 
   * Thrown when an attempt to call {@link MqttClient#disconnect()} has been 
   * made from within a method on {@link ClientListener}. These methods are 
   * invoked by the client's thread, and must not be used to control 
   * disconnection. @see ClientListener#messageArrived(String, MqttMessage)
   */
  public static final short CLIENT_DISCONNECT_PROHIBITED = 207;

  /** 
   * Protocol error: the message was not recognized as a valid MQTT packet.
   * Possible reasons for this include connecting to a non-MQTT server, or
   * connecting to an SSL server port when the client isn't using SSL.
   */
  public static final short INVALID_MESSAGE = 208;

  /**
   * The client has been unexpectedly disconnected from the server. The {@link 
   * #getCause() cause} will provide more details. 
   */
  public static final short CONNECTION_LOST = 209;

  /**
   * A connect operation in already in progress, only one connect can happen
   * at a time.
   */
  public static final short CONNECT_IN_PROGRESS = 210;

  /**
   * The client is closed - no operations are permitted on the client in this
   * state. New up a new client to continue.
   */
  public static final short CLIENT_CLOSED = 211;

  /**
   * A request has been made to use a token that is already associated with
   * another action. If the action is complete the reset() can be called on 
   * the token to allow it to be reused. 
   */
  public static final short TOKEN_INUSE = 301;

  /**
   * A request has been made to send a message but the maximum number of 
   * inflight messages has already been reached. Once one or more messages have 
   * been moved then new messages can be sent.  
   */
  public static final short MAX_INFLIGHT = 302;

  /**
   * The Client has attempted to publish a message whilst in the 'resting' / 
   * offline state with Disconnected Publishing enabled, however the buffer is 
   * full and deleteOldestMessages is disabled, therefore no more messages can 
   * be published until the client reconnects, or the application deletes 
   * buffered message manually. 
   */
  public static final short DISCONNECTED_BUFFER_FULL = 303;

  private final int reasonCode;
  private Throwable cause;




  /**
   * Constructs a new {@code MqttException} with the specified code as the 
   * underlying reason.
   * 
   * @param reasonCode the reason code for the exception.
   */
  public MqttException( final int reasonCode ) {
    super();
    this.reasonCode = reasonCode;
  }




  /**
   * Constructs a new {@code MqttException} with the specified {@code 
   * Throwable} as the underlying reason.
   * 
   * @param reason the reason code for the exception.
   * @param cause the underlying cause of the exception.
   */
  public MqttException( final int reason, final Throwable cause ) {
    super();
    reasonCode = reason;
    this.cause = cause;
  }




  /**
   * Constructs a new {@code MqttException} with the specified {@code Throwable} 
   * as the underlying reason.
   * 
   * @param cause the underlying cause of the exception.
   */
  public MqttException( final Throwable cause ) {
    super();
    reasonCode = CLIENT_EXCEPTION;
    this.cause = cause;
  }




  /**
   * Returns the underlying cause of this exception, if available.
   * 
   * @return the Throwable that was the root cause of this exception, which may 
   *             be {@code null}.
   */
  @Override
  public Throwable getCause() {
    return cause;
  }




  /**
   * Returns the detail message for this exception.
   * 
   * @return the detail message, which may be {@code null}.
   */
  @Override
  public String getMessage() {
    return MQTT.getLocalizedMessage( reasonCode );
  }




  /**
   * Returns the reason code for this exception.
   * 
   * @return the code representing the reason for this exception.
   */
  public int getReasonCode() {
    return reasonCode;
  }




  /**
   * Returns a {@code String} representation of this exception.
   * 
   * @return a {@code String} representation of this exception.
   */
  @Override
  public String toString() {
    String result = getMessage() + " (" + reasonCode + ")";
    if ( cause != null ) {
      result = result + " - " + cause.toString();
    }
    return result;
  }

}
