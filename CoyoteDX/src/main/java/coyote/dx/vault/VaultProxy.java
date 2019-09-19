/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.vault;

/**
 * The Vault proxy wraps different Vault implementations and makes them uniformly accessible.
 *
 * <p>This is the object which is placed in the template under the name of "Vault" and delegates calls to the
 * underlying Vault implementations. The {@code VaultEntry} is used </p>
 */
public class VaultProxy {
  private final Vault vault;

  public VaultProxy(Vault vault) {
    this.vault = vault;
  }

  /**
   * Retreive the vault entry with the given identifier and return the value of the named vault entry element.
   *
   * @param entryId      the identifier for the vault entry
   * @param entryElement the name of the entry element to return.
   * @return the identified element value or an empty string if not found or if the element is empty. Will never return null.
   */
  public String get(String entryId, String entryElement) {
    return "";
  }

}
