/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import java.io.IOException;
import java.sql.Connection;

import coyote.dx.context.TransformContext;


/**
 * This is a base class for DataStore
 */
public abstract class AbstractDataStore extends AbstractConfigurableComponent implements DataStore {

  /**
   * @see coyote.dx.DataStore#getConnection()
   */
  @Override
  public Connection getConnection() {
    // TODO Auto-generated method stub
    return null;
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    // TODO Auto-generated method stub

  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub

  }

}
