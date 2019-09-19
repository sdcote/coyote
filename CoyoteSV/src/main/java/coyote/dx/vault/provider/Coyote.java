package coyote.dx.vault.provider;

import coyote.dx.vault.Vault;
import coyote.dx.vault.VaultEntry;
import coyote.dx.vault.VaultException;

public class Coyote implements Vault {
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

  }

  @Override
  public VaultEntry getEntry(String key) {
    return null;
  }
}
