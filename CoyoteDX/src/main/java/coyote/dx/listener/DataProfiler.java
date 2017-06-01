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
import java.text.FieldPosition;
import java.util.Date;

import coyote.commons.StringUtil;
import coyote.commons.template.SymbolTable;
import coyote.dataframe.DataFrame;
import coyote.dx.FrameReader;
import coyote.dx.FrameWriter;
import coyote.dx.context.ContextListener;
import coyote.dx.context.OperationalContext;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.dx.db.DatabaseDialect;
import coyote.dx.db.FieldMetrics;
import coyote.dx.db.MetricSchema;


/**
 * This listener keeps track of the data read in to and out of the engine and 
 * reports on the characteristics of the data observed.
 */
public class DataProfiler extends FileRecorder implements ContextListener {
  private MetricSchema inputSchema = new MetricSchema();
  private MetricSchema outputSchema = new MetricSchema();
  protected static final SymbolTable symbols = new SymbolTable();

  long readBytes = 0;
  long writeBytes = 0;
  static DecimalFormat MILLIS = new DecimalFormat( "000" );
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat( "#,###,##0.00" );
  private static final DecimalFormat P_FORMAT = new DecimalFormat( "##0.0" );
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
   * @see coyote.dx.listener.FileRecorder#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.open( context ); // initialize FileRecorder

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
      writePerformanceSummary();
      writeInputSummary();
      writeInputSQL();
      writeOutputSummary();
      writeOutputSQL();
    }
  }




  /**
   * 
   */
  private void writeOutputSQL() {
    if ( outputSchema.getSampleCount() > 0 ) {
      StringBuffer b = new StringBuffer( "Table Creation for Output" );
      b.append( StringUtil.LINE_FEED );
      symbols.put( DatabaseDialect.DB_SCHEMA_SYM, "DBUser" );
      symbols.put( DatabaseDialect.TABLE_NAME_SYM, "TableName" );
      b.append( "H2: " );
      b.append( DatabaseDialect.getCreate( DatabaseDialect.H2, outputSchema, symbols ) );
      b.append( StringUtil.LINE_FEED );
      b.append( "Oracle: " );
      b.append( DatabaseDialect.getCreate( DatabaseDialect.ORACLE, outputSchema, symbols ) );
      b.append( StringUtil.LINE_FEED );
      b.append( "MySQL: " );
      b.append( DatabaseDialect.getCreate( DatabaseDialect.MYSQL, outputSchema, symbols ) );
      b.append( StringUtil.LINE_FEED );
      b.append( StringUtil.LINE_FEED );
      write( b.toString() );
    }
  }




  /**
   * 
   */
  private void writeInputSQL() {
    if ( inputSchema.getSampleCount() > 0 ) {
      StringBuffer b = new StringBuffer( "Table Creation for Input" );
      b.append( StringUtil.LINE_FEED );
      symbols.put( DatabaseDialect.DB_SCHEMA_SYM, "DBUser" );
      symbols.put( DatabaseDialect.TABLE_NAME_SYM, "TableName" );
      b.append( "H2: " );
      b.append( DatabaseDialect.getCreate( DatabaseDialect.H2, inputSchema, symbols ) );
      b.append( StringUtil.LINE_FEED );
      b.append( "Oracle: " );
      b.append( DatabaseDialect.getCreate( DatabaseDialect.ORACLE, inputSchema, symbols ) );
      b.append( StringUtil.LINE_FEED );
      b.append( "MySQL: " );
      b.append( DatabaseDialect.getCreate( DatabaseDialect.MYSQL, inputSchema, symbols ) );
      b.append( StringUtil.LINE_FEED );
      b.append( StringUtil.LINE_FEED );
      write( b.toString() );
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
    b.append( NUMBER_FORMAT.format( inputSchema.getSampleCount() ) );
    if ( elapsed > 0 ) {
      b.append( " - " );
      b.append( DECIMAL_FORMAT.format( (double)inputSchema.getSampleCount() / (double)( elapsed / (double)1000 ) ) );
      b.append( " records per second" );
    }
    b.append( StringUtil.LINE_FEED );
    b.append( "Records Written: " );
    b.append( NUMBER_FORMAT.format( outputSchema.getSampleCount() ) );
    if ( elapsed > 0 ) {
      b.append( " - " );
      b.append( DECIMAL_FORMAT.format( (double)outputSchema.getSampleCount() / (double)( elapsed / (double)1000 ) ) );
      b.append( " records per second" );
    }
    b.append( StringUtil.LINE_FEED );
    b.append( StringUtil.LINE_FEED );

    write( b.toString() );
  }




  /**
   * @see coyote.dx.listener.AbstractListener#onRead(coyote.dx.context.TransactionContext, coyote.dx.FrameReader)
   */
  @Override
  public void onRead( TransactionContext context, FrameReader reader ) {
    DataFrame frame = context.getSourceFrame();
    if ( frame != null ) {
      readBytes += frame.getBytes().length;
      inputSchema.sample( frame );
    } // null
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
      for ( FieldMetrics metric : outputSchema.getMetrics() ) {
        if ( name.equals( metric.getName() ) ) {
          retval = metric;
          break;
        }
      }
    }

    if ( retval == null ) {
      retval = new FieldMetrics( name );
      outputSchema.getMetrics().add( retval );
    }

    return retval;
  }




  /**
   * @see coyote.dx.listener.AbstractListener#onWrite(coyote.dx.context.TransactionContext, coyote.dx.FrameWriter)
   */
  @Override
  public void onWrite( TransactionContext context, FrameWriter writer ) {
    DataFrame frame = context.getTargetFrame();
    if ( frame != null ) {
      writeBytes += frame.getBytes().length;
      outputSchema.sample( frame );
    } // null
  }




  /**
   * Write the summary of the data read in.
   */
  private void writeInputSummary() {
    StringBuffer b = new StringBuffer( "Input Data Profile:" );
    b.append( StringUtil.LINE_FEED );
    b.append( "Read Count: " );
    b.append( inputSchema.getSampleCount() );
    b.append( StringUtil.LINE_FEED );
    b.append( "Byte Count: " );
    b.append( readBytes );
    b.append( StringUtil.LINE_FEED );
    b.append( "Field Count: " );
    b.append( inputSchema.getMetrics().size() );
    b.append( StringUtil.LINE_FEED );
    write( b.toString() );
    b.delete( 0, b.length() );

    if ( inputSchema.getSampleCount() > 0 ) {

      int nameSize = 5;
      int typeSize = 5;
      long totalChars = 0;
      long totalBytes = 0;

      for ( FieldMetrics metric : inputSchema.getMetrics() ) {
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
      b.append( StringUtil.fixedLength( "Field", nameSize + 1, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Type", typeSize + 1, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Nulls", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Empty", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Blank", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Unique", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Coincidence", 12, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Chars", 13, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Avg", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Minimum", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Maximum", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Sdev", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Bytes", 13, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Avg", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Minimum", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Maximum", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "SDev", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.LINE_FEED );
      write( b.toString() );

      for ( FieldMetrics metric : inputSchema.getMetrics() ) {
        b.delete( 0, b.length() );
        b.append( StringUtil.fixedLength( metric.getName(), nameSize, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( metric.getType(), typeSize, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getNullCount() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getEmptyCount() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getBlankCount() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getUniqueValues() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( P_FORMAT.format( metric.getCoincidence() * 100 ), 5, StringUtil.RIGHT_ALIGNMENT ) );
        b.append( "       " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getTotalStringLength() ), 12, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getAverageStringLength() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getMinimumStringLength() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getMaximumStringLength() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getStdDevStringLength() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getTotalByteLength() ), 12, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getAverageByteLength() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getMinimumByteLength() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getMaximumByteLength() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getStdDevByteLength() ), 8, StringUtil.LEFT_ALIGNMENT ) );

        b.append( StringUtil.LINE_FEED );
        write( b.toString() );
      }

      b.delete( 0, b.length() );
      b.append( StringUtil.fixedLength( "Totals", nameSize + typeSize + 50, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( Long.toString( totalChars ), 12, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "", 37, StringUtil.LEFT_ALIGNMENT ) );
      b.append( totalBytes );
      b.append( "  " );
      b.append( formatSizeBytes( totalBytes ) );
      b.append( StringUtil.LINE_FEED );
      write( b.toString() );
    }

    b.delete( 0, b.length() );
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
    b.append( outputSchema.getSampleCount() );
    b.append( StringUtil.LINE_FEED );
    b.append( "Byte Count: " );
    b.append( writeBytes );
    b.append( StringUtil.LINE_FEED );
    b.append( "Field Count: " );
    b.append( outputSchema.getMetrics().size() );
    b.append( StringUtil.LINE_FEED );
    write( b.toString() );
    b.delete( 0, b.length() );

    if ( outputSchema.getSampleCount() > 0 ) {
      int nameSize = 5;
      int typeSize = 5;
      long totalChars = 0;
      long totalBytes = 0;

      for ( FieldMetrics metric : outputSchema.getMetrics() ) {
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
      b.append( StringUtil.fixedLength( "Field", nameSize + 1, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Type", typeSize + 1, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Nulls", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Empty", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Blank", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Unique", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Coincidence", 12, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Chars", 13, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Avg", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Minimum", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Maximum", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Sdev", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Bytes", 13, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Avg", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Minimum", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "Maximum", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "SDev", 9, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.LINE_FEED );
      write( b.toString() );

      for ( FieldMetrics metric : outputSchema.getMetrics() ) {
        b.delete( 0, b.length() );
        b.append( StringUtil.fixedLength( metric.getName(), nameSize, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( metric.getType(), typeSize, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getNullCount() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getEmptyCount() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getBlankCount() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getUniqueValues() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( P_FORMAT.format( metric.getCoincidence() * 100 ), 5, StringUtil.RIGHT_ALIGNMENT ) );
        b.append( "       " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getTotalStringLength() ), 12, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getAverageStringLength() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getMinimumStringLength() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getMaximumStringLength() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getStdDevStringLength() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getTotalByteLength() ), 12, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getAverageByteLength() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getMinimumByteLength() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getMaximumByteLength() ), 8, StringUtil.LEFT_ALIGNMENT ) );
        b.append( " " );
        b.append( StringUtil.fixedLength( Long.toString( metric.getStdDevByteLength() ), 8, StringUtil.LEFT_ALIGNMENT ) );

        b.append( StringUtil.LINE_FEED );
        write( b.toString() );
      }

      b.delete( 0, b.length() );
      b.append( StringUtil.fixedLength( "Totals", nameSize + typeSize + 50, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( Long.toString( totalChars ), 12, StringUtil.LEFT_ALIGNMENT ) );
      b.append( StringUtil.fixedLength( "", 37, StringUtil.LEFT_ALIGNMENT ) );
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