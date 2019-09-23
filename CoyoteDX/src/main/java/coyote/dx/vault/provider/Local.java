/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.vault.provider;

import coyote.commons.CipherUtil;
import coyote.commons.StringUtil;
import coyote.dx.ConfigTag;
import coyote.dx.vault.ConfigurationException;
import coyote.dx.vault.Provider;
import coyote.dx.vault.Vault;
import coyote.dx.vault.VaultException;
import coyote.loader.Loader;
import coyote.loader.log.Log;

import java.util.Map;


/**
 * This represents a plug-in for creating a secrets provider.
 *
 * <p>This can be used as a reference implementation or template for other providers.</p>
 */
public class Local implements Provider {

  /**
   * Name ({@value}) of the system property containing the master password to the secrets vault we will be using.
   */
  public static final String VAULT_SECRET = "vault.secret";
  public static final String DEFAULT_SECRET = "changeme";

  /**
   * Name of this provider
   */
  private static final String NAME = Local.class.getSimpleName();


  /**
   * Create an instance of a local, file-based vault.
   *
   * @param method     the method to store and retrieve secrets.
   * @param source     The source of the secrets. This might be a URL or a file path or some other specification understandable by the provider
   * @param properties the configuration properties for the returned vault
   * @return an instance of a MiniVaultProvider
   * @throws ConfigurationException if there were problems configuring the vault during its creation
   * @throws VaultException         it there were problems opening the vault with its set configuration
   */
  @Override
  public Vault createVault(String method, String source, Map<String, String> properties) throws ConfigurationException, VaultException {
    LocalVault retval = new LocalVault();
    if (StringUtil.isNotEmpty(source)) {

      retval.setFilename(source);
    } else {
      throw new ConfigurationException("Local provider requires a source configuration element");
    }
    String password = getProperty(ConfigTag.PASSWORD, properties);
    if (StringUtil.isBlank(password)) {
      String encpasswd = getProperty(Loader.ENCRYPT_PREFIX, properties);
      if (StringUtil.isNotBlank(encpasswd)) {
        password = CipherUtil.decryptString(encpasswd);
      }
    }
    if (StringUtil.isBlank(password)) {
      password = System.getProperty(VAULT_SECRET);
      if (StringUtil.isNotBlank(password)) {
        Log.notice("Vault is using provided vault secret from system property");
      } else {
        Log.notice("Vault could not find vault secret from system property");
        password = DEFAULT_SECRET;
      }
    }
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
