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
package coyote.mq;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.context.TransformContext;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class AbstractMessagingTest {
  protected static TestBroker broker;
  private final TransformContext context = new TransformContext();




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );
    broker = new TestBroker();
    broker.open();
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    broker.close();
  }




  public void sendMessage( String queueName, DataFrame message ) {
    if ( StringUtil.isNotBlank( queueName ) && message != null ) {
      byte[] data = message.getBytes();
      try {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri( broker.getBrokerUri() );
        factory.useSslProtocol();
        // username:password should be in the URI for our tests
        try (Connection connection = factory.newConnection()) {
          Channel channel = connection.createChannel();
          channel.queueDeclare( queueName, false, false, false, null );
          channel.basicPublish( "", queueName, null, data );
          Log.debug( "Sent " + data.length + " bytes to '" + queueName + "'" );
        }
      } catch ( Exception e ) {
        Log.error( "Could not send message: " + e.getClass().getSimpleName() + "-" + e.getMessage() );
      }
    }
  }




  /**
   * @return the context
   */
  public TransformContext getContext() {
    return context;
  }

}
