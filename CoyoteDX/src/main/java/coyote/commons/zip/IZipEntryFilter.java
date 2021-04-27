/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.zip;

import java.io.File;


/**
 * Checks whether a particular zip entry name is acceptable for some action. For
 * instance, {@link ZipArchive#extractTo(File, IZipEntryFilter)} uses instances of
 * this interface to decide which entries to extract.
 */
public interface IZipEntryFilter {

  /**
   *
   * @param name name of the entry to check
   * @return true if the entry is accepted, false otherwise
   */
  boolean accept(String name);
}