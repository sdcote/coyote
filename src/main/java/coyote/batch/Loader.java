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
package coyote.batch;

import java.io.File;
import java.io.IOException;

import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.commons.SystemPropertyUtil;
import coyote.commons.Version;


/**
 * This is a utility launcher for the library.
 */
public class Loader {

  private static final String NAME = "Batch";

  private static final Version VERSION = new Version( 0, 2, 0, Version.EXPERIMENTAL );




  /**
   * @param args
   */
  public static void main( final String[] args ) {

    if ( ( args == null ) || ( args.length == 0 ) ) {
      System.out.println( NAME + " v" + VERSION );
      System.exit( 0 );
    }

    // Load properties to set system properties telling the library to use a
    // proxy and what authentication to use along with any other properties 
    // which might be expected by any components.
    SystemPropertyUtil.load( NAME.toLowerCase() );

    if ( StringUtil.isNotBlank( args[0] ) ) {
      final String fileName = args[0];

      // Get the reference to the file which contains our transformation engine 
      // configuration
      final File cfgFile = new File( fileName );

      if ( cfgFile.exists() && cfgFile.canRead() ) {

        // have the Engine Factory create a transformation engine based on the
        // configuration in the file
        final TransformEngine engine = TransformEngineFactory.getInstance( cfgFile );

        // Make sure the context contains a name so it can find artifacts 
        // related to this transformation 
        if ( StringUtil.isBlank( engine.getName() ) ) {
          engine.setName( FileUtil.getBase( fileName ).toLowerCase() );
        }

        System.out.println( "Running '" + engine.getName() + "' ..." );

        // run the transformation 
        try {

          engine.run();

        } catch ( final Exception e ) {

          System.out.println( e.getMessage() );

        }
        finally {

          try {
            engine.close();
          } catch ( final IOException ignore ) {}

          System.out.println( "Batch '" + engine.getName() + "' completed." );

        } // try-catch-finally 

      } else {

        System.err.println( "Cannot read " + cfgFile.getAbsolutePath() );

      } // can read file

    } // args

  } // main

}
