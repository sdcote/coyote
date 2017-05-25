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
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
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
import coyote.mq.MessageDrop;


/**
 * 
 */
public class RabbitReader extends AbstractFrameReader implements FrameReader, ConfigurableComponent {

  // blocking message exchange component
  private final MessageDrop messageDrop = new MessageDrop();

  // the connection to the broker
  private Connection connection = null;

  Consumer consumer = null;

  private int prefetchCount = 1;




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
    if ( configuration.containsIgnoreCase( CMQ.QUEUE_NAME ) ) {
      return configuration.getFieldIgnoreCase( CMQ.QUEUE_NAME ).getStringValue();
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
      Channel channel = connection.createChannel();
      channel.basicQos( prefetchCount );
      channel.queueDeclare( getQueueName(), true, false, false, null );

      consumer = new DefaultConsumer( channel ) {
        @Override
        public void handleDelivery( String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] message ) throws IOException {
          Log.trace( "Handling delivery of a message" );
          try {
            if ( messageDrop.hasData() ) {
              Log.error( "There is already data in the message drop. Waiting for the data to clear" );
              messageDrop.waitForRetrieval();
            }
            messageDrop.leave( message );
            Log.trace( "Message has been delivered" );
          } catch ( InterruptedException e ) {
            Log.warn( "Interrupted waiting for message retrieval:\n" + ExceptionUtil.toString( e ) );
            return; // return without acknowledging...what will happen??
          }
          if ( messageDrop.hasData() ) {
            throw new IOException( "Data has not been retrieved" );
          } else {
            channel.basicAck( envelope.getDeliveryTag(), false );
            Log.trace( "Message " + envelope.getDeliveryTag() + " has been delivered" );
          }
        }
      };

      channel.basicConsume( getQueueName(), true, consumer );

    } catch ( KeyManagementException | NoSuchAlgorithmException | URISyntaxException | IOException | TimeoutException | ShutdownSignalException | ConsumerCancelledException e ) {
      Log.error( e.getClass().getSimpleName() + ":" + e.getMessage() + "\n" + ExceptionUtil.stackTrace( e ) );
      getContext().setError( "Could not open "+getClass().getSimpleName()+": "+e.getMessage() );
    }

    // pause for a short time to allow the consumer to spin-up
    try {
      messageDrop.wait( 500 );
    } catch ( Exception ignore ) {}
    Log.debug( "Reader opened" );

    try {
      messageDrop.waitForDelivery();
    } catch ( InterruptedException e ) {
      Log.warn( e.getClass().getSimpleName() + ":" + e.getMessage() + "\n" + ExceptionUtil.stackTrace( e ) );
    }
  }




  /**
   * @see coyote.dx.FrameReader#read(coyote.dx.context.TransactionContext)
   */
  @Override
  public DataFrame read( TransactionContext context ) {
    byte[] message = messageDrop.take();
    if ( message != null ) {
      try {
        DataFrame retval = new DataFrame( message );
        return retval;
      } catch ( Exception e ) {
        Log.error( ExceptionUtil.stackTrace( e ) );
      }
    }
    return null;
  }




  /**
   * Checks to see if there is any data in the message drop.
   * 
   * <p>If the message drop is empty, this check will wait for a message for 
   * 100ms before returning true (no more messages). This is to give threads a 
   * chance to swap off the CPU and deliver a message without adversely 
   * affecting performance.
   * 
   * @see coyote.dx.FrameReader#eof()
   */
  @Override
  public boolean eof() {
    if ( !messageDrop.hasData() ) {
      try {
        messageDrop.waitForDelivery( 100 );
      } catch ( InterruptedException ignore ) {}
    }
    return !messageDrop.hasData();
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
