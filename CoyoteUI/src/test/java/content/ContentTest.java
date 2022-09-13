/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package content;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import coyote.commons.StreamUtil;
import coyote.commons.network.MimeType;


/**
 * Ensure basic content exits
 */
public class ContentTest {
  ClassLoader cLoader = this.getClass().getClassLoader();
  private static final String ROOT = "content";




  private static void show(URL rsc) {
    try {
      byte[] data = StreamUtil.loadBytes(rsc.openStream());
      System.out.println(rsc + " is " + data.length + " bytes in length (" + MimeType.get(rsc.toString()).get(0) + ")");
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }




  @Test
  public void favicon() {
    String target = "/favicon.ico";
    String localPath = ROOT + target;
    // System.out.println( "Locating '" + localPath + "'" );
    URL rsc = cLoader.getResource(localPath);
    if (rsc == null) {
      fail("Could not find '" + localPath + "'");
    } else {
      show(rsc);
    }
  }




  @Test
  public void index() {
    String localPath = "views/Dashboard.html";
    URL rsc = cLoader.getResource(localPath);
    if (rsc == null) {
      fail("Could not find '" + localPath + "'");
    } else {
      show(rsc);
    }

  }




  @Test
  public void bootstrap() {
    String target = "/plugins/bootstrap/js/bootstrap.js";
    String localPath = ROOT + target;
    URL rsc = cLoader.getResource(localPath);
    if (rsc == null) {
      fail("Could not find '" + localPath + "'");
    } else {
      show(rsc);
    }
  }

}
