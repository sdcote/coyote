/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
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

import java.text.DecimalFormat;

import coyote.commons.StringUtil;
import coyote.dx.FrameReader;
import coyote.dx.FrameValidator;
import coyote.dx.FrameWriter;
import coyote.dx.OperationalContext;
import coyote.dx.TransactionContext;
import coyote.dx.TransformContext;
import coyote.i13n.SimpleMetric;


/**
 * This logs events to a file for the batch.
 * 
 * This file requires a file target to  use for storing its data
 */
public class ContextLogger extends FileRecorder {
  DecimalFormat DECIMAL = new DecimalFormat( "#,###,##0.000" );
  DecimalFormat NUMBER = new DecimalFormat( "#,###,##0" );

  // TODO: Fix this to use the proper class
  SimpleMetric metric = new SimpleMetric( "Millis/Row", "ms" );




  /**
   * @see coyote.dx.listener.AbstractListener#onRead(coyote.dx.TransactionContext, coyote.dx.FrameReader)
   */
  @Override
  public void onRead( TransactionContext context, FrameReader reader ) {
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
        b.append( context.getErrorMessage() );
      } else {
        b.append( " - Success" );
      }
      b.append( StringUtil.LINE_FEED );

      write( b.toString() );
    }
  }




  /**
   * @see coyote.dx.listener.AbstractListener#onWrite(coyote.dx.TransactionContext, coyote.dx.FrameWriter)
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
        b.append( context.getErrorMessage() );
      } else {
        b.append( " - Success" );
      }
      b.append( StringUtil.LINE_FEED );

      write( b.toString() );
    }
  }




  /**
   * @see coyote.dx.listener.AbstractListener#onError(coyote.dx.OperationalContext)
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

    b.append( context.getErrorMessage() );
    b.append( StringUtil.LINE_FEED );
    write( b.toString() );
  }




  /**
   * @see coyote.dx.listener.AbstractListener#onEnd(coyote.dx.OperationalContext)
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
   * @see coyote.dx.listener.AbstractListener#onValidationFailed(coyote.dx.OperationalContext, coyote.dx.FrameValidator, java.lang.String)
   */
  @Override
  public void onValidationFailed( OperationalContext context, FrameValidator validator, String msg ) {

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
