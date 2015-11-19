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

import java.util.ArrayList;
import java.util.List;

import coyote.batch.ContextListener;
import coyote.batch.OperationalContext;
import coyote.batch.TransactionContext;
import coyote.batch.TransformContext;
import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;


/**
 * This listener keeps track of the data read in to and out of the engine and 
 * reports on the characteristics of the data observed.
 * 
 * Field metrics as the header, each field as a row metrics to support:
 * count
 * nulls
 * smallest length
 * longest length
 * avg length
 * length SDEV
 * numeric (true/false)
 * numeric count
 * numeric smallest
 * numeric largest
 * numeric avg
 * numeric SDEV
 * date (true/false)
 * date count
 * date smallest
 * date largest
 * date avg
 * date SDEV
 * date formats (list of formats YYYY-MM-DD, etc)
 * normalization count (how many different values)
 * normalization rating (ratio of value count to instance count)
 * 
 */
public class DataProfiler extends FileRecorder implements ContextListener {
  private List<FieldMetrics> inputFields = new ArrayList<FieldMetrics>();
  private List<FieldMetrics> outputFields = new ArrayList<FieldMetrics>();
  long readCount = 0;
  long writeCount = 0;
  long readBytes = 0;
  long writeBytes = 0;




  /**
   * @see coyote.batch.listener.FileRecorder#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.open( context ); // initialize FileRecorder

  }




  /**
   * @see coyote.batch.listener.AbstractListener#onEnd(coyote.batch.OperationalContext)
   */
  @Override
  public void onEnd( OperationalContext context ) {

    if ( context instanceof TransformContext ) {
      writeSummary();
    }
  }




  /**
   * @see coyote.batch.listener.AbstractListener#onRead(coyote.batch.TransactionContext)
   */
  @Override
  public void onRead( TransactionContext context ) {
    DataFrame frame = context.getSourceFrame();
    if ( frame != null ) {
      readCount++;
      readBytes += frame.getBytes().length;
      for ( DataField field : frame.getFields() ) {
        getInputFieldMetric( field.getName() ).sample( field );
      } // for
    } // null
  }




  /**
   * Return input (source frame) FieldMetrics associated with the named field. 
   * 
   * <p>If one is not found, one will be created and placed in the cache for 
   * later reference. This method never returns null.</p>
   * 
   * @param name The name of the field to be represented by the returned metric
   * 
   * @return a FieldMetric associated with the named field. Never returns null.
   */
  private FieldMetrics getInputFieldMetric( String name ) {
    FieldMetrics retval = null;
    if ( name != null ) {
      for ( FieldMetrics metric : inputFields ) {
        if ( name.equals( metric.getName() ) ) {
          retval = metric;
          break;
        }
      }
    }

    if ( retval == null ) {
      retval = new FieldMetrics( name );
      inputFields.add( retval );
    }

    return retval;
  }




  /**
   * Return output (target frame) FieldMetrics associated with the named field. 
   * 
   * <p>If one is not found, one will be created and placed in the cache for 
   * later reference. This method never returns null.</p>
   * 
   * @param name The name of the field to be represented by the returned metric
   * 
   * @return a FieldMetric associated with the named field. Never returns null.
   */
  private FieldMetrics getOutputFieldMetric( String name ) {
    FieldMetrics retval = null;
    if ( name != null ) {
      for ( FieldMetrics metric : outputFields ) {
        if ( name.equals( metric.getName() ) ) {
          retval = metric;
          break;
        }
      }
    }

    if ( retval == null ) {
      retval = new FieldMetrics( name );
      outputFields.add( retval );
    }

    return retval;
  }




  /**
   * @see coyote.batch.listener.AbstractListener#onWrite(coyote.batch.TransactionContext)
   */
  @Override
  public void onWrite( TransactionContext context ) {
    DataFrame frame = context.getTargetFrame();
    if ( frame != null ) {
      writeCount++;
      writeBytes += frame.getBytes().length;
      for ( DataField field : frame.getFields() ) {
        getOutputFieldMetric( field.getName() ).sample( field );
      } // for
    } // null
  }




