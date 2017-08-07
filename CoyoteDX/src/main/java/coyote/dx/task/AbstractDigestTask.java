/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    final String source = getSourceOrFile();
    String expectedDigest = null;

    // Retrieve the digest from a context variable
    final String contextKey = getConfiguration().getString(ConfigTag.CONTEXT);
    if (StringUtil.isNotBlank(contextKey)) {
      expectedDigest = getString(contextKey);
    }

    if (StringUtil.isNotBlank(source)) {
      final File file = getAbsoluteFile(source);
      if (file.exists()) {
        if (file.canRead()) {
          if (file.length() > 0) {
            String digest = null;
            try {
              digest = digest(file, getDigest());
              Log.debug(LogMsg.createMsg(CDX.MSG, "Digest.results", file.getAbsolutePath(), ALGORITHM, digest));

              final String digestFilename = file.getAbsolutePath() + CHECKSUM_EXTENSION;
              final File digestFile = new File(digestFilename);

              if (StringUtil.isNotBlank(expectedDigest)) {
                if (digest.equalsIgnoreCase(expectedDigest.trim())) {
                  Log.info(LogMsg.createMsg(CDX.MSG, "Digest.verified", ALGORITHM, file.getAbsolutePath()));
                  getContext().set(digestFilename, digest);
                } else {
                  final String msg = LogMsg.createMsg(CDX.MSG, "Digest.verification_failed", ALGORITHM, source, file.getAbsolutePath()).toString();
                  if (haltOnError) {
                    throw new TaskException(msg);
                  } else {
                    Log.error(msg);
                    return;
                  }
                }
              } else {
                if (digestFile.exists()) {
                  if (digestFile.canRead()) {
                    final String expected = FileUtil.fileToString(digestFile);
                    if (StringUtil.isNotBlank(expected)) {
                      if (digest.equalsIgnoreCase(expected.trim())) {
                        Log.info(LogMsg.createMsg(CDX.MSG, "Digest.verified", ALGORITHM, file.getAbsolutePath()));
                        getContext().set(digestFilename, digest);
                      } else {
                        final String msg = LogMsg.createMsg(CDX.MSG, "Digest.verification_failed", ALGORITHM, source, file.getAbsolutePath()).toString();
                        if (haltOnError) {
                          throw new TaskException(msg);
                        } else {
                          Log.error(msg);
                          return;
                        }
                      }
                    } else {
                      final String msg = LogMsg.createMsg(CDX.MSG, "Digest.blank_digest_data", ALGORITHM, source, file.getAbsolutePath()).toString();
                      if (haltOnError) {
                        throw new TaskException(msg);
                      } else {
                        Log.error(msg);
                        return;
                      }
                    }
                  } else {
                    final String msg = LogMsg.createMsg(CDX.MSG, "Digest.could_not_read_digest_file", ALGORITHM, source, file.getAbsolutePath()).toString();
                    if (haltOnError) {
                      throw new TaskException(msg);
                    } else {
                      Log.error(msg);
                      return;
                    }
                  }
                } else {
                  Log.warn(LogMsg.createMsg(CDX.MSG, "Digest.no_digest_data", ALGORITHM, file.getAbsolutePath()));
                }
              }
            } catch (final IOException e) {
              final String msg = LogMsg.createMsg(CDX.MSG, "Digest.calculation_error", ALGORITHM, e.getMessage(), source, file.getAbsolutePath()).toString();
              if (haltOnError) {
                throw new TaskException(msg);
              } else {
                Log.error(msg);
                return;
              }
            }
          } else {
            final String msg = LogMsg.createMsg(CDX.MSG, "Digest.empty_source_file", ALGORITHM, source, file.getAbsolutePath()).toString();
            if (haltOnError) {
              throw new TaskException(msg);
            } else {
              Log.error(msg);
              return;
            }
          }
        } else {
          final String msg = LogMsg.createMsg(CDX.MSG, "Digest.source_could_not_be_read", ALGORITHM, source, file.getAbsolutePath()).toString();
          if (haltOnError) {
            throw new TaskException(msg);
          } else {
            Log.error(msg);
            return;
          }
        }
      } else {
        final String msg = LogMsg.createMsg(CDX.MSG, "Digest.source_does_not_exist", ALGORITHM, source, file.getAbsolutePath()).toString();
        if (haltOnError) {
          throw new TaskException(msg);
        } else {
          Log.error(msg);
          return;
        }
      }
    } else {
      final String msg = LogMsg.createMsg(CDX.MSG, "Digest.configuration_error", getClass().getSimpleName(), ConfigTag.SOURCE).toString();
      if (haltOnError) {
        throw new TaskException(msg);
      } else {
        Log.error(msg);
        return;
      }
    }
  }




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
  protected static String digest(final File file, final MessageDigest md) throws IOException {
    try (InputStream fis = new FileInputStream(file)) {
      final byte[] buffer = new byte[STREAM_BUFFER_LENGTH];
      int read = fis.read(buffer, 0, STREAM_BUFFER_LENGTH);
      while (read > -1) {
        md.update(buffer, 0, read);
        read = fis.read(buffer, 0, STREAM_BUFFER_LENGTH);
      }
      return ByteUtil.bytesToHex(md.digest(), "").toLowerCase();
    }
  }

}
