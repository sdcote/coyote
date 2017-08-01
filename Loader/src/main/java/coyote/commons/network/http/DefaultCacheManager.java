/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import coyote.loader.log.Log;


/**
 * Default strategy for creating and cleaning up temporary files.
 * 
 * <p>This class stores its files in the standard location (that is, wherever
 * {@code java.io.tmpdir} points to). Files are added to an internal list, and 
 * deleted when no longer needed; when {@code clear()} is invoked at the end of 
 * processing a request).</p>
 */
public class DefaultCacheManager implements CacheManager {

  private final File tmpdir;

  private final List<CacheFile> tempFiles;




  public DefaultCacheManager() {
    tmpdir = new File( System.getProperty( "java.io.tmpdir" ) );
    if ( !tmpdir.exists() ) {
      tmpdir.mkdirs();
    }
    tempFiles = new ArrayList<CacheFile>();
  }




  @Override
  public void clear() {
    for ( final CacheFile file : tempFiles ) {
      try {
        file.delete();
      } catch ( final Exception ignored ) {
        Log.append( HTTPD.EVENT, "WARNING: Could not delete file ", ignored );
      }
    }
    tempFiles.clear();
  }




  @Override
  public CacheFile createCacheFile( final String filename_hint ) throws Exception {
    final DefaultCacheFile tempFile = new DefaultCacheFile( tmpdir );
    tempFiles.add( tempFile );
    return tempFile;
  }

}