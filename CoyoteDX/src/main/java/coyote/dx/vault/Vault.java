/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.vault;

/**
 * This represents a secrets provider implementation.
 *
 * <p>Through this interface callers can interact with the underlying secrets management infrastructure in a uniform,
 * decoupled manner. Even if the underlying implementation of the secrets management implementation is changed, client
 * code should remain relatively stable.</p>
 *
 * <p>{@code VaultBuilder} instances create {@code Vault} instances which represent vendor-specific code. Instead of
 * binding client code to vendor-specific API models, the {@code Vault} interface provides a contract to the underlying
 * {@code Provider} implementations acting as a mediator to vendor-specific models and API calls.</p>
 */
public interface Vault {
  static final String LOOKUP_TAG = "Vault";


  /**
   * @return true if the vault can only retrieve secrets, false if it can also store them
   */
  boolean isReadOnly();

  /**
   * Save the current state of the vault
   */
  void save();

  /**
   * Close the vault
   */
  void close();

  /**
   * Open / initialize the vault.
   *
   * @throws VaultException if the vault provider could not be opened.
   */
  void open() throws VaultException;

  /**
   * Get a VaultEntry with the given identifier.
   *
   * <p>Vaults store many types of information. A {@code VaultEntry} holds that variable data structure as a Map. Each
   * structure can be different depending on the {@code Provider}, but each provider should have a way to retrieve that
   * data structure based on some key. This key may be a simple string, a path, or some expression which allows the
   * secrets to be retrieved. This method retrieves a unique set of secrets from the vault provider.</p>
   *
   * @param key the identifier for the entry in the vault
   * @return An entry with the identifier or null if no entry with that identifier is found.
   */
  VaultEntry getEntry(String key);

}
