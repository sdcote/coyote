/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import coyote.commons.ByteUtil;
import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Methods common to multiple checksum tasks
 */
public abstract class AbstractDigestTask extends AbstractFileTask {

  /**
   * Generate the digest for the file.
   *
   * @param file The file to process
   * @param md the message digest to use
   *
   * @return a string representing the digest of the file
   *
   * @throws IOException if there were problems reading the given file
   */
  protected static String digest( final File file, final MessageDigest md ) throws IOException {
    try (InputStream fis = new FileInputStream( file ); DigestInputStream dis = new DigestInputStream( fis, md )) {
      final byte[] buffer = new byte[BLOCK_SIZE];
      int numRead;
      do {
        numRead = fis.read( buffer );
      }
      while ( numRead != -1 );
    }
    final byte[] digest = md.digest();
    return ByteUtil.bytesToHex( digest, "" ).toLowerCase();
  }

  protected String CHECKSUM_EXTENSION;

  protected String ALGORITHM;




  /**
   * @return the name of the algorithm used
   */
  public String getAlgorithm() {
    return ALGORITHM;
  }




  /**
   * @return a new instance of a message digest to use;
   */
  abstract MessageDigest getDigest();




  /**
   * @return the file extension used for the checksum file
   */
  public String getFileExtension() {
    return CHECKSUM_EXTENSION;
  }




  /**
   * Handle all digest checks the same way.
   *
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {
    final String filename = getString( ConfigTag.FILE );
    final File file = new File( filename );
    if ( file.exists() ) {
      if ( file.canRead() ) {
        if ( file.length() > 0 ) {
          String digest = null;
          try {
            digest = digest( file, getDigest() );
            Log.debug( file.getAbsolutePath() + " has a " + ALGORITHM + " digest of " + digest );
            final String digestFilename = file.getAbsolutePath() + CHECKSUM_EXTENSION;
            final File digestFile = new File( digestFilename );
            if ( digestFile.exists() ) {
              if ( digestFile.canRead() ) {
                final String expected = FileUtil.fileToString( digestFile );
                if ( StringUtil.isNotBlank( expected ) ) {
                  if ( digest.equalsIgnoreCase( expected.trim() ) ) {
                    Log.info( ALGORITHM + " checksum verified for " + file.getAbsolutePath() );
                  } else {
                    final String msg = LogMsg.createMsg( CDX.MSG, "%s verification failed for '%s' (%s)", ALGORITHM, filename, file.getAbsolutePath() ).toString();
                    Log.error( msg );
                    if ( haltOnError ) {
                      getContext().setError( msg );
                      return;
                    }
                  }
                } else {
                  final String msg = LogMsg.createMsg( CDX.MSG, "%s data was blank in checksum file '%s' (%s)", ALGORITHM, filename, file.getAbsolutePath() ).toString();
                  Log.error( msg );
                  if ( haltOnError ) {
                    getContext().setError( msg );
                    return;
                  }
                }
              } else {
                final String msg = LogMsg.createMsg( CDX.MSG, "%s data could not read from checksum file '%s' (%s)", ALGORITHM, filename, file.getAbsolutePath() ).toString();
                Log.error( msg );
                if ( haltOnError ) {
                  getContext().setError( msg );
                  return;
                }
              }
            } else {
              FileUtil.stringToFile( digest, digestFilename );
            }
            getContext().set( digestFilename, digest );
          } catch ( final IOException e ) {
            final String msg = LogMsg.createMsg( CDX.MSG, "%s digest could not be calculated: %s - '%s' (%s)", ALGORITHM, e.getMessage(), filename, file.getAbsolutePath() ).toString();
            Log.error( msg );
            if ( haltOnError ) {
              getContext().setError( msg );
              return;
            }
          }
        } else {
          final String msg = LogMsg.createMsg( CDX.MSG, "%s digest task could process empty file '%s' (%s)", ALGORITHM, filename, file.getAbsolutePath() ).toString();
          Log.error( msg );
          if ( haltOnError ) {
            getContext().setError( msg );
            return;
          }
        }
      } else {
        final String msg = LogMsg.createMsg( CDX.MSG, "%s digest task could not read '%s' (%s)", ALGORITHM, filename, file.getAbsolutePath() ).toString();
        Log.error( msg );
        if ( haltOnError ) {
          getContext().setError( msg );
          return;
        }
      }
    } else {
      Log.error( "File does not exist: " + filename + " (" + file.getAbsolutePath() + ")" );
      final String msg = LogMsg.createMsg( CDX.MSG, "%s digest task could not read '%s' - file does not exist (%s)", ALGORITHM, filename, file.getAbsolutePath() ).toString();
      Log.error( msg );
      if ( haltOnError ) {
        getContext().setError( msg );
        return;
      }
    }

  }

}
