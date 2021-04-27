package coyote.dx.vault;

import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class VaultProxyTest {

  private static final SymbolTable symbols = new SymbolTable();


  /**
   * set test data in our symbol table
   */
  @BeforeClass
  public static void setUpBeforeClass() {
    symbols.put("One", 1.02);
    symbols.put("Today", new Date());
    symbols.put("Greeting", "Hello World!");
  }


  /**
   * Test the Vault proxy which will wrap different Vault implementations and make them uniformly accessible.
   */
  @Test
  public void simpleProxy() {
    Template.put("Vault", new VaultProxy(new TestVault()));

    String text = ">[#Vault.get(\"EntryID\",\"username\")#]<-username";
    String formattedText = Template.resolve(text, symbols);
    assertEquals("><-username", formattedText);
  }


}
