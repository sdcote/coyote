/*
 * Copyright (c) 2007 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.loader.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;


/**
 * Class CyclingFileAppender.
 *
 * <p>Generations - number of files to keep around dflt=1</p>
 * <p>MaxSize = maximum size of a log file in bytes (MB,KB,M,K,B suffix allowed) dflt=10MB</p>
 * <p>CycleTime = the reference time from which intervals are calculated dflt=00:00</p>
 * <p>Interval = number of seconds between cycling the logs</p>
 *
 * @author Stephan D. Cote' - Enterprise Architecture
 */
public class CyclingFileAppender extends FileAppender {
  /** The name of the property containing the maximum generations to keep. */
  public static final String GENERATION_TAG = "Generations";

  /**
   * The name of the property containing the maximum size in bytes of the file
   * to keep before cycling.
   */
  public static final String MAX_SIZE_TAG = "MaxSize";
  /** The name of the property that defines an interval for cycling the logs. */
  public static final String INTERVAL_TAG = "Interval";

  /** The name of the property that containing a reference cycle time of day. */
  public static final String CYCLE_TIME_TAG = "CycleTime";

  protected long lastCycleTime = 0;

  protected long cycleInterval = Long.MAX_VALUE;

  protected long nextCycleTime = Long.MAX_VALUE;

  /** The default maximum file size is 10MB. */
  protected long maxFileSize = 10 * 1024 * 1024;

  /** There is one generation (backup file) by default. */
  protected int generations = 7;




  /**
   * @see coyote.loader.log.Logger#append(String, Object, Throwable)
   *
   * @param category the category to log
   * @param event the event to log
   * @param cause The exception that caused the log entry. Can be null.
   */
  public void append( final String category, final Object event, final Throwable cause ) {
    // check to see if it is time to cycle
    if ( timeToCycle() ) {
      cycle();
    }

    super.append( category, event, cause );
  }




  /**
   * Move the current file to the next file in the generation while renaming
   * all the existing files to an older generation.
   */
  public void cycle() {
    final long now = System.currentTimeMillis();

    File target;
    File file;
    final String fileName = targetFile.getAbsolutePath();

    // If maxBackups <= 0, then there is no file renaming to be done.
    if ( generations > 0 ) {
      // Delete the oldest file
      file = new File( fileName + '.' + generations );

      if ( file.exists() ) {
        file.delete();
      }

      // Shift names to the left...or is it to the right?...Hmmmm
      for ( int i = generations - 1; i >= 1; i-- ) {
        file = new File( fileName + "." + i );

        if ( file.exists() ) {
          target = new File( fileName + '.' + ( i + 1 ) );

          // System.out.println( "Renaming file " + file + " to " + target );
          file.renameTo( target );
        }
      }

      // Write the footer if necessary
      try {
        final byte[] footer = getFormatter().terminate();

        if ( footer != null ) {
          log_writer.write( new String( footer ) );
        }

        log_writer.flush();
        log_writer.close();
      } catch ( final Exception e ) {}

      // Rename fileName to fileName.1
      target = new File( fileName + ".1" );

      super.targetFile.renameTo( target );

      // re-establish the file (is this really necessary?)
      super.targetFile = new File( fileName );

      // open the writer to the new file
      try {
        log_writer = new OutputStreamWriter( new FileOutputStream( targetFile.toString(), true ) );
      } catch ( final FileNotFoundException e1 ) {
        e1.printStackTrace();
        terminate();
      }

      // output the header (if one exists) to the new file
      final byte[] header = getFormatter().initialize();

      if ( header != null ) {
        try {
          log_writer.write( new String( header ) );
        } catch ( final IOException e ) {
          System.err.println( "Error writing header to newly-cycled file: " + e.getMessage() );
        }
      }

      // If a cycle interval has been defined and we are cycling due to a time
      // as opposed to a file size trigger, then reset the next cycle time
      if ( ( cycleInterval < Long.MAX_VALUE ) && ( nextCycleTime < now ) ) {
        if ( lastCycleTime != 0 ) {
          // what if we have not cycled for several days? We can't just 
          // increment the time once, we have to increment the time until it is 
          // in the future.
          while ( nextCycleTime < System.currentTimeMillis() ) {
            nextCycleTime = nextCycleTime + cycleInterval;
          }
        } else {
          // This is our first cycle
          nextCycleTime = now + cycleInterval;
        }
      }

      // track the last time we cycled
      lastCycleTime = now;

    }

  }




