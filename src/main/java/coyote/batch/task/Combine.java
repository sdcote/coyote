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
package coyote.batch.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import coyote.batch.Batch;
import coyote.batch.ConfigTag;
import coyote.batch.Symbols;
import coyote.batch.TaskException;
import coyote.batch.TransformContext;
import coyote.batch.TransformTask;
import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Combine all the text files into one.
 * 
 * <p>The order of combination is determined by filename including the path.</p>
 * 
 * "CombineTextFiles" : { "directory": "\datadir", "pattern": "([^\\s]+(\\.(?i)(csv))$)", "target": "big.csv", "recurse": false }
 *  
 */
public class Combine extends AbstractFileTask implements TransformTask {

  protected static final String STDOUT = "STDOUT";
  protected static final String STDERR = "STDERR";
  protected PrintWriter printwriter = null;

  File directory = null;
  String pattern = null;
  boolean recurse = false;




  /**
   * @see coyote.batch.task.AbstractTransformTask#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.open( context );

    // Get our source directory
    String sourcedir = getString( ConfigTag.DIRECTORY );
    directory = new File( sourcedir );
    if ( !directory.exists() ) {
      String msg = LogMsg.createMsg( Batch.MSG, "Task.source directory does not exist " + directory.getAbsolutePath() ).toString();
      if ( haltOnError() ) {
        context.setError( msg );
        return;
      } else {
        Log.error( msg );
      }
    }
    if ( !directory.isDirectory() ) {
      String msg = LogMsg.createMsg( Batch.MSG, "Task.source directory is not a directory " + directory.getAbsolutePath() ).toString();
      if ( haltOnError() ) {
        context.setError( msg );
        return;
      } else {
        Log.error( msg );
      }
    }
    if ( !directory.canRead() ) {
      String msg = LogMsg.createMsg( Batch.MSG, "Task.cannot read source directory " + directory.getAbsolutePath() ).toString();
      if ( haltOnError() ) {
        context.setError( msg );
        return;
      } else {
        Log.error( msg );
      }
    }

    // get our pattern
    pattern = getString( ConfigTag.PATTERN );

    // get if we should recurse into sub directories when searching for files
    recurse = getBoolean( ConfigTag.RECURSE );

    // if we don't already have a printwriter, set one up based on the configuration
    if ( printwriter == null ) {
      // check for a target in our configuration
      String target = getString( ConfigTag.TARGET );
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.using_target", target ) );

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

            if ( targetFile == null ) {
              Log.warn( LogMsg.createMsg( Batch.MSG, "The target '{}' does not represent a file", target ) );
            }
          } else {
            // if all we have is a filename, there is not scheme to check...
            // check that there is a scheme, if not then assume a filename!
            if ( uri.getScheme() == null ) {
              targetFile = new File( target );
            }
          }
        } else {
          targetFile = new File( target );
        }

        // if not absolute, use the current working directory
        if ( !targetFile.isAbsolute() ) {
          targetFile = new File( context.getSymbols().getString( Symbols.JOB_DIRECTORY ), targetFile.getPath() );
        }
        Log.debug( "Using a target file of " + targetFile.getAbsolutePath() );

        try {
          final Writer fwriter = new FileWriter( targetFile );
          printwriter = new PrintWriter( fwriter );

        } catch ( final Exception e ) {
          Log.error( "Could not create writer: " + e.getMessage() );
          context.setError( e.getMessage() );
        }

      } else {
        Log.error( "No target specified" );
        context.setError( getClass().getName() + " could not determine target" );
      }
    }

  }




  /**
   * @see coyote.batch.TransformTask#execute()
   */
  @Override
  public void execute() throws TaskException {

    // get the list of files
    List<File> flist = FileUtil.getFiles( directory, pattern, recurse );

    // place the filenames in a list
    List<String> files = new ArrayList<String>( 50 );
    for ( File file : flist ) {
      files.add( file.getAbsolutePath() );
    }

    // sort the list of files by complete file name
    Collections.sort( files );

    // for each of the files...
    for ( String fname : files ) {
      // read it completely
      String contents = FileUtil.fileToString( fname );

      // write its contents to the target
      printwriter.write( contents );

      // make sure there is at least a line feed at the end of the contents
      if ( !contents.endsWith( "\n" ) ) {
        printwriter.write( StringUtil.LINE_FEED );
      }

      // flush the buffer
      printwriter.flush();

    }

  }




  @Override
  public void close() throws IOException {
    if ( printwriter != null ) {
      printwriter.flush();
      printwriter.close();
    }
  }

}
