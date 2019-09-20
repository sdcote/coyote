/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.vault.provider;

import coyote.commons.StringUtil;
import coyote.dx.ConfigTag;
import coyote.dx.vault.ConfigurationException;
import coyote.dx.vault.Provider;
import coyote.dx.vault.Vault;
import coyote.dx.vault.VaultException;

import java.util.Map;


/**
 * This represents a plug-in for creating a secrets provider.
 *
 * <p>This can be used as a reference implementation or template for other providers.</p>
 */
public class Local implements Provider {

  /**
   * Name of this provider
   */
  private static final String NAME = Local.class.getSimpleName();


  /**
   * Create an instance of a local, file-based vault.
   *
   * @param method     the method to store and retrieve secrets.
   * @param properties the configuration properties for the returned vault
   * @return an instance of a MiniVaultProvider
   * @throws ConfigurationException if there were problems configuring the vault during its creation
   * @throws VaultException      it there were problems opening the vault with its set configuration
   */
  @Override
  public Vault createVault(String method, Map<String, String> properties) throws ConfigurationException, VaultException {
    LocalVault retval = new LocalVault();
    String filename = getProperty(ConfigTag.SOURCE, properties);
    if (StringUtil.isNotEmpty(filename)) {
      retval.setFilename(filename);
    } else {
      throw new ConfigurationException("Local provider requires a filename");
    }
    String password = getProperty(ConfigTag.PASSWORD, properties);
    if (StringUtil.isNotEmpty(password)) {
      retval.setPassword(password);
    } else {
      throw new ConfigurationException("Local provider requires a password");
    }
    retval.open();
    return retval;
  }

  private String getProperty(String key, Map<String, String> properties) {
    String retval = null;
    if (key != null && properties != null) {
      for (String entryName : properties.keySet()) {
        if (key.equalsIgnoreCase(entryName)) {
          retval = properties.get(entryName);
          break;
        }
      }
    }
    return retval;
  }

  @Override
  public String getName() {
    return NAME;
  }

}
