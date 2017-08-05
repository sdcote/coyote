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
package coyote.dx.task;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import coyote.commons.Assert;
import coyote.commons.UriUtil;
import coyote.dx.CFT;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.dx.TransformTask;
import coyote.dx.context.TransformContext;
import coyote.dx.ftp.RemoteSite;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Publishes a local file to a remote site
 * 
 * <pre>
 * "Publish" : {
 *   "source": "file:///path/to/local/file.txt",
 *   "target": "sftp://username:password@host:port/path/to/remote/file.txt"
 * }
 * </pre>
 */
public class Publish extends AbstractFileTransferTask implements TransformTask {

  /**
   * @see coyote.dx.task.AbstractTransformTask#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.open( context );

    // Determine the source which is supposed to be a local file
    String source = getString( ConfigTag.SOURCE );

    try {
      Assert.notBlank( source, "Source URI cannot be null or empty" );

      // Try to parse the source as a URI, failures result in a null
      if ( UriUtil.parse( source ) == null ) {
        // Windows systems often have a drive letter in fully qualified filenames
        if ( source.charAt( 1 ) == ':' ) {
          // convert it to a file URI
          File f = new File( source );
          localFile = f.getAbsolutePath();
        } else {
          throw new ConfigurationException( "Source is not a valid URI '" + source + "'" );
        }

      } else {
        localFile = UriUtil.getFilePath( new URI( source ) );
      }
      Log.debug( LogMsg.createMsg( CFT.MSG, "Publish.using_local_file", localFile ) );
    } catch ( Exception e ) {
      String msg = String.format( "Publish task source initialization failed: %s - %s", e.getClass().getName(), e.getMessage() );
      Log.error( msg );
      if ( haltOnError() ) {
        context.setError( msg );
        if ( site != null ) {
          site.close();
        }
        return;
      }
    }

    // Determine the target which is supposed to be a remote site
    final String target = getString( ConfigTag.TARGET );

    try {
      // The target configuration must be a URI
      URI targetUri = new URI( target );
      site = new RemoteSite( targetUri );

      // TODO: support the separate setting of username, password, port, protocol and host

      Log.debug( LogMsg.createMsg( CFT.MSG, "Publish.using_site", site.toString() ) );

      remoteFile = targetUri.getPath();
      Log.debug( LogMsg.createMsg( CFT.MSG, "Publish.using_remote_file", remoteFile ) );

    } catch ( URISyntaxException e ) {
      String msg = String.format( "Publish task target initialization failed: %s - %s", e.getClass().getName(), e.getMessage() );
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
   * @see coyote.dx.TransformTask#execute()
   */
  @Override
  public void execute() throws TaskException {

    try {
      // perform the retrieval
      if ( !site.publishFile( localFile, remoteFile ) ) {
        String msg = String.format( "Publish task failed to send %s to %s at %s", localFile, site.getHost(), remoteFile );
        Log.error( msg );
        if ( haltOnError() ) {
          context.setError( msg );
          if ( site != null ) {
            site.close();
          }
          return;
        }
      } else {
        Log.debug( LogMsg.createMsg( CFT.MSG, "Publish.publish_success", localFile, site.getHost(), remoteFile ) );
      }
    }
    finally {
      if ( site != null ) {
        site.close();
      }
    }
  }

}
