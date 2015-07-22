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


/**
 * This is a frame reader which uses a JDBC result set to create frames.
 */
public class JdbcReader extends AbstractFrameReader {

  /** The logger for this class */
  final Logger log = LoggerFactory.getLogger( getClass() );

  private Connection connection;

  ResultSet result = null;
  private volatile boolean EOF = true;




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
          result = statement.executeQuery(query);
          EOF = result.isLast();

        } catch ( SQLException e ) {
          // TODO Auto-generated catch block
          e.printStackTrace();
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

    if ( result != null ) {
      try {

        result.next();
        log.info( "Read" );

        EOF = result.isLast();
      } catch ( SQLException e ) {
        e.printStackTrace();
        EOF = true;
      }

    } else {
      EOF = true;
    }

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
