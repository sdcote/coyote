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
package coyote.batch.reader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.ConfigTag;
import coyote.batch.Database;
import coyote.batch.TransactionContext;
import coyote.batch.TransformContext;
import coyote.commons.StringUtil;
import coyote.commons.jdbc.DriverDelegate;
import coyote.dataframe.DataFrame;


/**
 * This is a frame reader which uses a JDBC result set to create frames.
 */
public class JdbcReader extends AbstractFrameReader {

  /** The logger for this class */
  final Logger log = LoggerFactory.getLogger( getClass() );

  private Connection connection;

  ResultSet result = null;
  private volatile boolean EOF = true;
  ResultSetMetaData rsmd = null;
  private int columnCount = 0;




  /**
   * 
   * @see coyote.batch.Component#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.context = context;

    // check for a source it might be a 
    String source = getString( ConfigTag.SOURCE );
    log.debug( "using a source of {}", source );
    if ( StringUtil.isNotBlank( source ) ) {

      // first see if it is a named database in the context
      Database db = context.getDatabase( source );

      if ( db != null ) {
        log.debug( "We have a shared database definition! {}", db.toString() );
        connection = db.getConnection();
      } else {

        // configure a connection ourselves

        String library = getString( ConfigTag.LIBRARY );
        log.debug( "Using a driver JAR of {}", library );

        String driver = getString( ConfigTag.DRIVER );
        log.debug( "Using a driver of {}", driver );

        // get our configuration data
        String target = getString( ConfigTag.TARGET );
        log.debug( "Using a target of {}", target );

        String username = getString( ConfigTag.USERNAME );
        log.debug( "Using a user of {}", username );

        String password = getString( ConfigTag.PASSWORD );
        log.debug( "Using a password with a length of {}", StringUtil.isBlank( password ) ? 0 : password.length() );

        // get the connection to the database
        try {
          URL u = new URL( library );
          URLClassLoader ucl = new URLClassLoader( new URL[] { u } );
          Driver dvr = (Driver)Class.forName( driver, true, ucl ).newInstance();
          DriverManager.registerDriver( new DriverDelegate( dvr ) );

          connection = DriverManager.getConnection( target, username, password );

          if ( connection != null ) {
            log.debug( "Connected to {}", target );
          }
        } catch ( InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | MalformedURLException e ) {
          log.error( "Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage() );
        }
      }

      String query = getString( ConfigTag.QUERY );
      log.debug( "Using a query of '{}'", query );

      if ( connection != null ) {

        try {
          Statement statement = connection.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY );
          result = statement.executeQuery( query );

          rsmd = result.getMetaData();

          columnCount = rsmd.getColumnCount();

          if ( result.isBeforeFirst() ) {
            EOF = false;
            log.debug( "No results returned for query of '{}'",query );
          }

        } catch ( SQLException e ) {
          String msg = String.format( "Error querying database: '%s' - query = '%s'", e.getMessage().trim(), query );
          //log.error( msg );
          context.setError( getClass().getName() + " " + msg );
        }

      }

    } else {
      log.error( "No source specified" );
      context.setError( getClass().getName() + " could not determine source" );
    }

  }




  /**
   * @see coyote.batch.FrameReader#read(coyote.batch.TransactionContext)
   */
  @Override
  public void read( TransactionContext context ) {
    DataFrame retval = null;

    if ( result != null ) {
      try {

        if ( result.next() ) {
          retval = new DataFrame();

          log.info( "Read" );
          EOF = result.isLast();

          for ( int i = 1; i <= columnCount; i++ ) {

            String columnValue = result.getString( i );

            log.info( rsmd.getColumnName( i ) + " - '" + columnValue + "' (" + rsmd.getColumnType( i ) + ")" );

            retval.add( rsmd.getColumnName( i ), resolveValue( result.getObject( i ), rsmd.getColumnType( i ) ) );

          }

          context.setSourceFrame( retval );
          context.setRow( result.getRow() );

        } else {
          log.error( "Read past EOF" );
          EOF = true;
          return;
        }
      } catch ( SQLException e ) {
        e.printStackTrace();
        EOF = true;
      }

    } else {
      EOF = true;
    }

  }




  /**
   * Resolve the given object to a valid type supported by the DataFrame using
   * the given SQL type as the type indicator.
   * 
   * <p>The following table is used to translate SQL types to Field Types:
   * <table border="1"><tr><th>Value</th><th>SQL</th><th>Field</th></tr>
   * <tr><td>-7</td><td>BIT</td><td>BOL</td></tr>
   * <tr><td>-6</td><td>TINYINT</td><td>S8</td></tr>
   * <tr><td>-5</td><td>BIGINT</td><td>DBL</td></tr>
   * <tr><td>-4</td><td>LONGVARBINARY</td><td>STR</td></tr>
   * <tr><td>-3</td><td>VARBINARY</td><td>STR</td></tr>
   * <tr><td>-2</td><td>BINARY</td><td>STR</td></tr>
   * <tr><td>-1</td><td>LONGVARCHAR</td><td>STR</td></tr>
   * <tr><td>0</td><td>NULL</td><td>NUL</td></tr>
   * <tr><td>1</td><td>CHAR</td><td>STR</td></tr>
   * <tr><td>2</td><td>NUMERIC</td><td>DBL</td></tr>
   * <tr><td>3</td><td>DECIMAL</td><td>FLT</td></tr>
   * <tr><td>4</td><td>INTEGER</td><td>S32</td></tr>
   * <tr><td>5</td><td>SMALLINT</td><td>S16</td></tr>
   * <tr><td>6</td><td>FLOAT</td><td>FLT</td></tr>
   * <tr><td>7</td><td>REAL</td><td>DBL</td></tr>
   * <tr><td>8</td><td>DOUBLE</td><td>DBL</td></tr>
   * <tr><td>12</td><td>VARCHAR</td><td>STR</td></tr>
   * <tr><td>91</td><td>DATE</td><td>DAT</td></tr>
   * <tr><td>92</td><td>TIME</td><td>DAT</td></tr>
   * <tr><td>93</td><td>TIMESTAMP</td><td>DAT</td></tr>
   * <tr><td>1111&nbsp;</td><td>OTHER</td><td>UDF</td></tr></table></p>
   * 
   * @param value the value to convert
   * @param type the SQL type of the value passed
   * 
   * @return an object which can be safely placed in a DataFrame field.
   */
  private Object resolveValue( Object value, int type ) {

    if ( value != null )
      return value.toString();
    else
      return null;
  }




  /**
   * @see coyote.batch.FrameReader#eof()
   */
  @Override
  public boolean eof() {
    return EOF;
  }




  /**
   * @see coyote.batch.reader.AbstractFrameReader#close()
   */
  @Override
  public void close() throws IOException {

    if ( result != null ) {
      try {
        result.close();
      } catch ( SQLException ignore ) {}
    }

    super.close();
  }

}
