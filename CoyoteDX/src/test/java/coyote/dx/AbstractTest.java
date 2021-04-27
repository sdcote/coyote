/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import coyote.commons.FileUtil;
import coyote.commons.SystemPropertyUtil;
import coyote.commons.template.SymbolTable;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;


/**
 * 
 */
public class AbstractTest {
  TransformContext transformContext;




  public AbstractTest() {
    transformContext = new TransformContext();
    transformContext.setSymbols(new SymbolTable());
  }




  /**
   * Marshal the given JSON text into a config
   * 
   * @param cfgData The JSON text to marshal
   * 
   * @return the first configuration frame found in the data.
   */
  protected Config parseConfiguration(String cfgData) {
    List<DataFrame> cfglist = JSONMarshaler.marshal(cfgData);
    return new Config(cfglist.get(0));
  }




  protected TransactionContext createTransactionContext() {
    return new TransactionContext(transformContext);
  }




  /**
   * @return the Transform Context for this test
   */
  protected TransformContext getTransformContext() {
    return transformContext;
  }




  protected TransformEngine createEngine(DataFrame config) {
    TransformEngine retval = null;
    retval = TransformEngineFactory.getInstance(config);
    return retval;
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
  protected TransformEngine loadEngine(String name) {
    TransformEngine engine = null;

    // load named system properties
    SystemPropertyUtil.load(name.toLowerCase());

    // now read the named configuration file
    StringBuffer b = new StringBuffer();
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(AbstractTest.class.getClassLoader().getResourceAsStream(name + ".json")));
      String line;
      while ((line = reader.readLine()) != null) {
        b.append(line);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    String cfgFile = b.toString();

    // create an engine out of the file
    engine = TransformEngineFactory.getInstance(cfgFile);

    // return the configured engine
    return engine;
  }




  protected static void deleteWorkDirectory(File dir) {
    FileUtil.deleteDirectory(dir);
  }




  protected static void makeWorkDirectory(File dir) throws Exception {
    if (dir != null && !dir.exists()) {
      FileUtil.makeDirectory(dir);
    }
  }




  protected static void resetDirectory(File testDir) {
    try {
      deleteWorkDirectory(testDir);
      if (!testDir.exists()) {
        makeWorkDirectory(testDir);
      }
    } catch (Exception ignore) {}
  }




  /**
   * Run and close the given engine.
   * @param engine to run and close
   */
  protected void turnOver(TransformEngine engine) {
    try {
      engine.run();
    } finally {
      try {
        engine.close();
      } catch (Exception ignore) {}
    }
  }
}
