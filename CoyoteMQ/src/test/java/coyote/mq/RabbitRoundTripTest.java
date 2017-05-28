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

import org.junit.Test;

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

  @Test
  public void test() throws ConfigurationException {
    final String QUEUE_NAME = "rtw/work";

    List<DataFrame> received = new ArrayList<DataFrame>();

    DataFrame cfg = new DataFrame();
    cfg.set( ConfigTag.SOURCE, "amqp://localhost:" + broker.port );
    cfg.set( ConfigTag.USERNAME, "guest" );
    cfg.set( ConfigTag.PASSWORD, "guest" );
    cfg.set( ConfigTag.QUEUE, QUEUE_NAME );
    cfg.set( ConfigTag.USE_SSL, true );

    FrameReader reader = new RabbitReader();
    cfg.set( ConfigTag.TARGET, cfg.getAsString( ConfigTag.SOURCE ) );
    reader.setConfiguration( cfg );
    reader.open( getContext() );

    FrameWriter writer = new RabbitWriter();
    writer.setConfiguration( cfg );
    writer.open( getContext() );

    int limit = 100000;
    for ( int x = 0; x < limit; x++ ) {
      DataFrame message = new DataFrame().set( "Binary", Integer.toBinaryString( x ) ).set( "Hex", Integer.toHexString( x ) ).set( "Octal", Integer.toOctalString( x ) );
      writer.write( message );
      read( reader, received );
    }
    System.out.println( "=========================================================================" );
    if ( received.size() < limit ) {
      System.out.println( "Short by " + ( limit - received.size() ) + ", catching up..." );
    }
    long timeout = 3000;
    long endtime = System.currentTimeMillis() + timeout;
    while ( received.size() < limit ) {
      read( reader, received );
      if ( System.currentTimeMillis() > endtime ) {
        fail( "Only received " + received.size() + " of " + limit + " messages within a " + timeout + "ms timeout period" );
      }
    }

    assertTrue( received.size() == limit );
  }




  private void read( FrameReader reader, List<DataFrame> received ) {
    TransactionContext txnContext = new TransactionContext( getContext() );
    getContext().setTransaction( txnContext );
    DataFrame retval = reader.read( txnContext );
    if ( retval != null ) {
      received.add( retval );
      if ( received.size() % 100 == 0 ) {
        System.out.println( "Received msg " + received.size() + " - " + retval.toString() );
      }
    }
  }

}
