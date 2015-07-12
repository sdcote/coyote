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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.ConfigTag;
import coyote.batch.TransformContext;
import coyote.commons.UriUtil;


/**
 * Base class for context listeners that want to record data to a file.
 * 
 * <p>Note: this is not logging, but a way to write strings to a file.</p>
 */
public abstract class FileRecorder extends ContextRecorder {

  /** The logger for this class */
  final Logger log = LoggerFactory.getLogger( getClass() );

  protected Writer log_writer;

  protected File targetFile = null;




  /**
   * @see coyote.batch.listener.AbstractListener#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.open( context );

    log.info( "Opening FileRecorder" );

    // get our configuration data
    setTarget( getString( ConfigTag.TARGET ) );
    log.debug( "Using a target of {}", getTarget() );

    try {
      URI target = new URI( getTarget() );

      // Make sure we have a complete path to the target file
      File dest = new File( UriUtil.getFilePath( target ) );

      // if not absolute, use the current working directory
      if ( !dest.isAbsolute() ) {
        dest = new File( System.getProperty( "user.dir" ), UriUtil.getFilePath( target ) );
      }

      // make any directories as necessary
      dest.getParentFile().mkdirs();

      // 
      targetFile = dest;

      // Create the writer
      log_writer = new OutputStreamWriter( new FileOutputStream( targetFile.toString(), false ) );

    } catch ( final URISyntaxException e ) {
      context.setError( "Invalid target URI (" + e.getMessage() + ") - '" + getTarget() + "'" );
    } catch ( FileNotFoundException e ) {
      context.setError( "Could not write to target URI (" + e.getMessage() + ") - '" + getTarget() + "'" );
    } catch ( Exception e ) {
      context.setError( "Processing exception (" + e.getMessage() + ") during open " );
      e.printStackTrace();
    }

  }




  protected void write( String text ) {

    if ( !targetFile.exists() ) {
      try {
        log_writer = new OutputStreamWriter( new FileOutputStream( targetFile.toString(), false ) );
        //final byte[] header = getFormatter().initialize();
        //if ( header != null ) { log_writer.write( new String( header ) ); }
      } catch ( final Exception ex ) {
        System.err.println( "Could not recreate " + targetFile.getAbsolutePath() + " - " + ex.getMessage() );
        if ( log_writer != null ) {
          try {
            log_writer.close();
          } catch ( final Exception ignore ) {}
          finally {
            log_writer = null;
          }
        }
      }
    }

    if ( log_writer == null ) {
      return;
    }

    try {
      log_writer.write( text );
      log_writer.flush();
    } catch ( final IOException ioe ) {} catch ( final Exception e ) {
      context.setError( this.getClass().getName() + " context logging error: " + e + ":" + e.getMessage() );
    }
  }




  /**
   * @see coyote.batch.listener.AbstractListener#close()
   */
  @Override
  public void close() throws IOException {

    if ( log_writer != null ) {
      try {
        log_writer.close();
      } catch ( final Exception ignore ) {}
      finally {
        log_writer = null;
      }
    }

    super.close();
  }

}
