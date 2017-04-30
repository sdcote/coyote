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
package coyote.dx;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import coyote.commons.SystemPropertyUtil;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dx.TransactionContext;
import coyote.dx.TransformContext;
import coyote.dx.TransformEngine;
import coyote.dx.TransformEngineFactory;


/**
 * 
 */
public class AbstractTest {
  TransformContext transformContext = new TransformContext();




  /**
   * Marshal the given JSON text into a dataframe
   * 
   * @param cfgData The JSON text to marshal
   * 
   * @return the first configuration frame found in the data.
   */
  protected DataFrame parseConfiguration( String cfgData ) {
    List<DataFrame> cfglist = JSONMarshaler.marshal( cfgData );
    return cfglist.get( 0 );
  }




  protected TransactionContext createTransactionContext() {
    return new TransactionContext( transformContext );
  }


  /**
   * @return the Transform Context for this test
   */
  protected TransformContext getTransformContext() {
    return transformContext;
  }



  /**
   * Loads a configuration file and set of properties from the class path
   * 
   * <p>This loads [name].properties as the system properties and [name.json as 
   * the engine configuration.
   * 
   * @param name the name of the files to use
   * 
   * @return The transform engine configured with the requested configuration
   */
  protected TransformEngine loadEngine( String name ) {
    TransformEngine engine = null;

    // load named system properties
    SystemPropertyUtil.load( name.toLowerCase() );

    // now read the named configuration file
    StringBuffer b = new StringBuffer();
    try {
      BufferedReader reader = new BufferedReader( new InputStreamReader( AbstractTest.class.getClassLoader().getResourceAsStream( name + ".json" ) ) );
      String line;
      while ( ( line = reader.readLine() ) != null ) {
        b.append( line );
      }
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    String cfgFile = b.toString();

    // create an engine out of the file
    engine = TransformEngineFactory.getInstance( cfgFile );

    // return the configured engine
    return engine;
  }




}
