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
   * Retrieve the vault entry with the given identifier and return the default vault entry element.
   *
   * <p>For most implementations this will be the password. But, there are other implementation which are simply
   * "secrets" vaults and the identifier is simply the identifier of the secret. For example, CyberArk only returns a
   * "content" element.</p>
   *
   * <p>Implementers of this method are encourages to return the password element as a matter of convention, allowing
   * more granular access via {@link #get(String, String)} method call.</p>
   *
   * @param entryId the identifier for the vault entry
   * @return the identified element value or an empty string if not found or if the element is empty. Will never return null.
   */
  public String get(String entryId) {
    return "";
  }

  /**
   * Retrieve the vault entry with the given identifier and return the value of the named vault entry element.
   *
   * @param entryId      the identifier for the vault entry
   * @param entryElement the name of the entry element to return.
   * @return the identified element value or an empty string if not found or if the element is empty. Will never return null.
   */
  public String get(String entryId, String entryElement) {
    return "";
  }

}
