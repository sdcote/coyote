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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;
import coyote.dx.reader.RabbitReader;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;


/**
 * 
 */
public class RabbitReaderTest extends AbstractMessagingTest {

  @Test
  public void test() throws ConfigurationException {
    final String QUEUE_NAME = "rtw/work";

    List<DataFrame> received = new ArrayList<DataFrame>();

    sendMessage( QUEUE_NAME, new DataFrame( "MSG", "Hello" ) );
    sendMessage( QUEUE_NAME, new DataFrame( "MSG", "World" ) );

    long currentFrameNumber = 0;
    DataFrame cfg = new DataFrame();
    cfg.set( ConfigTag.SOURCE, "amqp://localhost:" + broker.port );
    cfg.set( ConfigTag.USERNAME, "guest" );
    cfg.set( ConfigTag.PASSWORD, "guest" );
    cfg.set( CMQ.QUEUE_NAME, QUEUE_NAME );
    cfg.set( ConfigTag.USE_SSL, true );

    // Engine factory creates an configures the reader
    FrameReader reader = new RabbitReader();
    reader.setConfiguration( cfg );

    // The engine opens the reader after pre-processing tasks
    reader.open( getContext() );

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
    Log.trace( "Reads completed - Error=" + getContext().isInError() + " EOF=" + (reader == null ? "NoReader" : reader.eof()) + " Reads=" + getContext().getRow() );

    assertTrue( received.size() == 2 );
    assertTrue( getContext().getRow() == 2 );
    assertEquals( received.get( 0 ).getAsString( "MSG" ), "Hello" );

  }

}
