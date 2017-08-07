/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.zip;

/**
 * Accepts all zip entry names.
 */
public class AllZipEntryFilter implements IZipEntryFilter {
  @Override
  public boolean accept(final String name) {
    return true;
  }
}