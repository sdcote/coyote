/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.testdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * This is a base class for all those strategies which rely on reading text 
 * from files.
 */
public abstract class DataFileStrategy extends AbstractStrategy {
  protected List<String> data = new ArrayList<String>();




  /**
   * Load a file using the basename and populate data list.
   * 
   * <p>This method will use a variety of naming conventions to find the 
   * appropriate filename on the system including locating filenames base on 
   * the locale of the VM This allows the runtime environment to customize the
   * data used.</p>
   * 
   * @param basename The base name of the data file to load.
   */
  protected void loadData( final String basename ) {
    List<String> lines = null;

    // TODO: try different filename combinations factoring in locale

    // Finally, just load from the standard text file
    String name = basename + ".txt";
    lines = loadResource( name );

    if ( lines != null ) {
      data = lines;
    }
  }




  /**
   * This tries to load the file from the current directory and if that fails, 
   * tries the classpath.
   * 
   * @param name The name of the resource to load.
   * 
   * @return The list of line read in from the resource or null if the named resource could not be found 
   */
  private List<String> loadResource( String name ) {
    List<String> retval = null;

    // First try to load from the current directory    
    final File datafile = new File( name );

    if ( datafile.exists() && ( !datafile.isDirectory() ) && datafile.canRead() ) {
      try {
        retval = readLines( new FileInputStream( datafile ) );
      } catch ( FileNotFoundException e ) {
        retval = null;
      }
    }

    // If reading from the directory failed, try the classpath
    if ( retval == null ) {
      //InputStream stream = getClass().getResourceAsStream( "/" + name );
      InputStream stream = this.getClass().getClassLoader().getResourceAsStream( name );
      retval = readLines( stream );
    }

    return retval;
  }




  /**
   * Read the input stream and return the contents as a list of strings.
   * 
   * @param in the stream from which to read
   * 
   * @return the lines of text read in from the stream.
   */
  private List<String> readLines( InputStream in ) {
    List<String> retval = new ArrayList<String>();
    try {
      InputStreamReader reader = new InputStreamReader( in );
      BufferedReader buf = new BufferedReader( reader );
      String line;
      while ( ( line = buf.readLine() ) != null ) {
        retval.add( line );
      }
    } catch ( IOException e ) {
      // can be expected in some deployments
      return null;
    }
    finally {
      try {
        in.close();
      } catch ( IOException e ) {
        // can be expected in some deployments
      }
    }
    return retval;
  }

}
