/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.listener;

import java.text.DecimalFormat;

import coyote.batch.FrameWriter;
import coyote.batch.OperationalContext;
import coyote.batch.TransactionContext;
import coyote.batch.TransformContext;
import coyote.commons.StringUtil;
import coyote.i13n.Metric;


/**
 * This logs events to a file for the batch.
 * 
 * This file requires a file target to  use for storing its data
 */
public class ContextLogger extends FileRecorder {
  DecimalFormat DECIMAL = new DecimalFormat( "#,###,##0.000" );
  DecimalFormat NUMBER = new DecimalFormat( "#,###,##0" );

  Metric metric = new Metric( "Millis/Row", "ms" );




  /**
   * @see coyote.batch.listener.AbstractListener#onRead(coyote.batch.TransactionContext)
   */
  @Override
  public void onRead( TransactionContext context ) {
    if ( onRead ) {
      StringBuffer b = new StringBuffer();
      b.append( context.getRow() );
      b.append( ": " );
      b.append( "Read: " );
      if ( context.getSourceFrame() != null ) {
        b.append( context.getSourceFrame().toString() );
      } else {
        b.append( "NULL SOURCE FRAME" );
      }
      if ( context.isInError() ) {
        b.append( " - Error: " );
        b.append( context.getMessage() );
      } else {
        b.append( " - Success" );
      }
      b.append( StringUtil.LINE_FEED );

      write( b.toString() );
    }
  }




  /**
   * @see coyote.batch.listener.AbstractListener#onWrite(coyote.batch.TransactionContext, coyote.batch.FrameWriter)
   */
  @Override
  public void onWrite( TransactionContext context, FrameWriter writer ) {
    if ( onWrite ) {
      StringBuffer b = new StringBuffer();
      b.append( context.getRow() );
      b.append( ": " );
      b.append( "Write: " );
      if ( context.getTargetFrame() != null ) {
        b.append( context.getTargetFrame().toString() );
      } else {
        b.append( "NULL TARGET FRAME" );
      }
      if ( context.isInError() ) {
        b.append( " - Error: " );
        b.append( context.getMessage() );
      } else {
        b.append( " - Success" );
      }
      b.append( StringUtil.LINE_FEED );

      write( b.toString() );
    }
  }




  /**
   * @see coyote.batch.listener.AbstractListener#onError(coyote.batch.OperationalContext)
   */
  @Override
  public void onError( OperationalContext context ) {
    StringBuffer b = new StringBuffer();

    if ( context instanceof TransactionContext ) {
      b.append( "Transaction error: " );
    } else if ( context instanceof TransformContext ) {
      b.append( "Tranform error: " );
    } else {
      b.append( "Operational error: " );
    }

    b.append( context.getMessage() );
    b.append( StringUtil.LINE_FEED );
    write( b.toString() );
  }




  /**
   * @see coyote.batch.listener.AbstractListener#onEnd(coyote.batch.OperationalContext)
   */
  @Override
  public void onEnd( OperationalContext context ) {
    StringBuffer b = new StringBuffer();

    // End of a transaction or end of the batch?
    if ( context instanceof TransactionContext ) {
      metric.sample( context.getElapsed() );
      b.append( "Transaction end: " );
    } else if ( context instanceof TransformContext ) {
      b.append( "Tranform end: " );
    } else {
      b.append( "Operational end: " );
    }
    b.append( " elapsed " );
    b.append( NUMBER.format( context.getElapsed() ) );
    b.append( " ms" );

    if ( context instanceof TransformContext ) {
      b.append( " - " );
      b.append( metric.toString() );
      b.append( StringUtil.LINE_FEED );

      long elapsed = context.getElapsed();
      long total = metric.getTotal();
      long overhead = elapsed - total;

      b.append( "Transform Overhead: " );
      b.append( NUMBER.format( overhead ) );
      b.append( " ms" );

      long rows = metric.getSamplesCount();
      if ( rows > 0 ) {
        b.append( " - " );
        b.append( DECIMAL.format( (double)overhead / (double)rows ) );
        b.append( " ms/row " );
      }
      b.append( StringUtil.LINE_FEED );

    }

    b.append( StringUtil.LINE_FEED );
    write( b.toString() );
  }




  /**
   * @see coyote.batch.ContextListener#onValidationFailed(coyote.batch.OperationalContext, java.lang.String)
   */
  @Override
  public void onValidationFailed( OperationalContext context, String msg ) {

    StringBuffer b = new StringBuffer();

    if ( context instanceof TransactionContext || context instanceof TransformContext ) {
      b.append( context.getRow() );
      b.append( ": " );
    } else {
      b.append( "Operational validation failure: " );
    }

    b.append( msg );
    b.append( StringUtil.LINE_FEED );
    write( b.toString() );
  }

}
