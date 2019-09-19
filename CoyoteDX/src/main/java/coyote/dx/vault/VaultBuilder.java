/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.vault;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * This is the builder of vault instances.
 */
public class VaultBuilder {

  /**
   * The vendor technology to be used
   */
  private String provider = null;
  /**
   * The type of vault to be created - dependent on the vendor specified.
   **/
  private String method = null;
  /**
   * The source of the secrets. This might be a URL or a file path or some other specification understandable by the provider
   */
  private String source = null;


  /**
   * Additional properties the provider may need for creating an instance.
   */
  private Hashtable<String, String> properties = new Hashtable<>();

  public String getProvider() {
    return provider;
  }

  public VaultBuilder setProvider(String name) {
    this.provider = name;
    return this;
  }

  public String getMethod() {
    return method;
  }

  public VaultBuilder setMethod(String name) {
    this.method = name;
    return this;
  }

  public String getSource() {
    return source;
  }

  public VaultBuilder setSource(String source) {
    this.source = source;
    return this;
  }

  /**
   * Build and return an instance of a Vault.
   *
   * @return an instance of {@code Vault} which is ready to use
   * @throws ConfigurationException if the configuration of the {@code Vault} was incorrect in some manner
   * @throws VaultException         if the {@code Vault} instance could not be opened for operation
   */
  public Vault build() throws ConfigurationException, VaultException {
    Vault retval = null;
    if (getProvider() != null) {
      Provider vaultProvider = ProviderLoader.loadProvider(getProvider());
      if (vaultProvider != null) {
        retval = vaultProvider.createVault(getMethod(), getProperties());
      } else {
        throw new ConfigurationException("Could not retrieve vault provider: " + getProvider());
      }
    }
    return retval;
  }

  public VaultBuilder setProperty(String name, String value) {
    if (name != null) {
      if (value != null) {
        properties.put(name, value);
      } else {
        properties.remove(name);
      }
    }
    return this;
  }

  public String getProperty(String name) {
    return properties.get(name);
  }

  public Map<String, String> getProperties() {
    Map<String, String> retval = new HashMap<>();
    retval.putAll(properties);
    return retval;
  }

}