  /**
   * Write the summary of the data read in.
   */
  private void writeInputSummary() {
    StringBuffer b = new StringBuffer( "Input Data Profile:" );
    b.append( StringUtil.LINE_FEED );
    b.append( "Read Count: " );
    b.append( readCount );
    b.append( StringUtil.LINE_FEED );
    b.append( "Byte Count: " );
    b.append( readBytes );
    b.append( StringUtil.LINE_FEED );
    b.append( "Field Count: " );
    b.append( inputFields.size() );
    b.append( StringUtil.LINE_FEED );
    write( b.toString() );

    if ( readCount > 0 ) {

      int nameSize = 5;
      int typeSize = 5;
      long totalChars = 0;
      long totalBytes = 0;

      for ( FieldMetrics metric : inputFields ) {
        if ( metric.getName().length() > nameSize ) {
          nameSize = metric.getName().length();
        }
        if ( metric.getType().length() > typeSize ) {
          typeSize = metric.getType().length();
        }
        totalChars += metric.getTotalStringLength();
        totalBytes += metric.getTotalByteLength();
      }

      b.delete( 0, b.length() );
      b.append( StringUtil.fixedLength( "Field", nameSize + 1, 0 ) );
      b.append( StringUtil.fixedLength( "Type", typeSize + 1, 0 ) );
      b.append( StringUtil.fixedLength( "Nulls", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Empty", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Blank", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Chars", 13, 0 ) );
      b.append( StringUtil.fixedLength( "Avg", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Minimum", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Maximum", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Sdev", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Bytes", 13, 0 ) );
      b.append( StringUtil.fixedLength( "Avg", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Minimum", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Maximum", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Sdev", 9, 0 ) );
      b.append( StringUtil.LINE_FEED );
      write( b.toString() );

      for ( FieldMetrics metric : inputFields ) {
        b.delete( 0, b.length() );
        b.append( StringUtil.fixedLength( metric.getName(), nameSize, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( metric.getType(), typeSize, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getNullCount() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getEmptyCount() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getBlankCount() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getTotalStringLength() ), 12, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getAverageStringLength() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getMinimumStringLength() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getMaximumStringLength() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getStdDevStringLength() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getTotalByteLength() ), 12, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getAverageByteLength() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getMinimumByteLength() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getMaximumByteLength() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getStdDevByteLength() ), 8, 0 ) );

        b.append( StringUtil.LINE_FEED );
        write( b.toString() );
      }

      b.delete( 0, b.length() );
      b.append( StringUtil.fixedLength( "Totals", nameSize + typeSize + 29, 0 ) );
      b.append( StringUtil.fixedLength( Long.toString( totalChars ), 12, 0 ) );
      b.append( StringUtil.fixedLength( "", 37, 0 ) );
      b.append( totalBytes );
      b.append( StringUtil.LINE_FEED );
    }
    b.append( StringUtil.LINE_FEED );
    write( b.toString() );
  }




  /**
   * Write the summary of the data written out.
   */
  private void writeOutputSummary() {
    StringBuffer b = new StringBuffer( "Output Data Profile:" );
    b.append( StringUtil.LINE_FEED );
    b.append( "Write Count: " );
    b.append( writeCount );
    b.append( StringUtil.LINE_FEED );
    b.append( "Byte Count: " );
    b.append( writeBytes );
    b.append( StringUtil.LINE_FEED );
    b.append( "Field Count: " );
    b.append( outputFields.size() );
    b.append( StringUtil.LINE_FEED );
    write( b.toString() );

    if ( writeCount > 0 ) {
      int nameSize = 5;
      int typeSize = 5;
      long totalChars = 0;
      long totalBytes = 0;

      for ( FieldMetrics metric : outputFields ) {
        if ( metric.getName().length() > nameSize ) {
          nameSize = metric.getName().length();
        }
        if ( metric.getType().length() > typeSize ) {
          typeSize = metric.getType().length();
        }
        totalChars += metric.getTotalStringLength();
        totalBytes += metric.getTotalByteLength();
      }

      b.delete( 0, b.length() );
      b.append( StringUtil.fixedLength( "Field", nameSize + 1, 0 ) );
      b.append( StringUtil.fixedLength( "Type", typeSize + 1, 0 ) );
      b.append( StringUtil.fixedLength( "Nulls", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Empty", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Blank", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Chars", 13, 0 ) );
      b.append( StringUtil.fixedLength( "Avg", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Minimum", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Maximum", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Sdev", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Bytes", 13, 0 ) );
      b.append( StringUtil.fixedLength( "Avg", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Minimum", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Maximum", 9, 0 ) );
      b.append( StringUtil.fixedLength( "Sdev", 9, 0 ) );
      b.append( StringUtil.LINE_FEED );
      write( b.toString() );

      for ( FieldMetrics metric : outputFields ) {
        b.delete( 0, b.length() );
        b.append( StringUtil.fixedLength( metric.getName(), nameSize, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( metric.getType(), typeSize, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getNullCount() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getEmptyCount() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getBlankCount() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getTotalStringLength() ), 12, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getAverageStringLength() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getMinimumStringLength() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getMaximumStringLength() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getStdDevStringLength() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getTotalByteLength() ), 12, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getAverageByteLength() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getMinimumByteLength() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getMaximumByteLength() ), 8, 0 ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getStdDevByteLength() ), 8, 0 ) );

        b.append( StringUtil.LINE_FEED );
        write( b.toString() );
      }

      b.delete( 0, b.length() );
      b.append( StringUtil.fixedLength( "Totals", nameSize + typeSize + 29, 0 ) );
      b.append( StringUtil.fixedLength( Long.toString( totalChars ), 12, 0 ) );
      b.append( StringUtil.fixedLength( "", 37, 0 ) );
      b.append( totalBytes );
      b.append( StringUtil.LINE_FEED );
    }
    
    b.append( StringUtil.LINE_FEED );
    write( b.toString() );
  }




  /**
   * Write the summary report of the data processed
   */
  private void writeSummary() {
    writeInputSummary();
    writeOutputSummary();
  }

}