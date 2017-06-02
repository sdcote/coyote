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
package coyote.dx.writer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;

import coyote.commons.CipherUtil;
import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameWriter;
import coyote.dx.context.TransformContext;
import coyote.loader.Loader;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.mq.CMQ;


/**
 * 
 */
public class RabbitWriter extends AbstractFrameWriter implements FrameWriter, ConfigurableComponent {

  private Connection connection = null;
  private Channel channel = null;




  public URI getBrokerURI() {
    if ( configuration.containsIgnoreCase( ConfigTag.TARGET ) ) {
      URI retval;
      try {
        String fieldname = configuration.getFieldIgnoreCase( ConfigTag.TARGET ).getName();
        retval = new URI( configuration.getAsString( fieldname ) );
        return retval;
      } catch ( URISyntaxException e ) {
        Log.debug( LogMsg.createMsg( CMQ.MSG, "Reader.config_attribute_is_not_valid_uri", ConfigTag.TARGET, configuration.getAsString( ConfigTag.TARGET ) ) );
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
   * @see coyote.dx.writer.AbstractFrameFileWriter#open(coyote.dx.context.TransformContext)
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
      channel.queueDeclare( getQueueName(), true, false, false, null );

    } catch ( KeyManagementException | NoSuchAlgorithmException | URISyntaxException | IOException | TimeoutException | ShutdownSignalException | ConsumerCancelledException e ) {
      Log.error( e.getClass().getSimpleName() + ":" + e.getMessage() + "\n" + ExceptionUtil.stackTrace( e ) );
      getContext().setError( "Could not open " + getClass().getSimpleName() + ": " + e.getMessage() );
    }

  }




  /**
   * @see coyote.dx.FrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write( DataFrame frame ) {
    if ( frame != null ) {
      try {
        byte[] data = frame.getBytes();
        channel.basicPublish( "", getQueueName(), null, data );
        //Log.debug( "Sent " + data.length + " bytes to '" + getQueueName() + "'" );
      } catch ( IOException e ) {
        Log.error( e.getClass().getSimpleName() + ":" + e.getMessage() + "\n" + ExceptionUtil.stackTrace( e ) );
      }
    }
  }




  /**
   * @see coyote.dx.writer.AbstractFrameFileWriter#close()
   */
  @Override
  public void close() throws IOException {
    if ( connection != null ) {
      connection.close();
    }

    super.close();
  }

}
