package coyote.commons.network.http;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


public class JavaIOTempDirExistTest {

  @Ignore
  public void testJavaIoTempDefault() throws Exception {
    final String tmpdir = System.getProperty( "java.io.tmpdir" );
    final DefaultCacheManager manager = new DefaultCacheManager();
    final DefaultCacheFile tempFile = (DefaultCacheFile)manager.createCacheFile( "xx" );
    final File tempFileBackRef = new File( tempFile.getName() );
    Assert.assertEquals( tempFileBackRef.getParentFile(), new File( tmpdir ) );

    // force an exception
    tempFileBackRef.delete();
    Exception e = null;
    try {
      tempFile.delete();
    } catch ( final Exception ex ) {
      e = ex;
    }
    Assert.assertNotNull( e );
    manager.clear();
  }




  @Test
  public void testJavaIoTempSpecific() throws IOException {
    final String tmpdir = System.getProperty( "java.io.tmpdir" );
    try {
      final String tempFileName = UUID.randomUUID().toString();
      final File newDir = new File( "target", tempFileName );
      System.setProperty( "java.io.tmpdir", newDir.getAbsolutePath() );
      Assert.assertEquals( false, newDir.exists() );
      new DefaultCacheManager();
      Assert.assertEquals( true, newDir.exists() );
      newDir.delete();
    }
    finally {
      System.setProperty( "java.io.tmpdir", tmpdir );
    }

  }

}
