/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.junit.Test;


/**
 * 
 */
public class FileUtilTest {

  /**
   * Method testFileToURI
   */
  @Test
  public void testFileToURI() {
    try {
      File homedir = new File( System.getProperty( "user.home" ) );
      URI fileuri = FileUtil.getFileURI( homedir );
      assertNotNull( fileuri );

      String furi = fileuri.toString();
      assertTrue( furi.startsWith( "file:///" ) );

      URI fileuri2 = new URI( furi );
      assertNotNull( fileuri2 );

      //System.err.println( "File to URI = " + fileuri );
      //System.err.flush();
      //System.err.println( "File to URI path = " + UriUtil.getFilePath( fileuri ) );
      //System.err.flush();

    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }




  @Test
  public void testGetFiles() {
    File cwd = FileUtil.getCurrentWorkingDirectory();

    String regex = "([^\\s]+(\\.(?i)(JAVA))$)";// java files

    List<File> files = FileUtil.getFiles( cwd, regex, true );
    assertTrue( files.size() > 20 );

    for ( File file : files ) {
      //System.out.println( file.getName() );
      String path = file.getAbsolutePath();
      assertFalse( !".java".equalsIgnoreCase( path.substring( path.length() - 5 ) ) );
    }

  }

}
