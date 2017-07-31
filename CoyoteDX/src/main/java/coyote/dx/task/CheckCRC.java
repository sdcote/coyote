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
import java.util.zip.CRC32;
import java.util.zip.Checksum;


/**
 * Perform a CRC32 checksum on the given file and compare it with the checksum
 * in a file with the same name with a ".crc32" suffix.
 *
 * <p>If the checksum file does not exist, it will be created.
 *
 * <p>The checksum will be posted in the transform context with the name of
 * the checksum file so other components and tasks can ensure the integrity of
 * the file throughout the job.
 */
public class CheckCRC extends AbstractChecksumTask {

  /**
   * Utility method to get the checksum for the given file.
   *
   * @param file file to check
   *
   * @return the checksum of the file or "0" if there were errors
   */
  public static String checksum( final File file ) {
    return getCRC32Checksum( file );
  }




  public CheckCRC() {
    CHECKSUM_EXTENSION = ".crc32";
    ALGORITHM = "CRC32";
  }




  @Override
  public Checksum getChecksum() {
    return new CRC32();
  }

}
