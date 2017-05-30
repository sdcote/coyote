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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;
import coyote.dx.reader.RabbitReader;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;


/**
 * 
 */
public class RabbitReaderTest extends AbstractMessagingTest {

  @Test
  public void externalTest() throws ConfigurationException {
    final String QUEUE_NAME = "test/work";

    // connect to an external service using encrypted username and password
    Config cfg = new Config();
    cfg.set( ConfigTag.SOURCE, "amqp://orangutan.rmq.cloudamqp.com/qqxhunvl" );
    cfg.set( Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME, "3NaWHlOog2nkA/Wn3CO0i4uGgozkPiFy" );
    cfg.set( Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD, "gAFobDfLvfDUtbn+jAI8/1y/dnhBueMSzI4A1niA87ks2Oo7PWqgYv6nPzAGor1tB1kYiig995Gb1vzXWdo21pd19yuXtGXZ" );
    cfg.set( ConfigTag.QUEUE, QUEUE_NAME );

    //System.out.println( JSONMarshaler.toFormattedString( cfg ) );

    long currentFrameNumber = 0;
    FrameReader reader = new RabbitReader();
    reader.setConfiguration( cfg );

    reader.open( getContext() );

    while ( getContext().isNotInError() && reader != null && !reader.eof() ) {
      TransactionContext txnContext = new TransactionContext( getContext() );
      getContext().setTransaction( txnContext );

      DataFrame retval = reader.read( txnContext );
      if ( retval != null ) {
        txnContext.setSourceFrame( retval );
        txnContext.setRow( ++currentFrameNumber );
        getContext().setRow( currentFrameNumber );
        txnContext.fireRead( txnContext, reader );
        System.out.println( "Received: " + retval.toString() );
      } else {
        try {
          Thread.sleep( 1000 );
        } catch ( InterruptedException ignore ) {}
      }
    }
    Log.trace( "Reads completed - Error=" + getContext().isInError() + " EOF=" + ( reader == null ? "NoReader" : reader.eof() ) + " Reads=" + getContext().getRow() );

    try {
      reader.close();
    } catch ( IOException e ) {
      Log.warn( e.getClass().getSimpleName() + ":" + e.getMessage() );
    }
  }




  @Test
  public void test() throws ConfigurationException {
    final String QUEUE_NAME = "rtw/work";

    List<DataFrame> received = new ArrayList<DataFrame>();

    sendMessage( QUEUE_NAME, new DataFrame( "MSG", "Hello" ) );
    sendMessage( QUEUE_NAME, new DataFrame( "MSG", "World" ) );

    long currentFrameNumber = 0;
    Config cfg = new Config();
    cfg.set( ConfigTag.SOURCE, "amqp://localhost:" + broker.port );
    cfg.set( ConfigTag.USERNAME, "guest" );
    cfg.set( ConfigTag.PASSWORD, "guest" );
    cfg.set( ConfigTag.QUEUE, QUEUE_NAME );
    cfg.set( ConfigTag.USE_SSL, true );

    // Engine factory creates an configures the reader
    FrameReader reader = new RabbitReader();
    reader.setConfiguration( cfg );

    // The engine opens the reader after pre-processing tasks
    reader.open( getContext() );

    // there should be 2 messages in the queue, so EOF should be false
    assertFalse( reader.eof() );

    // The main loop of the engine is based on the reader, model it here
    while ( getContext().isNotInError() && reader != null && !reader.eof() ) {
      // each read is a new transaction
      TransactionContext txnContext = new TransactionContext( getContext() );
      getContext().setTransaction( txnContext );

      // perform the read
      DataFrame retval = reader.read( txnContext );
      if ( retval != null ) {

        // update the 
        txnContext.setSourceFrame( retval );
        txnContext.setRow( ++currentFrameNumber );
        getContext().setRow( currentFrameNumber );

        // fire the read event in all the listeners
        txnContext.fireRead( txnContext, reader );

        System.out.println( "Received: " + retval.toString() );
        received.add( retval );
      } else {
        fail( "Received null message/frame" );
      }
    } // while
    Log.trace( "Reads completed - Error=" + getContext().isInError() + " EOF=" + ( reader == null ? "NoReader" : reader.eof() ) + " Reads=" + getContext().getRow() );

    // shere should be no more messages to read
    assertTrue( reader.eof() );
    
    try {
      reader.close();
    } catch ( IOException e ) {
      Log.warn( e.getClass().getSimpleName() + ":" + e.getMessage() );
    }

    assertTrue( received.size() == 2 );
    assertTrue( getContext().getRow() == 2 );
    assertEquals( received.get( 0 ).getAsString( "MSG" ), "Hello" );

  }

}
