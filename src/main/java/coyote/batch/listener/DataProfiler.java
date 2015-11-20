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
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Date;
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
 */
public class DataProfiler extends FileRecorder implements ContextListener {
  private List<FieldMetrics> inputFields = new ArrayList<FieldMetrics>();
  private List<FieldMetrics> outputFields = new ArrayList<FieldMetrics>();
  long readCount = 0;
  long writeCount = 0;
  long readBytes = 0;
  long writeBytes = 0;
  static DecimalFormat MILLIS = new DecimalFormat( "000" );
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat( "#,###,##0.00" );
  private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat( "###,###,###,###,###" );

  Date start = null;
  Date end = null;
  long elapsed = -1;

  /** Represents 1 Kilo Byte ( 1024 ). */
  private final static long ONE_KB = 1024L;

  /** Represents 1 Mega Byte ( 1024^2 ). */
  private final static long ONE_MB = ONE_KB * 1024L;

  /** Represents 1 Giga Byte ( 1024^3 ). */
  private final static long ONE_GB = ONE_MB * 1024L;

  /** Represents 1 Tera Byte ( 1024^4 ). */
  private final static long ONE_TB = ONE_GB * 1024L;




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
      start = new Date( context.getStartTime() );
      end = new Date( context.getEndTime() );
      elapsed = context.getElapsed();
      writePerformanceSummary();
      writeInputSummary();
      writeOutputSummary();
    }
  }




  private void writePerformanceSummary() {
    StringBuffer b = new StringBuffer( "Transform Performance:" );
    b.append( StringUtil.LINE_FEED );

    b.append( "Job Start: " );
    b.append( start.toString() );
    b.append( StringUtil.LINE_FEED );
    b.append( "Job End: " );
    b.append( end.toString() );
    b.append( StringUtil.LINE_FEED );
    b.append( "Elapsed: " );
    b.append( elapsed );
    b.append( "ms  -  " );
    b.append( formatElapsed( elapsed ) );
    b.append( StringUtil.LINE_FEED );
    b.append( "Records Read: " );
    b.append( NUMBER_FORMAT.format( readCount ) );
    if ( elapsed > 0 ) {
      b.append( " - " );
      b.append( DECIMAL_FORMAT.format( (double)readCount / (double)( elapsed / (double)1000 ) ) );
      b.append( " records per second" );
    }
    b.append( StringUtil.LINE_FEED );
    b.append( "Records Written: " );
    b.append( NUMBER_FORMAT.format( writeCount ) );
    if ( elapsed > 0 ) {
      b.append( " - " );
      b.append( DECIMAL_FORMAT.format( (double)writeCount / (double)( elapsed / (double)1000 ) ) );
      b.append( " records per second" );
    }
    b.append( StringUtil.LINE_FEED );
    b.append( StringUtil.LINE_FEED );

    write( b.toString() );
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
      b.append( StringUtil.fixedLength( "SDev", 9, 0 ) );
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
      b.append( "  " );
      b.append( formatSizeBytes( totalBytes ) );
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
      b.append( StringUtil.fixedLength( "SDev", 9, 0 ) );
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
      b.append( "  " );
      b.append( formatSizeBytes( totalBytes ) );
      b.append( StringUtil.LINE_FEED );
    }

    b.append( StringUtil.LINE_FEED );
    write( b.toString() );
  }




  /**
   * Get a formatted string representing the difference between the two times.
   * 
   * @param millis number of elapsed milliseconds.
   * 
   * @return formatted string representing weeks, days, hours minutes and seconds.
   */
  private static String formatElapsed( long millis ) {
    if ( millis < 0 || millis == Long.MAX_VALUE ) {
      return "?";
    }

    long secondsInMilli = 1000;
    long minutesInMilli = secondsInMilli * 60;
    long hoursInMilli = minutesInMilli * 60;
    long daysInMilli = hoursInMilli * 24;
    long weeksInMilli = daysInMilli * 7;

    long elapsedWeeks = millis / weeksInMilli;
    millis = millis % weeksInMilli;

    long elapsedDays = millis / daysInMilli;
    millis = millis % daysInMilli;

    long elapsedHours = millis / hoursInMilli;
    millis = millis % hoursInMilli;

    long elapsedMinutes = millis / minutesInMilli;
    millis = millis % minutesInMilli;

    long elapsedSeconds = millis / secondsInMilli;
    millis = millis % secondsInMilli;

    StringBuilder b = new StringBuilder();

    if ( elapsedWeeks > 0 ) {
      b.append( elapsedWeeks );
      if ( elapsedWeeks > 1 )
        b.append( " wks " );
      else
        b.append( " wk " );
    }
    if ( elapsedDays > 0 ) {
      b.append( elapsedDays );
      if ( elapsedDays > 1 )
        b.append( " days " );
      else
        b.append( " day " );

    }
    if ( elapsedHours > 0 ) {
      b.append( elapsedHours );
      if ( elapsedHours > 1 )
        b.append( " hrs " );
      else
        b.append( " hr " );
    }
    if ( elapsedMinutes > 0 ) {
      b.append( elapsedMinutes );
      b.append( " min " );
    }
    b.append( elapsedSeconds );
    if ( millis > 0 ) {
      b.append( "." );
      b.append( MILLIS.format( millis ) );
    }
    b.append( " sec" );

    return b.toString();
  }




  /**
   * Formats the size as a most significant number of bytes.
   * 
   * @param size in bytes
   * 
   * @return the size formatted for display
   */
  private static String formatSizeBytes( final double size ) {
    final StringBuffer buf = new StringBuffer( 16 );
    String text;
    double divider;

    if ( size < ONE_KB ) {
      text = "bytes";
      divider = 1.0;
    } else if ( size < ONE_MB ) {
      text = "KB";
      divider = ONE_KB;
    } else if ( size < ONE_GB ) {
      text = "MB";
      divider = ONE_MB;
    } else if ( size < ONE_TB ) {
      text = "GB";
      divider = ONE_GB;
    } else {
      text = "TB";
      divider = ONE_TB;
    }

    final double d = ( (double)size ) / divider;
    DECIMAL_FORMAT.format( d, buf, new FieldPosition( 0 ) ).append( ' ' ).append( text );

    return buf.toString();
  }

}