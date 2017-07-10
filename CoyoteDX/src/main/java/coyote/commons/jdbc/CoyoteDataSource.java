/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial implementation
 */
package coyote.commons.jdbc;

import java.io.Closeable;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.sql.DataSource;

import coyote.commons.jdbc.pool.ConnectionPool;
import coyote.dx.ConfigTag;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;


/**
 * A datasource which uses connection pooling.
 */
public class CoyoteDataSource implements DataSource, Closeable {

  private final Config configuration = new Config();
  private final AtomicBoolean isClosed = new AtomicBoolean();
  private volatile ConnectionPool pool = null;




  public void open() {
    if ( getName() == null ) {
      setName( "CoyoteDataSource." + getInstanceCount() );
    }
  }




  /**
   * Shutdown the DataSource and its associated pool.
   */
  @Override
  public void close() {
    if ( isClosed.getAndSet( true ) ) {
      return;
    }

    ConnectionPool p = pool;
    if ( p != null ) {
      try {
        Log.info( getName() + " - Shutdown initiated..." );
        p.shutdown();
        Log.info( getName() + " - Shutdown completed." );
      } catch ( InterruptedException e ) {
        Log.warn( getName() + " - Interrupted during closing: " + e.getMessage() );
        Thread.currentThread().interrupt();
      }
    }
  }




  private void setName( String value ) {
    configuration.put( ConfigTag.NAME, value );
  }




  private String getName() {
    return configuration.getString( ConfigTag.NAME );
  }




  @Override
  public PrintWriter getLogWriter() throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }




  @Override
  public void setLogWriter( PrintWriter out ) throws SQLException {
    // TODO Auto-generated method stub

  }




  @Override
  public void setLoginTimeout( int seconds ) throws SQLException {
    // TODO Auto-generated method stub

  }




  @Override
  public int getLoginTimeout() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }




  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    // TODO Auto-generated method stub
    return null;
  }




  @Override
  public <T> T unwrap( Class<T> iface ) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }




  @Override
  public boolean isWrapperFor( Class<?> iface ) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }




  @Override
  public Connection getConnection() throws SQLException {

    if ( isClosed() ) {
      throw new SQLException( "CoyoteDataSource " + this + " has been closed." );
    }
    pool = new ConnectionPool( this );

    return pool.getConnection();
  }




  /**
   * Not supported as our configuration controls how we operate.
   * 
   * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
   */
  @Override
  public Connection getConnection( String username, String password ) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }




  /**
   * @return the number of DataSources in this runtime.
   */
  private int getInstanceCount() {
    synchronized( System.getProperties() ) {
      final int next = Integer.getInteger( "CoyoteDataSource.instance_count", 0 ) + 1;
      System.setProperty( "CoyoteDataSource.instance_count", String.valueOf( next ) );
      return next;
    }
  }




  /**
   * Determine if the DataSource has been closed.
   *
   * @return true if the CoyoteDataSource has been closed, false otherwise
   */
  public boolean isClosed() {
    return isClosed.get();
  }




  @Override
  public String toString() {
    return getName();
  }

}
