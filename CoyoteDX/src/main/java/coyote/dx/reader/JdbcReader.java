/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.reader;

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

import coyote.commons.StringUtil;
import coyote.commons.jdbc.DriverDelegate;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.Database;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.dx.db.DatabaseDialect;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This is a frame reader which uses a JDBC result set to create frames.
 * 
 * TODO: support TransactionContext.setLastFrame( true )
 */
public class JdbcReader extends AbstractFrameReader {

  private Connection connection;

  ResultSet result = null;
  private volatile boolean EOF = true;
  ResultSetMetaData rsmd = null;
  private int columnCount = 0;




  /**
   * 
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.context = context;

    // check for a source it might be a 
    String source = getString(ConfigTag.SOURCE);
    Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.using a source of {%s}", source));
    if (StringUtil.isNotBlank(source)) {

      // first see if it is a named database in the context
      Database db = context.getDatabase(source);

      if (db != null) {
        Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.We have a shared database definition! {%s}", db.toString()));
        connection = db.getConnection();
      } else {

        // configure a connection ourselves

        String library = getString(ConfigTag.LIBRARY);
        Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.Using a driver JAR of {%s}", library));

        String driver = getString(ConfigTag.DRIVER);
        Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.Using a driver of {%s}", driver));

        // get our configuration data
        String target = getString(ConfigTag.TARGET);
        Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.Using a target of {%s}", target));

        String username = getString(ConfigTag.USERNAME);
        Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.Using a user of {%s}", username));

        String password = getString(ConfigTag.PASSWORD);
        Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.Using a password with a length of {%s}", StringUtil.isBlank(password) ? 0 : password.length()));

        // get the connection to the database
        try {
          URL u = new URL(library);
          URLClassLoader ucl = new URLClassLoader(new URL[]{u});
          Driver dvr = (Driver)Class.forName(driver, true, ucl).newInstance();
          DriverManager.registerDriver(new DriverDelegate(dvr));

          connection = DriverManager.getConnection(target, username, password);

          if (connection != null) {
            Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.Connected to {%s}", target));
          }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | MalformedURLException e) {
          Log.error("Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
      }

      String query = getString(ConfigTag.QUERY);
      Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.Using a query of '{%s}'", query));

      if (connection != null) {

        try {
          Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
          result = statement.executeQuery(query);

          rsmd = result.getMetaData();

          columnCount = rsmd.getColumnCount();

          if (result.isBeforeFirst()) {
            EOF = false;
          }

        } catch (SQLException e) {
          String msg = String.format("Error querying database: '%s' - query = '%s'", e.getMessage().trim(), query);
          //Log.error( msg );
          context.setError(getClass().getName() + " " + msg);
        }

      }

    } else {
      Log.error("No source specified");
      context.setError(getClass().getName() + " could not determine source");
    }

  }




  /**
   * @see coyote.dx.FrameReader#read(coyote.dx.context.TransactionContext)
   */
  @Override
  public DataFrame read(TransactionContext context) {
    DataFrame retval = null;

    if (result != null) {
      try {
        if (result.next()) {
          retval = new DataFrame();

          // Set EOF if this is the last record
          EOF = result.isLast();

          // add each of the fields to the dataframe
          for (int i = 1; i <= columnCount; i++) {
            // Log.info( rsmd.getColumnName( i ) + " - '" + result.getString( i ) + "' (" + rsmd.getColumnType( i ) + ")" );
            retval.add(rsmd.getColumnName(i), DatabaseDialect.resolveValue(result.getObject(i), rsmd.getColumnType(i)));
          }

        } else {
          Log.error("Read past EOF");
          EOF = true;
          return retval;
        }
      } catch (SQLException e) {
        e.printStackTrace();
        EOF = true;
      }
    } else {
      EOF = true;
    }

    // TODO: support TransactionContext.setLastFrame( true )

    return retval;
  }




  /**
   * @see coyote.dx.FrameReader#eof()
   */
  @Override
  public boolean eof() {
    return EOF;
  }




  /**
   * @see coyote.dx.reader.AbstractFrameReader#close()
   */
  @Override
  public void close() throws IOException {

    if (result != null) {
      try {
        result.close();
      } catch (SQLException ignore) {}
    }

    super.close();
  }

}
