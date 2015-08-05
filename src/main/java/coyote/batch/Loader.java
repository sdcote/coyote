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

import coyote.commons.CipherUtil;
import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.commons.SystemPropertyUtil;
import coyote.commons.security.BlowfishCipher;


/**
 * This is a utility launcher for the library.
 */
public class Loader {

  private static final String ENCRYPT = "encrypt";




  /**
   * Performs encryption operation from the command line arguments.
   * 
   * <p>The loader provides a way for the operator to generate encrypted values 
   * which can be placed in configuration files. This allows user names and 
   * passwords to be hidden from those with access to the files but who do not 
   * have access to the encryption keys.</p>
   * 
   * @param args the entire command line arguments to parse for encryption details
   */
  private static void encrypt( String[] args ) {
    String token = null;
    String key = System.getProperty( ConfigTag.CIPHER_KEY, CipherUtil.getKey( Batch.CIPHER_KEY ) );
    String cipherName = System.getProperty( ConfigTag.CIPHER_NAME, Batch.CIPHER_NAME );
    if ( args.length < 2 ) {
      System.err.println( "Nothing to encrypt" );
      return;
    } else {
      token = args[1];
      if ( args.length > 2 ) {
        String rawkey = args[2];
        // make sure is it base64 encoded or make it so
        try {
          CipherUtil.decode( rawkey );
          key = rawkey;
        } catch ( Exception e ) {
          System.out.println( "User-specified key did not appear to be Base64 encoded, encoding it." );
          key = CipherUtil.getKey( rawkey );
        }

        if ( args.length > 3 ) {
          cipherName = args[3];
        }

      }
    }
    System.out.println( "Encrypting '" + token + "'" );
    System.out.println( "with a key of '" + key + "'" );
    System.out.println( "using a '" + cipherName + "' cipher" );

    if ( CipherUtil.getCipher( cipherName ) != null ) {
      String ciphertext = CipherUtil.encipher( token, cipherName, key );
      System.out.println( ciphertext );
    } else {
      System.err.println( "Cipher '" + cipherName + "' is not supported" );
    }

  }




  /**
   * @param args
   */
  public static void main( final String[] args ) {

    if ( ( args == null ) || ( args.length == 0 ) ) {
      System.out.println( Batch.NAME + " v" + Batch.VERSION );
      System.exit( 0 );
    }

    // Load properties to set system properties telling the library to use a
    // proxy and what authentication to use along with any other properties 
    // which might be expected by any components.
    SystemPropertyUtil.load( Batch.NAME.toLowerCase() );

    if ( StringUtil.isNotBlank( args[0] ) ) {

      // if the first argument is "encrypt" perform an encryption operation 
      // using the rest of the command line arguments
      if ( ENCRYPT.equalsIgnoreCase( args[0] ) ) {
        encrypt( args );
      } else {

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
            System.out.println( "Encountered a '" + e.getClass().getSimpleName() + "' exception running the engine: " + e.getMessage() );
            e.printStackTrace();
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
      } // encrypt or load
    } // args

  } // main

}
