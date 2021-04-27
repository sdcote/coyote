package coyote.commons.zip;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Test;

import coyote.commons.FileUtil;


/**
 * Some simple tests.
 */
public class ZipArchiveTest {
  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {}




  /**
   * Method testExtractTo
   *
   * @throws Exception
   */
  @Test
  public void testExtractTo() throws Exception {
    File tstJar = new File( "test.zip" );

    ZipArchive archive = new ZipArchive( tstJar ); // create a new archive
    archive.addFiles( new File( "src" ) ); // add all the source files
    archive.flush(); // now commit to disk

    File tmpDir = new File( "tmp" );
    tmpDir.mkdirs();

    ZipArchive arc = new ZipArchive( new File( "test.zip" ) );
    arc.extractTo( tmpDir );

    FileUtil.removeDir( tmpDir );
    FileUtil.deleteFile( tstJar );

  }




  /**
   * Method XtestCreateJar
   *
   * @throws Exception
   */
  @Test
  public void testCreateZip() throws Exception {
    File tstJar = new File( "test.zip" );
    ZipArchive archive = new ZipArchive( tstJar );

    // This MUST be a directory!!!
    archive.addFiles( new File( "src" ) );

    // we should be able to "see" this right away
    // NOTE: the directory name is omitted as it is the root AND the path
    // separator is standardized to the slash and not the Windoze back-slash
    byte[] data = archive.getEntry( "main/java/coyote/commons/zip/ZipArchive.java" );
    assertNotNull( data );

    // now commit to disk
    archive.flush();

    // should now be able to read the same entry from disk
    archive = null;
    archive = new ZipArchive( tstJar );

    byte[] newData = archive.getEntry( "main/java/coyote/commons/zip/ZipArchive.java" );
    assertNotNull( newData );
    assertEquals( new String( data ), new String( newData ) );

    // Nifty if it would work every time
    tstJar.deleteOnExit();

    FileUtil.deleteFile( tstJar );

  }




  /**
   * Method testAddFilesToExistingArchive
   *
   * @throws Exception
   */
  @Test
  public void testAddFilesToExistingArchive() throws Exception {

    File tstFile = new File( "test.zip" );

    ZipArchive archive = new ZipArchive( tstFile );
    archive.addFiles( new File( "cfg" ) );

    // commit to disk
    archive.flush();

    // check to see if we were able to add the new data
    // and if the old data is still there
    new ZipArchive( new File( "test.zip" ) );

    //assertNotNull( archive.getEntry( "bus.xml" ) );
    //assertNotNull( archive.getEntry( "busconnector.xml" ) );

    FileUtil.deleteFile( tstFile );

  }
}