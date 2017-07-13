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
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

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
public abstract class AbstractChecksumTask extends AbstractFileTask {

  protected String CHECKSUM_EXTENSION;
  protected String ALGORITHM;




  /**
   * @return a new instance of a message checksum to use;
   */
  abstract Checksum getChecksum();




  protected static String getCRC32Checksum( final File file ) {
    try {
      return getChecksum( file, new CRC32() );
    } catch ( final IOException e ) {
      return null;
    }
  }




  protected static String getAdler32Checksum( final File file ) {
    try {
      return getChecksum( file, new Adler32() );
    } catch ( final IOException e ) {
      return null;
    }
  }




  private static String getChecksum( final File file, final Checksum algorithm ) throws IOException {
    long checksum = 0;
    try (InputStream fis = new FileInputStream( file ); CheckedInputStream cis = new CheckedInputStream( fis, algorithm )) {
      final byte[] buffer = new byte[STREAM_BUFFER_LENGTH];
      while ( cis.read( buffer ) >= 0 ) {}
      checksum = cis.getChecksum().getValue();
    }
    return Long.toHexString( checksum );
  }




  /**
   * @return the name of the algorithm used
   */
  public String getAlgorithm() {
    return ALGORITHM;
  }




  /**
   * @return the file extension used for the checksum file
   */
  public String getFileExtension() {
    return CHECKSUM_EXTENSION;
  }




  /**
   * Handle all checksums the same way.
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
          String checksum = null;
          try {
            checksum = getChecksum( file, getChecksum() );
            Log.debug( file.getAbsolutePath() + " has a " + ALGORITHM + " checksum of " + checksum );
            final String checksumFilename = file.getAbsolutePath() + CHECKSUM_EXTENSION;
            final File checksumFile = new File( checksumFilename );
            if ( checksumFile.exists() ) {
              if ( checksumFile.canRead() ) {
                final String expected = FileUtil.fileToString( checksumFile );
                if ( StringUtil.isNotBlank( expected ) ) {
                  if ( checksum.equalsIgnoreCase( expected.trim() ) ) {
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
              FileUtil.stringToFile( checksum, checksumFilename );
            }
            getContext().set( checksumFilename, checksum );
          } catch ( final IOException e ) {
            final String msg = LogMsg.createMsg( CDX.MSG, "%s checksum could not be calculated: %s - '%s' (%s)", ALGORITHM, e.getMessage(), filename, file.getAbsolutePath() ).toString();
            Log.error( msg );
            if ( haltOnError ) {
              getContext().setError( msg );
              return;
            }
          }
        } else {
          final String msg = LogMsg.createMsg( CDX.MSG, "%s checksum task could process empty file '%s' (%s)", ALGORITHM, filename, file.getAbsolutePath() ).toString();
          Log.error( msg );
          if ( haltOnError ) {
            getContext().setError( msg );
            return;
          }
        }
      } else {
        final String msg = LogMsg.createMsg( CDX.MSG, "%s checksum task could not read '%s' (%s)", ALGORITHM, filename, file.getAbsolutePath() ).toString();
        Log.error( msg );
        if ( haltOnError ) {
          getContext().setError( msg );
          return;
        }
      }
    } else {
      Log.error( "File does not exist: " + filename + " (" + file.getAbsolutePath() + ")" );
      final String msg = LogMsg.createMsg( CDX.MSG, "%s checksum task could not read '%s' - file does not exist (%s)", ALGORITHM, filename, file.getAbsolutePath() ).toString();
      Log.error( msg );
      if ( haltOnError ) {
        getContext().setError( msg );
        return;
      }
    }

  }

}
