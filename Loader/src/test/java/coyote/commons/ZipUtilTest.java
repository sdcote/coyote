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
package coyote.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;


/**
 * 
 */
public class ZipUtilTest {

  @Test
  public void deflateFileToFile() throws IOException {
    File sourceDir = new File( "src" );
    File targetFile = new File( "srcZip" );
    System.out.println( sourceDir.getAbsolutePath() );
    assertTrue( sourceDir.exists() );
    assertTrue( sourceDir.isDirectory() );
    ZipUtil.deflateFileToFile( sourceDir, targetFile );
    assertTrue( targetFile.exists() );
    targetFile.delete();
  }




  @Test
  public void zipDir() throws IOException {
    File sourceDir = new File( "src" );
    System.out.println( sourceDir.getAbsolutePath() );
    assertTrue( sourceDir.exists() );
    assertTrue( sourceDir.isDirectory() );
    ZipUtil.zip( sourceDir );
    File targetFile = new File( "src.zip" );
    assertTrue( targetFile.exists() );
    targetFile.delete();
  }




  @Test
  public void roundTripInflate() throws IOException {
    final File source = new File( "README.md" );
    final File target = new File( "README.deflated" );
    final File result = new File( "README.inflated" );
    ZipUtil.deflateFileToFile( source, target );
    ZipUtil.inflateFileToFile( target, result );
    String expected = FileUtil.fileToString( source );
    String results = FileUtil.fileToString( result );
    assertEquals( expected, results );
    target.delete();
    result.delete();
  }

}
