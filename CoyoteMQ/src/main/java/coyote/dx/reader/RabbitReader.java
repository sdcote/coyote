/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.reader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownSignalException;

import coyote.commons.CipherUtil;
import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.Loader;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.mq.CMQ;


/**
 * 
 */
public class RabbitReader extends AbstractFrameReader implements FrameReader, ConfigurableComponent {

  private static final boolean NO_AUTO_ACK = false;
  private static final boolean REQUEUE = true;
  private static final boolean DURABLE = true; 
  private static final boolean PUBLIC = false; 
  private static final boolean KEEP = false;
  private static final Map<String, Object> NO_ARGUMENTS = null;


  
  private Connection connection = null;
  private Channel channel = null;
  private int prefetchCount = 1;
  private boolean peekEofCheck = true;



  public URI getBrokerURI() {
    if ( configuration.containsIgnoreCase( ConfigTag.SOURCE ) ) {
      URI retval;
      try {
        String fieldname = configuration.getFieldIgnoreCase( ConfigTag.SOURCE ).getName();
        retval = new URI( configuration.getAsString( fieldname ) );
        return retval;
      } catch ( URISyntaxException e ) {
        Log.debug( LogMsg.createMsg( CMQ.MSG, "Reader.config_attribute_is_not_valid_uri", ConfigTag.SOURCE, configuration.getAsString( ConfigTag.SOURCE ) ) );
      }
    }
    return null;
  }




  public String getPassword() {
    if ( configuration.containsIgnoreCase( ConfigTag.PASSWORD ) ) {
      return configuration.getAsString( ConfigTag.PASSWORD );
    } else if ( configuration.containsIgnoreCase( Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD ) ) {
      return CipherUtil.decryptString( configuration.getAsString( Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD ) );
    } else {
      return null;
    }
  }




  public String getUsername() {
    if ( configuration.containsIgnoreCase( ConfigTag.USERNAME ) ) {
      return configuration.getFieldIgnoreCase( ConfigTag.USERNAME ).getStringValue();
    } else if ( configuration.containsIgnoreCase( Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME ) ) {
      return CipherUtil.decryptString( configuration.getFieldIgnoreCase( Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME ).getStringValue() );
    } else {
      return null;
    }
  }




  public boolean useSSL() {
    if ( configuration.containsIgnoreCase( ConfigTag.USE_SSL ) ) {
      try {
        String fieldname = configuration.getFieldIgnoreCase( ConfigTag.USE_SSL ).getName();
        return configuration.getAsBoolean( fieldname );
      } catch ( final DataFrameException ignore ) {
        Log.debug( LogMsg.createMsg( CMQ.MSG, "Reader.config_attribute_is_not_valid_boolean", ConfigTag.USE_SSL, configuration.getAsString( ConfigTag.USE_SSL ) ) );
      }
    }
    return false;
  }




  public String getQueueName() {
    if ( configuration.containsIgnoreCase( ConfigTag.QUEUE ) ) {
      return configuration.getFieldIgnoreCase( ConfigTag.QUEUE ).getStringValue();
    }
    return null;
  }




  /**
   * @see coyote.dx.reader.AbstractFrameReader#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.open( context );

    ConnectionFactory factory = new ConnectionFactory();

    try {
      factory.setUri( getBrokerURI() );
      if ( useSSL() ) {
        factory.useSslProtocol();
      }

      String username = getUsername();
      if ( StringUtil.isNotBlank( username ) ) {
        factory.setUsername( username );
        factory.setPassword( getPassword() );
      }

      connection = factory.newConnection();
      channel = connection.createChannel();
      channel.basicQos( prefetchCount );
      channel.queueDeclare( getQueueName(), DURABLE, PUBLIC, KEEP, NO_ARGUMENTS );
    } catch ( KeyManagementException | NoSuchAlgorithmException | URISyntaxException | IOException | TimeoutException | ShutdownSignalException | ConsumerCancelledException e ) {
      Log.error( e.getClass().getSimpleName() + ":" + e.getMessage() + "\n" + ExceptionUtil.stackTrace( e ) );
      getContext().setError( "Could not open " + getClass().getSimpleName() + ": " + e.getMessage() );
    }

  }




  /**
   * @see coyote.dx.FrameReader#read(coyote.dx.context.TransactionContext)
   */
  @Override
  public DataFrame read( TransactionContext context ) {
    DataFrame retval = null;
    try {
      GetResponse response = channel.basicGet( getQueueName(), NO_AUTO_ACK );
      if ( response != null ) {
        byte[] data = null;
        try {
          data = response.getBody();
          channel.basicAck( response.getEnvelope().getDeliveryTag(), false );
        } catch ( IOException e ) {
          Log.error( "Could not get data from message body: " + e.getClass().getName() + " - " + e.getMessage() );
        }
        if ( data != null ) {
          retval = new DataFrame( data );
        } else {
          Log.warn( "Retrieved an empty body from a message: " + response.getEnvelope().getDeliveryTag() );
        }
      }
    } catch ( IOException e ) {
      Log.warn( "Exception on message retrieval: " + e.getClass().getName() + " - " + e.getMessage() );
    }
    return retval;
  }




  /**
   * Always returns false, because messages can arrive at any time.
   *  
   * @see coyote.dx.FrameReader#eof()
   */
  @Override
  public boolean eof() {
    boolean retval = true;
    if ( peekEofCheck ) {
      try {
        GetResponse response = channel.basicGet( getQueueName(), NO_AUTO_ACK );
        if ( response != null ) {
          retval = false;
          try {
            channel.basicReject( response.getEnvelope().getDeliveryTag(), REQUEUE );
          } catch ( Exception e ) {
            Log.error( "Could not requeue message on EOF check: " + e.getClass().getName() + " - " + e.getMessage() );
          }
        }
      } catch ( IOException e ) {
        Log.error( "Exception on EOF check: " + e.getClass().getName() + " - " + e.getMessage() );
      }
    }
    return retval;
  }




  /**
   * @see coyote.dx.reader.AbstractFrameReader#close()
   */
  @Override
  public void close() throws IOException {
    // perform our closing functions first
    if ( connection != null ) {
      connection.close();
    }

    // perform base class closing functions last
    super.close();
  }

}
