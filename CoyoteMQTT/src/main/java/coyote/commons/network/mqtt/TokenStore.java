package coyote.commons.network.mqtt;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import coyote.commons.network.mqtt.protocol.AbstractMessage;
import coyote.commons.network.mqtt.protocol.PublishMessage;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Provides a "token" based system for storing and tracking actions across 
 * multiple threads.
 * 
 * <p>When a message is sent, a token is associated with the message and saved 
 * using the {@link #saveToken(MqttTokenImpl, AbstractMessage)} method. 
 * Anything interested in tacking the state can call one of the wait methods on 
 * the token or using the asynchronous listener callback method on the 
 * operation. The {@link Receiver} class, on another thread, reads 
 * responses back from the network. It uses the response to find the relevant 
 * token, which it can then notify.</p> 
 * 
 * <p>Ping, connect and disconnect do not have a unique message id as only one 
 * outstanding request of each type is allowed to be outstanding.</p>
 */
public class TokenStore {

  // Maps message-specific data (usually message IDs) to tokens
  private final Hashtable tokens;
  private MqttException closedResponse = null;




  public TokenStore( final String logContext ) {
    tokens = new Hashtable();
  }




  /**
   * Empties the token store without notifying any of the tokens.
   */
  public void clear() {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "tokenstore.cleared", tokens.size() ) );
    synchronized( tokens ) {
      tokens.clear();
    }
  }




  public int count() {
    synchronized( tokens ) {
      return tokens.size();
    }
  }




  public MqttDeliveryTokenImpl[] getOutstandingDelTokens() {
    synchronized( tokens ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "tokenstore.get_outstanding_delivery_tokens" ) );

      final Vector list = new Vector();
      final Enumeration enumeration = tokens.elements();
      MqttTokenImpl token;
      while ( enumeration.hasMoreElements() ) {
        token = (MqttTokenImpl)enumeration.nextElement();
        if ( ( token != null ) && ( token instanceof MqttDeliveryToken ) && !token.isNotified() ) {
          list.addElement( token );
        }
      }

      final MqttDeliveryTokenImpl[] result = new MqttDeliveryTokenImpl[list.size()];
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "tokenstore.get_outstanding_delivery_tokens_result", result.length ) );

      return (MqttDeliveryTokenImpl[])list.toArray( result );
    }
  }




  public Vector getOutstandingTokens() {

    synchronized( tokens ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "tokenstore.get_outstanding_tokens" ) );

      final Vector retval = new Vector();
      final Enumeration enumeration = tokens.elements();
      MqttTokenImpl token;
      while ( enumeration.hasMoreElements() ) {
        token = (MqttTokenImpl)enumeration.nextElement();
        if ( token != null ) {
          retval.addElement( token );
        }
      }
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "tokenstore.get_outstanding_tokens_result", retval.size() ) );
      return retval;
    }
  }




  /**
   * Based on the message type that has just been received, return the 
   * associated token from the token store or null if one does not exist.
   * 
   * @param message whose token is to be returned 
   * 
   * @return token for the requested message
   */
  public MqttTokenImpl getToken( final AbstractMessage message ) {
    final String key = message.getKey();
    return (MqttTokenImpl)tokens.get( key );
  }




  public MqttTokenImpl getToken( final String key ) {
    return (MqttTokenImpl)tokens.get( key );
  }




  public void open() {

    synchronized( tokens ) {
      closedResponse = null;
    }
  }




  protected void quiesce( final MqttException quiesceResponse ) {

    synchronized( tokens ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "tokenstore.quiesce", quiesceResponse ) );
      closedResponse = quiesceResponse;
    }
  }




  public MqttTokenImpl removeToken( final AbstractMessage message ) {
    if ( message != null ) {
      return removeToken( message.getKey() );
    }
    return null;
  }




  public MqttTokenImpl removeToken( final String key ) {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "tokenstore.remove_token", key ) );

    if ( null != key ) {
      return (MqttTokenImpl)tokens.remove( key );
    }

    return null;
  }




  /**
   * Restores a token after a client restart.  
   * 
   * <p>This method could be called for a SEND of CONFIRM. The original SEND is 
   * what's needed to re-build the token.</p>
   */
  protected MqttDeliveryTokenImpl restoreToken( final PublishMessage message ) {
    MqttDeliveryTokenImpl token;
    synchronized( tokens ) {
      final String key = new Integer( message.getMessageId() ).toString();
      if ( tokens.containsKey( key ) ) {
        token = (MqttDeliveryTokenImpl)tokens.get( key );
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "tokenstore.restore_existing", key, message, token ) );
      } else {
        token = new MqttDeliveryTokenImpl();
        token.setKey( key );
        tokens.put( key, token );
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "tokenstore.restore_new", key, message, token ) );
      }
    }
    return token;
  }




  // For outbound messages store the token in the token store 
  // For pubrel use the existing publish token 
  protected void saveToken( final MqttTokenImpl token, final AbstractMessage message ) throws MqttException {
    synchronized( tokens ) {
      if ( closedResponse == null ) {
        final String key = message.getKey();
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "tokenstore.saving_outbound_token", key, message ) );
        saveToken( token, key );
      } else {
        throw closedResponse;
      }
    }
  }




  protected void saveToken( final MqttTokenImpl token, final String key ) {

    synchronized( tokens ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "tokenstore.saving_token", key, token.toString() ) );
      token.setKey( key );
      tokens.put( key, token );
    }
  }




  @Override
  public String toString() {
    final String lineSep = System.getProperty( "line.separator", "\n" );
    final StringBuffer toks = new StringBuffer();
    synchronized( tokens ) {
      final Enumeration enumeration = tokens.elements();
      MqttTokenImpl token;
      while ( enumeration.hasMoreElements() ) {
        token = (MqttTokenImpl)enumeration.nextElement();
        toks.append( "{" + token + "}" + lineSep );
      }
      return toks.toString();
    }
  }

}
