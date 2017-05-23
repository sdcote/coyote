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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;

import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.dx.FrameReader;
import coyote.dx.FrameWriter;
import coyote.dx.context.TransactionContext;
import coyote.dx.reader.RabbitReader;
import coyote.dx.writer.RabbitWriter;
import coyote.loader.cfg.ConfigurationException;


/**
 * 
 */
public class RabbitRoundTripTest extends AbstractMessagingTest {

  @Ignore
  public void test() throws ConfigurationException {
    final String QUEUE_NAME = "rtw/work";

    List<DataFrame> received = new ArrayList<DataFrame>();

    long currentFrameNumber = 0;
    DataFrame cfg = new DataFrame();
    cfg.set( ConfigTag.SOURCE, "amqp://localhost:" + broker.port );
    cfg.set( ConfigTag.USERNAME, "guest" );
    cfg.set( ConfigTag.PASSWORD, "guest" );
    cfg.set( CMQ.QUEUE_NAME, QUEUE_NAME );
    cfg.set( ConfigTag.USE_SSL, true );

    FrameReader reader = new RabbitReader();
    cfg.set( ConfigTag.TARGET, cfg.getAsString( ConfigTag.SOURCE ) );
    reader.setConfiguration( cfg );
    reader.open( getContext() );

    FrameWriter writer = new RabbitWriter();
    writer.setConfiguration( cfg );
    writer.open( getContext() );

    int limit = 10;
    for ( int x = 0; x < limit; x++ ) {
      TransactionContext txnContext = new TransactionContext( getContext() );
      getContext().setTransaction( txnContext );
      DataFrame message = new DataFrame( "Binary", Integer.toBinaryString( x ) ).set( "Hex", Integer.toHexString( x ) ).set( "Octal", Integer.toOctalString( x ) );
      writer.write( message );

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

    }
    assertTrue( received.size() == limit );

  }

}
