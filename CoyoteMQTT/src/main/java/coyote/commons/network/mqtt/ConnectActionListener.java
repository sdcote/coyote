package coyote.commons.network.mqtt;

import coyote.commons.network.mqtt.cache.CacheException;
import coyote.commons.network.mqtt.cache.ClientCache;


/**
 * This class handles the connection of the AsyncClient to one of the available 
 * URIs.
 *   
 * <p>The URIs are supplied as either the singleton when the client is created, 
 * or as a list in the connect options.</p>
 *  
 * <p>This class uses its own onSuccess and onFailure call-backs in preference 
 * to the user supplied call-backs.</p>
 * 
 * <p>An attempt is made to connect to each URI in the list until either a 
 * connection attempt succeeds or all the URIs have been tried</p>
 *  
 * <p>If a connection succeeds then the users token is notified and the users 
 * onSuccess callback is called.</p>
 * 
 * <p>If a connection fails then another URI in the list is attempted, 
 * otherwise the users token is notified and the users onFailure callback is 
 * called.</p>
 */
public class ConnectActionListener implements AsyncActionListener {

  private final ClientCache persistence;
  private final MqttClientImpl client;
  private final Connection connection;
  private final MqttConnectOptions options;
  private final MqttToken userToken;
  private final Object context;
  private final AsyncActionListener userCallback;
  private final int originalMqttVersion;
  private ClientListener clientListener;
  private final boolean reconnect;




  /**
     * @param cache
     * @param client 
     * @param conn
     * @param opts 
     * @param token  
     * @param context
     * @param callback
     * @param reconnect
     */
  public ConnectActionListener( final MqttClientImpl client, final ClientCache cache, final Connection conn, final MqttConnectOptions opts, final MqttTokenImpl token, final Object context, final AsyncActionListener callback, final boolean reconnect ) {
    persistence = cache;
    this.client = client;
    connection = conn;
    options = opts;
    userToken = token;
    this.context = context;
    userCallback = callback;
    originalMqttVersion = opts.getMqttVersion();
    this.reconnect = reconnect;
  }




  /**
   * Start the connect processing
   * 
   * @throws CacheException 
   */
  public void connect() throws CacheException {
    final MqttTokenImpl token = new MqttTokenImpl();
    token.setActionCallback( this );
    token.setUserContext( this );

    persistence.open( client.getClientId(), client.getServerURI() );

    if ( options.isCleanSession() ) {
      persistence.clear();
    }

    if ( options.getMqttVersion() == MQTT.VERSION_DEFAULT ) {
      options.setMqttVersion( MQTT.VERSION_3_1_1 );
    }

    try {
      connection.connect( options, token );
    } catch ( final MqttException e ) {
      onFailure( token, e );
    }
  }




  /**
   * The connect failed, so try the next URI on the list.
   * If there are no more URIs, then fail the overall connect. 
   * 
   * @param token 
   * @param exception 
   */
  @Override
  public void onFailure( final MqttToken token, final Throwable exception ) {

    final int numberOfURIs = connection.getTransports().length;
    final int index = connection.getTransportIndex();

    if ( ( ( index + 1 ) < numberOfURIs ) || ( ( originalMqttVersion == MQTT.VERSION_DEFAULT ) && ( options.getMqttVersion() == MQTT.VERSION_3_1_1 ) ) ) {

      if ( originalMqttVersion == MQTT.VERSION_DEFAULT ) {
        if ( options.getMqttVersion() == MQTT.VERSION_3_1_1 ) {
          options.setMqttVersion( MQTT.VERSION_3_1 );
        } else {
          options.setMqttVersion( MQTT.VERSION_3_1_1 );
          connection.setTransportIndex( index + 1 );
        }
      } else {
        connection.setTransportIndex( index + 1 );
      }
      try {
        connect();
      } catch ( final CacheException e ) {
        onFailure( token, e ); // try the next URI in the list
      }
    } else {
      if ( originalMqttVersion == MQTT.VERSION_DEFAULT ) {
        options.setMqttVersion( MQTT.VERSION_DEFAULT );
      }
      MqttException ex;
      if ( exception instanceof MqttException ) {
        ex = (MqttException)exception;
      } else {
        ex = new MqttException( exception );
      }
      userToken.markComplete( null, ex );
      userToken.notifyComplete();
      userToken.setClient( client );

      if ( userCallback != null ) {
        userToken.setUserContext( context );
        userCallback.onFailure( userToken, exception );
      }
    }
  }




  /**
   * If the connect succeeded then call the users onSuccess callback
   * 
   * @param token the token to mark complete
   */
  @Override
  public void onSuccess( final MqttToken token ) {
    if ( originalMqttVersion == MQTT.VERSION_DEFAULT ) {
      options.setMqttVersion( MQTT.VERSION_DEFAULT );
    }
    userToken.markComplete( token.getResponse(), null );
    userToken.notifyComplete();
    userToken.setClient( client );

    if ( reconnect ) {
      connection.notifyReconnect();
    }

    if ( userCallback != null ) {
      userToken.setUserContext( context );
      userCallback.onSuccess( userToken );
    }

    if ( clientListener != null ) {
      final String serverURI = connection.getTransports()[connection.getTransportIndex()].getServerURI();
      clientListener.connectComplete( reconnect, serverURI );
    }

  }




  /**
   * Set the ClientListener callback to receive connectComplete call-backs
   * 
   * @param listener the listener to notify when connection completes.
   */
  public void setListener( final ClientListener listener ) {
    this.clientListener = listener;
  }

}
