/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.vault;

import java.util.HashMap;
import java.util.Map;

/**
 * A VaultEntry represents a collection of data stored in a vault under a given identifier.
 *
 * <p>Vaults can store many different types of information and even a collection of secrets for a given identifier. A
 * vault entry may also contain metadata about the secret such as the list time it was changed or accessed. This
 * structure allows providers to return a variety of data about the secret to the caller.</p>
 */
public class VaultEntry {
  private Map<String, String> records = new HashMap<>();

  /**
   * Performs a case insensitive search for the record with the given key.
   *
   * <p>If the caller wants to retrieve the password record for this entry, then the call may look like the following:<pre>
   *   String password = entry.get("password");
   * </pre>
   *
   * @param key
   * @return
   */
  public String get(String key) {
    String retval = null;
    if (key != null) {
      for (String name : records.keySet()) {
        if (key.equalsIgnoreCase(name)) {
          retval = records.get(name);
        }
      }
    }
    return retval;
  }

  /**
   * Set the given entry key to the given value.
   *
   * <p>If the value is null, then the value at the given key will be removed from the list.</p>
   *
   * @param name  the name of the value to set (i.e. the key)
   * @param value the value to place at that named key
   */
  public void set(String name, String value) {
    if (name != null) {
      if (value != null) {
        records.put(name, value);
      } else {
        records.remove(name);
      }
    }
  }
}
