package coyote.mc.snow;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SnowDateTimeTest {

  @Test
  public void toQueryFormat() {
    String queryFormat = new SnowDateTime(new Date()).toQueryFormat();
    assertNotNull(queryFormat);
    assertTrue(queryFormat.startsWith("javascript:gs.dateGenerate('"));
    assertTrue(queryFormat.endsWith("')"));
    assertTrue(queryFormat.contains("','"));
    assertTrue(queryFormat.contains(":"));
    assertTrue(queryFormat.contains("-"));
  }
}