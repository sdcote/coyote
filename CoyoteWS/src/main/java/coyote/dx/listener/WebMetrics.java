/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.listener;

import java.util.Date;

import coyote.commons.StringUtil;
import coyote.dx.FrameReader;
import coyote.dx.FrameWriter;
import coyote.dx.context.ContextListener;
import coyote.dx.context.OperationalContext;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.dx.reader.WebServiceReader;
import coyote.dx.web.Response;
import coyote.dx.writer.WebServiceWriter;
import coyote.i13n.SimpleMetric;


/**
 * Collects performance metrics for web service readers and writers.
 * 
 * <p>After a read or write, this listener gets called and has the opportunity 
 * to grab the performance metrics on the last operation. This listener then 
 * aggregates the collected data and generates a performance report when the 
 * transform context completes.</p> 
 */
public class WebMetrics extends AbstractFileRecorder implements ContextListener {

  Date start = null;
  Date end = null;
  long elapsed = -1;

  SimpleMetric readOprMetrics = new SimpleMetric( "READ(Opr)" );
  SimpleMetric readTxnMetrics = new SimpleMetric( "READ(Txn)" );
  SimpleMetric readWebMetrics = new SimpleMetric( "READ(web)" );
  SimpleMetric writeOprMetrics = new SimpleMetric( "WRITE(opr)" );
  SimpleMetric writeTxnMetrics = new SimpleMetric( "WRITE(txn)" );
  SimpleMetric writeWebMetrics = new SimpleMetric( "WRITE(web)" );




  /**
   * @see coyote.dx.listener.AbstractListener#onRead(coyote.dx.context.TransactionContext, coyote.dx.FrameReader)
   */
  @Override
  public void onRead( TransactionContext context, FrameReader reader ) {
    if ( reader instanceof WebServiceReader ) {
      Response lastResponse = ( (WebServiceReader)reader ).getLastResponse();
      if ( lastResponse != null ) {
        readOprMetrics.sample( lastResponse.getOperationElapsed() );
        readTxnMetrics.sample( lastResponse.getTransactionElapsed() );
        readWebMetrics.sample( lastResponse.getRequestElapsed() );
      }
    }
  }




  /**
   * @see coyote.dx.listener.AbstractListener#onWrite(coyote.dx.context.TransactionContext, coyote.dx.FrameWriter)
   */
  @Override
  public void onWrite( TransactionContext context, FrameWriter writer ) {
    if ( writer instanceof WebServiceWriter ) {
      Response lastResponse = ( (WebServiceWriter)writer ).getLastResponse();
      if ( lastResponse != null ) {
        writeOprMetrics.sample( lastResponse.getOperationElapsed() );
        writeTxnMetrics.sample( lastResponse.getTransactionElapsed() );
        writeWebMetrics.sample( lastResponse.getRequestElapsed() );
      }
    }
  }




  /**
   * @see coyote.dx.listener.AbstractListener#onEnd(coyote.dx.context.OperationalContext)
   */
  @Override
  public void onEnd( OperationalContext context ) {

    // generate report at the end of the Transform, not each Transaction
    if ( context instanceof TransformContext ) {
      start = new Date( context.getStartTime() );
      end = new Date( context.getEndTime() );
      elapsed = context.getElapsed();
      performanceSummary();
      readSummary();
      writeSummary();
    }
  }




  private void writeSummary() {
    if ( writeOprMetrics.getSamplesCount() > 0 ) {
      write( writeOprMetrics.toString() );
      write( StringUtil.LINE_FEED );
      write( writeTxnMetrics.toString() );
      write( StringUtil.LINE_FEED );
      write( writeWebMetrics.toString() );
      write( StringUtil.LINE_FEED );
    } else {
      write( "No records written." );
      write( StringUtil.LINE_FEED );
    }
  }




  private void readSummary() {
    if ( readOprMetrics.getSamplesCount() > 0 ) {
      write( readOprMetrics.toString() );
      write( StringUtil.LINE_FEED );
      write( readTxnMetrics.toString() );
      write( StringUtil.LINE_FEED );
      write( readWebMetrics.toString() );
      write( StringUtil.LINE_FEED );
    } else {
      write( "No records read." );
      write( StringUtil.LINE_FEED );
    }
  }




  private void performanceSummary() {
    // TODO Auto-generated method stub

  }

}
