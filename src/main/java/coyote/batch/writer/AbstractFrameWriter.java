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
package coyote.batch.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.AbstractConfigurableComponent;
import coyote.batch.ConfigTag;
import coyote.batch.FrameWriter;
import coyote.batch.TransformContext;
import coyote.commons.StringUtil;
import coyote.commons.UriUtil;


/**
 * 
 */
public abstract class AbstractFrameWriter extends AbstractConfigurableComponent implements FrameWriter {

  /** The logger for the base class */
  final Logger log = LoggerFactory.getLogger( getClass() );

  protected static final String STDOUT = "STDOUT";
  protected static final String STDERR = "STDERR";
  protected int rowNumber = 0;
  protected PrintWriter printwriter = null;




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    if ( printwriter != null ) {
      printwriter.flush();
      printwriter.close();
    }
  }




  /**
   * @return the print writer used for output
   */
  public PrintWriter getPrintwriter() {
    return printwriter;
  }




  /**
   * @return the target URI to which the writer will write
   */
  public String getTarget() {
    return configuration.getAsString( ConfigTag.TARGET );
  }




  /**
   * @see coyote.batch.Component#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( final TransformContext context ) {
    super.context = context;

    // if we don't already have a printwriter, set one up based on the configuration
    if ( printwriter == null ) {
      // check for a target in our configuration
      String target = getString( ConfigTag.TARGET );
      log.debug( "using a target of {}", target );

      // Make sure we have a target
      if ( StringUtil.isNotBlank( target ) ) {

        // Try to parse the target as a URI, failures result in a null
        final URI uri = UriUtil.parse( target );

        File targetFile = null;

        // Check to see if it is STDOUT or STDERR
        if ( STDOUT.equalsIgnoreCase( target ) ) {
          printwriter = new PrintWriter( System.out );
        } else if ( STDERR.equalsIgnoreCase( target ) ) {
          printwriter = new PrintWriter( System.err );
        } else if ( uri != null ) {
          if ( UriUtil.isFile( uri ) ) {
            targetFile = UriUtil.getFile( uri );
            if ( targetFile != null ) {
              log.debug( "Using a target file of " + targetFile.getAbsolutePath() );
            } else {
              log.warn( "The target '{}' does not represent a file", target );
            }
          }
        } else {
          targetFile = new File( target );
          log.debug( "Using a target file of " + targetFile.getAbsolutePath() );
        }

        if ( targetFile != null ) {
          try {
            final Writer fwriter = new FileWriter( targetFile );
            printwriter = new PrintWriter( fwriter );

          } catch ( final Exception e ) {
            log.error( "Could not create writer: " + e.getMessage() );
            context.setError( e.getMessage() );
          }
        } else {
          log.error( "Invalid target '{}'", target );
          context.setError( getClass().getName() + " could not parse target '" + target + "' into a file path" );
        }

      } else {
        log.error( "No target specified" );
        context.setError( getClass().getName() + " could not determine target" );
      }
    }
  }




  /**
   * Set the PrintWriter used for output
   * 
   * @param writer the print writer to set
   */
  public void setPrintwriter( final PrintWriter writer ) {
    printwriter = writer;
  }




  /**
   * Set the URI to where the write will write its data.
   * 
   * @param value the URI to where the writer should write its data
   */
  public void setTarget( final String value ) {
    configuration.put( ConfigTag.TARGET, value );
  }

}
