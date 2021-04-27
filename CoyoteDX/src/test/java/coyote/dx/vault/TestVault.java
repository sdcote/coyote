package coyote.dx.vault;

/**
 * Mock vault for our tests
 */
public class TestVault implements Vault {
  @Override
  public boolean isReadOnly() {
    return true;
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
