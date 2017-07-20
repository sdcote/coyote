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
    final String source = getSourceOrFile();
    String expectedChecksum = null;

    // Retrieve the checksum from a context variable
    final String contextKey = getConfiguration().getString( ConfigTag.CONTEXT );
    if ( StringUtil.isNotBlank( contextKey ) ) {
      expectedChecksum = getString( contextKey );
    }

    if ( StringUtil.isNotBlank( source ) ) {
      final File file = getAbsoluteFile( source );
      if ( file.exists() ) {
        if ( file.canRead() ) {
          if ( file.length() > 0 ) {
            String checksum = null;
            try {
              checksum = getChecksum( file, getChecksum() );
              Log.debug( LogMsg.createMsg( CDX.MSG, "Checksum.results", file.getAbsolutePath(), ALGORITHM, checksum ) );

              final String checksumFilename = file.getAbsolutePath() + CHECKSUM_EXTENSION;
              final File checksumFile = new File( checksumFilename );

              if ( StringUtil.isNotBlank( expectedChecksum ) ) {
                if ( checksum.equalsIgnoreCase( expectedChecksum.trim() ) ) {
                  Log.info( LogMsg.createMsg( CDX.MSG, "Checksum.verified", ALGORITHM, file.getAbsolutePath() ) );
                  getContext().set( checksumFilename, checksum );
                } else {
                  final String msg = LogMsg.createMsg( CDX.MSG, "Checksum.verification_failed", ALGORITHM, source, file.getAbsolutePath() ).toString();
                  if ( haltOnError ) {
                    throw new TaskException( msg );
                  } else {
                    Log.error( msg );
                    return;
                  }
                }
              } else {
                if ( checksumFile.exists() ) {
                  if ( checksumFile.canRead() ) {
                    final String expected = FileUtil.fileToString( checksumFile );
                    if ( StringUtil.isNotBlank( expected ) ) {
                      if ( checksum.equalsIgnoreCase( expected.trim() ) ) {
                        Log.info( LogMsg.createMsg( CDX.MSG, "Checksum.verified", ALGORITHM, file.getAbsolutePath() ) );
                        getContext().set( checksumFilename, checksum );
                      } else {
                        final String msg = LogMsg.createMsg( CDX.MSG, "Checksum.verification_failed", ALGORITHM, source, file.getAbsolutePath() ).toString();
                        if ( haltOnError ) {
                          throw new TaskException( msg );
                        } else {
                          Log.error( msg );
                          return;
                        }
                      }
                    } else {
                      final String msg = LogMsg.createMsg( CDX.MSG, "Checksum.blank_digest_data", ALGORITHM, source, file.getAbsolutePath() ).toString();
                      if ( haltOnError ) {
                        throw new TaskException( msg );
                      } else {
                        Log.error( msg );
                        return;
                      }
                    }
                  } else {
                    final String msg = LogMsg.createMsg( CDX.MSG, "Checksum.could_not_read_digest_file", ALGORITHM, source, file.getAbsolutePath() ).toString();
                    if ( haltOnError ) {
                      throw new TaskException( msg );
                    } else {
                      Log.error( msg );
                      return;
                    }
                  }
                } else {
                  Log.warn( LogMsg.createMsg( CDX.MSG, "Checksum.no_digest_data", ALGORITHM, file.getAbsolutePath() ) );
                }
              }
            } catch ( final IOException e ) {
              final String msg = LogMsg.createMsg( CDX.MSG, "Checksum.calculation_error", ALGORITHM, e.getMessage(), source, file.getAbsolutePath() ).toString();
              if ( haltOnError ) {
                throw new TaskException( msg );
              } else {
                Log.error( msg );
                return;
              }
            }
          } else {
            final String msg = LogMsg.createMsg( CDX.MSG, "Checksum.empty_source_file", ALGORITHM, source, file.getAbsolutePath() ).toString();
            if ( haltOnError ) {
              throw new TaskException( msg );
            } else {
              Log.error( msg );
              return;
            }
          }
        } else {
          final String msg = LogMsg.createMsg( CDX.MSG, "Checksum.source_could_not_be_read", ALGORITHM, source, file.getAbsolutePath() ).toString();
          if ( haltOnError ) {
            throw new TaskException( msg );
          } else {
            Log.error( msg );
            return;
          }
        }
      } else {
        final String msg = LogMsg.createMsg( CDX.MSG, "Checksum.source_does_not_exist", ALGORITHM, source, file.getAbsolutePath() ).toString();
        if ( haltOnError ) {
          throw new TaskException( msg );
        } else {
          Log.error( msg );
          return;
        }
      }
    } else {
      final String msg = LogMsg.createMsg( CDX.MSG, "Checksum.configuration_error", getClass().getSimpleName(), ConfigTag.SOURCE ).toString();
      if ( haltOnError ) {
        throw new TaskException( msg );
      } else {
        Log.error( msg );
        return;
      }
    }
  }

}
