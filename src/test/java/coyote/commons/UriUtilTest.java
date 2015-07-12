/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.commons;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.Test;

/**
 * 
 */
public class UriUtilTest {

  
  String fs = System.getProperty( "file.separator" );


  /**
   * Test method for {@link coyote.commons.UriUtil#parse(java.lang.String)}.
   */
 // @Test
  public void testParse() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.commons.UriUtil#isFile(java.net.URI)}.
   */
//  @Test
  public void testIsFile() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.commons.UriUtil#isJar(java.net.URI)}.
   */
 // @Test
  public void testIsJar() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.commons.UriUtil#getFilePath(java.net.URI)}.
   */
 // @Test
  public void testGetFilePath() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.commons.UriUtil#normalizePath(java.lang.String)}.
   */
 // @Test
  public void testNormalizePath() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.commons.UriUtil#normalizeSlashes(java.lang.String)}.
   */
 // @Test
  public void testNormalizeSlashes() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.commons.UriUtil#removeRelations(java.lang.String)}.
   */
 // @Test
  public void testRemoveRelations() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.commons.UriUtil#getFile(java.net.URI)}.
   */
 // @Test
  public void testGetFile() {
    fail( "Not yet implemented" );
  }

  

  /**
   * Method testGetFilePath1
   */
  @Test
  public void testGetFilePath1() {
    try {
      URI uri = new URI( "jar:file:/c:/almanac/my.jar!/com/mycompany/MyClass.class" );
      String path = UriUtil.getFilePath( uri );
      System.out.println( "Jar filepath 1 = '" + path + "'" );
    } catch ( URISyntaxException e ) {
      fail( e.getMessage() );
    }
  }




  /**
   * Method testGetFilePath2
   */
  @Test
  public void testGetFilePath2() {
    try {
      URI uri = new URI( "jar:file:/c:/almanac/my.jar!/" );
      String path = UriUtil.getFilePath( uri );
      System.out.println( "Jar filepath 2 = '" + path + "'" );
    } catch ( URISyntaxException e ) {
      fail( e.getMessage() );
    }
  }




  /**
   * Method testGetFilePath3
   */
  @Test
  public void testGetFilePath3() {
    try {
      URI uri = new URI( "jar:/c:/almanac/my.jar!/" );
      String path = UriUtil.getFilePath( uri );
      System.out.println( "Jar filepath 3 = '" + path + "'" );
    } catch ( URISyntaxException e ) {
      fail( e.getMessage() );
    }
  }




  /**
   * Method testGetFilePath4
   */
  @Test
  public void testGetFilePath4() {
    try {
      URI uri = new URI( "jar://c:/almanac/my.jar" );
      String path = UriUtil.getFilePath( uri );
      System.out.println( "Jar filepath 4 = '" + path + "'" );
    } catch ( URISyntaxException e ) {
      fail( e.getMessage() );
    }
  }
  
  

  /**
   * Method testFileURIFour
   */
  @Test
  public void testFileURIFour() {
    try {
      URI uri = new URI( "file://config/node3.xml" );
      assertTrue( uri.getScheme().equals( "file" ) );
      assertTrue( "Not proper path", UriUtil.getFilePath( uri ).equals( "config" + fs + "node3.xml" ) );
    } catch ( URISyntaxException e ) {
      fail( e.getMessage() );
    }
  }




  /**
   * Method testFileURIFive
   */
  @Test
  public void testFileURIFive() {
    try {
      URI uri = new URI( "file:///C:/Documents%20and%20Settings/CoteS/EXML-7.0b2.zip" );
      assertTrue( uri.getScheme().equals( "file" ) );
      assertTrue( "Not proper path", UriUtil.getFilePath( uri ).equals( "C:" + fs + "Documents and Settings" + fs + "CoteS" + fs + "EXML-7.0b2.zip" ) );
    } catch ( URISyntaxException e ) {
      fail( e.getMessage() );
    }
  }


  // TODO: Need to add tests for URI without schemes  =>  filename.txt == file://filename.txt 
  @Test
  public void testFileURISix() {
    try {
      URI uri = new URI( "/home/sdcote/filename.txt" );
      String path = UriUtil.getFilePath( uri );
      System.out.println(path);
    } catch ( URISyntaxException e ) {
      fail( e.getMessage() );
    }
  }
}
