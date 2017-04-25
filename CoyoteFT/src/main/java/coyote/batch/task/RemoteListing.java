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
import java.util.List;

import coyote.batch.BatchFT;
import coyote.batch.ConfigTag;
import coyote.batch.TaskException;
import coyote.batch.TransformContext;
import coyote.batch.ftp.FileTransferException;
import coyote.batch.ftp.RemoteFile;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Lists the files on a remote site.
 * 
 * <p>This is a diagnostic and development tool which allows the discovery of 
 * the directory and file structure on a remote site.</p>
 * 
 * <pre>
 * "ListSite" : {
 *   "source": "sftp://username:password@host:port/path/to/directory",
 *   "recurse" : false
 * }
 * </pre>
 */
public class RemoteListing extends AbstractFileTransferTask {

  /** Flag indicating the listing is to recurse into sub-directories */
  private boolean recurse = false;




  /**
   * @see coyote.batch.task.AbstractTransformTask#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.open( context );

    // Setup the remote site
    String source = getString( ConfigTag.SOURCE );
    try {
      URI sourceUri = new URI( source );

      site = configureSite( sourceUri );
      if ( site != null ) {
        Log.debug( LogMsg.createMsg( BatchFT.MSG, "Using Site: {}", site.toString() ) );
      } else {
        throw new ConfigurationException( "Could not determine site from source URI of '" + source + "'" );
      }
      remoteFile = sourceUri.getPath();
      Log.debug( LogMsg.createMsg( BatchFT.MSG, "Using remote file: {}", remoteFile ) );

    } catch ( URISyntaxException | ConfigurationException e ) {
      String msg = String.format( "ListSite task source initialization failed: %s - %s", e.getClass().getName(), e.getMessage() );
      Log.error( msg );
      if ( haltOnError() ) {
        context.setError( msg );
        if ( site != null ) {
          site.close();
        }
        return;
      }
    }

    // Check for the recurse flag
    if ( contains( ConfigTag.RECURSE ) ) {
      recurse = getBoolean( ConfigTag.RECURSE );
    }

  }




  /**
   * @see coyote.batch.TransformTask#execute()
   */
  @Override
  public void execute() throws TaskException {

    try {
      // perform the retrieval
      listSite( remoteFile );
    } catch ( FileTransferException e ) {
      e.printStackTrace();
      String msg = String.format( "ListSite task failed to list %s from %s - %s: %s", remoteFile, site.getHost(), e.getClass().getSimpleName(), e.getMessage() );
      Log.error( msg );
      if ( haltOnError() ) {
        context.setError( msg );
        return;
      }
    }
    finally {
      if ( site != null ) {
        site.close();
      }
    }

  }




  private void listSite( String directory ) throws FileTransferException {
    List<RemoteFile> listing = site.listFiles( directory );
    for ( RemoteFile file : listing ) {
      Log.info( file.toString() );
      if ( file.isDirectory() && recurse ) {
        listSite( file.getName() );
      }
    }

  }

}
