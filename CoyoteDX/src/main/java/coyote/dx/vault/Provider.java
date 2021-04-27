/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.vault;

import java.util.Map;

/**
 * Providers create {@code Vault} instances.
 *
 * <p>Providers provide specific {@code Vault} instances based on the underlying secrets management technology.</p>
 */
public interface Provider {

  /**
   * Create a vault using the given method and the given set of configuration properties.
   *
   * <p>The returned vault will be configured, initialized and ready to use.</p>
   *
   * <p>VaultProviders should not be stateful; they should build fresh instances of Vaults.</p>
   *
   * @param method     the method to store and retrieve secrets.
   * @param source     The source of the secrets. This might be a URL or a file path or some other specification understandable by the provider
   * @param properties the configuration properties for the returned vault
   * @return a vault configured and ready to use to store and retrieve secrets
   * @throws ConfigurationException if there were problems configuring the vault during its creation
   * @throws VaultException         it there were problems opening the vault with its set configuration
   */
  Vault createVault(String method, String source, Map<String, String> properties) throws ConfigurationException, VaultException;

  /**
   * @return the name of the provider factory
   */
  String getName();

}