  /**
   * @see coyote.loader.log.FileAppender#initialize()
   */
  public void initialize() {
    // super class stuff
    super.initialize();

    // grab a calendar using the current localized time
    final Calendar cal = Calendar.getInstance();
    final long now = System.currentTimeMillis();

    if ( config != null ) {
      // How many generations do we keep?
      if ( config.contains( CyclingFileAppender.GENERATION_TAG ) ) {
        try {
          generations = config.getAsInt( CyclingFileAppender.GENERATION_TAG );

          if ( generations < 1 ) {
            System.err.println( "Value cannot be less than one" );

            generations = 1;
          }
        } catch ( final Exception e ) {
          System.err.println( "Could not parse '" + config.get( CyclingFileAppender.GENERATION_TAG ) + "' into a file size number" );

          generations = 1;
        }
      }

      // What is the maximum size of the log file?
      if ( config.contains( CyclingFileAppender.MAX_SIZE_TAG ) ) {
        String sizeTag = config.getAsString( CyclingFileAppender.MAX_SIZE_TAG );

        if ( ( sizeTag != null ) && ( sizeTag.trim().length() > 0 ) ) {
          sizeTag = sizeTag.trim().toUpperCase();

          long maxSize = maxFileSize;

          try {
            if ( sizeTag.endsWith( "MB" ) ) {
              maxSize = Long.parseLong( sizeTag.substring( 0, ( sizeTag.length() - 2 ) ) ) * ( ( 1024 * 1024 ) );
            } else if ( sizeTag.endsWith( "KB" ) ) {
              maxSize = Long.parseLong( sizeTag.substring( 0, ( sizeTag.length() - 2 ) ) ) * ( 1024 );
            } else if ( sizeTag.endsWith( "M" ) ) {
              maxSize = Long.parseLong( sizeTag.substring( 0, ( sizeTag.length() - 1 ) ) ) * ( ( 1024 * 1024 ) );
            } else if ( sizeTag.endsWith( "K" ) ) {
              maxSize = Long.parseLong( sizeTag.substring( 0, ( sizeTag.length() - 1 ) ) ) * ( 1024 );
            } else if ( sizeTag.endsWith( "B" ) ) {
              maxSize = Long.parseLong( sizeTag.substring( 0, ( sizeTag.length() - 1 ) ) );
            } else if ( sizeTag.equalsIgnoreCase( "NONE" ) ) {
              maxSize = Long.MAX_VALUE;
            } else {
              maxSize = Long.parseLong( sizeTag );
            }
          } catch ( final NumberFormatException e ) {
            System.err.println( "Could not parse '" + sizeTag + "' into a file size number" );
          }

          // Make sure the file size is positive and not zero
          if ( maxSize > 0 ) {
            maxFileSize = maxSize;
          }
        }
      } // if MAX_SIZE_TAG

      // See if we are to cycle at regular intervals
      if ( config.contains( CyclingFileAppender.INTERVAL_TAG ) ) {
        String intervalTag = config.getAsString( CyclingFileAppender.INTERVAL_TAG );

        if ( ( intervalTag != null ) && ( intervalTag.trim().length() > 0 ) ) {
          intervalTag = intervalTag.trim().toUpperCase();

          long interval = cycleInterval;

          try {
            if ( intervalTag.endsWith( "D" ) ) {
              interval = Long.parseLong( intervalTag.substring( 0, ( intervalTag.length() - 1 ) ) ) * ( 24 * 60 * 60 * 1000 );
            } else if ( intervalTag.endsWith( "H" ) ) {
              interval = Long.parseLong( intervalTag.substring( 0, ( intervalTag.length() - 1 ) ) ) * ( 60 * 60 * 1000 );
            } else if ( intervalTag.endsWith( "M" ) ) {
              interval = Long.parseLong( intervalTag.substring( 0, ( intervalTag.length() - 1 ) ) ) * ( 60 * 1000 );
            } else if ( intervalTag.endsWith( "S" ) ) {
              interval = Long.parseLong( intervalTag.substring( 0, ( intervalTag.length() - 1 ) ) ) * ( 1000 );
            } else {
              interval = Long.parseLong( intervalTag ) * ( 1000 );
            }

          } catch ( final NumberFormatException e ) {
            System.err.println( "Could not parse '" + intervalTag + "' into an interval number" );
          }

          // Make sure the interval is positive and not zero
          if ( interval > 0 ) {
            cycleInterval = interval;
          }

        } // if intervalTag !null | empty

      }

      // If an interval was correctly set, calculate the next cycle time
      if ( cycleInterval != Long.MAX_VALUE ) {
        nextCycleTime = now + cycleInterval;
      }

      // See if we have a reference time from which intervals are to be based
      if ( config.contains( CyclingFileAppender.CYCLE_TIME_TAG ) ) {
        final String time = config.getAsString( CyclingFileAppender.CYCLE_TIME_TAG );

        if ( ( time != null ) && ( time.trim().length() > 0 ) ) {
          final String text = time.trim();

          if ( text.length() > 0 ) {
            if ( text.toUpperCase().endsWith( "S" ) ) {
              final int secs = Integer.parseInt( text.substring( 0, text.length() - 1 ) );

              if ( ( secs > 59 ) || ( secs < 0 ) ) {
                throw new IllegalArgumentException( "Seconds are out or range" );
              }

              // Set the minutes and zero-out the lower values
              cal.set( Calendar.SECOND, secs );
              cal.set( Calendar.MILLISECOND, 0 );

              // if the time is too far in the future
              if ( cal.getTimeInMillis() > now + cycleInterval ) {
                // set the clock back by one time point
                cal.add( Calendar.SECOND, secs * -1 );
              }

              // if the time has already passed, increment the seconds
              while ( cal.getTimeInMillis() < now ) {
                // try to find the proper time by using either the interval or
                // the specified seconds, whichever is less
                if ( ( cycleInterval != 0 ) && ( cycleInterval / 1000 < secs ) ) {
                  cal.add( Calendar.SECOND, (int)cycleInterval / 1000 );
                } else {
                  cal.add( Calendar.SECOND, secs );
                }
              }

            } else {
              String hrs;
              String mns;
              String scs = "00";
              int ptr = text.lastIndexOf( ':' );

              if ( ptr > -1 ) {
                mns = text.substring( ptr + 1 );
                hrs = text.substring( 0, ptr );

                ptr = hrs.lastIndexOf( ':' );

                if ( ptr > -1 ) {
                  scs = mns;
                  mns = hrs.substring( ptr + 1 );
                  hrs = hrs.substring( 0, ptr );
                }

                try {
                  final int hours = Integer.parseInt( hrs );

                  if ( ( hours > 23 ) || ( hours < 0 ) ) {
                    throw new IllegalArgumentException( "Hours are out or range" );
                  }

                  final int mins = Integer.parseInt( mns );

                  if ( ( mins > 59 ) || ( mins < 0 ) ) {
                    throw new IllegalArgumentException( "Minutes are out or range" );
                  }

                  final int secs = Integer.parseInt( scs );

                  if ( ( secs > 59 ) || ( secs < 0 ) ) {
                    throw new IllegalArgumentException( "Seconds are out or range" );
                  }

                  // Set all the parsed values
                  cal.set( Calendar.HOUR_OF_DAY, hours );
                  cal.set( Calendar.MINUTE, mins );
                  cal.set( Calendar.SECOND, secs );
                  cal.set( Calendar.MILLISECOND, 0 );

                  // if the time has already passed, increment the date
                  if ( cal.getTimeInMillis() < now ) {
                    cal.add( Calendar.DATE, 1 );
                  }

                  // If a cycle interval wasn't specified, assume a 1d interval
                  if ( cycleInterval == Long.MAX_VALUE ) {
                    cycleInterval = ( 24 * 60 * 60 * 1000 );
                  }
                } catch ( final NumberFormatException e ) {
                  throw new IllegalArgumentException( "Time is not in HH:MM format" );
                }

              } else {
                // only minutes
                final int mins = Integer.parseInt( text );

                if ( ( mins > 59 ) || ( mins < 0 ) ) {
                  throw new IllegalArgumentException( "Minutes are out or range" );
                }

                // Set the minutes and zero-out the lower values
                cal.set( Calendar.MINUTE, mins );
                cal.set( Calendar.SECOND, 0 );
                cal.set( Calendar.MILLISECOND, 0 );

                // if the time has already passed, increment the hour
                if ( cal.getTimeInMillis() < now ) {
                  cal.add( Calendar.HOUR_OF_DAY, 1 );
                }

                // If a cycle interval wasn't specified, assume a 1hr interval
                if ( cycleInterval == Long.MAX_VALUE ) {
                  cycleInterval = ( 60 * 60 * 1000 );
                }

              } // if text contains ':' else

            } // ends in 's'

          } // if text !""

          nextCycleTime = cal.getTimeInMillis();

        } // time !null & !""

      }

    } // if config !null

  }




  /**
   * @see coyote.loader.log.FileAppender#terminate()
   */
  public void terminate() {
    // our stuff

    // super class
    super.terminate();
  }




  /**
   * Check to see if it is time to cycle our log file.
   *
   * @return True if the files are to be cycled, false otherwise.
   */
  public boolean timeToCycle() {
    // Have we reached our size limit?
    if ( targetFile.length() > maxFileSize ) {
      return true;
    }

    // Has our cycle time passed?
    if ( nextCycleTime < System.currentTimeMillis() ) {
      return true;
    }

    return false;
  }

}