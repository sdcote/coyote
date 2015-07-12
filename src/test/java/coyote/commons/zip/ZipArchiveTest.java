package coyote.commons.zip;

/**
 * Tests the ZipArchive class using the Junit unit test framework available
 * from http://www.junit.org
 */
import java.io.File;

import junit.framework.TestCase;
import coyote.commons.FileUtil;


/**
 * Some simple tests.
 */
public class ZipArchiveTest extends TestCase {

  /**
   * Method testExtractTo
   *
   * @throws Exception
   */
  public void testExtractTo() throws Exception {
    File tstJar = new File( "test.zip" );
    ZipArchive archive = new ZipArchive( tstJar );
    archive.addFiles( new File( "src" ) );
    // now commit to disk
    archive.flush();

    File tmpDir = new File( "tmp" );
    tmpDir.mkdirs();

    ZipArchive arc = new ZipArchive( new File( "test.zip" ) );
    arc.extractTo( tmpDir );
    FileUtil.removeDir( tmpDir );
  }




  /**
   * Method XtestCreateJar
   *
   * @throws Exception
   */
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

    // Nifty
    tstJar.deleteOnExit();
  }




  /**
   * Method testAddFilesToExistingArchive
   *
   * @throws Exception
   */
  public void testAddFilesToExistingArchive() throws Exception {
    ZipArchive archive = new ZipArchive( new File( "test.zip" ) );
    archive.addFiles( new File( "cfg" ) );

    // commit to disk
    archive.flush();

    // check to see if we were able to add the new data
    // and if the old data is still there
    archive = null;
    archive = new ZipArchive( new File( "test.zip" ) );

    //assertNotNull( archive.getEntry( "bus.xml" ) );
    //assertNotNull( archive.getEntry( "busconnector.xml" ) );
  }
}