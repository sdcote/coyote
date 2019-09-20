/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.vault.provider;


import coyote.dx.vault.ConfigurationException;
import coyote.dx.vault.Vault;
import coyote.dx.vault.VaultEntry;
import coyote.dx.vault.VaultException;
import coyote.vault.Entries;
import coyote.vault.Entry;
import coyote.vault.data.DocumentHelper;
import coyote.vault.data.DocumentProcessException;
import coyote.vault.util.CryptUtils;

import java.io.File;
import java.io.IOException;


/**
 * coyote.vault.VaultVault is an JVault {@code Vault} implementation of a coyote.vault.Vault secrets store for TTD design/development and
 * API testing.
 *
 * <p>This is the main API into the coyote.vault.Vault secrets store.</p>
 */
public class LocalVault implements Vault {
  Entries entries = null;
  String filename = null;
  byte[] passwordHash = null;


  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public void save() {

  }

  @Override
  public void close() {

  }

  @Override
  public void open() throws VaultException {
    try {
      entries = DocumentHelper.newInstance(filename, passwordHash).readJsonDocument();
    } catch (IOException e) {
      throw new VaultException("Problems reading file", e);
    } catch (DocumentProcessException e) {
      throw new VaultException("Problems decrypting file", e);
    }
  }

  @Override
  public VaultEntry getEntry(String key) {
    VaultEntry retval = null;
    if (key != null) {
      if (entries != null) {
        for (Entry entry : entries.getEntry()) {
          if (key.equals(entry.getName())) {
            retval = new VaultEntry();
            retval.set(coyote.vault.Vault.NAME_TAG, entry.getName());
            retval.set(coyote.vault.Vault.USER_TAG, entry.getUser());
            retval.set(coyote.vault.Vault.URL_TAG, entry.getUrl());
            retval.set(coyote.vault.Vault.NOTES_TAG, entry.getNotes());
            retval.set(coyote.vault.Vault.PUBLIC_KEY_TAG, entry.getPublicKey());
            retval.set(coyote.vault.Vault.PRIVATE_KEY_TAG, entry.getPrivateKey());
            retval.set(coyote.vault.Vault.PASSWORD_TAG, entry.getPassword());
          }
        }
      }
    }
    return retval;
  }

  /**
   * @param value
   * @return an instance of this vault for method chaining.
   * @throws ConfigurationException if the file could not be found or is not readable
   */
  public Vault setFilename(String value) throws ConfigurationException {
    File file = new File(value);
    if (!file.exists()) {
      throw new ConfigurationException("Vault file does not exist: '" + value + "' (" + file.getAbsolutePath() + ")");
    }
    if (!file.canRead()) {
      throw new ConfigurationException("Cannot read vault file: '" + value + "' (" + file.getAbsolutePath() + ")");
    }
    filename = file.getAbsolutePath();
    return this;
  }

  /**
   * Set the password hash for this vault.
   *
   * @param value the array of characters to hash in creating the key for the encrypted vault file
   * @return an instance of this vault for method chaining.
   * @throws ConfigurationException if the password is not a valid string of characters (e.g., null)
   */
  public Vault setPassword(String value) throws ConfigurationException {
    try {
      passwordHash = CryptUtils.getPKCS5Sha256Hash(value.toCharArray());
    } catch (Exception e) {
      throw new ConfigurationException("Invalid password", e);
    }
    return this;
  }

}
