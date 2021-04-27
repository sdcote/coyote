/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.mc.snow;

/**
 * Thin wrapper for a sys_id (GUID).
 */
public class SnowKey {

  String value;


  protected SnowKey() { }


  public SnowKey(final String value) {
    if ((value == null) || (value.trim().length() != 32)) {
      throw new IllegalArgumentException("bad key value");
    }
    this.value = value.trim();
  }


  @Override
  public String toString() {
    return value;
  }
}
