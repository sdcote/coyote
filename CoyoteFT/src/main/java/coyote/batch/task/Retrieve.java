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

import java.net.URI;
import java.net.URISyntaxException;

import coyote.batch.BatchFT;
import coyote.batch.ConfigTag;
import coyote.batch.TaskException;
import coyote.batch.TransformContext;
import coyote.batch.TransformTask;
import coyote.batch.ftp.FileAttributes;
import coyote.batch.ftp.FileTransferException;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Retrieves a file from a remote site
 * 
 * <pre>
 * "Retrieve" : {
 *   "source": "sftp://username:password@host:port/path/to/file.txt",
 *   "target": "file:///path/to/file.txt"
 * }
 * </pre>
 */
public class Retrieve extends AbstractFileTransferTask implements TransformTask {

  FileAttributes remoteFileAttributes = null;
  private String pattern = null;
  private boolean recurse = false;
  private boolean preserve = false;
  private boolean delete = false;




  /**
   * @see coyote.batch.task.AbstractTransformTask#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.open( context );

    // Setup the remote site
    String source = getString( ConfigTag.SOURCE );
    try {
      // The source configuration must be a URI
      URI sourceUri = new URI( source );

      site = configureSite( sourceUri );
      if ( site != null ) {
        Log.debug( LogMsg.createMsg( BatchFT.MSG, "Retrieve.using_site", site.toString() ) );
      } else {
        throw new ConfigurationException( "Could not determine site from source URI of '" + source + "'" );
      }
      remoteFile = sourceUri.getPath();
      Log.debug( LogMsg.createMsg( BatchFT.MSG, "Retrieve.using_remote_file", remoteFile ) );

      remoteFileAttributes = site.getAttributes( remoteFile );

      // If the connection failed, an exception would have been thrown, if the
      // attributes are null, the remote file does not exist.
      if ( remoteFileAttributes != null ) {

        if ( remoteFileAttributes.isDirectory() ) {

          // look for pattern
          pattern = getString( ConfigTag.PATTERN );

          // look for recurse
          recurse = getBoolean( ConfigTag.RECURSE );

          // determine if we should preserve hierarchy or flatten to one directory
          preserve = getBoolean( ConfigTag.PRESERVE );

          // determine if we should delete the file after it has been retrieved
          delete = getBoolean( ConfigTag.DELETE );

        } // if is directory

      } else {

        String msg = "The remote file '" + remoteFile + "' does not exist";
        System.out.println( msg );

        if ( haltOnError() ) {
          context.setError( msg );
          if ( site != null ) {
            site.close();
          }
          return;
        }
      }

    } catch ( URISyntaxException | ConfigurationException e ) {
      String msg = String.format( "Retrieve task source initialization failed: %s - %s", e.getClass().getName(), e.getMessage() );
      Log.error( msg );
      if ( haltOnError() ) {
        context.setError( msg );
        if ( site != null ) {
          site.close();
        }
        return;
      }
    } catch ( FileTransferException e ) {
      String msg = "The connection to the remote site '" + remoteFile + "' failed - " + e.getMessage();
      System.out.println( msg );

      if ( haltOnError() ) {
        context.setError( msg );
        if ( site != null ) {
          site.close();
        }
        return;
      }
    }

    // Now determine the target which is supposed to be a local file
    try {
      localFile = getLocalFile( getString( ConfigTag.TARGET ) );
      Log.debug( LogMsg.createMsg( BatchFT.MSG, "Retrieve.using_local_file", localFile ) );
    } catch ( Exception e ) {
      String msg = String.format( "Retrieve task target initialization failed: %s - %s", e.getClass().getName(), e.getMessage() );
      Log.error( msg );
      if ( haltOnError() ) {
        context.setError( msg );
        if ( site != null ) {
          site.close();
        }
        return;
      }
    }

  }




  /**
   * @see coyote.batch.TransformTask#execute()
   */
  @Override
  public void execute() throws TaskException {

    try {

      if ( remoteFileAttributes.isDirectory() ) {

        if ( !site.retrieveDirectory( remoteFile, localFile, pattern, recurse, preserve, delete ) ) {
          String msg = String.format( "Retrieve task failed to retrieve all files from %s from %s to %s", remoteFile, site.getHost(), localFile );
          Log.error( msg );
          if ( haltOnError() ) {
            context.setError( msg );
            if ( site != null ) {
              site.close();
            }
            return;
          }
        } else {
          Log.debug( LogMsg.createMsg( BatchFT.MSG, "Retrieve.retrieved_directory", remoteFile, site.getHost(), localFile ) );
        }

      } else {

        // perform the retrieval of the remote file
        if ( !site.retrieveFile( remoteFile, localFile ) ) {
          String msg = String.format( "Retrieve task failed to retrieve %s from %s to %s", remoteFile, site.getHost(), localFile );
          Log.error( msg );
          if ( haltOnError() ) {
            context.setError( msg );
            if ( site != null ) {
              site.close();
            }
            return;
          }
        } else {
          Log.debug( LogMsg.createMsg( BatchFT.MSG, "Retrieve.retrieved_file", remoteFile, site.getHost(), localFile ) );
        }

      }
    }
    finally {
      if ( site != null ) {
        site.close();
      }
    }

  }

}